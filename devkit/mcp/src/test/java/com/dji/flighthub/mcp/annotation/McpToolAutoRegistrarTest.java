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
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class McpToolAutoRegistrarTest {

    private McpToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new McpToolRegistry();
    }

    static class LegacyTool extends AbstractMcpTool {
        LegacyTool() { super(mock(SessionManager.class)); }

        @Override
        public Map<String, Object> getToolDefinition() {
            return Map.of("name", "legacy_tool", "description", "Legacy");
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of(), "ok");
        }
    }

    @McpTool(name = "annotated_tool", description = "Annotated test tool")
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
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class));
        registrar.afterSingletonsInstantiated();

        assertEquals(2, registry.getToolCount());
    }

    @Test
    void autoRegister_shouldRegisterLegacyToolCorrectly() {
        List<AbstractMcpTool> tools = List.of(new LegacyTool());

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class));
        registrar.afterSingletonsInstantiated();

        List<Map<String, Object>> defs = registry.getToolDefinitions();
        assertEquals("legacy_tool", defs.get(0).get("name"));
    }

    @Test
    void autoRegister_shouldRegisterAnnotatedToolCorrectly() {
        List<AbstractMcpTool> tools = List.of(new AnnotatedTool());

        McpToolAutoRegistrar registrar = new McpToolAutoRegistrar(
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class));
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
                tools, registry, mock(AuditService.class), mock(IdempotencyCache.class));
        registrar.afterSingletonsInstantiated();

        assertTrue(registry.isHumanConfirmRequired("confirm_tool"));
    }
}
