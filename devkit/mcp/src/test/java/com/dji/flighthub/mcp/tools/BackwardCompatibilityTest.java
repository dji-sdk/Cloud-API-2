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

package com.dji.flighthub.mcp.tools;

import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * 向后兼容性测试：验证已有的 52 个 legacy tool（覆写 execute() 和 getToolDefinition()）
 * 在 AbstractMcpTool 引入注解驱动模式后仍然正常工作。
 *
 * @author DJI FlightHub SDK Team
 */
class BackwardCompatibilityTest {

    /**
     * 模拟已有 legacy tool：直接覆写 execute() 和 getToolDefinition()，不使用 @McpTool 注解。
     */
    static class LegacyTool extends AbstractMcpTool {
        LegacyTool() {
            super(mock(SessionManager.class));
        }

        @Override
        public Map<String, Object> getToolDefinition() {
            Map<String, Object> def = new LinkedHashMap<>();
            def.put("name", "legacy_tool");
            def.put("description", "A hand-written legacy tool");
            return def;
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return successResult(Map.of("legacy", true), "Legacy tool executed");
        }
    }

    @Test
    void legacyTool_shouldUseOverriddenExecute() {
        LegacyTool tool = new LegacyTool();
        Map<String, Object> result = tool.execute(Map.of());
        assertEquals(true, result.get("success"));
        assertEquals("Legacy tool executed", result.get("message"));
    }

    @Test
    void legacyTool_shouldUseOverriddenGetToolDefinition() {
        LegacyTool tool = new LegacyTool();
        Map<String, Object> def = tool.getToolDefinition();
        assertEquals("legacy_tool", def.get("name"));
        assertEquals("A hand-written legacy tool", def.get("description"));
    }

    @Test
    void legacyTool_shouldNotBeAffectedByAnnotationFramework() {
        // Legacy tool 没有 @McpTool 注解
        assertNull(LegacyTool.class.getAnnotation(
                com.dji.flighthub.mcp.annotation.McpTool.class));
        // 执行不应抛异常，走的是覆写路径而非注解路径
        LegacyTool tool = new LegacyTool();
        assertDoesNotThrow(() -> tool.execute(Map.of()));
        assertDoesNotThrow(() -> tool.getToolDefinition());
    }

    @Test
    void legacyTool_requiresHumanConfirmation_shouldReturnFalse() {
        // 无注解的 legacy tool，requiresHumanConfirmation 应返回 false（默认值）
        LegacyTool tool = new LegacyTool();
        assertFalse(tool.requiresHumanConfirmation());
    }

    @Test
    void legacyTool_getSecurityLevel_shouldReturnReadOnly() {
        // 无注解的 legacy tool，getSecurityLevel 应返回 READ_ONLY（默认值）
        LegacyTool tool = new LegacyTool();
        assertEquals(com.dji.flighthub.mcp.security.SecurityLevel.READ_ONLY,
                tool.getSecurityLevel());
    }
}
