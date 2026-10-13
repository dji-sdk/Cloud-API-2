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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhFlightAreaSyncTool 单元测试。
 * 覆盖 session/workspace 校验、device_sns 参数校验、sync/status 两种操作的 mock 模式执行。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFlightAreaSyncToolTest {

    private SessionManager sessionManager;
    private FhFlightAreaSyncTool syncTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = SafetyTestHelper.createSdkServiceWithWorkspace();
        syncTool = new FhFlightAreaSyncTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = syncTool.getToolDefinition();

        assertEquals("fh_flight_area_sync", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "sync",
                "device_sns", List.of("DOCK-001"),
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
        FhFlightAreaSyncTool tool = new FhFlightAreaSyncTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "sync",
                "device_sns", List.of("DOCK-001"),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "invalid",
                "device_sns", List.of("DOCK-001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnsMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "sync");
        args.put("confirm", true);

        Map<String, Object> result = syncTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid device_sns"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnsEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "sync",
                "device_sns", List.of(),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid device_sns"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnsExceedMax() {
        sessionManager.createSession("uuid", "ws");

        // 生成 21 个设备 SN
        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            tooMany.add("DOCK-" + String.format("%03d", i));
        }

        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "sync",
                "device_sns", tooMany,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Too many devices"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSync_whenValidInput() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "sync",
                "device_sns", List.of("DOCK-001", "DOCK-002"),
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("sync", data.get("action"));
        assertEquals(2, data.get("sync_count"));
        List<String> sns = (List<String>) data.get("device_sns");
        assertEquals(2, sns.size());
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnStatus_withoutConfirm() {
        sessionManager.createSession("uuid", "ws");

        // status 操作不需要 confirm
        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "status",
                "device_sns", List.of("DOCK-001", "DOCK-002")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("status", data.get("action"));
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
        assertEquals(2, devices.size());
        assertEquals("DOCK-001", devices.get(0).get("device_sn"));
        assertNotNull(devices.get(0).get("status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldDeduplicateDeviceSns() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = syncTool.execute(Map.of(
                "action", "status",
                "device_sns", List.of("DOCK-001", "DOCK-001", "DOCK-002")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
        // 去重后只有 2 个设备
        assertEquals(2, devices.size());
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        // status 是只读操作，工具级标志为 false
        assertFalse(syncTool.requiresHumanConfirmation());
    }
}
