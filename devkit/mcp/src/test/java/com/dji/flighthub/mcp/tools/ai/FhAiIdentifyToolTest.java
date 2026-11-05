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

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhAiIdentifyTool 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhAiIdentifyToolTest {

    private static final String DEVICE_SN = "DRONE-AI-001";
    private SessionManager sessionManager;
    private FhAiIdentifyTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService() {
            @Override public boolean isMockMode() { return true; }
        };
        tool = new FhAiIdentifyTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_ai_identify", def.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        assertEquals("object", schema.get("type"));
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("device_sn"));
        assertTrue(required.contains("action"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "set", "on", 1, "confirm", true));
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenNoDrcLink() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "set", "on", 1, "confirm", true));
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No DRC link"));
    }

    @Test
    void execute_shouldReturnError_whenMissingAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveDrcDevice(DEVICE_SN);
        Map<String, Object> result = tool.execute(Map.of("device_sn", DEVICE_SN, "confirm", true));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveDrcDevice(DEVICE_SN);
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "bogus", "confirm", true));
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockSuccess_whenSetAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveDrcDevice(DEVICE_SN);
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "set", "on", 1, "confirm", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue((boolean) data.get("_mock"));
        assertEquals("set", data.get("action"));
        assertEquals(1, data.get("on"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockSuccess_whenFilterAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveDrcDevice(DEVICE_SN);
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "filter", "light_type", 1,
                "filters", List.of(0, 1), "confirm", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("filter", data.get("action"));
    }
}
