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

package com.dji.flighthub.mcp.session;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.transport.ClientRegistry;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 MCP session 生命周期：创建、查找、超时清理和安全关闭。
 *
 * <p>多 session 架构：每个用户（通过 JWT userId 标识）拥有独立的 SessionContext，
 * 隔离飞行控制状态（control authority、DRC link、flight phase）。
 * 所有 session 共享底层 SDK 单例实例。
 *
 * <p>session 路由机制（getActiveSession）：
 * <ol>
 *   <li>RequestContext.sessionId（transport 层预解析）→ 直接查 sessions map</li>
 *   <li>RequestContext.clientId → ClientRegistry → sessionId → sessions map</li>
 *   <li>Fallback（stdio 模式或无上下文）→ 返回第一个 session</li>
 * </ol>
 */
@Component
@EnableScheduling
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    /** stdio 传输模式的客户端标识。 */
    public static final String STDIO_CLIENT_ID = "stdio";

    private final FlightHubProperties properties;
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    /** SDK 服务（可选，测试环境可能为 null） */
    private SdkService sdkService;

    /** SSE 客户端注册中心（可选，stdio 模式为 null） */
    private ClientRegistry clientRegistry;

    public SessionManager(FlightHubProperties properties) {
        this.properties = properties;
        registerShutdownHook();
    }

    /**
     * 设置 SDK 服务实例（Spring setter 注入，可选）。
     *
     * @param sdkService SDK 服务
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setSdkService(SdkService sdkService) {
        this.sdkService = sdkService;
    }

    /**
     * 设置客户端注册中心（Spring setter 注入，SSE 模式可用）。
     *
     * @param clientRegistry 客户端注册中心
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setClientRegistry(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    // ==================== Session 创建 ====================

    /**
     * SDK 初始化成功后创建新 session（向后兼容，无 userId）。
     */
    public SessionContext createSession(String workspaceUuid, String workspaceName) {
        return createSessionForUser(null, workspaceUuid, workspaceName);
    }

    /**
     * 为指定用户创建新 session。
     * 多 session 模式核心方法：每个 userId 独立的 SessionContext。
     *
     * @param userId        用户标识（JWT sub claim），stdio 模式为 null
     * @param workspaceUuid workspace UUID
     * @param workspaceName workspace 显示名
     * @return 新创建的 session 上下文
     */
    public SessionContext createSessionForUser(String userId, String workspaceUuid, String workspaceName) {
        String sessionId = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        int maxLevel = properties.getSecurity().getMaxLevel();

        SessionContext session = new SessionContext(sessionId, workspaceUuid, workspaceName, maxLevel, userId);
        sessions.put(sessionId, session);

        log.info("Session created: id={}, userId={}, workspace={}, maxLevel=L{}",
                sessionId, userId, workspaceName, maxLevel);
        return session;
    }

    // ==================== Session 查找（多 session 路由核心） ====================

    /**
     * 获取当前请求对应的活跃 session — 多 session 路由核心。
     *
     * <p>解析优先级：
     * <ol>
     *   <li>RequestContext.sessionId != null → 直接查（transport 层预解析）</li>
     *   <li>RequestContext.clientId（非 stdio）→ ClientRegistry → sessionId</li>
     *   <li>Fallback → 返回第一个 session（兼容 stdio 单 session 模式）</li>
     * </ol>
     *
     * <p>此方法被所有 51 个 Tool 间接调用（通过 AbstractMcpTool.requireSession()），
     * 修改此处的路由逻辑即可实现零工具改动的多 session 支持。
     */
    public SessionContext getActiveSession() {
        if (sessions.isEmpty()) {
            return null;
        }

        RequestContext ctx = RequestContext.current();

        // 1. transport 层已预解析 sessionId（最优路径，O(1) 哈希查找）
        if (ctx != null && ctx.getSessionId() != null) {
            SessionContext s = sessions.get(ctx.getSessionId());
            if (s != null) {
                return s;
            }
        }

        // 2. SSE 模式：通过 clientId → ClientRegistry → sessionId
        if (ctx != null && ctx.getClientId() != null && !STDIO_CLIENT_ID.equals(ctx.getClientId())) {
            SessionContext s = resolveViaClientRegistry(ctx.getClientId());
            if (s != null) {
                return s;
            }
            // SSE 客户端未绑定 session → 不得 fallback 到其他用户的 session（跨用户隔离）
            return null;
        }

        // 3. Fallback：stdio 模式或无 RequestContext → 返回第一个 session
        return sessions.values().iterator().next();
    }

    /**
     * 获取活跃 session；若不存在则使用环境变量配置自动重新连接并创建新 session。
     *
     * <p>解决场景：session 超时（默认 30 分钟无活动）后，后续 tool 调用
     * 无需让 AI 额外发一轮 fh_connect，对调用方完全透明。
     *
     * <p>多 session 模式（SSE + JWT）下，自动重连仅适用于 stdio 模式；
     * SSE 用户 session 过期后需要重新调用 fh_connect。
     *
     * @return session 上下文；自动重连也失败时返回 null
     */
    public SessionContext getOrAutoConnect() {
        SessionContext existing = getActiveSession();
        if (existing != null) {
            return existing;
        }

        // 多 session SSE 模式下不进行自动重连（缺少用户凭证）
        RequestContext ctx = RequestContext.current();
        if (ctx != null && ctx.getUserId() != null) {
            log.info("自动重连跳过：多 session 模式下用户 {} 需重新调用 fh_connect", ctx.getUserId());
            return null;
        }

        // 自动重连：使用环境变量中预配置的连接参数（stdio 模式）
        String serverAddress = properties.getServer().getAddress();
        int port = properties.getServer().getPort();
        String accessToken = properties.getAuth().getAccessToken();
        String workspaceUuid = properties.getAuth().getWorkspaceUuid();

        if (serverAddress == null || serverAddress.isBlank()) {
            log.warn("自动重连跳过：FH_SERVER_ADDRESS 未配置");
            return null;
        }
        if (sdkService == null) {
            log.warn("自动重连跳过：SdkService 未注入");
            return null;
        }

        // 推导 WebSocket 通知地址
        String wsUrl = deriveWebSocketUrl(serverAddress, port);

        log.info("Session 已过期，自动重连: {}:{}", serverAddress, port);
        try {
            long sdkSessionId = sdkService.initialize(serverAddress, port, accessToken,
                    workspaceUuid, wsUrl);
            if (sdkSessionId < 0) {
                log.error("自动重连失败：SDK initialize 返回 {}", sdkSessionId);
                return null;
            }

            String wsLabel = (workspaceUuid != null && workspaceUuid.length() >= 8)
                    ? workspaceUuid.substring(0, 8) : (workspaceUuid != null ? workspaceUuid : "default");
            SessionContext session = createSession(
                    workspaceUuid != null ? workspaceUuid : "", "Workspace-" + wsLabel);
            log.info("自动重连成功，新 session: id={}", session.getSessionId());
            return session;
        } catch (Exception e) {
            log.error("自动重连异常: {}", e.getMessage(), e);
            return null;
        }
    }

    // ==================== 按用户/ID 查找 ====================

    /**
     * 根据 userId 查找现有 session。
     *
     * @param userId 用户标识
     * @return session 上下文，不存在时返回 null
     */
    public SessionContext getSessionByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return sessions.values().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据 ID 获取 session（用于 SSE 多 session 模式）。
     */
    public SessionContext getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 检查是否存在活跃 session。
     */
    public boolean hasActiveSession() {
        return !sessions.isEmpty();
    }

    // ==================== 跨 session 冲突检测 ====================

    /**
     * 查找当前持有指定设备控制权的 session（跨 session 冲突检测）。
     *
     * @param droneSn 目标设备 SN
     * @return 持有该设备控制权的 session，无冲突时返回 null
     */
    public SessionContext findSessionControlling(String droneSn) {
        if (droneSn == null || droneSn.isBlank()) {
            return null;
        }
        for (SessionContext session : sessions.values()) {
            if (session.hasControlOf(droneSn)) {
                return session;
            }
        }
        return null;
    }

    // ==================== Session 关闭 ====================

    /**
     * 关闭指定 session 并释放所有资源。
     */
    public void closeSession(String sessionId) {
        SessionContext session = sessions.remove(sessionId);
        if (session != null) {
            releaseSessionResources(session);
            log.info("Session closed: id={}, userId={}", sessionId, session.getUserId());
        }
    }

    /**
     * 关闭所有 session（用于服务关停时）。
     */
    public void closeAllSessions() {
        sessions.values().forEach(this::releaseSessionResources);
        sessions.clear();
        log.info("All sessions closed");
    }

    /**
     * 定期检查并清理过期 session。
     * 处于 grace period 的 session 跳过常规超时检查（由 HeartbeatScheduler 管理）。
     */
    @Scheduled(fixedDelayString = "${flighthub.security.session-check-interval:30000}")
    public void cleanupExpiredSessions() {
        int timeout = properties.getSecurity().getSessionTimeout();
        sessions.entrySet().removeIf(entry -> {
            SessionContext session = entry.getValue();
            if (session.isInGracePeriod()) {
                return false;
            }
            if (session.isExpired(timeout)) {
                log.warn("Session expired (idle {}s > {}s): id={}, userId={}",
                        timeout, timeout, session.getSessionId(), session.getUserId());
                releaseSessionResources(session);
                return true;
            }
            return false;
        });
    }

    // ==================== Grace Period 管理 ====================

    /**
     * 将 session 置入 grace period（断连保护期）。
     * 客户端 SSE 连接断开时由 HeartbeatScheduler 调用。
     *
     * @param sessionId 要保护的 session ID
     */
    public void enterGracePeriod(String sessionId) {
        SessionContext session = sessions.get(sessionId);
        if (session == null) {
            log.warn("enterGracePeriod 失败，session 不存在: sessionId={}", sessionId);
            return;
        }
        session.enterGracePeriod();
        log.info("Session 进入 grace period: sessionId={}, workspace={}", sessionId, session.getWorkspaceName());
    }

    /**
     * 将 session 退出 grace period（客户端成功重连）。
     * 使用 compute() 保证与 expireGracePeriod 的互斥性。
     *
     * @param sessionId 恢复的 session ID
     * @return true 表示成功退出 grace period，false 表示 session 已不存在（被 expire 抢先销毁）
     */
    public boolean exitGracePeriod(String sessionId) {
        SessionContext[] holder = new SessionContext[1];
        sessions.compute(sessionId, (key, session) -> {
            if (session == null) {
                return null;
            }
            session.exitGracePeriod();
            holder[0] = session;
            return session;
        });

        if (holder[0] != null) {
            log.info("Session 退出 grace period（客户端重连成功）: sessionId={}", sessionId);
            return true;
        } else {
            log.warn("exitGracePeriod 失败，session 已被销毁（竞态）: sessionId={}", sessionId);
            return false;
        }
    }

    /**
     * Grace period 过期，释放 session 资源。
     * 使用 compute() 保证与 exitGracePeriod 的互斥性：
     * 仅当 session 仍处于 grace period 时才销毁（防止销毁已重连恢复的 session）。
     *
     * @param sessionId 要销毁的 session ID
     */
    public void expireGracePeriod(String sessionId) {
        SessionContext[] holder = new SessionContext[1];
        sessions.compute(sessionId, (key, session) -> {
            if (session == null) {
                return null;
            }
            if (!session.isInGracePeriod()) {
                log.info("expireGracePeriod 跳过（session 已退出 grace period）: sessionId={}", sessionId);
                return session;
            }
            holder[0] = session;
            return null;
        });

        if (holder[0] != null) {
            log.warn("Grace period 过期，释放 session 资源: sessionId={}, userId={}, controlDevice={}",
                    sessionId, holder[0].getUserId(), holder[0].getActiveControlDeviceSn());
            releaseSessionResources(holder[0]);
        }
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 通过 ClientRegistry 解析 clientId 对应的 session。
     */
    private SessionContext resolveViaClientRegistry(String clientId) {
        if (clientRegistry == null) {
            return null;
        }
        ClientRegistry.ClientEntry entry = clientRegistry.getClient(clientId);
        if (entry == null || entry.getSessionId() == null) {
            return null;
        }
        return sessions.get(entry.getSessionId());
    }

    /**
     * 从 HTTP server_address + port 推导 WebSocket 通知地址。
     */
    private static String deriveWebSocketUrl(String serverAddress, int port) {
        String base = serverAddress.trim();
        if (base.startsWith("https://")) {
            base = "wss://" + base.substring("https://".length());
        } else if (base.startsWith("http://")) {
            base = "ws://" + base.substring("http://".length());
        } else {
            base = "ws://" + base;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String hostPart = base.substring(base.indexOf("://") + 3);
        if (hostPart.matches(".*:\\d+$")) {
            return base + "/openapi/v0.9/notification/ws/v1/notifications";
        }
        return base + ":" + port + "/openapi/v0.9/notification/ws/v1/notifications";
    }

    /**
     * Fail-Safe 第二层：JVM 退出时尽力释放飞行资源。
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("JVM ShutdownHook triggered — releasing flight resources...");
            try {
                closeAllSessions();
            } catch (Exception e) {
                log.error("ShutdownHook: error releasing resources", e);
            }
            log.info("ShutdownHook: flight resources release completed.");
        }, "flighthub-shutdown-hook"));
    }

    /**
     * 释放 session 持有的所有资源（飞行控制权、DRC 连接、订阅）。
     */
    private void releaseSessionResources(SessionContext session) {
        if (session.hasControlAuthority()) {
            String droneSn = session.getActiveControlDeviceSn();
            log.info("Releasing control authority for device: {}", droneSn);
            releaseControlViaSdk(droneSn);
            session.clearControlDevice();
        }

        if (session.hasDrcLink()) {
            String droneSn = session.getActiveDrcDeviceSn();
            log.info("Disconnecting DRC for device: {}", droneSn);
            disconnectDrcViaSdk(droneSn);
            session.clearDrcDevice();
        }

        for (Integer listenId : session.getActiveListenIds()) {
            log.debug("Cancelling listen: id={}", listenId);
        }

        log.info("Session resources released: id={}", session.getSessionId());
    }

    private static final int SHUTDOWN_SDK_TIMEOUT_SECONDS = 3;

    private void releaseControlViaSdk(String droneSn) {
        if (sdkService == null || sdkService.isMockMode()) {
            return;
        }
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone != null) {
                SdkCallbackBridge.awaitVoid(
                        cb -> drone.getControlAuthModule().releaseFlightControlAuthority(cb),
                        SHUTDOWN_SDK_TIMEOUT_SECONDS);
            }
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.warn("Timeout releasing control for {} ({}s limit)", droneSn, SHUTDOWN_SDK_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.warn("Error releasing control for {}: {}", droneSn, e.getMessage());
        }
    }

    private void disconnectDrcViaSdk(String droneSn) {
        if (sdkService == null || sdkService.isMockMode()) {
            return;
        }
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone != null) {
                SdkCallbackBridge.awaitVoidRc(
                        cb -> drone.getDrcModule().disconnectDrcLink(cb),
                        SHUTDOWN_SDK_TIMEOUT_SECONDS);
            }
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.warn("Timeout disconnecting DRC for {} ({}s limit)", droneSn, SHUTDOWN_SDK_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.warn("Error disconnecting DRC for {}: {}", droneSn, e.getMessage());
        }
    }
}
