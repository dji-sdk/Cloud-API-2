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

import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ParamBinderTest {

    @McpTool(name = "test_bind", description = "Test binding")
    static class BindTool extends AbstractMcpTool {
        @McpParam(description = "Device SN", required = true)
        private String drone_sn;

        @McpParam(description = "Speed", defaultValue = "15")
        private Integer max_speed;

        @McpParam(description = "Height", range = {2, 10000})
        private Double height;

        @McpParam(description = "Confirm flag")
        private Boolean confirm;

        BindTool() { super(mock(SessionManager.class)); }

        @Override
        public Map<String, Object> getToolDefinition() {
            return SchemaGenerator.generate(BindTool.class);
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return Map.of();
        }
    }

    @McpTool(name = "test_conditional", description = "Test conditional required")
    static class ConditionalTool extends AbstractMcpTool {
        @McpParam(description = "Action", required = true,
                  enumValues = {"start", "stop"})
        private String action;

        @McpParam(description = "Latitude", requiredWhen = "action in [start]")
        private Double latitude;

        ConditionalTool() { super(mock(SessionManager.class)); }

        @Override
        public Map<String, Object> getToolDefinition() {
            return SchemaGenerator.generate(ConditionalTool.class);
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return Map.of();
        }
    }

    @Test
    void bind_shouldSucceed_whenAllRequiredPresent() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", "DRONE-001");
        args.put("height", 50.0);

        Map<String, Object> result = ParamBinder.bind(BindTool.class, args);
        assertNull(result, "Should return null on success");
        assertEquals(15, args.get("max_speed"));
    }

    @Test
    void bind_shouldFail_whenRequiredMissing() {
        Map<String, Object> args = new HashMap<>();

        Map<String, Object> result = ParamBinder.bind(BindTool.class, args);
        assertNotNull(result, "Should return error");
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    @Test
    void bind_shouldApplyDefaultValue() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", "DRONE-001");

        ParamBinder.bind(BindTool.class, args);
        assertEquals(15, args.get("max_speed"));
    }

    @Test
    void bind_shouldNotOverrideExplicitValue() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", "DRONE-001");
        args.put("max_speed", 30);

        ParamBinder.bind(BindTool.class, args);
        assertEquals(30, args.get("max_speed"));
    }

    @Test
    void bind_shouldFailOnRangeViolation() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", "DRONE-001");
        args.put("height", 1.0);

        Map<String, Object> result = ParamBinder.bind(BindTool.class, args);
        assertNotNull(result, "Should return range error");
        assertTrue(result.get("error_message").toString().contains("height"));
    }

    @Test
    void bind_shouldPassOnValidRange() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", "DRONE-001");
        args.put("height", 100.0);

        Map<String, Object> result = ParamBinder.bind(BindTool.class, args);
        assertNull(result, "Should pass");
    }

    @Test
    void bind_shouldConvertStringToNumber() {
        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", "DRONE-001");
        args.put("height", "50.5");

        Map<String, Object> result = ParamBinder.bind(BindTool.class, args);
        assertNull(result);
        assertEquals(50.5, args.get("height"));
    }

    @Test
    void bind_shouldEnforceConditionalRequired() {
        Map<String, Object> args = new HashMap<>();
        args.put("action", "start");

        Map<String, Object> result = ParamBinder.bind(ConditionalTool.class, args);
        assertNotNull(result);
        assertTrue(result.get("error_message").toString().contains("latitude"));
    }

    @Test
    void bind_shouldSkipConditionalRequired_whenConditionNotMet() {
        Map<String, Object> args = new HashMap<>();
        args.put("action", "stop");

        Map<String, Object> result = ParamBinder.bind(ConditionalTool.class, args);
        assertNull(result, "Should pass when condition not met");
    }

    @Test
    void bind_shouldHandleNullArgs() {
        Map<String, Object> result = ParamBinder.bind(BindTool.class, null);
        assertNotNull(result, "Should fail with null args (required param missing)");
    }
}
