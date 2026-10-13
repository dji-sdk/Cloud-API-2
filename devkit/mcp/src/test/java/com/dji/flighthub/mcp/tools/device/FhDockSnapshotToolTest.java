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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDockSnapshotTool 单元测试。
 * 覆盖 session 校验、参数校验和 Mock 模式下的返回结构。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDockSnapshotToolTest {

    private static final String DOCK_SN = "DOCK-SNAP-001";

    private SessionManager sessionManager;
    private FhDockSnapshotTool dockSnapshotTool;

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
        dockSnapshotTool = new FhDockSnapshotTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = dockSnapshotTool.getToolDefinition();

        assertEquals("fh_dock_snapshot", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = dockSnapshotTool.execute(Map.of(
                "dock_sn", DOCK_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = dockSnapshotTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnBlank() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = dockSnapshotTool.execute(Map.of("dock_sn", "  "));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing dock_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockData_whenMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = dockSnapshotTool.execute(Map.of(
                "dock_sn", DOCK_SN
        ));

        assertEquals(true, result.get("success"));
        assertNotNull(result.get("message"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DOCK_SN, data.get("dock_sn"));
        assertEquals(22.5435, data.get("latitude"));
        assertEquals(113.9470, data.get("longitude"));
        assertEquals(25.0, data.get("height"));
        assertEquals(90.0, data.get("heading"));
        assertEquals(0, data.get("cover_state"));
        assertEquals("closed", data.get("cover_state_desc"));
        assertEquals(1, data.get("drone_in_dock"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldIncludeNextSteps_whenMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = dockSnapshotTool.execute(Map.of(
                "dock_sn", DOCK_SN
        ));

        assertNotNull(result.get("next_steps"));
    }
}
