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
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhWaylineControlTool 单元测试。
 * 覆盖 session/workspace 校验、参数校验、三种 action 的 mock 模式执行及 session 状态更新。
 *
 * @author DJI FlightHub SDK Team
 */
class FhWaylineControlToolTest {

    private static final String TASK_UUID = "task_1234567890";

    private SessionManager sessionManager;
    private FhWaylineControlTool controlTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = createSdkServiceWithWorkspace();
        controlTool = new FhWaylineControlTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = controlTool.getToolDefinition();

        assertEquals("fh_wayline_control", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = controlTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "action", "pause",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        SdkService sdkServiceNoWorkspace = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhWaylineControlTool tool = new FhWaylineControlTool(sessionManager, sdkServiceNoWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "action", "pause",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenTaskUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "pause");
        args.put("confirm", true);

        Map<String, Object> result = controlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: task_uuid"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = controlTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "action", "explode",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenActionMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("task_uuid", TASK_UUID);
        args.put("confirm", true);

        Map<String, Object> result = controlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldPauseTask_whenActionIsPause() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);

        Map<String, Object> result = controlTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "action", "pause",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("PAUSED", data.get("status"));
        assertEquals("pause", data.get("action"));
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldResumeTask_whenActionIsResume() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.HOVERING);

        Map<String, Object> result = controlTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "action", "resume",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("EXECUTING", data.get("status"));
        assertEquals("resume", data.get("action"));
        assertEquals(FlightPhase.WAYLINE_EXECUTING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldCancelTask_whenActionIsCancel() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveWaylineTaskUuid(TASK_UUID);
        session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);

        Map<String, Object> result = controlTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "action", "cancel",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("CANCELLED", data.get("status"));
        assertEquals("cancel", data.get("action"));

        // cancel 应清除 activeWaylineTaskUuid 并设为 RETURNING
        assertNull(session.getActiveWaylineTaskUuid());
        assertEquals(FlightPhase.RETURNING, session.getFlightPhase());
    }

    // ================ 辅助方法 ================

    private static SdkService createSdkServiceWithWorkspace() {
        return WaylineTestHelper.createSdkServiceWithWorkspace();
    }
}
