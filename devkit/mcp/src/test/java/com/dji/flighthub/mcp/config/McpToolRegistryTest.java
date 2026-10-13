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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        verify(audit).logExecution(eq("audited_tool"), eq(true), isNull(), anyLong(), eq(false));
    }

    @Test
    void executeTool_shouldCallAuditOnFailure() {
        AuditService audit = mock(AuditService.class);
        registry.setAuditService(audit);
        registry.register(Map.of("name", "fail_tool"),
                args -> Map.of("success", false, "error_message", "something broke"));

        RequestContext.set("req-a2", "fail_tool", "client-x");

        registry.executeTool("fail_tool", Map.of());

        verify(audit).logExecution(eq("fail_tool"), eq(false), eq("something broke"), anyLong(), eq(false));
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

        verify(audit).logExecution(eq("cached_tool"), eq(true), isNull(), anyLong(), eq(true));
    }

    @Test
    void executeTool_shouldNotBreak_whenAuditThrows() {
        AuditService audit = mock(AuditService.class);
        doThrow(new RuntimeException("audit down")).when(audit)
                .logExecution(anyString(), anyBoolean(), any(), anyLong(), anyBoolean());

        registry.setAuditService(audit);
        registry.register(Map.of("name", "resilient_tool"),
                args -> Map.of("success", true));

        RequestContext.set("req-a4", "resilient_tool", "stdio");

        // 审计异常不应传播
        Map<String, Object> result = registry.executeTool("resilient_tool", Map.of());
        assertEquals(true, result.get("success"));
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
}
