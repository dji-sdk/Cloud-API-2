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

package com.dji.flighthub.mcp.tools.settings;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FhSettingTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、confirm 门控、Mock 路径。
 *
 * @author DJI FlightHub SDK Team
 */
class FhSettingToolTest {

    private SessionManager sessionManager;
    private FhSettingTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() { public boolean isMockMode() { return true; } };
        tool = new FhSettingTool(sessionManager, sdkService);
    }

    // ==================== getToolDefinition ====================

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_setting", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
        assertTrue(required.contains("drone_sn"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(tool.requiresHumanConfirmation());
    }

    @Test
    void getSecurityLevel_shouldReturnReversible() {
        assertEquals(SecurityLevel.REVERSIBLE, tool.getSecurityLevel());
    }

    // ==================== session / action 校验 ====================

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("action", "set_rth_altitude"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "invalid", "drone_sn", "DRONE-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "set_rth_altitude", "altitude", 100));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    // ==================== set_rth_altitude ====================

    @Test
    void executeSetRthAltitude_shouldReturnError_whenAltitudeMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_rth_altitude");
        args.put("drone_sn", "DRONE-001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("altitude"));
    }

    @Test
    void executeSetRthAltitude_shouldReturnError_whenAltitudeTooLow() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_rth_altitude");
        args.put("drone_sn", "DRONE-001");
        args.put("altitude", 5);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("altitude"));
    }

    @Test
    void executeSetRthAltitude_shouldReturnError_whenAltitudeTooHigh() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_rth_altitude");
        args.put("drone_sn", "DRONE-001");
        args.put("altitude", 2000);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("altitude"));
    }

    @Test
    void executeSetRthAltitude_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_rth_altitude");
        args.put("drone_sn", "DRONE-001");
        args.put("altitude", 100);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetRthAltitude_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_rth_altitude");
        args.put("drone_sn", "DRONE-001");
        args.put("altitude", 100);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("DRONE-001", data.get("drone_sn"));
        assertEquals(100, data.get("rth_altitude"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== set_out_of_control_action ====================

    @Test
    void executeSetOutOfControlAction_shouldReturnError_whenActionValueMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_out_of_control_action");
        args.put("drone_sn", "DRONE-001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action"));
    }

    @Test
    void executeSetOutOfControlAction_shouldReturnError_whenActionValueInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_out_of_control_action");
        args.put("drone_sn", "DRONE-001");
        args.put("out_of_control_action", "fly_away");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("out_of_control_action"));
    }

    @Test
    void executeSetOutOfControlAction_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_out_of_control_action");
        args.put("drone_sn", "DRONE-001");
        args.put("out_of_control_action", "hover");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetOutOfControlAction_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_out_of_control_action");
        args.put("drone_sn", "DRONE-001");
        args.put("out_of_control_action", "return_home");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("DRONE-001", data.get("drone_sn"));
        assertEquals("return_home", data.get("out_of_control_action"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetOutOfControlAction_shouldSucceed_withHover() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_out_of_control_action");
        args.put("drone_sn", "DRONE-001");
        args.put("out_of_control_action", "hover");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("hover", data.get("out_of_control_action"));
    }

    // ==================== 安全等级硬拦截 ====================

    @Test
    void execute_shouldReturnError_whenSecurityLevelInsufficient() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(0);
        SessionManager limitedManager = new SessionManager(properties);
        limitedManager.createSession("uuid", "ws");

        SdkService sdkService = new SdkService() { public boolean isMockMode() { return true; } };
        FhSettingTool limitedTool = new FhSettingTool(limitedManager, sdkService);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_rth_altitude");
        args.put("drone_sn", "DRONE-001");
        args.put("altitude", 100);
        args.put("confirm", true);

        Map<String, Object> result = limitedTool.execute(args);

        assertEquals(false, result.get("success"));
        assertFalse(result.get("error_message").toString().isBlank());
    }
}
