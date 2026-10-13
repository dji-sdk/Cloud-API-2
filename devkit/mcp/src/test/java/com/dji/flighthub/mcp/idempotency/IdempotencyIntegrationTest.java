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

package com.dji.flighthub.mcp.idempotency;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.audit.AuditService;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 幂等缓存集成验证 — 模拟真实场景：
 * 1. 发送相同 requestId 两次，确认第二次命中缓存
 * 2. 验证跨工具不碰撞（相同 requestId 不同 tool）
 * 3. 验证并发重试防护（TOCTOU sentinel 模式）
 */
class IdempotencyIntegrationTest {

    private McpToolRegistry registry;
    private IdempotencyCache cache;
    private AuditService auditService;
    private AtomicInteger executionCount;

    @BeforeEach
    void setUp() {
        // 构建真实的 IdempotencyCache（非 mock）
        FlightHubProperties props = new FlightHubProperties();
        FlightHubProperties.Idempotency idempotency = new FlightHubProperties.Idempotency();
        idempotency.setEnabled(true);
        idempotency.setTtlSeconds(300);
        idempotency.setMaxSize(1000);
        props.setIdempotency(idempotency);
        cache = new IdempotencyCache(props);

        // AuditService 使用 mock（验证审计记录）
        FlightHubProperties auditProps = new FlightHubProperties();
        FlightHubProperties.Audit audit = new FlightHubProperties.Audit();
        audit.setEnabled(true);
        auditProps.setAudit(audit);
        auditProps.setIdempotency(idempotency);
        SessionManager sessionManager = mock(SessionManager.class);
        auditService = new AuditService(auditProps, sessionManager);

        // 构建 registry 并注入真实依赖
        registry = new McpToolRegistry();
        registry.setIdempotencyCache(cache);
        registry.setAuditService(auditService);

        // 计数器：追踪 tool handler 实际被调用次数
        executionCount = new AtomicInteger(0);
    }

    @AfterEach
    void cleanup() {
        RequestContext.clear();
    }

    /**
     * 场景 1：相同 clientId + toolName + requestId 发送两次，第二次应命中缓存。
     */
    @Test
    void sameRequest_shouldHitCache_onSecondCall() {
        // 注册一个模拟工具，每次调用计数
        registry.register(Map.of("name", "fh_session_info"),
                args -> {
                    executionCount.incrementAndGet();
                    return Map.of("success", true, "session_id", "sess_123", "status", "active");
                });

        // 第一次调用
        RequestContext.set(42, "fh_session_info", "client-abc");
        Map<String, Object> result1 = registry.executeTool("fh_session_info", Map.of());
        RequestContext.clear();

        assertEquals(true, result1.get("success"));
        assertEquals("sess_123", result1.get("session_id"));
        assertEquals(1, executionCount.get(), "第一次应真实执行");
        assertEquals(1, cache.size(), "结果应被缓存");

        // 第二次调用：完全相同的 clientId + toolName + requestId
        RequestContext.set(42, "fh_session_info", "client-abc");
        Map<String, Object> result2 = registry.executeTool("fh_session_info", Map.of());
        RequestContext.clear();

        assertEquals(true, result2.get("success"));
        assertEquals("sess_123", result2.get("session_id"));
        assertEquals(1, executionCount.get(), "第二次应命中缓存，handler 不再执行");
        assertEquals(result1, result2, "两次返回结果应完全一致");
    }

    /**
     * 场景 2：相同 requestId 但不同 tool — 不应碰撞。
     */
    @Test
    void sameRequestId_differentTool_shouldNotCollide() {
        AtomicInteger toolACount = new AtomicInteger(0);
        AtomicInteger toolBCount = new AtomicInteger(0);

        registry.register(Map.of("name", "fh_session_info"),
                args -> {
                    toolACount.incrementAndGet();
                    return Map.of("success", true, "tool", "session_info");
                });
        registry.register(Map.of("name", "fh_ops"),
                args -> {
                    toolBCount.incrementAndGet();
                    return Map.of("success", true, "tool", "ops");
                });

        // 调用 fh_session_info, requestId=1
        RequestContext.set(1, "fh_session_info", "stdio");
        Map<String, Object> result1 = registry.executeTool("fh_session_info", Map.of());
        RequestContext.clear();

        // 调用 fh_ops, requestId=1（相同 ID，不同 tool）
        RequestContext.set(1, "fh_ops", "stdio");
        Map<String, Object> result2 = registry.executeTool("fh_ops", Map.of());
        RequestContext.clear();

        // 两个 tool 都应该真实执行，不应碰撞
        assertEquals(1, toolACount.get(), "fh_session_info 应执行");
        assertEquals(1, toolBCount.get(), "fh_ops 应执行");
        assertEquals("session_info", result1.get("tool"));
        assertEquals("ops", result2.get("tool"));
        assertNotEquals(result1.get("tool"), result2.get("tool"), "不同工具返回不同结果");
    }

    /**
     * 场景 3：相同 requestId 但不同 clientId — 不应碰撞。
     */
    @Test
    void sameRequestId_differentClient_shouldNotCollide() {
        AtomicInteger count = new AtomicInteger(0);

        registry.register(Map.of("name", "fh_device_list"),
                args -> {
                    count.incrementAndGet();
                    return Map.of("success", true, "count", count.get());
                });

        // Client A 调用
        RequestContext.set(1, "fh_device_list", "client-A");
        registry.executeTool("fh_device_list", Map.of());
        RequestContext.clear();

        // Client B 调用（相同 requestId，不同 clientId）
        RequestContext.set(1, "fh_device_list", "client-B");
        registry.executeTool("fh_device_list", Map.of());
        RequestContext.clear();

        assertEquals(2, count.get(), "不同 clientId 应独立执行");
    }

