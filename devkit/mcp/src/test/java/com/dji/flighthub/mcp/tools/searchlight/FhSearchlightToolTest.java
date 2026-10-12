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

package com.dji.flighthub.mcp.tools.searchlight;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhSearchlightTool Mock 模式单元测试。
 * 验证参数校验、安全确认、mock 数据结构。
 */
class FhSearchlightToolTest {

    private FhSearchlightTool tool;
    private SessionManager sessionManager;

    private static final String DRONE_SN = "DRONE-SEARCHLIGHT-001";

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService();
        tool = new FhSearchlightTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldHaveCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_searchlight", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("drone_sn"));
        assertTrue(required.contains("action"));
        assertFalse(required.contains("confirm"),
                "confirm should NOT be required — two-step confirmation UX");
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(tool.requiresHumanConfirmation());
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "mode_set",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDroneSn() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "mode_set",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNoControlAuthority() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "mode_set",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No control authority"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenNoConfirm() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "mode_set",
                "mode", 1
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidMode() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "mode_set",
                "confirm", true,
                "mode", 99
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_modeSet() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "mode_set",
                "confirm", true,
                "mode", 2
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("mode_set", data.get("action"));
        assertEquals(2, data.get("mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_calibration() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "calibration",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("started", data.get("status"));
    }

    @Test
    void execute_shouldReturnError_fineTuning_missingPosition() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "fine_tuning",
                "confirm", true,
                "value", 1.5
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("position"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_fineTuning() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "fine_tuning",
                "confirm", true,
                "position", 1,
                "value", 2.5,
                "saved", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1, data.get("position"));
        assertEquals(2.5, data.get("value"));
        assertEquals(true, data.get("saved"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_gimbalControl() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "gimbal_control",
                "confirm", true,
                "gimbal_control", 1
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1, data.get("gimbal_control"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_gimbalControl_negativeValue() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "gimbal_control",
                "confirm", true,
                "gimbal_control", -1
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(-1, data.get("gimbal_control"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_typeSet() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "type_set",
                "confirm", true,
                "type", 2
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_wideFieldMode() {
        createSessionWithControl();

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "wide_field_mode",
                "confirm", true,
                "wide_field_mode", 1
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1, data.get("wide_field_mode"));
    }

    // ==================== 辅助方法 ====================

    private void createSessionWithControl() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
    }
}
