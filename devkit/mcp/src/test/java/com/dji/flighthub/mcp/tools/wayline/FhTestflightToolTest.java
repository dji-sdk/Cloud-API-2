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
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FhTestflightTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action mock 成功路径、必填参数校验、
 * out_of_control_action_in_flight 取值限制、人工确认门控。
 *
 * @author DJI FlightHub SDK Team
 */
class FhTestflightToolTest {

    private SessionManager sessionManager;
    private FhTestflightTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {
            public boolean isMockMode() {
                return true;
            }
        };
        tool = new FhTestflightTool(sessionManager, sdkService);
    }

    private Map<String, Object> generateWaylineArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("action", "generate_wayline");
        args.put("project_id", "PROJ-001");
        args.put("name", "test-wayline");
        args.put("device_type", "M350");
        args.put("target_point", Map.of("lng", 116.3, "lat", 39.9, "alt", 100.0));
        args.put("observer_point", Map.of("lng", 116.31, "lat", 39.91, "alt", 50.0));
        return args;
    }

    private Map<String, Object> createTaskArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_task");
        args.put("project_id", "PROJ-001");
        args.put("name", "test-task");
        args.put("wayline_uuid", "mock-wayline-123");
        args.put("sn", "DRONE-001");
        args.put("task_type", 0);
        args.put("out_of_control_action_in_flight", 1);
        return args;
    }

    // ==================== getToolDefinition ====================

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndActions() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_testflight", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actionEnum = (List<String>) actionProp.get("enum");

        assertTrue(actionEnum.contains("generate_wayline"));
        assertTrue(actionEnum.contains("create_task"));
    }

    // ==================== 无 session ====================

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(generateWaylineArgs());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ==================== generate_wayline ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeGenerateWayline_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = generateWaylineArgs();
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("uuid"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void executeGenerateWayline_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(generateWaylineArgs());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void executeGenerateWayline_shouldReturnError_whenDeviceTypeMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = generateWaylineArgs();
        args.remove("device_type");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_type"));
    }

    @Test
    void executeGenerateWayline_shouldReturnError_whenTargetPointMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = generateWaylineArgs();
        args.remove("target_point");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("target_point"));
    }

    @Test
    void executeGenerateWayline_shouldReturnError_whenObserverPointMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = generateWaylineArgs();
        args.remove("observer_point");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("observer_point"));
    }

    // ==================== create_task ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeCreateTask_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("uuid"));
        assertEquals("DRONE-001", data.get("sn"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void executeCreateTask_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(createTaskArgs());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void executeCreateTask_shouldReturnError_whenWaylineUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.remove("wayline_uuid");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_uuid"));
    }

    @Test
    void executeCreateTask_shouldReturnError_whenSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.remove("sn");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("sn"));
    }

    @Test
    void executeCreateTask_shouldReturnError_whenTaskTypeMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.remove("task_type");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_type"));
    }

    // ==================== out_of_control_action_in_flight 取值限制 ====================

    @Test
    void executeCreateTask_shouldReturnError_whenOutOfControlActionIsHover() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.put("out_of_control_action_in_flight", 0);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action_in_flight"));
    }

    @Test
    void executeCreateTask_shouldReturnError_whenOutOfControlActionMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.remove("out_of_control_action_in_flight");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action_in_flight"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreateTask_shouldAccept_whenOutOfControlActionIsReturnHome() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.put("out_of_control_action_in_flight", 1);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreateTask_shouldAccept_whenOutOfControlActionIsContinue() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.put("out_of_control_action_in_flight", 2);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
    }

    @Test
    void executeCreateTask_shouldReturnError_whenOutOfControlActionOutOfRange() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = createTaskArgs();
        args.put("out_of_control_action_in_flight", 3);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action_in_flight"));
    }

    // ==================== 安全等级 ====================

    @Test
    void getSecurityLevel_shouldReturnFlight() {
        assertEquals(SecurityLevel.FLIGHT, tool.getSecurityLevel());
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(tool.requiresHumanConfirmation());
    }

    @Test
    void executeCreateTask_shouldReturnError_whenSecurityLevelInsufficient() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(1);
        SessionManager limitedManager = new SessionManager(properties);
        limitedManager.createSession("uuid", "ws");

        SdkService sdkService = new SdkService() {
            public boolean isMockMode() {
                return true;
            }
        };
        FhTestflightTool limitedTool = new FhTestflightTool(limitedManager, sdkService);

        Map<String, Object> args = createTaskArgs();
        args.put("confirm", true);

        Map<String, Object> result = limitedTool.execute(args);

        assertEquals(false, result.get("success"));
    }
}
