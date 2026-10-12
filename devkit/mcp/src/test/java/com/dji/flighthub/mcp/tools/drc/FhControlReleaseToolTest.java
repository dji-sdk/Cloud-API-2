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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhControlReleaseTool 单元测试。
 * 覆盖 session 校验、drone_sn 校验、控制权持有校验、DRC 链路校验及 mock 模式正常释放流程。
 *
 * @author DJI FlightHub SDK Team
 */
class FhControlReleaseToolTest {

    private static final String DRONE_SN = "DRONE-REL-001";

    private SessionManager sessionManager;
    private FhControlReleaseTool controlReleaseTool;

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
        controlReleaseTool = new FhControlReleaseTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = controlReleaseTool.getToolDefinition();

        assertEquals("fh_control_release", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = controlReleaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = controlReleaseTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNotControlling() {
        // session 存在，但没有持有控制权
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = controlReleaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Not controlling drone"));
    }

    @Test
    void execute_shouldReturnError_whenControllingDifferentDrone() {
        // 持有另一架无人机的控制权
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice("OTHER-DRONE");

        Map<String, Object> result = controlReleaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Not controlling drone"));
    }

    @Test
    void execute_shouldReturnError_whenDrcLinkStillActive() {
        // 持有控制权但 DRC 链路仍活跃
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = controlReleaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("DRC link still active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenMockMode() {
        // 满足所有前置条件：持有控制权 + 无 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = controlReleaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(true, data.get("released"));
        assertEquals(true, data.get("_mock"));

        // 包含 next_steps
        assertNotNull(result.get("next_steps"));
    }

    @Test
    void execute_shouldClearControlState_whenSuccessful() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        assertTrue(session.hasControlOf(DRONE_SN));

        controlReleaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        // 控制权应被清除
        assertFalse(session.hasControlOf(DRONE_SN));
        assertFalse(session.hasControlAuthority());
    }
}
