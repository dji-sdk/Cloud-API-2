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

package com.dji.flighthub.mcp.config;

import com.dji.flighthub.mcp.audit.AuditService;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.ratelimit.ToolRateLimiter;
import com.dji.flighthub.mcp.security.SecurityLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * McpToolRegistry 单元测试。
 */
class McpToolRegistryTest {

    private McpToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new McpToolRegistry();
    }

    @AfterEach
    void cleanup() {
        RequestContext.clear();
    }

    @Test
    void register_shouldIncreaseToolCount() {
        assertEquals(0, registry.getToolCount());

        registry.register(
                Map.of("name", "test_tool"),
                args -> Map.of("success", true));

        assertEquals(1, registry.getToolCount());
    }

    @Test
    void getToolDefinitions_shouldReturnAllDefinitions() {
        registry.register(Map.of("name", "tool_a"), args -> Map.of());
        registry.register(Map.of("name", "tool_b"), args -> Map.of());

        var defs = registry.getToolDefinitions();
        assertEquals(2, defs.size());
        assertEquals("tool_a", defs.get(0).get("name"));
        assertEquals("tool_b", defs.get(1).get("name"));
    }

    @Test
    void executeTool_shouldCallCorrectHandler() {
        registry.register(
                Map.of("name", "echo"),
                args -> Map.of("success", true, "echo", args.get("msg")));

        Map<String, Object> result = registry.executeTool("echo", Map.of("msg", "hello"));

        assertEquals(true, result.get("success"));
        assertEquals("hello", result.get("echo"));
    }

    @Test
    void executeTool_shouldReturnErrorForUnknownTool() {
        Map<String, Object> result = registry.executeTool("nonexistent", Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Capability not available"));
        assertTrue(result.get("error_message").toString().contains("nonexistent"));
    }

    @Test
    void executeTool_shouldPassEmptyArgsWhenNull() {
        registry.register(
                Map.of("name", "null_safe"),
                args -> Map.of("success", true, "has_args", args != null));

        Map<String, Object> result = registry.executeTool("null_safe", Map.of());
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("has_args"));
    }

    @Test
    void isHumanConfirmRequired_shouldReturnTrue_whenRegisteredWithFlag() {
        registry.register(Map.of("name", "dangerous_tool"), args -> Map.of(), true);
        assertTrue(registry.isHumanConfirmRequired("dangerous_tool"));
    }

    @Test
    void isHumanConfirmRequired_shouldReturnFalse_whenRegisteredWithoutFlag() {
        registry.register(Map.of("name", "safe_tool"), args -> Map.of());
        assertFalse(registry.isHumanConfirmRequired("safe_tool"));
    }

    @Test
    void isHumanConfirmRequired_shouldReturnFalse_whenToolNotFound() {
        assertFalse(registry.isHumanConfirmRequired("nonexistent"));
    }

    // ==================== 幂等缓存集成测试 ====================

    @Test
    void executeTool_shouldReturnCachedResult_whenIdempotencyHit() {
        // 准备：mock 幂等缓存，模拟命中
        IdempotencyCache cache = mock(IdempotencyCache.class);
        Map<String, Object> cachedResult = Map.of("success", true, "data", "cached");
        when(cache.getOrMarkPending("client1:expensive_tool:req1")).thenReturn(cachedResult);

        registry.setIdempotencyCache(cache);
        registry.register(Map.of("name", "expensive_tool"),
                args -> Map.of("success", true, "data", "fresh"));

        // 设置 RequestContext
        RequestContext.set("req1", "expensive_tool", "client1");

        Map<String, Object> result = registry.executeTool("expensive_tool", Map.of());

        // 应返回缓存结果，handler 不被调用
        assertEquals("cached", result.get("data"));
    }

    @Test
    void executeTool_shouldCacheSuccessResult() {
        IdempotencyCache cache = mock(IdempotencyCache.class);
        when(cache.getOrMarkPending(anyString())).thenReturn(null); // 未命中

        registry.setIdempotencyCache(cache);
        registry.register(Map.of("name", "simple_tool"),
                args -> Map.of("success", true, "result", "ok"));

        RequestContext.set("req2", "simple_tool", "client2");

        registry.executeTool("simple_tool", Map.of());

        // 验证成功结果被写入缓存
        verify(cache).put(eq("client2:simple_tool:req2"), argThat(r ->
                Boolean.TRUE.equals(r.get("success"))));
    }

    @Test
    void executeTool_shouldNotCacheFailedResult() {
        IdempotencyCache cache = mock(IdempotencyCache.class);
        when(cache.getOrMarkPending(anyString())).thenReturn(null);

        registry.setIdempotencyCache(cache);
        registry.register(Map.of("name", "failing_tool"),
                args -> Map.of("success", false, "error_message", "SDK error"));

        RequestContext.set("req3", "failing_tool", "client3");

        registry.executeTool("failing_tool", Map.of());

        // 失败结果不应写入缓存，且应移除 pending 占位
        verify(cache, never()).put(eq("client3:failing_tool:req3"), any());
        verify(cache).removePending("client3:failing_tool:req3");
    }

    @Test
    void executeTool_shouldWorkWithoutCache() {
        // 不设置 idempotencyCache，registry 应仍正常工作
        registry.register(Map.of("name", "basic_tool"),
                args -> Map.of("success", true));

        Map<String, Object> result = registry.executeTool("basic_tool", Map.of());
        assertEquals(true, result.get("success"));
    }

    // ==================== 审计日志集成测试 ====================

    @Test
    void executeTool_shouldCallAuditOnSuccess() {
        AuditService audit = mock(AuditService.class);
        registry.setAuditService(audit);
        registry.register(Map.of("name", "audited_tool"),
                args -> Map.of("success", true));

        RequestContext.set("req-a1", "audited_tool", "stdio");

        registry.executeTool("audited_tool", Map.of());

        verify(audit).logExecution(eq("audited_tool"), eq(true), isNull(), anyLong(), eq(false), eq(false), eq(false));
    }

    @Test
    void executeTool_shouldCallAuditOnFailure() {
        AuditService audit = mock(AuditService.class);
        registry.setAuditService(audit);
        registry.register(Map.of("name", "fail_tool"),
                args -> Map.of("success", false, "error_message", "something broke"));

        RequestContext.set("req-a2", "fail_tool", "client-x");

        registry.executeTool("fail_tool", Map.of());

        verify(audit).logExecution(eq("fail_tool"), eq(false), eq("something broke"), anyLong(), eq(false), eq(false), eq(false));
    }

    @Test
    void executeTool_shouldCallAuditWithIdempotentHit() {
        AuditService audit = mock(AuditService.class);
        IdempotencyCache cache = mock(IdempotencyCache.class);
        when(cache.getOrMarkPending("client-y:cached_tool:req-a3")).thenReturn(Map.of("success", true));

        registry.setAuditService(audit);
        registry.setIdempotencyCache(cache);
        registry.register(Map.of("name", "cached_tool"), args -> Map.of("success", true));

        RequestContext.set("req-a3", "cached_tool", "client-y");

        registry.executeTool("cached_tool", Map.of());

        verify(audit).logExecution(eq("cached_tool"), eq(true), isNull(), anyLong(), eq(true), eq(false), eq(false));
    }

    @Test
    void executeTool_shouldNotBreak_whenAuditThrows() {
        AuditService audit = mock(AuditService.class);
        doThrow(new RuntimeException("audit down")).when(audit)
                .logExecution(anyString(), anyBoolean(), any(), anyLong(), anyBoolean(), anyBoolean(), anyBoolean());

        registry.setAuditService(audit);
        registry.register(Map.of("name", "resilient_tool"),
                args -> Map.of("success", true));

        RequestContext.set("req-a4", "resilient_tool", "stdio");

        // 审计异常不应传播
        Map<String, Object> result = registry.executeTool("resilient_tool", Map.of());
        assertEquals(true, result.get("success"));
    }

    // ==================== 输出预算集成测试 ====================

    /**
     * 构造带默认序列化器的输出预算守卫。
     *
     * @param maxBytes 字节阈值
     * @return 守卫实例
     */
    private OutputBudgetGuard newBudgetGuard(int maxBytes) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getOutput().setMaxBytes(maxBytes);
        properties.getOutput().setMaxArrayItems(10);
        return new OutputBudgetGuard(new com.fasterxml.jackson.databind.ObjectMapper(), properties);
    }

    /**
     * 构造超预算的成功响应。
     *
     * @param count 列表条数
     * @return 响应 Map
     */
    private Map<String, Object> buildOversizedResult(int count) {
        List<Map<String, Object>> items = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            items.add(Map.of("id", "item-" + i, "name", "payload-of-item-" + i));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", count);
        data.put("items", items);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    @Test
    void executeTool_shouldTruncateResult_whenOutputExceedsBudget() {
        registry.setOutputBudgetGuard(newBudgetGuard(1024));
        registry.register(Map.of("name", "chatty_tool"), args -> buildOversizedResult(500));

        Map<String, Object> result = registry.executeTool("chatty_tool", Map.of());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(Boolean.TRUE, data.get(OutputBudgetGuard.KEY_TRUNCATED));
        assertNotNull(data.get(OutputBudgetGuard.KEY_DETAIL));
        assertEquals(true, result.get("success"));
    }

    @Test
    void executeTool_shouldKeepResultUnchanged_whenOutputWithinBudget() {
        registry.setOutputBudgetGuard(newBudgetGuard(1024 * 1024));
        registry.register(Map.of("name", "quiet_tool"), args -> buildOversizedResult(2));

        Map<String, Object> result = registry.executeTool("quiet_tool", Map.of());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNull(data.get(OutputBudgetGuard.KEY_TRUNCATED));
    }

    @Test
    void executeTool_shouldCacheTruncatedResult_whenOutputBudgetApplies() {
        IdempotencyCache cache = mock(IdempotencyCache.class);
        when(cache.getOrMarkPending(anyString())).thenReturn(null);

        registry.setIdempotencyCache(cache);
        registry.setOutputBudgetGuard(newBudgetGuard(1024));
        registry.register(Map.of("name", "cached_chatty_tool"), args -> buildOversizedResult(500));

        RequestContext.set("req-b1", "cached_chatty_tool", "client-b");

        registry.executeTool("cached_chatty_tool", Map.of());

        // 写入缓存的必须是截断后的结果，避免缓存放大内存占用
        verify(cache).put(eq("client-b:cached_chatty_tool:req-b1"), argThat(r -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) r.get("data");
            return data != null && Boolean.TRUE.equals(data.get(OutputBudgetGuard.KEY_TRUNCATED));
        }));
    }

    @Test
    void executeTool_shouldReturnOriginal_whenOutputBudgetGuardThrows() {
        OutputBudgetGuard guard = mock(OutputBudgetGuard.class);
        when(guard.apply(anyString(), any())).thenThrow(new RuntimeException("guard down"));

        registry.setOutputBudgetGuard(guard);
        registry.register(Map.of("name", "resilient_budget_tool"),
                args -> Map.of("success", true, "data", "original"));

        Map<String, Object> result = registry.executeTool("resilient_budget_tool", Map.of());

        // 兜底异常不应传播，业务结果原样返回
        assertEquals("original", result.get("data"));
    }

    @Test
    void executeTool_shouldWorkWithoutOutputBudgetGuard() {
        // 不设置 outputBudgetGuard，registry 应仍正常工作
        registry.register(Map.of("name", "no_guard_tool"),
                args -> Map.of("success", true, "data", "raw"));

        Map<String, Object> result = registry.executeTool("no_guard_tool", Map.of());

        assertEquals("raw", result.get("data"));
    }

    // ==================== 不支持能力提示（模糊匹配）测试 ====================

    @Test
    void executeTool_shouldSuggestSimilarTools_whenPrefixMatches() {
        registry.register(Map.of("name", "fh_takeoff"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_take_photo"), args -> Map.of("success", true));

        Map<String, Object> result = registry.executeTool("fh_take", Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Capability not available"));
        assertNotNull(result.get("similar_tools"));
        @SuppressWarnings("unchecked")
        List<String> similar = (List<String>) result.get("similar_tools");
        assertTrue(similar.contains("fh_takeoff"));
        assertTrue(similar.contains("fh_take_photo"));
    }

    @Test
    void executeTool_shouldSuggestSimilarTools_whenKeywordContains() {
        registry.register(Map.of("name", "fh_live_start"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_media_list"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_media_record"), args -> Map.of("success", true));

        Map<String, Object> result = registry.executeTool("fh_media", Map.of());

        assertEquals(false, result.get("success"));
        @SuppressWarnings("unchecked")
        List<String> similar = (List<String>) result.get("similar_tools");
        assertNotNull(similar);
        // 前缀匹配应该命中 fh_media_list 和 fh_media_record
        assertTrue(similar.contains("fh_media_list"));
        assertTrue(similar.contains("fh_media_record"));
    }

    @Test
    void executeTool_shouldSuggestSimilarTools_byLevenshteinDistance() {
        registry.register(Map.of("name", "fh_takeoff"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_flyto"), args -> Map.of("success", true));

        // "fh_takeof" has Levenshtein distance 1 from "fh_takeoff"
        Map<String, Object> result = registry.executeTool("fh_takeof", Map.of());

        assertEquals(false, result.get("success"));
        @SuppressWarnings("unchecked")
        List<String> similar = (List<String>) result.get("similar_tools");
        assertNotNull(similar);
        assertTrue(similar.contains("fh_takeoff"));
    }

    @Test
    void executeTool_shouldNotIncludeSimilarTools_whenNoneMatch() {
        registry.register(Map.of("name", "fh_takeoff"), args -> Map.of("success", true));

        // 一个完全不相关的名字
        Map<String, Object> result = registry.executeTool("zzz_completely_different_xyz", Map.of());

        assertEquals(false, result.get("success"));
        // similar_tools 应为 null（未添加到响应）
        assertNull(result.get("similar_tools"));
        assertTrue(result.get("suggestion").toString().contains("tools/list"));
    }

    @Test
    void executeTool_shouldLimitSuggestionsToThree() {
        registry.register(Map.of("name", "fh_media_list"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_media_download"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_media_delete"), args -> Map.of("success", true));
        registry.register(Map.of("name", "fh_media_record"), args -> Map.of("success", true));

        // "fh_media" 前缀匹配 4 个 tool，但结果应限制为最多 3 个
        Map<String, Object> result = registry.executeTool("fh_media", Map.of());

        @SuppressWarnings("unchecked")
        List<String> similar = (List<String>) result.get("similar_tools");
        assertNotNull(similar);
        assertTrue(similar.size() <= 3, "Should suggest at most 3 tools, got: " + similar.size());
    }

    // ==================== 工具级限流集成测试 ====================

    /** 测试用可控时钟 */
    private final AtomicLong clock = new AtomicLong(1_000L);

    /**
     * 构建一个真实限流器（可控时钟），便于验证拦截顺序与配额消耗。
     *
     * @param config 限流配置
     * @return 限流器实例
     */
    private ToolRateLimiter createRateLimiter(FlightHubProperties.RateLimit config) {
        FlightHubProperties props = new FlightHubProperties();
        props.setRateLimit(config);
        return new ToolRateLimiter(props, clock::get);
    }

    /**
     * 设置带 sessionId 的请求上下文。
     *
     * @param requestId JSON-RPC request id
     * @param toolName  工具名
     * @param clientId  客户端标识
     * @param sessionId session ID
     */
    private void setContext(String requestId, String toolName, String clientId, String sessionId) {
        RequestContext.set(requestId, toolName, clientId);
        RequestContext.current().setSessionId(sessionId);
    }

    @Test
    void executeTool_shouldReject_whenRateLimitExceeded() {
        AuditService audit = mock(AuditService.class);
        registry.setAuditService(audit);
        registry.setToolRateLimiter(createRateLimiter(new FlightHubProperties.RateLimit()));

        boolean[] invoked = {false};
        registry.register(Map.of("name", "fh_takeoff"),
                args -> {
                    invoked[0] = true;
                    return Map.of("success", true);
                }, false, SecurityLevel.FLIGHT);

        setContext("req-rl-1", "fh_takeoff", "client-rl", "sess-rl");
        assertTrue(Boolean.TRUE.equals(registry.executeTool("fh_takeoff", Map.of()).get("success")));

        // 同一 session 立即再次调用：应被拒绝且不进入 handler
        invoked[0] = false;
        Map<String, Object> rejected = registry.executeTool("fh_takeoff", Map.of());

        assertEquals(false, rejected.get("success"));
        assertEquals(McpToolRegistry.ERROR_CODE_RATE_LIMITED, rejected.get("error_code"));
        assertEquals(true, rejected.get("rate_limited"));
        assertTrue((Long) rejected.get("retry_after_ms") > 0L);
        assertEquals(6, rejected.get("limit_rpm"));
        assertFalse(invoked[0], "被限流时不应调用 tool handler");

        verify(audit).logExecution(eq("fh_takeoff"), eq(false), anyString(), anyLong(),
                eq(false), eq(true), eq(false));
    }

    @Test
    void executeTool_shouldReleasePending_whenRateLimited() {
        IdempotencyCache cache = mock(IdempotencyCache.class);
        // Mockito 对 Map 返回值默认给空 Map 而非 null，必须显式打桩为「未命中」
        when(cache.getOrMarkPending(anyString())).thenReturn(null);
        registry.setIdempotencyCache(cache);
        registry.setToolRateLimiter(createRateLimiter(new FlightHubProperties.RateLimit()));
        registry.register(Map.of("name", "fh_takeoff"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);

        setContext("req-leak-1", "fh_takeoff", "client-leak", "sess-leak");
        assertTrue(Boolean.TRUE.equals(registry.executeTool("fh_takeoff", Map.of()).get("success")));

        // 第二次调用被限流：幂等占位必须释放，否则死循环会把缓存占满
        setContext("req-leak-2", "fh_takeoff", "client-leak", "sess-leak");
        Map<String, Object> rejected = registry.executeTool("fh_takeoff", Map.of());
        assertEquals(false, rejected.get("success"));

        verify(cache).removePending("client-leak:fh_takeoff:req-leak-2");
        verify(cache, never()).put(eq("client-leak:fh_takeoff:req-leak-2"), any());
    }

    @Test
    void executeTool_shouldNotConsumeQuota_whenIdempotentHit() {
        IdempotencyCache cache = mock(IdempotencyCache.class);
        when(cache.getOrMarkPending("client-idem:cached_tool:req-idem"))
                .thenReturn(Map.of("success", true, "data", "cached"));

        registry.setIdempotencyCache(cache);
        registry.setToolRateLimiter(createRateLimiter(new FlightHubProperties.RateLimit()));
        registry.register(Map.of("name", "cached_tool"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);

        setContext("req-idem", "cached_tool", "client-idem", "sess-idem");

        // 幂等命中在前，限流在后：重放多次都应返回缓存结果
        for (int i = 0; i < 5; i++) {
            Map<String, Object> result = registry.executeTool("cached_tool", Map.of());
            assertEquals("cached", result.get("data"));
        }
    }

    @Test
    void executeTool_shouldNeverReject_whenSecurityLevelIsCritical() {
        registry.setToolRateLimiter(createRateLimiter(new FlightHubProperties.RateLimit()));
        registry.register(Map.of("name", "fh_emergency_stop"),
                args -> Map.of("success", true), true, SecurityLevel.CRITICAL);

        setContext("req-l3", "fh_emergency_stop", "client-l3", "sess-l3");

        // L3 配额为 0（不限流）：紧急停止必须永远可执行
        for (int i = 0; i < 50; i++) {
            assertEquals(true, registry.executeTool("fh_emergency_stop", Map.of()).get("success"));
        }
    }

    @Test
    void executeTool_shouldPassArgsToLimiter_whenDeviceDiffers() {
        registry.setToolRateLimiter(createRateLimiter(new FlightHubProperties.RateLimit()));
        registry.register(Map.of("name", "fh_takeoff"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);

        setContext("req-dev-a", "fh_takeoff", "client-dev", "sess-dev");
        assertEquals(true, registry.executeTool("fh_takeoff", Map.of("drone_sn", "SN-A")).get("success"));

        // 参数必须真正下传到限流器：换一台飞机后同一 session 仍有独立配额
        assertEquals(true, registry.executeTool("fh_takeoff", Map.of("drone_sn", "SN-B")).get("success"));

        // 各自再次调用都应被拒，证明前两次确实记在了两个不同的桶上
        assertEquals(false, registry.executeTool("fh_takeoff", Map.of("drone_sn", "SN-A")).get("success"));
        assertEquals(false, registry.executeTool("fh_takeoff", Map.of("drone_sn", "SN-B")).get("success"));
    }

    @Test
    void executeTool_shouldNotConsumeQuota_whenToolUnknown() {
        registry.setToolRateLimiter(createRateLimiter(new FlightHubProperties.RateLimit()));
        registry.register(Map.of("name", "fh_takeoff"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);

        setContext("req-unknown", "fh_unknown", "client-u", "sess-u");

        // 未知 tool 不消耗配额：反复拼错不会耗尽该 session 的预算
        for (int i = 0; i < 10; i++) {
            assertEquals(false, registry.executeTool("fh_unknown", Map.of()).get("success"));
        }

        // 随后合法工具首次调用仍应放行
        RequestContext.set("req-ok", "fh_takeoff", "client-u");
        RequestContext.current().setSessionId("sess-u");
        assertEquals(true, registry.executeTool("fh_takeoff", Map.of()).get("success"));
    }

    @Test
    void executeTool_shouldNotRateLimit_whenLimiterNotInjected() {
        // 未注入限流器时行为与改造前完全一致
        registry.register(Map.of("name", "fh_takeoff"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);

        for (int i = 0; i < 10; i++) {
            assertEquals(true, registry.executeTool("fh_takeoff", Map.of()).get("success"));
        }
    }
}
