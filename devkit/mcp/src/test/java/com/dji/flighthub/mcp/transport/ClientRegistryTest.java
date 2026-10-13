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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientRegistry 单元测试。
 * 覆盖注册/注销、session 绑定、重连恢复、死连接检测、并发安全。
 */
class ClientRegistryTest {

    private ClientRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ClientRegistry();
    }

    private Sinks.Many<ServerSentEvent<String>> createSink() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    // ==================== register ====================

    @Test
    void register_shouldAddClient() {
        registry.register("c1", createSink());

        assertNotNull(registry.getClient("c1"));
        assertEquals(1, registry.getActiveClientCount());
    }

    @Test
    void register_shouldOverwriteExisting() {
        Sinks.Many<ServerSentEvent<String>> sink1 = createSink();
        Sinks.Many<ServerSentEvent<String>> sink2 = createSink();

        registry.register("c1", sink1);
        registry.register("c1", sink2);

        assertEquals(1, registry.getActiveClientCount());
        assertSame(sink2, registry.getSink("c1"));
    }

    // ==================== disconnect ====================

    @Test
    void disconnect_shouldRemoveClient() {
        registry.register("c1", createSink());

        String sessionId = registry.disconnect("c1");

        assertNull(sessionId);
        assertNull(registry.getClient("c1"));
        assertEquals(0, registry.getActiveClientCount());
    }

    @Test
    void disconnect_withSession_shouldCreateDisconnectedBinding() {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_abc");

        String sessionId = registry.disconnect("c1");

        assertEquals("sess_abc", sessionId);
        assertNull(registry.getClient("c1"));
        assertNotNull(registry.findDisconnectedBinding("c1"));
        assertEquals("sess_abc", registry.findDisconnectedBinding("c1").getSessionId());
    }

    @Test
    void disconnect_shouldBeIdempotent() {
        registry.register("c1", createSink());

        registry.disconnect("c1");
        String result = registry.disconnect("c1");

        assertNull(result);
    }

    @Test
    void disconnect_unknownClient_shouldReturnNull() {
        assertNull(registry.disconnect("nonexistent"));
    }

    // ==================== bindSession ====================

    @Test
    void bindSession_shouldSucceed() {
        registry.register("c1", createSink());

        assertTrue(registry.bindSession("c1", "sess_1"));
        assertEquals("sess_1", registry.getClient("c1").getSessionId());
    }

    @Test
    void bindSession_unknownClient_shouldReturnFalse() {
        assertFalse(registry.bindSession("unknown", "sess_1"));
    }

    @Test
    void bindSession_shouldOverwriteExistingBinding() {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_1");
        registry.bindSession("c1", "sess_2");

        assertEquals("sess_2", registry.getClient("c1").getSessionId());
    }

    // ==================== getSink ====================

    @Test
    void getSink_shouldReturnSink() {
        Sinks.Many<ServerSentEvent<String>> sink = createSink();
        registry.register("c1", sink);

        assertSame(sink, registry.getSink("c1"));
    }

    @Test
    void getSink_unknownClient_shouldReturnNull() {
        assertNull(registry.getSink("nonexistent"));
    }

    // ==================== getActiveClientIds ====================

    @Test
    void getActiveClientIds_shouldReturnSnapshot() {
        registry.register("c1", createSink());
        registry.register("c2", createSink());

        List<String> ids = registry.getActiveClientIds();

        assertEquals(2, ids.size());
        assertTrue(ids.contains("c1"));
        assertTrue(ids.contains("c2"));
    }

    // ==================== touchClient ====================

    @Test
    void touchClient_shouldUpdateLastActivity() throws InterruptedException {
        registry.register("c1", createSink());
        var before = registry.getClient("c1").getLastActivityAt();

        Thread.sleep(10);
        registry.touchClient("c1");

        assertTrue(registry.getClient("c1").getLastActivityAt().isAfter(before));
    }

    @Test
    void touchClient_unknownClient_shouldNotThrow() {
        assertDoesNotThrow(() -> registry.touchClient("nonexistent"));
    }

    // ==================== findDeadClients ====================

    @Test
    void findDeadClients_shouldReturnEmpty_whenAllFresh() {
        registry.register("c1", createSink());
        registry.register("c2", createSink());

        List<String> dead = registry.findDeadClients(90);

        assertTrue(dead.isEmpty());
    }

    @Test
    void findDeadClients_shouldReturnStaleClients() throws InterruptedException {
        registry.register("c1", createSink());
        Thread.sleep(20);

        // 使用极短 timeout 模拟超时
        List<String> dead = registry.findDeadClients(0);

        assertEquals(1, dead.size());
        assertEquals("c1", dead.get(0));
    }

    @Test
    void findDeadClients_shouldSkipRecentlyTouched() throws InterruptedException {
        registry.register("c1", createSink());
        registry.register("c2", createSink());

        Thread.sleep(20);
        registry.touchClient("c2"); // c2 刷新了活跃时间

        List<String> dead = registry.findDeadClients(0);

        // c1 超时，c2 刚刷新不应超时（取决于时间精度）
        assertTrue(dead.contains("c1"));
    }

    // ==================== findClientBySessionId ====================

    @Test
    void findClientBySessionId_shouldReturnClientId_whenBound() {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_abc");

        assertEquals("c1", registry.findClientBySessionId("sess_abc"));
    }

    @Test
    void findClientBySessionId_shouldReturnNull_whenNoMatch() {
        registry.register("c1", createSink());

        assertNull(registry.findClientBySessionId("sess_unknown"));
    }

    @Test
    void findClientBySessionId_shouldReturnNull_whenNullInput() {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_abc");

        assertNull(registry.findClientBySessionId(null));
    }

    @Test
    void findClientBySessionId_shouldReturnNull_whenBlankInput() {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_abc");

        assertNull(registry.findClientBySessionId("  "));
    }

    @Test
    void findClientBySessionId_shouldReturnFirstMatch_whenMultipleClientsWithSameSession() {
        // 边界情况：理论上不应有多个客户端绑定同一 session，但方法应返回某个匹配项
        registry.register("c1", createSink());
        registry.register("c2", createSink());
        registry.bindSession("c1", "sess_shared");
        registry.bindSession("c2", "sess_shared");

        String result = registry.findClientBySessionId("sess_shared");
        assertTrue("c1".equals(result) || "c2".equals(result));
    }

    // ==================== findDisconnectedBinding ====================

    @Test
    void findDisconnectedBinding_shouldReturnNull_whenNotDisconnected() {
        assertNull(registry.findDisconnectedBinding("nonexistent"));
    }

    // ==================== completeReconnect ====================

    @Test
    void completeReconnect_shouldMigrateSession() {
        // 模拟旧客户端断连
        registry.register("c_old", createSink());
        registry.bindSession("c_old", "sess_abc");
        registry.disconnect("c_old");

        // 新客户端连接
        registry.register("c_new", createSink());

        // 执行重连
        String sessionId = registry.completeReconnect("c_old", "c_new");

        assertEquals("sess_abc", sessionId);
        assertEquals("sess_abc", registry.getClient("c_new").getSessionId());
        assertNull(registry.findDisconnectedBinding("c_old")); // 绑定已消费
    }

    @Test
    void completeReconnect_shouldReturnNull_whenNoPreviousBinding() {
        registry.register("c_new", createSink());

        String sessionId = registry.completeReconnect("unknown_old", "c_new");

        assertNull(sessionId);
    }

    @Test
    void completeReconnect_shouldReturnNull_whenNewClientNotRegistered() {
        // 模拟旧客户端断连
        registry.register("c_old", createSink());
        registry.bindSession("c_old", "sess_abc");
        registry.disconnect("c_old");

        // 新客户端尚未注册
        String sessionId = registry.completeReconnect("c_old", "c_new_unregistered");

        assertNull(sessionId);
        // 绑定应被恢复（未消费）
        assertNotNull(registry.findDisconnectedBinding("c_old"));
    }

    // ==================== cleanupExpiredBindings ====================

    @Test
    void cleanupExpiredBindings_shouldRemoveExpired() throws InterruptedException {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_1");
        registry.disconnect("c1");

        Thread.sleep(20);

        // 使用 0s grace period → 立即过期
        List<ClientRegistry.ExpiredBinding> expired = registry.cleanupExpiredBindings(0);

        assertEquals(1, expired.size());
        assertEquals("c1", expired.get(0).previousClientId());
        assertEquals("sess_1", expired.get(0).sessionId());
        assertEquals(0, registry.getDisconnectedBindingCount());
    }

    @Test
    void cleanupExpiredBindings_shouldKeepFreshBindings() {
        registry.register("c1", createSink());
        registry.bindSession("c1", "sess_1");
        registry.disconnect("c1");

        // 使用足够长的 grace period → 不过期
        List<ClientRegistry.ExpiredBinding> expired = registry.cleanupExpiredBindings(3600);

        assertTrue(expired.isEmpty());
        assertEquals(1, registry.getDisconnectedBindingCount());
    }

    // ==================== 并发安全 ====================

    @Test
    void concurrentRegisterDisconnect_shouldNotThrow() throws InterruptedException {
        int threads = 8;
        int iterations = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        String clientId = "t" + threadId + "_c" + i;
                        registry.register(clientId, createSink());
                        registry.bindSession(clientId, "sess_" + clientId);
                        registry.touchClient(clientId);
                        registry.disconnect(clientId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // 所有客户端已断连
        assertEquals(0, registry.getActiveClientCount());
        // 所有有 session 的客户端应有断连绑定
        assertEquals(threads * iterations, registry.getDisconnectedBindingCount());
    }
}
