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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhWaylineExecuteTool 单元测试。
 * 覆盖 session/workspace 校验、task_uuid 校验及 mock 模式正常执行。
 *
 * @author DJI FlightHub SDK Team
 */
class FhWaylineExecuteToolTest {

    private static final String TASK_UUID = "task_1234567890";

    private SessionManager sessionManager;
    private FhWaylineExecuteTool executeTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = createSdkServiceWithWorkspace();
        executeTool = new FhWaylineExecuteTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = executeTool.getToolDefinition();

        assertEquals("fh_wayline_execute", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = executeTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        // 使用无 workspace 的 SdkService
        SdkService sdkServiceNoWorkspace = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhWaylineExecuteTool tool = new FhWaylineExecuteTool(sessionManager, sdkServiceNoWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenTaskUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("confirm", true);

        Map<String, Object> result = executeTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: task_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenMockMode() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = executeTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(TASK_UUID, data.get("task_uuid"));
        assertEquals("EXECUTING", data.get("status"));
        assertEquals(0, data.get("progress"));
        assertEquals(true, data.get("_mock"));

        // 应包含 next_steps
        assertNotNull(result.get("next_steps"));
    }

    @Test
    void execute_shouldUpdateSessionState_whenSuccessful() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        executeTool.execute(Map.of(
                "task_uuid", TASK_UUID,
                "confirm", true
        ));

        // session 应记录活跃航线任务
        assertEquals(TASK_UUID, session.getActiveWaylineTaskUuid());
        assertEquals(FlightPhase.WAYLINE_EXECUTING, session.getFlightPhase());
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(executeTool.requiresHumanConfirmation());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnDryRunPreview_whenDryRunTrue() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("task_uuid", TASK_UUID);
        args.put("dry_run", true);

        Map<String, Object> result = executeTool.execute(args);

        // dry_run 应返回成功的预览结果，不执行实际操作
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        assertNotNull(result.get("would_execute"));
        // 不应包含 confirm 就能通过（dry_run 跳过 confirm 校验）
        assertNull(result.get("error_message"));
    }

    // ================ 辅助方法 ================

    private static SdkService createSdkServiceWithWorkspace() {
        return WaylineTestHelper.createSdkServiceWithWorkspace();
    }
}
