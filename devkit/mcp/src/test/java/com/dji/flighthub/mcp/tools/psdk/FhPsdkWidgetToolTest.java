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

package com.dji.flighthub.mcp.tools.psdk;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhPsdkWidgetTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、Mock 路径。
 *
 * @author DJI FlightHub SDK Team
 */
class FhPsdkWidgetToolTest {

    private SessionManager sessionManager;
    private FhPsdkWidgetTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(props);
        tool = new FhPsdkWidgetTool(sessionManager, new SdkService() {  public boolean isMockMode() { return true; } });
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_psdk_widget", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
        assertTrue(required.contains("device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("action", "set_value", "device_sn", "DRONE-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "explode", "device_sn", "DRONE-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_value");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    // ==================== set_value ====================

    @Test
    void executeSetValue_shouldReturnError_whenWidgetIndexMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_value");
        args.put("device_sn", "DRONE-001");
        args.put("value", 42);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("widget_index"));
    }

    @Test
    void executeSetValue_shouldReturnError_whenValueMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_value");
        args.put("device_sn", "DRONE-001");
        args.put("widget_index", 3);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("value"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetValue_shouldReturnMockSuccess() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_value");
        args.put("device_sn", "DRONE-001");
        args.put("widget_index", 3);
        args.put("value", 42);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("DRONE-001", data.get("device_sn"));
        assertEquals(0, data.get("payload_index"));
        assertEquals(3, data.get("widget_index"));
        assertEquals(42, data.get("value"));
        assertEquals("success", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== set_input_text ====================

    @Test
    void executeSetInputText_shouldReturnError_whenTextMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_input_text");
        args.put("device_sn", "DRONE-001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("text"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetInputText_shouldReturnMockSuccess() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_input_text");
        args.put("device_sn", "DRONE-001");
        args.put("text", "Hello World");

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("DRONE-001", data.get("device_sn"));
        assertEquals(0, data.get("payload_index"));
        assertEquals("Hello World", data.get("text"));
        assertEquals("success", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }
}
