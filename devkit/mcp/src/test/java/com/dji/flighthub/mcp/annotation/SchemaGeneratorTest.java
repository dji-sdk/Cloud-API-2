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

import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SchemaGeneratorTest {

    @McpTool(name = "test_flyto", description = "Fly to coordinate",
             securityLevel = SecurityLevel.FLIGHT,
             prerequisites = {Prerequisite.SESSION, Prerequisite.SECURITY_LEVEL,
                              Prerequisite.CONTROL_AUTHORITY, Prerequisite.DRC_LINK},
             requiresConfirm = true)
    static class TestFlytoTool extends AbstractMcpTool {
        @McpParam(description = "Target drone SN", required = true)
        private String drone_sn;

        @McpParam(description = "Action type", required = true,
                  enumValues = {"start", "stop", "update"})
        private String action;

        @McpParam(description = "Target latitude", range = {-90, 90},
                  requiredWhen = "action in [start, update]")
        private Double latitude;

        @McpParam(description = "Target longitude", range = {-180, 180},
                  requiredWhen = "action in [start, update]")
        private Double longitude;

        @McpParam(description = "Target height in meters", range = {2, 10000},
                  requiredWhen = "action in [start, update]")
        private Double height;

        @McpParam(description = "Max speed in m/s", defaultValue = "15")
        private Integer max_speed;

        @McpParam(description = "Confirm execution", required = true)
        private Boolean confirm;

        TestFlytoTool() { super(mock(SessionManager.class)); }

        @Override
        public Map<String, Object> getToolDefinition() {
            return SchemaGenerator.generate(TestFlytoTool.class);
        }

        @Override
        public Map<String, Object> execute(Map<String, Object> args) {
            return Map.of();
        }
    }

    @Test
    void generate_shouldProduceName() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        assertEquals("test_flyto", schema.get("name"));
    }

    @Test
    void generate_shouldProduceDescription() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        assertEquals("Fly to coordinate", schema.get("description"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_shouldProduceInputSchema() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> inputSchema = (Map<String, Object>) schema.get("inputSchema");

        assertNotNull(inputSchema);
        assertEquals("object", inputSchema.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_shouldMapFieldTypes() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> inputSchema = (Map<String, Object>) schema.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) inputSchema.get("properties");

        Map<String, Object> droneSn = (Map<String, Object>) props.get("drone_sn");
        assertEquals("string", droneSn.get("type"));

        Map<String, Object> latitude = (Map<String, Object>) props.get("latitude");
        assertEquals("number", latitude.get("type"));

        Map<String, Object> maxSpeed = (Map<String, Object>) props.get("max_speed");
        assertEquals("integer", maxSpeed.get("type"));

        Map<String, Object> confirm = (Map<String, Object>) props.get("confirm");
        assertEquals("boolean", confirm.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_shouldIncludeEnumValues() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> inputSchema = (Map<String, Object>) schema.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) inputSchema.get("properties");
        Map<String, Object> action = (Map<String, Object>) props.get("action");

        assertEquals(List.of("start", "stop", "update"), action.get("enum"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_shouldIncludeRequiredList() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> inputSchema = (Map<String, Object>) schema.get("inputSchema");
        List<String> required = (List<String>) inputSchema.get("required");

        assertTrue(required.contains("drone_sn"));
        assertTrue(required.contains("action"));
        assertTrue(required.contains("confirm"));
        assertFalse(required.contains("latitude"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_shouldIncludeDefaultValue() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> inputSchema = (Map<String, Object>) schema.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) inputSchema.get("properties");
        Map<String, Object> maxSpeed = (Map<String, Object>) props.get("max_speed");

        assertEquals(15, maxSpeed.get("default"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_shouldAlwaysIncludeDryRunParam() {
        Map<String, Object> schema = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> inputSchema = (Map<String, Object>) schema.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) inputSchema.get("properties");

        assertTrue(props.containsKey("dry_run"));
        Map<String, Object> dryRun = (Map<String, Object>) props.get("dry_run");
        assertEquals("boolean", dryRun.get("type"));
    }

    @Test
    void generate_shouldCacheResult() {
        Map<String, Object> first = SchemaGenerator.generate(TestFlytoTool.class);
        Map<String, Object> second = SchemaGenerator.generate(TestFlytoTool.class);
        assertSame(first, second, "Should return cached instance");
    }
}