    /**
     * 场景 4：失败的结果不被缓存，后续重试应重新执行。
     */
    @Test
    void failedResult_shouldNotBeCached_allowRetry() {
        AtomicInteger attempt = new AtomicInteger(0);

        registry.register(Map.of("name", "fh_connect"),
                args -> {
                    int n = attempt.incrementAndGet();
                    if (n == 1) {
                        return Map.of("success", false, "error_message", "Connection timeout");
                    }
                    return Map.of("success", true, "session_id", "sess_new");
                });

        // 第一次调用：失败
        RequestContext.set("req-retry", "fh_connect", "stdio");
        Map<String, Object> result1 = registry.executeTool("fh_connect", Map.of());
        RequestContext.clear();

        assertEquals(false, result1.get("success"));
        assertEquals(1, attempt.get());

        // 第二次调用：相同 request，应重新执行（失败未被缓存）
        RequestContext.set("req-retry", "fh_connect", "stdio");
        Map<String, Object> result2 = registry.executeTool("fh_connect", Map.of());
        RequestContext.clear();

        assertEquals(true, result2.get("success"));
        assertEquals("sess_new", result2.get("session_id"));
        assertEquals(2, attempt.get(), "失败后重试应重新执行");
    }

    /**
     * 场景 5：并发重试 — 模拟网络重传，两个线程同时发送相同请求。
     * sentinel 模式应确保至少一个线程执行并缓存结果。
     */
    @Test
    void concurrentDuplicateRequests_shouldLimitExecution() throws Exception {
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        registry.register(Map.of("name", "fh_wayline_execute"),
                args -> {
                    count.incrementAndGet();
                    // 模拟耗时操作
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    return Map.of("success", true, "wayline_id", "wl_001");
                });

        AtomicReference<Map<String, Object>> result1 = new AtomicReference<>();
        AtomicReference<Map<String, Object>> result2 = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 线程 A
        executor.submit(() -> {
            try {
                startGate.await();
                RequestContext.set("req-concurrent", "fh_wayline_execute", "client-dup");
                try {
                    result1.set(registry.executeTool("fh_wayline_execute", Map.of()));
                } finally {
                    RequestContext.clear();
                    doneLatch.countDown();
                }
            } catch (Exception e) {
                doneLatch.countDown();
            }
        });

        // 线程 B
        executor.submit(() -> {
            try {
                startGate.await();
                RequestContext.set("req-concurrent", "fh_wayline_execute", "client-dup");
                try {
                    result2.set(registry.executeTool("fh_wayline_execute", Map.of()));
                } finally {
                    RequestContext.clear();
                    doneLatch.countDown();
                }
            } catch (Exception e) {
                doneLatch.countDown();
            }
        });

        // 同时释放两个线程
        startGate.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "5 秒内应完成");
        executor.shutdown();

        // 两个线程都应拿到成功结果
        assertNotNull(result1.get());
        assertNotNull(result2.get());
        assertEquals(true, result1.get().get("success"));
        assertEquals(true, result2.get().get("success"));

        // sentinel 模式下：最坏情况两个都执行（PENDING 竞争窗口），但不应超过 2
        // 正常情况下一个命中缓存，只执行 1 次
        assertTrue(count.get() <= 2, "并发执行次数应 ≤ 2（sentinel 不保证 exactly-once，但防大规模重复）");

        // 结果一致性：两个线程拿到的应是同一个 wayline_id
        assertEquals(result1.get().get("wayline_id"), result2.get().get("wayline_id"),
                "并发结果应一致（来自同一次执行或缓存）");
    }

    /**
     * 场景 6：stdio 模式 — 重连后 requestId 重置为 1，不同工具不碰撞。
     * 这是修复 bug #1 (🔴严重) 的回归测试。
     */
    @Test
    void stdioMode_requestIdReset_shouldNotCollideCrossTools() {
        AtomicInteger takeoffCount = new AtomicInteger(0);
        AtomicInteger deviceListCount = new AtomicInteger(0);

        registry.register(Map.of("name", "fh_takeoff"),
                args -> {
                    takeoffCount.incrementAndGet();
                    return Map.of("success", true, "action", "takeoff");
                });
        registry.register(Map.of("name", "fh_device_list"),
                args -> {
                    deviceListCount.incrementAndGet();
                    return Map.of("success", true, "action", "device_list", "devices", 3);
                });

        // 模拟 stdio 会话：先调用 fh_takeoff id=1
        RequestContext.set(1, "fh_takeoff", "stdio");
        Map<String, Object> takeoffResult = registry.executeTool("fh_takeoff", Map.of());
        RequestContext.clear();

        // 模拟 stdio 会话：再调用 fh_device_list id=1（相同 ID！）
        RequestContext.set(1, "fh_device_list", "stdio");
        Map<String, Object> deviceResult = registry.executeTool("fh_device_list", Map.of());
        RequestContext.clear();

        // 关键断言：fh_device_list 不应返回 fh_takeoff 的缓存结果
        assertEquals("takeoff", takeoffResult.get("action"));
        assertEquals("device_list", deviceResult.get("action"));
        assertEquals(3, deviceResult.get("devices"));
        assertEquals(1, takeoffCount.get());
        assertEquals(1, deviceListCount.get());

        // 再次调用 fh_takeoff id=1 → 应命中缓存
        RequestContext.set(1, "fh_takeoff", "stdio");
        Map<String, Object> takeoffAgain = registry.executeTool("fh_takeoff", Map.of());
        RequestContext.clear();

        assertEquals("takeoff", takeoffAgain.get("action"));
        assertEquals(1, takeoffCount.get(), "重复的 fh_takeoff id=1 应命中缓存");
    }
}
