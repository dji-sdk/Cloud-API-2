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

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE 心跳调度器 — 定期向所有活跃客户端广播心跳事件，检测死连接。
 *
 * <p>职责：
 * <ul>
 *   <li>定期广播 heartbeat SSE event（event: heartbeat）</li>
 *   <li>检测超时的死连接并触发 disconnect</li>
 *   <li>清理 grace period 过期的断连绑定</li>
 * </ul>
 *
 * <p>仅在 SSE profile 激活时启用。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Profile("sse")
public class HeartbeatScheduler {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatScheduler.class);

    private final ClientRegistry clientRegistry;
    private final FlightHubProperties properties;
    private final SessionManager sessionManager;
    private final AtomicLong heartbeatSequence = new AtomicLong(0);

    public HeartbeatScheduler(ClientRegistry clientRegistry, FlightHubProperties properties,
                              SessionManager sessionManager) {
        this.clientRegistry = clientRegistry;
        this.properties = properties;
        this.sessionManager = sessionManager;
    }

    /**
     * 心跳广播任务 — 向所有活跃客户端推送 heartbeat event。
     * 调度间隔由 flighthub.transport.heartbeat-interval-ms 配置。
     *
     * <p>心跳格式：event: heartbeat, data: {"ts":毫秒时间戳,"seq":递增序号}
     *
     * <p>注意：tryEmitNext().isSuccess() 仅表示事件进入了 Reactor 内存缓冲区，
     * 不代表字节已到达客户端。对于半开连接（客户端崩溃但未发送 TCP FIN），
     * OS TCP 发送缓冲区会静默吸收写入。因此推送成功不能证明客户端存活，
     * 只有推送失败可以确定连接已断 — 此时立即触发断连而非等待超时。
     */
    @Scheduled(fixedDelayString = "${flighthub.transport.heartbeat-interval-ms:30000}")
    public void emitHeartbeats() {
        List<String> clientIds = clientRegistry.getActiveClientIds();
        if (clientIds.isEmpty()) {
            return;
        }

        long seq = heartbeatSequence.incrementAndGet();
        long ts = System.currentTimeMillis();
        String data = String.format("{\"ts\":%d,\"seq\":%d}", ts, seq);

        int emitted = 0;
        for (String clientId : clientIds) {
            Sinks.Many<ServerSentEvent<String>> sink = clientRegistry.getSink(clientId);
            if (sink == null) {
                continue;
            }

            Sinks.EmitResult result = sink.tryEmitNext(ServerSentEvent.<String>builder()
                    .event("heartbeat")
                    .data(data)
                    .build());

            if (result.isSuccess()) {
                emitted++;
            } else {
                // 推送失败 = SSE 流已断 — 立即触发断连（比等待超时更快检测死连接）
                log.info("心跳推送失败，立即断连: clientId={}, result={}", clientId, result);
                String sessionId = clientRegistry.disconnect(clientId);
                if (sessionId != null) {
                    sessionManager.enterGracePeriod(sessionId);
                    log.warn("心跳推送失败触发 grace period: clientId={}, sessionId={}", clientId, sessionId);
                }
            }
        }

        log.debug("心跳广播完成: seq={}, clients={}, emitted={}", seq, clientIds.size(), emitted);
    }

    /**
     * 死连接检测任务 — 找出超时客户端并触发 disconnect + grace period。
     * 调度间隔与心跳间隔相同（相当于每个心跳周期检测一次）。
     */
    @Scheduled(fixedDelayString = "${flighthub.transport.heartbeat-interval-ms:30000}")
    public void detectDeadClients() {
        int timeoutSeconds = properties.getTransport().getHeartbeatTimeoutSeconds();
        List<String> deadClients = clientRegistry.findDeadClients(timeoutSeconds);

        for (String clientId : deadClients) {
            String sessionId = clientRegistry.disconnect(clientId);
            if (sessionId != null) {
                // 联动 SessionManager：session 进入 grace period
                sessionManager.enterGracePeriod(sessionId);
                log.warn("检测到死连接，session 进入 grace period: clientId={}, sessionId={}, timeoutSeconds={}",
                        clientId, sessionId, timeoutSeconds);
            } else {
                log.info("检测到死连接（无 session 绑定）: clientId={}", clientId);
            }
        }
    }

    /**
     * Grace period 过期清理 — 清理超时的断连绑定，释放关联 session。
     * 调度间隔为心跳间隔（频繁检查确保及时清理）。
     */
    @Scheduled(fixedDelayString = "${flighthub.transport.heartbeat-interval-ms:30000}")
    public void cleanupExpiredGracePeriods() {
        int gracePeriodSeconds = properties.getTransport().getGracePeriodSeconds();
        List<ClientRegistry.ExpiredBinding> expired = clientRegistry.cleanupExpiredBindings(gracePeriodSeconds);

        for (ClientRegistry.ExpiredBinding binding : expired) {
            // 联动 SessionManager：释放过期 session 资源
            sessionManager.expireGracePeriod(binding.sessionId());
            log.warn("Grace period 过期，session 已释放: previousClientId={}, sessionId={}",
                    binding.previousClientId(), binding.sessionId());
        }
    }

    /**
     * 获取当前心跳序号（用于测试/监控）。
     *
     * @return 已发送的心跳总数
     */
    public long getHeartbeatSequence() {
        return heartbeatSequence.get();
    }
}
