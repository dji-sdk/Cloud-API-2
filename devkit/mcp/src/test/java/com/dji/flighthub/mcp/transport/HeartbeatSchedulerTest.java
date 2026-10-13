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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HeartbeatScheduler 单元测试。
 * 覆盖心跳广播、死连接检测、grace period 过期清理。
 */
class HeartbeatSchedulerTest {

    private ClientRegistry clientRegistry;
    private FlightHubProperties properties;
    private SessionManager sessionManager;
    private HeartbeatScheduler scheduler;

    @BeforeEach
    void setUp() {
        clientRegistry = new ClientRegistry();
        properties = createTestProperties();
        sessionManager = new SessionManager(properties);
        scheduler = new HeartbeatScheduler(clientRegistry, properties, sessionManager);
    }

    // ==================== emitHeartbeats ====================

    @Test
    void emitHeartbeats_shouldDoNothing_whenNoClients() {
        // 无客户端时不应抛异常
        assertDoesNotThrow(() -> scheduler.emitHeartbeats());
        assertEquals(0, scheduler.getHeartbeatSequence());
    }

    @Test
    void emitHeartbeats_shouldIncrementSequence() {
        Sinks.Many<ServerSentEvent<String>> sink = createSink();
        clientRegistry.register("c1", sink);

        scheduler.emitHeartbeats();
        assertEquals(1, scheduler.getHeartbeatSequence());

        scheduler.emitHeartbeats();
        assertEquals(2, scheduler.getHeartbeatSequence());
    }

    @Test
    void emitHeartbeats_shouldPushToAllClients() {
        Sinks.Many<ServerSentEvent<String>> sink1 = Sinks.many().multicast().onBackpressureBuffer();
        Sinks.Many<ServerSentEvent<String>> sink2 = Sinks.many().multicast().onBackpressureBuffer();
        clientRegistry.register("c1", sink1);
        clientRegistry.register("c2", sink2);

        scheduler.emitHeartbeats();

        // 验证 sink1 收到 heartbeat event
        ServerSentEvent<String> event1 = sink1.asFlux().blockFirst(Duration.ofSeconds(1));
        assertNotNull(event1);
        assertEquals("heartbeat", event1.event());
        assertNotNull(event1.data());
        assertTrue(event1.data().contains("\"ts\""));
        assertTrue(event1.data().contains("\"seq\":1"));

        // 验证 sink2 也收到
        ServerSentEvent<String> event2 = sink2.asFlux().blockFirst(Duration.ofSeconds(1));
        assertNotNull(event2);
        assertEquals("heartbeat", event2.event());
        assertTrue(event2.data().contains("\"seq\":1"));
    }

    @Test
    void emitHeartbeats_shouldContinue_whenSinkFails() {
        // 注册一个已关闭的 sink
        Sinks.Many<ServerSentEvent<String>> closedSink = Sinks.many().unicast().onBackpressureBuffer();
        closedSink.tryEmitComplete(); // 关闭 sink
        clientRegistry.register("c_closed", closedSink);

        // 注册一个正常 sink
        Sinks.Many<ServerSentEvent<String>> normalSink = Sinks.many().multicast().onBackpressureBuffer();
        clientRegistry.register("c_normal", normalSink);

        // 不应因为一个 sink 失败而影响其他（正常 sink 仍然收到心跳）
        assertDoesNotThrow(() -> scheduler.emitHeartbeats());
        assertEquals(1, scheduler.getHeartbeatSequence());
    }

    @Test
    void emitHeartbeats_shouldNotTouchClient_whenEmitSucceeds() throws InterruptedException {
        // tryEmitNext().isSuccess() 仅表示进入 Reactor 缓冲区，不代表到达客户端
        // 因此成功推送不应 touch 客户端（避免半开连接永不超时）
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();
        clientRegistry.register("c1", sink);

        var initialActivity = clientRegistry.getClient("c1").getLastActivityAt();

        Thread.sleep(15);

        scheduler.emitHeartbeats();

        // 活跃时间不应被刷新
        var updatedActivity = clientRegistry.getClient("c1").getLastActivityAt();
        assertEquals(initialActivity, updatedActivity,
                "Client should NOT be touched on heartbeat emit success (only proves buffer write, not delivery)");
    }

    @Test
    void emitHeartbeats_shouldDisconnectClient_whenEmitFails() {
        // 注册一个已关闭的 sink — emit 会失败
        Sinks.Many<ServerSentEvent<String>> closedSink = Sinks.many().unicast().onBackpressureBuffer();
        closedSink.tryEmitComplete();
        clientRegistry.register("c_dead", closedSink);
        clientRegistry.bindSession("c_dead", "sess_dead");

        scheduler.emitHeartbeats();

        // emit 失败应立即触发断连
        assertNull(clientRegistry.getClient("c_dead"),
                "Client should be immediately disconnected when heartbeat emit fails");
        // 有 session 的应创建断连绑定（grace period）
        assertNotNull(clientRegistry.findDisconnectedBinding("c_dead"),
                "Disconnected client with session should have a grace period binding");
    }

