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

package com.dji.flighthub.mcp.tools.connect;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhSessionInfoTool 单元测试。
 * 覆盖 session 校验、正常返回结构、控制权/DRC 状态展示。
 *
 * @author DJI FlightHub SDK Team
 */
class FhSessionInfoToolTest {

    private SessionManager sessionManager;
    private FhSessionInfoTool sessionInfoTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        sessionInfoTool = new FhSessionInfoTool(sessionManager);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = sessionInfoTool.getToolDefinition();

        assertEquals("fh_session_info", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = sessionInfoTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSessionData_whenSessionExists() {
        sessionManager.createSession("test-uuid-12345678", "TestWorkspace");

        Map<String, Object> result = sessionInfoTool.execute(Map.of());

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertNotNull(data.get("session_id"));
        assertEquals("test-uuid-12345678", data.get("workspace_uuid"));
        assertEquals("TestWorkspace", data.get("workspace_name"));
        assertEquals("L3", data.get("max_security_level"));
        assertNotNull(data.get("uptime_minutes"));
        assertNotNull(data.get("last_activity"));
        assertEquals(0, data.get("active_subscriptions"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldShowControlAuthority_whenHasControl() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice("DRONE-001");

        Map<String, Object> result = sessionInfoTool.execute(Map.of());
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> controlAuth = (Map<String, Object>) data.get("control_authority");

        assertEquals(true, controlAuth.get("active"));
        assertEquals("DRONE-001", controlAuth.get("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldShowDrcLink_whenDrcActive() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice("DRONE-002");
        session.setActiveDrcDevice("DRONE-002");

        Map<String, Object> result = sessionInfoTool.execute(Map.of());
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> drcLink = (Map<String, Object>) data.get("drc_link");

        assertEquals(true, drcLink.get("active"));
        assertEquals("DRONE-002", drcLink.get("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldShowInactiveStates_whenNothingActive() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = sessionInfoTool.execute(Map.of());
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> controlAuth = (Map<String, Object>) data.get("control_authority");
        Map<String, Object> drcLink = (Map<String, Object>) data.get("drc_link");

        assertEquals(false, controlAuth.get("active"));
        assertEquals(false, drcLink.get("active"));
    }
}
