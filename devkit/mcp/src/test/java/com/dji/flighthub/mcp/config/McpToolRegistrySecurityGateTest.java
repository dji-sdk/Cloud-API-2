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
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 派发层安全等级门控（JAVA-01）单元测试。
 *
 * <p>覆盖 allow/deny 分支、sim 与 CRITICAL 豁免、未注入 SessionManager、审计标记、
 * 拒绝时不进入 handler，以及拒绝时释放幂等 PENDING 占位。
 *
 * @author DJI FlightHub SDK Team
 */
class McpToolRegistrySecurityGateTest {

    private SessionManager sessionManager;

    private McpToolRegistry newRegistry() {
        McpToolRegistry registry = new McpToolRegistry();
        sessionManager = mock(SessionManager.class);
        registry.setSessionManager(sessionManager);
        return registry;
    }

    private void mockActiveSession(int maxLevel) {
        SessionContext session = mock(SessionContext.class);
        when(session.getMaxSecurityLevel()).thenReturn(maxLevel);
        when(sessionManager.getActiveSession()).thenReturn(session);
    }

    @AfterEach
    void cleanup() {
        RequestContext.clear();
    }

    @Test
    void executeTool_shouldDeny_whenToolLevelExceedsSessionMax() {
        McpToolRegistry registry = newRegistry();
        registry.register(Map.of("name", "fh_l2_tool"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);
        mockActiveSession(1); // session 上限 L1，低于工具声明 L2

        Map<String, Object> result = registry.executeTool("fh_l2_tool", Map.of());

        assertEquals(false, result.get("success"));
        assertEquals(McpToolRegistry.ERROR_CODE_SECURITY_LEVEL, result.get("error_code"));
        assertEquals(true, result.get("security_denied"));
    }

    @Test
    void executeTool_shouldAllow_whenToolLevelWithinSessionMax() {
        McpToolRegistry registry = newRegistry();
        registry.register(Map.of("name", "fh_l1_tool"),
                args -> Map.of("success", true), false, SecurityLevel.REVERSIBLE);
        mockActiveSession(2);

        Map<String, Object> result = registry.executeTool("fh_l1_tool", Map.of());

        assertEquals(true, result.get("success"));
    }

    @Test
    void executeTool_shouldAllow_whenNoActiveSession() {
        McpToolRegistry registry = newRegistry();
        registry.register(Map.of("name", "fh_l2_tool"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);
        when(sessionManager.getActiveSession()).thenReturn(null);

        Map<String, Object> result = registry.executeTool("fh_l2_tool", Map.of());

        assertEquals(true, result.get("success"));
    }

    @Test
    void executeTool_shouldAllow_whenSessionManagerNotInjected() {
        McpToolRegistry registry = new McpToolRegistry();
        // 不注入 SessionManager：FLIGHT 工具也无法与 session 上限比较，应直接放行
        registry.register(Map.of("name", "fh_l2_tool"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);

        Map<String, Object> result = registry.executeTool("fh_l2_tool", Map.of());

        assertEquals(true, result.get("success"));
    }

    @Test
    void executeTool_shouldAllow_simTools() {
        McpToolRegistry registry = newRegistry();
        registry.register(Map.of("name", "sim_device"),
                args -> Map.of("success", true), false, SecurityLevel.REVERSIBLE);
        mockActiveSession(0); // 即便 session 上限 L0，sim_* 也放行（AbstractSimTool 自检）

        Map<String, Object> result = registry.executeTool("sim_device", Map.of());

        assertEquals(true, result.get("success"));
    }

    @Test
    void executeTool_shouldAllow_criticalTools_forSelfGatedTool() {
        McpToolRegistry registry = newRegistry();
        registry.register(Map.of("name", "fh_emergency_stop"),
                args -> Map.of("success", true), true, SecurityLevel.CRITICAL);
        mockActiveSession(2); // 默认 L2，但 CRITICAL 豁免交给工具自检

        Map<String, Object> result = registry.executeTool("fh_emergency_stop", Map.of());

        assertEquals(true, result.get("success"));
    }

    @Test
    void executeTool_shouldAllow_unknownTool() {
        McpToolRegistry registry = newRegistry();

        Map<String, Object> result = registry.executeTool("no_such_tool", Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Capability not available"));
    }

    @Test
    void executeTool_shouldNotInvokeHandler_whenSecurityDenied() {
        McpToolRegistry registry = newRegistry();
        AtomicBoolean handlerInvoked = new AtomicBoolean(false);
        registry.register(Map.of("name", "fh_l2_tool"), args -> {
            handlerInvoked.set(true);
            return Map.of("success", true);
        }, false, SecurityLevel.FLIGHT);
        mockActiveSession(1); // session 上限 L1，低于工具声明 L2

        Map<String, Object> result = registry.executeTool("fh_l2_tool", Map.of());

        assertEquals(false, result.get("success"));
        assertFalse(handlerInvoked.get(), "被安全门控拒绝时不应调用 tool handler");
    }

    @Test
    void executeTool_shouldReleasePending_whenSecurityDenied() {
        McpToolRegistry registry = newRegistry();
        IdempotencyCache cache = mock(IdempotencyCache.class);
        // Mockito 对 Map 返回值默认给空 Map 而非 null，必须显式打桩为「未命中」
        when(cache.getOrMarkPending(anyString())).thenReturn(null);
        registry.setIdempotencyCache(cache);
        registry.register(Map.of("name", "fh_l2_tool"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);
        mockActiveSession(1);
        RequestContext.set("req-sec-pending", "fh_l2_tool", "client-sec-pending");

        Map<String, Object> result = registry.executeTool("fh_l2_tool", Map.of());

        assertEquals(false, result.get("success"));
        // 拒绝时幂等占位必须释放，否则死循环会把缓存占满
        verify(cache).removePending("client-sec-pending:fh_l2_tool:req-sec-pending");
        verify(cache, never()).put(eq("client-sec-pending:fh_l2_tool:req-sec-pending"), any());
    }

    @Test
    void executeTool_shouldReportSecurityDeniedInAudit_whenGateDenies() {
        McpToolRegistry registry = newRegistry();
        AuditService audit = mock(AuditService.class);
        registry.setAuditService(audit);
        registry.register(Map.of("name", "fh_l2_tool"),
                args -> Map.of("success", true), false, SecurityLevel.FLIGHT);
        mockActiveSession(1);
        RequestContext.set("req-sec-1", "fh_l2_tool", "client-sec");

        registry.executeTool("fh_l2_tool", Map.of());

        verify(audit).logExecution(eq("fh_l2_tool"), eq(false), anyString(), anyLong(),
                eq(false), eq(false), eq(true));
    }
}
