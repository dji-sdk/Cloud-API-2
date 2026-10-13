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

package com.dji.flighthub.mcp.tools.debug;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDeviceDebugTool Mock 模式单元测试。
 * 验证参数校验、安全确认（L0/L1 混合安全等级）、mock 数据结构。
 */
class FhDeviceDebugToolTest {

    private FhDeviceDebugTool tool;
    private SessionManager sessionManager;

    private static final String DOCK_SN = "DOCK-DEBUG-001";

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService();
        tool = new FhDeviceDebugTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldHaveCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_device_debug", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("device_sn"));
        assertTrue(required.contains("action"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(tool.requiresHumanConfirmation());
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "open_debug",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "open_debug",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenNoConfirm_forL1Action() {
        sessionManager.createSession("uuid", "ws");

        // open_debug 是 L1 操作，需要 confirm
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "open_debug"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_openDebug_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "open_debug",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DOCK_SN, data.get("device_sn"));
        assertEquals("open_debug", data.get("action"));
        assertEquals(true, data.get("debug_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_closeDebug_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "close_debug",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(false, data.get("debug_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_openCover_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "open_cover",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("open", data.get("cover"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_formatDevice_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "format_device",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("format_device", data.get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_rebootDevice_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "reboot_device",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("rebooting", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setHeightLimit_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        // set_height_limit 是 L0 操作，无需 confirm
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "set_height_limit",
                "height_limit", 120
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(120, data.get("height_limit"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setDistanceLimit_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "set_distance_limit",
                "distance_limit_state", 1,
                "distance_limit_value", 5000
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("set_distance_limit", data.get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setObstacleAvoidance_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "set_obstacle_avoidance",
                "obstacle_avoidance_state", 1
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("set_obstacle_avoidance", data.get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_networkTestStart_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "network_test_start",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("network_test_start", data.get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setRelayPassword_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "set_relay_password",
                "passwd", "newpass123"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("set_relay_password", data.get("action"));
    }

    @Test
    void execute_shouldReturnError_relayReboot_withoutConfirm() {
        sessionManager.createSession("uuid", "ws");

        // relay_reboot 是 L1 操作，需要 confirm
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN,
                "action", "relay_reboot"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    // ==================== 补全未覆盖的 L1 action mock 路径 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_closeCover_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "close_cover", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals("closed", ((Map<String, Object>) result.get("data")).get("cover"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_openCharge_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "open_charge", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals(true, ((Map<String, Object>) result.get("data")).get("charging"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_closeCharge_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "close_charge", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals(false, ((Map<String, Object>) result.get("data")).get("charging"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_openDrone_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "open_drone", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals(true, ((Map<String, Object>) result.get("data")).get("drone_power"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_closeDrone_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "close_drone", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals(false, ((Map<String, Object>) result.get("data")).get("drone_power"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_formatDrone_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "format_drone", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals("formatting", ((Map<String, Object>) result.get("data")).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_networkTestStop_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "network_test_stop", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals("stopped", ((Map<String, Object>) result.get("data")).get("network_test"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_rtkCalibrateManual_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "rtk_calibrate_manual", "confirm", true,
                "longitude", 113.9738960, "latitude", 22.5824969, "height", 102.9));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("rtk_calibrate_manual", data.get("action"));
        assertEquals(113.9738960, (double) data.get("longitude"), 1e-7);
        assertEquals(22.5824969, (double) data.get("latitude"), 1e-7);
        assertEquals(102.9, (double) data.get("height"), 1e-7);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_rtkCalibrateNetwork_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "rtk_calibrate_network", "confirm", true,
                "rtk_host", "ntrip.example.com", "rtk_port", 2101,
                "rtk_account", "user", "rtk_password", "pass",
                "rtk_mount_point", "MOUNT1"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("rtk_calibrate_network", data.get("action"));
        assertEquals("ntrip.example.com", data.get("host"));
        assertEquals(2101, data.get("port"));
        assertEquals("MOUNT1", data.get("mount_point"));
    }

    @Test
    void execute_shouldFail_rtkCalibrateManual_missingLongitude() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "rtk_calibrate_manual", "confirm", true,
                "latitude", 22.5824969, "height", 102.9));

        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldFail_rtkCalibrateNetwork_missingHost() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "rtk_calibrate_network", "confirm", true,
                "rtk_port", 2101, "rtk_account", "user", "rtk_password", "pass",
                "rtk_mount_point", "MOUNT1"));

        assertEquals(false, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setNightLights_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_night_lights",
                "night_lights_state", 1));

        assertEquals(true, result.get("success"));
        assertEquals("set_night_lights", ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setBatteryStoreMode_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_battery_store_mode",
                "battery_store_mode", 1));

        assertEquals(true, result.get("success"));
        assertEquals("set_battery_store_mode",
                ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setRemainingPowerRth_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_remaining_power_rth",
                "remaining_power_for_rth", 20));

        assertEquals(true, result.get("success"));
        assertEquals("set_remaining_power_rth",
                ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setAirConditioner_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_air_conditioner",
                "air_conditioner_mode", 0));

        assertEquals(true, result.get("success"));
        assertEquals("set_air_conditioner",
                ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setSilentMode_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_silent_mode",
                "silent_mode", 1));

        assertEquals(true, result.get("success"));
        assertEquals("set_silent_mode", ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_switchAlarmLight_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "switch_alarm_light",
                "alarm_state", 1));

        assertEquals(true, result.get("success"));
        assertEquals("switch_alarm_light",
                ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_simSlotSwitch_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("device_sn", DOCK_SN);
        args.put("action", "sim_slot_switch");
        args.put("imei", "860000000000001");
        args.put("sim_slot", 0);
        args.put("device_type", "dock");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("860000000000001", data.get("imei"));
    }

    @Test
    void execute_shouldReturnError_simSlotSwitch_missingImei() {
        sessionManager.createSession("uuid", "ws");

        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("device_sn", DOCK_SN);
        args.put("action", "sim_slot_switch");
        args.put("sim_slot", 0);
        args.put("device_type", "dock");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("imei"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_esimOperatorSwitch_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("device_sn", DOCK_SN);
        args.put("action", "esim_operator_switch");
        args.put("imei", "860000000000001");
        args.put("operator_type", 1);
        args.put("device_type", "dock");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("esim_operator_switch", data.get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_relayReboot_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "relay_reboot", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals("rebooting", ((Map<String, Object>) result.get("data")).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_relayDisconnect_withConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "relay_disconnect", "confirm", true));

        assertEquals(true, result.get("success"));
        assertEquals("disconnected", ((Map<String, Object>) result.get("data")).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setRelaySsid_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_relay_ssid",
                "device_name", "MyRelay-5G"));

        assertEquals(true, result.get("success"));
        assertEquals("set_relay_ssid", ((Map<String, Object>) result.get("data")).get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_setRelaySearchMode_L0_noConfirmNeeded() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_relay_search_mode",
                "relay_search_mode", 1));

        assertEquals(true, result.get("success"));
        assertEquals("set_relay_search_mode",
                ((Map<String, Object>) result.get("data")).get("action"));
    }

    // ==================== 参数校验错误路径（mockSetIntParam/mockSetStringParam 分支） ====================

    @Test
    void execute_shouldReturnError_setNightLights_missingParam() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_night_lights"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("night_lights_state"));
    }

    @Test
    void execute_shouldReturnError_setDistanceLimit_missingState() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_distance_limit",
                "distance_limit_value", 5000));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("distance_limit_state"));
    }

    @Test
    void execute_shouldReturnError_setRelaySsid_missingParam() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DOCK_SN, "action", "set_relay_ssid"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_name"));
    }
}
