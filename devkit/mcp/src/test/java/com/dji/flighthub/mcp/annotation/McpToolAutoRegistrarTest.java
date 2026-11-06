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

package com.dji.flighthub.mcp.annotation;

import com.dji.flighthub.mcp.audit.AuditService;
import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.ratelimit.ToolRateLimiter;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class McpToolAutoRegistrarTest {

    private McpToolRegistry registry;
    private Logger registrarLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        registry = spy(new McpToolRegistry());
        registrarLogger = (Logger) LoggerFactory.getLogger(McpToolAutoRegistrar.class);
        appender = new ListAppender<>();
        appender.start();
        registrarLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        registrarLogger.detachAppender(appender);
    }

    /**
     * 判断启动告警是否针对「L1 工具缺少人工确认」这一项（避免被 CRITICAL 兜底告警误判）。
     *
     * @return 命中该告警的日志条目数
     */
    private long countUnconfirmedReversibleWarnings() {
        return appender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .filter(event -> event.getFormattedMessage().contains("L1（REVERSIBLE）工具"))
                .count();
    }

    /**
     * 使用真实限流器而非 mock：mock 的 {@code tryAcquire} 返回 null，
     * 是生产中不可达的形态，会让装配路径的测试失真。
     *
     * @return 关闭限流的真实实例（本测试只关心注册结果，不关心配额）
     */
    private static ToolRateLimiter createRateLimiter() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getRateLimit().setEnabled(false);
        return new ToolRateLimiter(properties, () -> 0L);
    }

    static class LegacyTool extends AbstractMcpTool {
        LegacyTool() { super(mock(SessionManager.class)); }

        @Override
        public SecurityLevel getSecurityLevel() {
            return SecurityLevel.READ_ONLY;
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "legacy_tool", "description", "Legacy");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }
    }

    @McpTool(name = "annotated_tool", description = "Annotated test tool",
            securityLevel = SecurityLevel.READ_ONLY)
    static class AnnotatedTool extends AbstractMcpTool {
        @McpParam(description = "Input value", required = true)
        private String input;

        AnnotatedTool() { super(mock(SessionManager.class)); }

        @Override
        protected Map<String, Object> doExecute(ToolContext ctx) {
            return successResult(Map.of("input", ctx.getString("input")), "ok");
        }
    }

    @Test
    void autoRegister_shouldRegisterAllTools() {
        List<AbstractMcpTool> tools = List.of(new LegacyTool(), new AnnotatedTool());

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class),
                null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        assertEquals(2, registry.getToolCount());
    }

    @Test
    void autoRegister_shouldRegisterLegacyToolCorrectly() {
        List<AbstractMcpTool> tools = List.of(new LegacyTool());

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class),
                null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        List<Map<String, Object>> defs = registry.getToolDefinitions();
        assertEquals("legacy_tool", defs.get(0).get("name"));
    }

    @Test
    void autoRegister_shouldRegisterAnnotatedToolCorrectly() {
        List<AbstractMcpTool> tools = List.of(new AnnotatedTool());

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class),
                null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        List<Map<String, Object>> defs = registry.getToolDefinitions();
        assertEquals("annotated_tool", defs.get(0).get("name"));
    }

    @Test
    void autoRegister_shouldDetectHumanConfirmFromAnnotation() {
        @McpTool(name = "confirm_tool", description = "Needs confirm", requiresConfirm = true)
        class ConfirmTool extends AbstractMcpTool {
            ConfirmTool() { super(mock(SessionManager.class)); }

            @Override
            protected Map<String, Object> doExecute(ToolContext ctx) {
                return Map.of();
            }
        }

        List<AbstractMcpTool> tools = List.of(new ConfirmTool());

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class),
                null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        assertTrue(registry.isHumanConfirmRequired("confirm_tool"));
    }

    @Test
    void afterSingletonsInstantiated_shouldInjectSessionManagerIntoRegistry() {
        // 回归守卫：曾因漏调 setSessionManager 导致生产环境派发层门控静默失效
        SessionManager sessionManager = mock(SessionManager.class);

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(new LegacyTool()), registry, mock(AuditService.class),
                mock(IdempotencyCache.class), null, createRateLimiter(), sessionManager);
        registrar.afterSingletonsInstantiated();

        verify(registry).setSessionManager(same(sessionManager));
    }

    @Test
    void afterSingletonsInstantiated_shouldMakeGateEffective() {
        // 行为断言：注入后 L0 session 下 L1 工具被派发层门控拒绝
        L1Tool l1Tool = new L1Tool();
        SessionManager sessionManager = mock(SessionManager.class);
        when(sessionManager.getActiveSession())
                .thenReturn(new SessionContext("sess_test", "ws", "ws", SecurityLevel.READ_ONLY.getLevel()));

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(l1Tool), registry, mock(AuditService.class),
                mock(IdempotencyCache.class), null, createRateLimiter(), sessionManager);
        registrar.afterSingletonsInstantiated();

        Map<String, Object> result = registry.executeTool("l1_tool", Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    /**
     * 声明 L1 的最简工具，用于验证派发层门控。
     */
    static class L1Tool extends AbstractMcpTool {
        L1Tool() { super(mock(SessionManager.class)); }

        @Override
        public SecurityLevel getSecurityLevel() {
            return SecurityLevel.REVERSIBLE;
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "l1_tool", "description", "L1");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }
    }

    /**
     * 声明 L2 且声明人工确认的最简工具（符合安全契约）。
     */
    static class FlightConfirmTool extends AbstractMcpTool {
        FlightConfirmTool() { super(mock(SessionManager.class)); }

        @Override
        public boolean requiresHumanConfirmation() {
            return true;
        }

        @Override
        public SecurityLevel getSecurityLevel() {
            return SecurityLevel.FLIGHT;
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "flight_confirm_tool", "description", "L2 w/ confirm");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }
    }

    /**
     * 声明 L2 但未声明人工确认的违规工具（应被注册期断言拦截）。
     */
    static class FlightNoConfirmTool extends AbstractMcpTool {
        FlightNoConfirmTool() { super(mock(SessionManager.class)); }

        @Override
        public SecurityLevel getSecurityLevel() {
            return SecurityLevel.FLIGHT;
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "flight_no_confirm_tool", "description", "L2 w/o confirm");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }
    }

    @Test
    void autoRegister_shouldRegisterFlightToolWhenConfirmed() {
        // L2 + requiresHumanConfirmation=true：契约满足，正常注册
        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(new FlightConfirmTool()), registry, mock(AuditService.class),
                mock(IdempotencyCache.class), null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        assertEquals(1, registry.getToolCount());
        assertTrue(registry.isHumanConfirmRequired("flight_confirm_tool"));
    }

    @Test
    void autoRegister_shouldSkipFlightTool_whenNoConfirm() {
        // 安全契约 fail-closed：L2 但无 confirm 的工具被注册期断言拒绝，不计入注册数
        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(new FlightNoConfirmTool()), registry, mock(AuditService.class),
                mock(IdempotencyCache.class), null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        assertEquals(0, registry.getToolCount());
    }

    /**
     * 声明 L1 且声明人工确认的最简工具（用于验证告警不误报）。
     */
    static class L1ConfirmTool extends AbstractMcpTool {
        L1ConfirmTool() { super(mock(SessionManager.class)); }

        @Override
        public boolean requiresHumanConfirmation() {
            return true;
        }

        @Override
        public SecurityLevel getSecurityLevel() {
            return SecurityLevel.REVERSIBLE;
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "l1_confirm_tool", "description", "L1 w/ confirm");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }
    }

    @Test
    void afterSingletonsInstantiated_shouldWarn_whenReversibleToolHasNoConfirmation() {
        // DK-P3-03：L1 且无人工确认 → 启动告警提示补确认或登记豁免
        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(new L1Tool()), registry, mock(AuditService.class),
                mock(IdempotencyCache.class), null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        assertEquals(1, countUnconfirmedReversibleWarnings());
        // 仅告警，不阻断注册
        assertEquals(1, registry.getToolCount());
    }

    @Test
    void afterSingletonsInstantiated_shouldNotWarn_whenReversibleToolHasConfirmation() {
        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                List.of(new L1ConfirmTool()), registry, mock(AuditService.class),
                mock(IdempotencyCache.class), null, createRateLimiter(), mock(SessionManager.class));
        registrar.afterSingletonsInstantiated();

        assertEquals(0, countUnconfirmedReversibleWarnings());
        assertEquals(1, registry.getToolCount());
    }
}
