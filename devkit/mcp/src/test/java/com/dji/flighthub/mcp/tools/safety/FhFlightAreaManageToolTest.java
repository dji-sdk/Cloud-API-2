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

package com.dji.flighthub.mcp.tools.safety;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhFlightAreaManageTool 单元测试。
 * 覆盖 session/workspace 校验、action/参数校验、三种操作的 mock 模式执行。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFlightAreaManageToolTest {

    private SessionManager sessionManager;
    private FhFlightAreaManageTool manageTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = SafetyTestHelper.createSdkServiceWithWorkspace();
        manageTool = new FhFlightAreaManageTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = manageTool.getToolDefinition();

        assertEquals("fh_flight_area_manage", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = manageTool.execute(Map.of(
                "action", "create",
                "name", "test",
                "type", "nfz",
                "content", Map.of("geometry", "polygon"),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        SdkService noWorkspace = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhFlightAreaManageTool tool = new FhFlightAreaManageTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "create",
                "name", "test",
                "type", "nfz",
                "content", Map.of("geometry", "polygon"),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = manageTool.execute(Map.of(
                "action", "invalid",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenCreateMissingName() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("type", "nfz");
        args.put("content", Map.of("geometry", "polygon"));
        args.put("confirm", true);

        Map<String, Object> result = manageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: name"));
    }

    @Test
    void execute_shouldReturnError_whenCreateMissingType() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "test area");
        args.put("content", Map.of("geometry", "polygon"));
        args.put("confirm", true);

        Map<String, Object> result = manageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid type"));
    }

    @Test
    void execute_shouldReturnError_whenCreateMissingContent() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("name", "test area");
        args.put("type", "nfz");
        args.put("confirm", true);

        Map<String, Object> result = manageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: content"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateMissingId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "update");
        args.put("name", "new name");
        args.put("confirm", true);

        Map<String, Object> result = manageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: flight_area_id"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateNothingToChange() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "update");
        args.put("flight_area_id", "area_001");
        args.put("confirm", true);

        Map<String, Object> result = manageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Nothing to update"));
    }

    @Test
    void execute_shouldReturnError_whenDeleteMissingId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete");
        args.put("confirm", true);

        Map<String, Object> result = manageTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: flight_area_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldCreateArea_whenValidInput() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = manageTool.execute(Map.of(
                "action", "create",
                "name", "测试禁飞区",
                "type", "nfz",
                "content", Map.of("type", "Polygon", "coordinates", "[[...]]"),
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("create", data.get("action"));
        assertEquals("测试禁飞区", data.get("name"));
        assertEquals("nfz", data.get("type"));
        assertNotNull(data.get("flight_area_id"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUpdateArea_whenValidInput() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = manageTool.execute(Map.of(
                "action", "update",
                "flight_area_id", "area_001",
                "name", "更新后的名称",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("update", data.get("action"));
        assertEquals("area_001", data.get("flight_area_id"));
        assertEquals("更新后的名称", data.get("name"));
        assertEquals(true, data.get("updated"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldDeleteArea_whenValidInput() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = manageTool.execute(Map.of(
                "action", "delete",
                "flight_area_id", "area_001",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("delete", data.get("action"));
        assertEquals("area_001", data.get("flight_area_id"));
        assertEquals(true, data.get("deleted"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(manageTool.requiresHumanConfirmation());
    }
}