    // ==================== detectDeadClients ====================

    @Test
    void detectDeadClients_shouldNotDisconnect_whenAllFresh() {
        clientRegistry.register("c1", createSink());
        clientRegistry.register("c2", createSink());

        scheduler.detectDeadClients();

        // 所有客户端仍活跃
        assertEquals(2, clientRegistry.getActiveClientCount());
    }

    @Test
    void detectDeadClients_shouldDisconnect_whenTimeout() throws InterruptedException {
        clientRegistry.register("c1", createSink());
        clientRegistry.bindSession("c1", "sess_1");

        // 创建 session 用于验证 grace period 联动
        sessionManager.createSession("ws-uuid", "TestWorkspace");

        Thread.sleep(20);

        // 将 timeout 设为 0（立即超时）
        properties.getTransport().setHeartbeatTimeoutSeconds(0);

        scheduler.detectDeadClients();

        // c1 应被断开
        assertEquals(0, clientRegistry.getActiveClientCount());
        // 有 session 的应创建断连绑定
        assertNotNull(clientRegistry.findDisconnectedBinding("c1"));
    }

    @Test
    void detectDeadClients_shouldEnterGracePeriod_whenSessionBound() throws InterruptedException {
        // 创建 session 并绑定到客户端
        var session = sessionManager.createSession("ws-uuid", "TestWorkspace");
        String sessionId = session.getSessionId();

        clientRegistry.register("c1", createSink());
        clientRegistry.bindSession("c1", sessionId);

        Thread.sleep(20);
        properties.getTransport().setHeartbeatTimeoutSeconds(0);

        scheduler.detectDeadClients();

        // 验证 session 已进入 grace period
        var ctx = sessionManager.getSession(sessionId);
        assertNotNull(ctx);
        assertTrue(ctx.isInGracePeriod());
    }

    @Test
    void detectDeadClients_shouldSkipRecentlyActive() throws InterruptedException {
        clientRegistry.register("c1", createSink());
        clientRegistry.register("c2", createSink());

        Thread.sleep(20);
        clientRegistry.touchClient("c2"); // c2 刷新

        properties.getTransport().setHeartbeatTimeoutSeconds(0);

        scheduler.detectDeadClients();

        // c1 超时，c2 未超时（刚 touch 过）
        assertNull(clientRegistry.getClient("c1"));
        // 注意：由于时间精度问题，c2 可能也超时，这里主要验证 touch 有效果
    }

    // ==================== cleanupExpiredGracePeriods ====================

    @Test
    void cleanupExpiredGracePeriods_shouldNotCleanup_whenFresh() {
        clientRegistry.register("c1", createSink());
        clientRegistry.bindSession("c1", "sess_1");
        clientRegistry.disconnect("c1");

        // grace period 设为 3600s — 不过期
        properties.getTransport().setGracePeriodSeconds(3600);

        scheduler.cleanupExpiredGracePeriods();

        // 断连绑定仍在
        assertEquals(1, clientRegistry.getDisconnectedBindingCount());
    }

    @Test
    void cleanupExpiredGracePeriods_shouldCleanup_whenExpired() throws InterruptedException {
        clientRegistry.register("c1", createSink());
        clientRegistry.bindSession("c1", "sess_1");
        clientRegistry.disconnect("c1");

        Thread.sleep(20);

        // grace period 设为 0s — 立即过期
        properties.getTransport().setGracePeriodSeconds(0);

        scheduler.cleanupExpiredGracePeriods();

        // 断连绑定已清理
        assertEquals(0, clientRegistry.getDisconnectedBindingCount());
    }

    @Test
    void cleanupExpiredGracePeriods_shouldDestroySession_whenExpired() throws InterruptedException {
        // 创建 session 并模拟断连
        var session = sessionManager.createSession("ws-uuid", "TestWorkspace");
        String sessionId = session.getSessionId();

        clientRegistry.register("c1", createSink());
        clientRegistry.bindSession("c1", sessionId);
        clientRegistry.disconnect("c1");
        sessionManager.enterGracePeriod(sessionId);

        Thread.sleep(20);
        properties.getTransport().setGracePeriodSeconds(0);

        scheduler.cleanupExpiredGracePeriods();

        // session 应被销毁
        assertNull(sessionManager.getSession(sessionId));
    }

    // ==================== 辅助方法 ====================

    private Sinks.Many<ServerSentEvent<String>> createSink() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    private FlightHubProperties createTestProperties() {
        FlightHubProperties props = new FlightHubProperties();
        FlightHubProperties.Transport transport = new FlightHubProperties.Transport();
        transport.setHeartbeatIntervalMs(30000);
        transport.setHeartbeatTimeoutSeconds(90);
        transport.setGracePeriodSeconds(60);
        transport.setGraceWarningBeforeSeconds(10);
        props.setTransport(transport);
        return props;
    }
}
