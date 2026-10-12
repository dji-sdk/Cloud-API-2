/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 客户端注册中心 — 管理客户端连接生命周期和 session 绑定。
 *
 * <p>职责：
 * <ul>
 *   <li>跟踪活跃客户端（clientId → ClientEntry）</li>
 *   <li>维护 clientId ↔ sessionId 绑定关系</li>
 *   <li>断连时保留绑定信息（grace period）用于重连恢复</li>
 *   <li>提供死连接检测辅助方法</li>
 * </ul>
 *
 * <p>线程安全：所有操作基于 ConcurrentHashMap，支持多线程并发访问。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class ClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(ClientRegistry.class);

    /**
     * 活跃客户端映射：clientId → ClientEntry
     */
    private final Map<String, ClientEntry> activeClients = new ConcurrentHashMap<>();

    /**
     * 断连绑定映射：previousClientId → DisconnectedBinding
     * 用于 grace period 内的重连恢复
     */
    private final Map<String, DisconnectedBinding> disconnectedBindings = new ConcurrentHashMap<>();

    // ==================== 客户端注册/注销 ====================

    /**
     * 注册新客户端连接。
     *
     * @param clientId 客户端唯一标识
     * @param sink     SSE 事件推送通道
     */
    public void register(String clientId, Sinks.Many<ServerSentEvent<String>> sink) {
        ClientEntry entry = new ClientEntry(clientId, sink);
        activeClients.put(clientId, entry);
        log.info("客户端已注册: clientId={}", clientId);
    }

    /**
     * 断开客户端连接（非删除）。
     * 如果该客户端绑定了 session，将绑定关系转入 disconnectedBindings 供重连使用。
     *
     * <p>幂等操作：重复调用安全。
     *
     * @param clientId 客户端唯一标识
     * @return 断开前绑定的 sessionId，未绑定时返回 null
     */
    public String disconnect(String clientId) {
        ClientEntry entry = activeClients.remove(clientId);
        if (entry == null) {
            log.debug("客户端 disconnect 时已不存在: clientId={}", clientId);
            return null;
        }

        String sessionId = entry.getSessionId();
        if (sessionId != null) {
            // 将绑定关系保存到 disconnectedBindings，携带原始身份标识供重连校验
            disconnectedBindings.put(clientId,
                    new DisconnectedBinding(sessionId, Instant.now(), entry.getTokenHash()));
            log.info("客户端断连，绑定转入 grace period: clientId={}, sessionId={}", clientId, sessionId);
        } else {
            log.info("客户端断连（无 session 绑定）: clientId={}", clientId);
        }

        return sessionId;
    }

    // ==================== Session 绑定 ====================

    /**
     * 绑定 clientId 和 sessionId。
     * 通常在 fh_connect 成功后调用。
     *
     * @param clientId  客户端唯一标识
     * @param sessionId MCP session 唯一标识
     * @return true 绑定成功，false 客户端不存在
     */
    public boolean bindSession(String clientId, String sessionId) {
        ClientEntry entry = activeClients.get(clientId);
        if (entry == null) {
            log.warn("尝试绑定 session 失败，客户端不存在: clientId={}, sessionId={}", clientId, sessionId);
            return false;
        }
        entry.setSessionId(sessionId);
        log.info("Session 绑定成功: clientId={}, sessionId={}", clientId, sessionId);
        return true;
    }

    // ==================== 查询方法 ====================

    /**
     * 获取活跃客户端条目。
     *
     * @param clientId 客户端唯一标识
     * @return ClientEntry，不存在时返回 null
     */
    public ClientEntry getClient(String clientId) {
        return activeClients.get(clientId);
    }

    /**
     * 获取客户端的 SSE sink。
     *
     * @param clientId 客户端唯一标识
     * @return SSE sink，不存在时返回 null
     */
    public Sinks.Many<ServerSentEvent<String>> getSink(String clientId) {
        ClientEntry entry = activeClients.get(clientId);
        return entry != null ? entry.getSink() : null;
    }


    /**
     * 获取活跃客户端 ID 列表。
     *
     * @return 活跃客户端 ID 的快照列表
     */
    public List<String> getActiveClientIds() {
        return new ArrayList<>(activeClients.keySet());
    }

    /**
     * 获取活跃客户端数量。
     *
     * @return 活跃客户端数
     */
    public int getActiveClientCount() {
        return activeClients.size();
    }

    /**
     * 通过 sessionId 反查绑定的 clientId。
     * 用于 Mcp-Session-Id header 回退解析场景。
     *
     * @param sessionId 目标 session ID
     * @return 绑定了该 sessionId 的活跃 clientId，未找到返回 null
     */
    public String findClientBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        for (Map.Entry<String, ClientEntry> entry : activeClients.entrySet()) {
            if (sessionId.equals(entry.getValue().getSessionId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ==================== 心跳/活跃度 ====================

    /**
     * 刷新客户端最后活跃时间。
     * 每次收到 POST /message 时调用。
     *
     * @param clientId 客户端唯一标识
     */
    public void touchClient(String clientId) {
        ClientEntry entry = activeClients.get(clientId);
        if (entry != null) {
            entry.touch();
        }
    }

    /**
     * 查找超过 timeout 阈值的死连接客户端。
     *
     * @param timeoutSeconds 超时阈值（秒）
     * @return 超时的 clientId 列表
     */
    public List<String> findDeadClients(int timeoutSeconds) {
        Instant threshold = Instant.now().minusSeconds(timeoutSeconds);
        List<String> dead = new ArrayList<>();
        for (Map.Entry<String, ClientEntry> e : activeClients.entrySet()) {
            if (e.getValue().getLastActivityAt().isBefore(threshold)) {
                dead.add(e.getKey());
            }
        }
        return dead;
    }

    // ==================== 重连支持 ====================

    /**
     * 查找断连绑定（grace period 内的重连凭证）。
     *
     * @param previousClientId 之前断连的 clientId
     * @return DisconnectedBinding，不存在或已过期清理返回 null
     */
    public DisconnectedBinding findDisconnectedBinding(String previousClientId) {
        return disconnectedBindings.get(previousClientId);
    }

    /**
     * 原子性尝试完成重连（合并 find + grace 校验 + 身份校验 + 迁移）。
     * 消除 TOCTOU 竞态：使用 ConcurrentHashMap.compute 保证操作原子性。
     *
     * @param previousClientId   之前断连的 clientId
     * @param newClientId        新连接的 clientId
     * @param gracePeriodSeconds grace period 超时阈值
     * @param newTokenHash       新连接的身份标识 hash（null 表示跳过身份校验）
     * @return 重连结果
     */
    public ReconnectResult tryCompleteReconnect(String previousClientId, String newClientId,
                                                 int gracePeriodSeconds, String newTokenHash) {
        final ReconnectResult[] resultHolder = new ReconnectResult[1];

        disconnectedBindings.compute(previousClientId, (key, binding) -> {
            if (binding == null) {
                resultHolder[0] = ReconnectResult.notFound();
                return null; // key 不存在，无变更
            }

            // 检查 grace period 是否过期
            if (binding.getDisconnectedAt().plusSeconds(gracePeriodSeconds).isBefore(Instant.now())) {
                resultHolder[0] = ReconnectResult.expired(binding);
                return null; // 过期，移除绑定
            }

            // 身份校验：如果原始连接有 tokenHash，新连接必须匹配
            if (binding.getOriginTokenHash() != null && newTokenHash != null
                    && !binding.getOriginTokenHash().equals(newTokenHash)) {
                resultHolder[0] = ReconnectResult.identityMismatch();
                return binding; // 校验失败，保留绑定不消费
            }

            // 验证新客户端存在
            ClientEntry newEntry = activeClients.get(newClientId);
            if (newEntry == null) {
                resultHolder[0] = ReconnectResult.newClientNotFound();
                return binding; // 新客户端不存在，保留绑定
            }

            // 一切校验通过，执行迁移
            newEntry.setSessionId(binding.getSessionId());
            resultHolder[0] = ReconnectResult.success(binding.getSessionId(), binding);
            log.info("重连成功(原子): previousClientId={} → newClientId={}, sessionId={}",
                    previousClientId, newClientId, binding.getSessionId());
            return null; // 消费绑定（移除）
        });

        return resultHolder[0];
    }

    /**
     * 完成重连：将 session 绑定从旧 clientId 迁移到新 clientId。
     * <p><b>已废弃</b>：优先使用 {@link #tryCompleteReconnect} 避免 TOCTOU 竞态。
     *
     * @param previousClientId 之前断连的 clientId
     * @param newClientId      新连接的 clientId
     * @return 恢复的 sessionId，失败返回 null
     */
    public String completeReconnect(String previousClientId, String newClientId) {
        DisconnectedBinding binding = disconnectedBindings.remove(previousClientId);
        if (binding == null) {
            log.warn("重连失败，未找到断连绑定: previousClientId={}", previousClientId);
            return null;
        }

        ClientEntry newEntry = activeClients.get(newClientId);
        if (newEntry == null) {
            // 新客户端不存在，将绑定放回（恢复状态）
            disconnectedBindings.put(previousClientId, binding);
            log.warn("重连失败，新客户端不存在: newClientId={}", newClientId);
            return null;
        }

        newEntry.setSessionId(binding.getSessionId());
        log.info("重连成功: previousClientId={} → newClientId={}, sessionId={}",
                previousClientId, newClientId, binding.getSessionId());
        return binding.getSessionId();
    }

    /**
     * 清理已过期的断连绑定。
     * 由 HeartbeatScheduler 定期调用。
     *
     * @param gracePeriodSeconds grace period 超时阈值（秒）
     * @return 已清理的过期绑定列表（previousClientId → sessionId）
     */
    public List<ExpiredBinding> cleanupExpiredBindings(int gracePeriodSeconds) {
        Instant threshold = Instant.now().minusSeconds(gracePeriodSeconds);
        List<ExpiredBinding> expired = new ArrayList<>();

        disconnectedBindings.entrySet().removeIf(entry -> {
            if (entry.getValue().getDisconnectedAt().isBefore(threshold)) {
                expired.add(new ExpiredBinding(entry.getKey(), entry.getValue().getSessionId()));
                return true;
            }
            return false;
        });

        if (!expired.isEmpty()) {
            log.info("清理过期断连绑定: count={}", expired.size());
        }
        return expired;
    }

    /**
     * 获取断连绑定数量（用于监控/测试）。
     *
     * @return 当前 grace period 中的断连绑定数
     */
    public int getDisconnectedBindingCount() {
        return disconnectedBindings.size();
    }

    // ==================== 内部数据类 ====================

    /**
     * 活跃客户端条目。
     */
    public static class ClientEntry {
        private final String clientId;
        private final Sinks.Many<ServerSentEvent<String>> sink;
        private final Instant connectedAt;
        private volatile Instant lastActivityAt;
        private volatile String sessionId;
        /** 连接时的身份标识 hash（Bearer Token hash，用于重连身份校验） */
        private volatile String tokenHash;
        /** 用户标识（JWT 模式下从 token sub claim 提取） */
        private volatile String userId;

        public ClientEntry(String clientId, Sinks.Many<ServerSentEvent<String>> sink) {
            this.clientId = clientId;
            this.sink = sink;
            this.connectedAt = Instant.now();
            this.lastActivityAt = Instant.now();
        }

        /** 刷新最后活跃时间 */
        public void touch() {
            this.lastActivityAt = Instant.now();
        }

        public String getClientId() {
            return clientId;
        }

        public Sinks.Many<ServerSentEvent<String>> getSink() {
            return sink;
        }

        public Instant getConnectedAt() {
            return connectedAt;
        }

        public Instant getLastActivityAt() {
            return lastActivityAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getTokenHash() {
            return tokenHash;
        }

        public void setTokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    /**
     * 断连绑定 — 保存 grace period 内等待重连的 session 信息。
     */
    public static class DisconnectedBinding {
        private final String sessionId;
        private final Instant disconnectedAt;
        /** 原始连接的身份标识 hash（用于重连时校验身份一致性） */
        private final String originTokenHash;

        public DisconnectedBinding(String sessionId, Instant disconnectedAt, String originTokenHash) {
            this.sessionId = sessionId;
            this.disconnectedAt = disconnectedAt;
            this.originTokenHash = originTokenHash;
        }

        public String getSessionId() {
            return sessionId;
        }

        public Instant getDisconnectedAt() {
            return disconnectedAt;
        }

        public String getOriginTokenHash() {
            return originTokenHash;
        }
    }

    /**
     * 过期绑定信息（cleanupExpiredBindings 返回值）。
     *
     * @param previousClientId 断连的客户端 ID
     * @param sessionId        关联的 session ID
     */
    public record ExpiredBinding(String previousClientId, String sessionId) {
    }

    /**
     * 原子重连操作结果。
     */
    public static class ReconnectResult {

        public enum Status {
            /** 重连成功 */
            SUCCESS,
            /** 未找到断连绑定 */
            NOT_FOUND,
            /** Grace period 已过期 */
            EXPIRED,
            /** 身份不匹配（token hash 不一致） */
            IDENTITY_MISMATCH,
            /** 新客户端未注册 */
            NEW_CLIENT_NOT_FOUND
        }

        private final Status status;
        private final String sessionId;
        private final DisconnectedBinding binding;

        private ReconnectResult(Status status, String sessionId, DisconnectedBinding binding) {
            this.status = status;
            this.sessionId = sessionId;
            this.binding = binding;
        }

        public static ReconnectResult success(String sessionId, DisconnectedBinding binding) {
            return new ReconnectResult(Status.SUCCESS, sessionId, binding);
        }

        public static ReconnectResult notFound() {
            return new ReconnectResult(Status.NOT_FOUND, null, null);
        }

        public static ReconnectResult expired(DisconnectedBinding binding) {
            return new ReconnectResult(Status.EXPIRED, null, binding);
        }

        public static ReconnectResult identityMismatch() {
            return new ReconnectResult(Status.IDENTITY_MISMATCH, null, null);
        }

        public static ReconnectResult newClientNotFound() {
            return new ReconnectResult(Status.NEW_CLIENT_NOT_FOUND, null, null);
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        public Status getStatus() {
            return status;
        }

        public String getSessionId() {
            return sessionId;
        }

        public DisconnectedBinding getBinding() {
            return binding;
        }
    }
}
