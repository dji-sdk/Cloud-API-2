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

package com.dji.flighthub.mcp.tools.wayline;

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
 * FhWaylineUploadTool 单元测试。
 * 覆盖 session/workspace 校验、必填参数校验、失控动作校验及 mock 模式正常返回。
 *
 * @author DJI FlightHub SDK Team
 */
class FhWaylineUploadToolTest {

    private static final String DRONE_SN = "DRONE-WL-001";
    private static final String WAYLINE_URL = "https://cloud.dji.com/waylines/test-route.kmz";

    private SessionManager sessionManager;
    private FhWaylineUploadTool uploadTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        uploadTool = new FhWaylineUploadTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = uploadTool.getToolDefinition();

        assertEquals("fh_wayline_upload", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = uploadTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "wayline_url", WAYLINE_URL,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = uploadTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "wayline_url", WAYLINE_URL,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        // 使用带 workspace 的 SdkService
        SdkService sdkServiceWithWorkspace = createSdkServiceWithWorkspace();
        FhWaylineUploadTool tool = new FhWaylineUploadTool(sessionManager, sdkServiceWithWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("wayline_url", WAYLINE_URL);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenWaylineUrlMissing() {
        SdkService sdkServiceWithWorkspace = createSdkServiceWithWorkspace();
        FhWaylineUploadTool tool = new FhWaylineUploadTool(sessionManager, sdkServiceWithWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: wayline_url"));
    }

    @Test
    void execute_shouldReturnError_whenOocActionInvalid() {
        SdkService sdkServiceWithWorkspace = createSdkServiceWithWorkspace();
        FhWaylineUploadTool tool = new FhWaylineUploadTool(sessionManager, sdkServiceWithWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "wayline_url", WAYLINE_URL,
                "out_of_control_action", "explode",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid out_of_control_action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenMockModeWithAllParams() {
        SdkService sdkServiceWithWorkspace = createSdkServiceWithWorkspace();
        FhWaylineUploadTool tool = new FhWaylineUploadTool(sessionManager, sdkServiceWithWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "wayline_url", WAYLINE_URL,
                "task_name", "Test Flight",
                "out_of_control_action", "hover",
                "description", "Unit test wayline",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertNotNull(data.get("task_uuid"));
        assertEquals("Test Flight", data.get("task_name"));
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(WAYLINE_URL, data.get("wayline_url"));
        assertEquals("hover", data.get("out_of_control_action"));
        assertEquals("CREATED", data.get("status"));
        assertEquals(true, data.get("_mock"));

        // 应包含 next_steps（结构化格式）
        assertNotNull(result.get("next_steps"));
        List<Map<String, Object>> nextSteps = (List<Map<String, Object>>) result.get("next_steps");
        assertFalse(nextSteps.isEmpty());
        assertEquals("fh_flight_task", nextSteps.get(0).get("tool"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldAutoGenerateTaskName_whenNotProvided() {
        SdkService sdkServiceWithWorkspace = createSdkServiceWithWorkspace();
        FhWaylineUploadTool tool = new FhWaylineUploadTool(sessionManager, sdkServiceWithWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "wayline_url", WAYLINE_URL,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        String taskName = (String) data.get("task_name");
        assertNotNull(taskName);
        assertTrue(taskName.startsWith("Wayline-"));
    }

    // ================ 辅助方法 ================

    /**
     * 创建带 workspace 的 SdkService（通过反射设置 workspace 字段为非 null）。
     */
    private static SdkService createSdkServiceWithWorkspace() {
        return WaylineTestHelper.createSdkServiceWithWorkspace();
    }
}
