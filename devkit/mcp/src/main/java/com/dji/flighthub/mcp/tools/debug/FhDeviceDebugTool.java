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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.ManulRtkCalibrationParams;
import com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams;
import com.dji.flighthubsdk.bean.RtkCalibrationResult;
import com.dji.flighthubsdk.core.DeviceDebugModule;
import com.dji.flighthubsdk.core.Dock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * fh_device_debug — 设备调试工具，覆盖机场/无人机的调试模式、舱盖、充电、开关机、
 * 格式化、重启、网络测试、RTK 校准、飞行参数、电池、环境设置、SIM/eSIM、中继等操作。
 *
 * <p>安全等级：
 * <ul>
 *   <li>L1（物理操作：open/close/format/reboot/network/rtk/sim/esim/relay_reboot/relay_disconnect）— 需人工确认</li>
 *   <li>L0（set_* 配置参数及 relay 设置类）— 无需确认</li>
 * </ul>
 *
 * <p>前置条件：必须有活跃 session（fh_connect）。
 * <p>SDK 接口来源：DeviceDebugModule / RtkCalibrationModule / NetworkTestModule（通过 Dock 实例获取），操作对象为 Dock 设备。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDeviceDebugTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDeviceDebugTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_device_debug";

    // ==================== 参数常量 ====================
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_HEIGHT_LIMIT = "height_limit";
    private static final String PARAM_DISTANCE_LIMIT_STATE = "distance_limit_state";
    private static final String PARAM_DISTANCE_LIMIT_VALUE = "distance_limit_value";
    private static final String PARAM_OBSTACLE_AVOIDANCE_STATE = "obstacle_avoidance_state";
    private static final String PARAM_NIGHT_LIGHTS_STATE = "night_lights_state";
    private static final String PARAM_BATTERY_STORE_MODE = "battery_store_mode";
    private static final String PARAM_REMAINING_POWER_RTH = "remaining_power_for_rth";
    private static final String PARAM_AIR_CONDITIONER_MODE = "air_conditioner_mode";
    private static final String PARAM_SILENT_MODE = "silent_mode";
    private static final String PARAM_ALARM_STATE = "alarm_state";
    private static final String PARAM_IMEI = "imei";
    private static final String PARAM_SIM_SLOT = "sim_slot";
    private static final String PARAM_DEVICE_TYPE = "device_type";
    private static final String PARAM_OPERATOR_TYPE = "operator_type";
    private static final String PARAM_DEVICE_NAME = "device_name";
    private static final String PARAM_PASSWD = "passwd";
    private static final String PARAM_RELAY_SEARCH_MODE = "relay_search_mode";

    // RTK 手动定标参数
    private static final String PARAM_RTK_LONGITUDE = "longitude";
    private static final String PARAM_RTK_LATITUDE = "latitude";
    private static final String PARAM_RTK_HEIGHT = "height";

    // RTK 网络定标参数
    private static final String PARAM_RTK_HOST = "rtk_host";
    private static final String PARAM_RTK_PORT = "rtk_port";
    private static final String PARAM_RTK_ACCOUNT = "rtk_account";
    private static final String PARAM_RTK_PASSWORD = "rtk_password";
    private static final String PARAM_RTK_MOUNT_POINT = "rtk_mount_point";
    private static final String PARAM_RTK_VERSION = "rtk_version";

    // ==================== Action 常量 ====================
    private static final String ACTION_OPEN_DEBUG = "open_debug";
    private static final String ACTION_CLOSE_DEBUG = "close_debug";
    private static final String ACTION_OPEN_COVER = "open_cover";
    private static final String ACTION_CLOSE_COVER = "close_cover";
    private static final String ACTION_OPEN_CHARGE = "open_charge";
    private static final String ACTION_CLOSE_CHARGE = "close_charge";
    private static final String ACTION_OPEN_DRONE = "open_drone";
    private static final String ACTION_CLOSE_DRONE = "close_drone";
    private static final String ACTION_FORMAT_DEVICE = "format_device";
    private static final String ACTION_FORMAT_DRONE = "format_drone";
    private static final String ACTION_REBOOT_DEVICE = "reboot_device";
    private static final String ACTION_NETWORK_TEST_START = "network_test_start";
    private static final String ACTION_NETWORK_TEST_STOP = "network_test_stop";
    private static final String ACTION_RTK_CALIBRATE_MANUAL = "rtk_calibrate_manual";
    private static final String ACTION_RTK_CALIBRATE_NETWORK = "rtk_calibrate_network";
    private static final String ACTION_SET_HEIGHT_LIMIT = "set_height_limit";
    private static final String ACTION_SET_DISTANCE_LIMIT = "set_distance_limit";
    private static final String ACTION_SET_OBSTACLE_AVOIDANCE = "set_obstacle_avoidance";
    private static final String ACTION_SET_NIGHT_LIGHTS = "set_night_lights";
    private static final String ACTION_SET_BATTERY_STORE_MODE = "set_battery_store_mode";
    private static final String ACTION_SET_REMAINING_POWER_RTH = "set_remaining_power_rth";
    private static final String ACTION_SET_AIR_CONDITIONER = "set_air_conditioner";
    private static final String ACTION_SET_SILENT_MODE = "set_silent_mode";
    private static final String ACTION_SWITCH_ALARM_LIGHT = "switch_alarm_light";
    private static final String ACTION_SIM_SLOT_SWITCH = "sim_slot_switch";
    private static final String ACTION_ESIM_OPERATOR_SWITCH = "esim_operator_switch";
    private static final String ACTION_RELAY_REBOOT = "relay_reboot";
    private static final String ACTION_RELAY_DISCONNECT = "relay_disconnect";
    private static final String ACTION_SET_RELAY_SSID = "set_relay_ssid";
    private static final String ACTION_SET_RELAY_PASSWORD = "set_relay_password";
    private static final String ACTION_SET_RELAY_SEARCH_MODE = "set_relay_search_mode";

    /** 有效 action 集合 */
    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_OPEN_DEBUG, ACTION_CLOSE_DEBUG,
            ACTION_OPEN_COVER, ACTION_CLOSE_COVER,
            ACTION_OPEN_CHARGE, ACTION_CLOSE_CHARGE,
            ACTION_OPEN_DRONE, ACTION_CLOSE_DRONE,
            ACTION_FORMAT_DEVICE, ACTION_FORMAT_DRONE,
            ACTION_REBOOT_DEVICE,
            ACTION_NETWORK_TEST_START, ACTION_NETWORK_TEST_STOP,
            ACTION_RTK_CALIBRATE_MANUAL, ACTION_RTK_CALIBRATE_NETWORK,
            ACTION_SET_HEIGHT_LIMIT, ACTION_SET_DISTANCE_LIMIT,
            ACTION_SET_OBSTACLE_AVOIDANCE, ACTION_SET_NIGHT_LIGHTS,
            ACTION_SET_BATTERY_STORE_MODE, ACTION_SET_REMAINING_POWER_RTH,
            ACTION_SET_AIR_CONDITIONER, ACTION_SET_SILENT_MODE, ACTION_SWITCH_ALARM_LIGHT,
            ACTION_SIM_SLOT_SWITCH, ACTION_ESIM_OPERATOR_SWITCH,
            ACTION_RELAY_REBOOT, ACTION_RELAY_DISCONNECT,
            ACTION_SET_RELAY_SSID, ACTION_SET_RELAY_PASSWORD, ACTION_SET_RELAY_SEARCH_MODE
    );

    /** 有效 action 排序列表（用于错误提示） */
    private static final List<String> VALID_ACTIONS_SORTED = VALID_ACTIONS.stream()
            .sorted().toList();

    /**
     * 需要人工确认的物理操作白名单（L1 等级）：
     * 涉及开关机、舱盖、充电、格式化、重启、网络测试、RTK 校准、SIM/eSIM 切换、中继重启/断开。
     * set_* 配置类操作及 relay 设置类操作为 L0，无需确认。
     */
    private static final Set<String> CONFIRM_REQUIRED_ACTIONS = Set.of(
            ACTION_OPEN_DEBUG, ACTION_CLOSE_DEBUG,
            ACTION_OPEN_COVER, ACTION_CLOSE_COVER,
            ACTION_OPEN_CHARGE, ACTION_CLOSE_CHARGE,
            ACTION_OPEN_DRONE, ACTION_CLOSE_DRONE,
            ACTION_FORMAT_DEVICE, ACTION_FORMAT_DRONE,
            ACTION_REBOOT_DEVICE,
            ACTION_NETWORK_TEST_START, ACTION_NETWORK_TEST_STOP,
            ACTION_RTK_CALIBRATE_MANUAL, ACTION_RTK_CALIBRATE_NETWORK,
            ACTION_SIM_SLOT_SWITCH, ACTION_ESIM_OPERATOR_SWITCH,
            ACTION_RELAY_REBOOT, ACTION_RELAY_DISCONNECT
    );

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhDeviceDebugTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 本工具为混合安全等级：物理操作需确认（L1），配置参数设置免确认（L0）。
     * 返回 true 以阻止 SSE 等无反向通道的远程传输层执行任何物理操作；
     * L0 操作在 execute() 内部通过 CONFIRM_REQUIRED_ACTIONS 白名单判断放行。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Device debug operations for dock/drone: debug mode, cover, charge, power, "
                        + "format storage, reboot, network test, RTK calibration, flight params "
                        + "(height/distance/obstacle/night-lights), battery, environment "
                        + "(air-conditioner/silent/alarm), SIM/eSIM switch, relay control. "
                        + "Physical operations require confirm; set_* config operations do not.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Target device serial number (dock SN)"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Debug action to execute"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true for physical operations (L1). Not needed for set_* config actions."
        ));
        // 飞行参数
        props.put(PARAM_HEIGHT_LIMIT, Map.of(
                "type", "integer",
                "description", "Height limit in meters (for set_height_limit)"
        ));
        props.put(PARAM_DISTANCE_LIMIT_STATE, Map.of(
                "type", "integer",
                "description", "Distance limit state: 0=off, 1=on (for set_distance_limit)"
        ));
        props.put(PARAM_DISTANCE_LIMIT_VALUE, Map.of(
                "type", "integer",
                "description", "Distance limit value in meters (for set_distance_limit)"
        ));
        props.put(PARAM_OBSTACLE_AVOIDANCE_STATE, Map.of(
                "type", "integer",
                "description", "Obstacle avoidance state: 0=off, 1=on (for set_obstacle_avoidance)"
        ));
        props.put(PARAM_NIGHT_LIGHTS_STATE, Map.of(
                "type", "integer",
                "description", "Night lights state: 0=off, 1=on (for set_night_lights)"
        ));
        // 电池
        props.put(PARAM_BATTERY_STORE_MODE, Map.of(
                "type", "integer",
                "description", "Battery store mode (for set_battery_store_mode)"
        ));
        props.put(PARAM_REMAINING_POWER_RTH, Map.of(
                "type", "integer",
                "description", "Remaining power percentage for RTH (for set_remaining_power_rth)"
        ));
        // 环境
        props.put(PARAM_AIR_CONDITIONER_MODE, Map.of(
                "type", "integer",
                "description", "Air conditioner mode (for set_air_conditioner)"
        ));
        props.put(PARAM_SILENT_MODE, Map.of(
                "type", "integer",
                "description", "Silent mode: 0=off, 1=on (for set_silent_mode)"
        ));
        props.put(PARAM_ALARM_STATE, Map.of(
                "type", "integer",
                "description", "Alarm light state: 0=off, 1=on (for switch_alarm_light)"
        ));
        // SIM/eSIM
        props.put(PARAM_IMEI, Map.of(
                "type", "string",
                "description", "Device IMEI (for sim_slot_switch / esim_operator_switch)"
        ));
        props.put(PARAM_SIM_SLOT, Map.of(
                "type", "integer",
                "description", "SIM slot index (for sim_slot_switch)"
        ));
        props.put(PARAM_DEVICE_TYPE, Map.of(
                "type", "string",
                "description", "Device type identifier (for sim_slot_switch / esim_operator_switch)"
        ));
        props.put(PARAM_OPERATOR_TYPE, Map.of(
                "type", "integer",
                "description", "eSIM operator type (for esim_operator_switch)"
        ));
        // Relay
        props.put(PARAM_DEVICE_NAME, Map.of(
                "type", "string",
                "description", "Relay SSID device name (for set_relay_ssid)"
        ));
        props.put(PARAM_PASSWD, Map.of(
                "type", "string",
                "description", "Relay password (for set_relay_password)"
        ));
        props.put(PARAM_RELAY_SEARCH_MODE, Map.of(
                "type", "integer",
                "description", "Relay search mode (for set_relay_search_mode)"
        ));
        // RTK 手动定标
        props.put(PARAM_RTK_LONGITUDE, Map.of(
                "type", "number",
                "description", "Dock longitude in decimal degrees (for rtk_calibrate_manual)"
        ));
        props.put(PARAM_RTK_LATITUDE, Map.of(
                "type", "number",
                "description", "Dock latitude in decimal degrees (for rtk_calibrate_manual)"
        ));
        props.put(PARAM_RTK_HEIGHT, Map.of(
                "type", "number",
                "description", "Dock altitude in meters (for rtk_calibrate_manual)"
        ));
        // RTK 网络定标
        props.put(PARAM_RTK_HOST, Map.of(
                "type", "string",
                "description", "RTK NTRIP server host (for rtk_calibrate_network)"
        ));
        props.put(PARAM_RTK_PORT, Map.of(
                "type", "integer",
                "description", "RTK NTRIP server port (for rtk_calibrate_network)"
        ));
        props.put(PARAM_RTK_ACCOUNT, Map.of(
                "type", "string",
                "description", "RTK NTRIP account (for rtk_calibrate_network)"
        ));
        props.put(PARAM_RTK_PASSWORD, Map.of(
                "type", "string",
                "description", "RTK NTRIP password (for rtk_calibrate_network)"
        ));
        props.put(PARAM_RTK_MOUNT_POINT, Map.of(
                "type", "string",
                "description", "RTK NTRIP mount point (for rtk_calibrate_network)"
        ));
        props.put(PARAM_RTK_VERSION, Map.of(
                "type", "integer",
                "description", "RTK NTRIP protocol version, e.g. 1 or 2 (for rtk_calibrate_network, optional)"
        ));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 2. 校验 device_sn
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device_sn (dock SN). Use fh_device_list to get available devices.");
        }

        // 3. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        // 4. 物理操作需安全等级 L1 + 人工确认；set_* 配置操作免确认（L0）
        if (CONFIRM_REQUIRED_ACTIONS.contains(action)) {
            Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
            if (denied != null) {
                return denied;
            }
            Map<String, Object> confirmError = requireHumanConfirm(args, action, deviceSn,
                    buildConfirmMessage(deviceSn, action));
            if (confirmError != null) {
                return confirmError;
            }
        }

        // 5. Mock 模式（含 workspace 为 null 的降级场景）
        if (sdkService.isMockMode() || !sdkService.isInitialized()) {
            return dispatchMock(deviceSn, action, args);
        }

        // 6. 真实 SDK 调用
        return executeWithRealSdk(deviceSn, action, args);
    }

    // ==================== 确认提示构建 ====================

    /**
     * 构建物理操作的人工确认提示。
     *
     * @param deviceSn 目标设备 SN
     * @param action   操作类型
     * @return 确认提示信息
     */
    private String buildConfirmMessage(String deviceSn, String action) {
        return String.format("⚠️ 设备 %s 即将执行调试操作（%s），此为物理操作，是否确认？", deviceSn, action);
    }

    // ==================== Mock 模式分发 ====================

    /**
     * Mock 模式下根据 action 分发调试指令。
     *
     * @param sn     目标设备 SN
     * @param action 操作类型
     * @param args   原始参数 Map
     * @return 执行结果
     */
    private Map<String, Object> dispatchMock(String sn, String action, Map<String, Object> args) {
        return switch (action) {
            // ---- Debug Mode ----
            case ACTION_OPEN_DEBUG -> successResult(
                    Map.of("device_sn", sn, "action", action, "debug_mode", true),
                    "Debug mode opened");
            case ACTION_CLOSE_DEBUG -> successResult(
                    Map.of("device_sn", sn, "action", action, "debug_mode", false),
                    "Debug mode closed");

            // ---- Dock Cover ----
            case ACTION_OPEN_COVER -> successResult(
                    Map.of("device_sn", sn, "action", action, "cover", "open"),
                    "Dock cover opened");
            case ACTION_CLOSE_COVER -> successResult(
                    Map.of("device_sn", sn, "action", action, "cover", "closed"),
                    "Dock cover closed");

            // ---- Charge ----
            case ACTION_OPEN_CHARGE -> successResult(
                    Map.of("device_sn", sn, "action", action, "charging", true),
                    "Charging started");
            case ACTION_CLOSE_CHARGE -> successResult(
                    Map.of("device_sn", sn, "action", action, "charging", false),
                    "Charging stopped");

            // ---- Drone Power ----
            case ACTION_OPEN_DRONE -> successResult(
                    Map.of("device_sn", sn, "action", action, "drone_power", true),
                    "Drone powered on");
            case ACTION_CLOSE_DRONE -> successResult(
                    Map.of("device_sn", sn, "action", action, "drone_power", false),
                    "Drone powered off");

            // ---- Format ----
            case ACTION_FORMAT_DEVICE -> successResult(
                    Map.of("device_sn", sn, "action", action, "status", "formatting"),
                    "Device storage format initiated");
            case ACTION_FORMAT_DRONE -> successResult(
                    Map.of("device_sn", sn, "action", action, "status", "formatting"),
                    "Drone storage format initiated");

            // ---- Reboot ----
            case ACTION_REBOOT_DEVICE -> successResult(
                    Map.of("device_sn", sn, "action", action, "status", "rebooting"),
                    "Device reboot initiated");

            // ---- Network Test ----
            case ACTION_NETWORK_TEST_START -> successResult(
                    Map.of("device_sn", sn, "action", action, "network_test", "started"),
                    "Network speed test started");
            case ACTION_NETWORK_TEST_STOP -> successResult(
                    Map.of("device_sn", sn, "action", action, "network_test", "stopped"),
                    "Network speed test stopped");

            // ---- RTK Calibration ----
            case ACTION_RTK_CALIBRATE_MANUAL -> mockRtkCalibrateManual(sn, args);
            case ACTION_RTK_CALIBRATE_NETWORK -> mockRtkCalibrateNetwork(sn, args);

            // ---- Flight Params (L0) ----
            case ACTION_SET_HEIGHT_LIMIT -> mockSetHeightLimit(sn, args);
            case ACTION_SET_DISTANCE_LIMIT -> mockSetDistanceLimit(sn, args);
            case ACTION_SET_OBSTACLE_AVOIDANCE -> mockSetIntParam(sn, action,
                    PARAM_OBSTACLE_AVOIDANCE_STATE, args, "Obstacle avoidance state set");
            case ACTION_SET_NIGHT_LIGHTS -> mockSetIntParam(sn, action,
                    PARAM_NIGHT_LIGHTS_STATE, args, "Night lights state set");

            // ---- Battery (L0) ----
            case ACTION_SET_BATTERY_STORE_MODE -> mockSetIntParam(sn, action,
                    PARAM_BATTERY_STORE_MODE, args, "Battery store mode set");
            case ACTION_SET_REMAINING_POWER_RTH -> mockSetIntParam(sn, action,
                    PARAM_REMAINING_POWER_RTH, args, "Remaining power for RTH set");

            // ---- Environment (L0) ----
            case ACTION_SET_AIR_CONDITIONER -> mockSetIntParam(sn, action,
                    PARAM_AIR_CONDITIONER_MODE, args, "Air conditioner mode set");
            case ACTION_SET_SILENT_MODE -> mockSetIntParam(sn, action,
                    PARAM_SILENT_MODE, args, "Silent mode set");
            case ACTION_SWITCH_ALARM_LIGHT -> mockSetIntParam(sn, action,
                    PARAM_ALARM_STATE, args, "Alarm light state set");

            // ---- SIM/eSIM ----
            case ACTION_SIM_SLOT_SWITCH -> mockSimSlotSwitch(sn, args);
            case ACTION_ESIM_OPERATOR_SWITCH -> mockEsimOperatorSwitch(sn, args);

            // ---- Relay ----
            case ACTION_RELAY_REBOOT -> successResult(
                    Map.of("device_sn", sn, "action", action, "status", "rebooting"),
                    "Relay reboot initiated");
            case ACTION_RELAY_DISCONNECT -> successResult(
                    Map.of("device_sn", sn, "action", action, "status", "disconnected"),
                    "Relay disconnected");
            case ACTION_SET_RELAY_SSID -> mockSetStringParam(sn, action,
                    PARAM_DEVICE_NAME, args, "Relay SSID set");
            case ACTION_SET_RELAY_PASSWORD -> mockSetStringParam(sn, action,
                    PARAM_PASSWD, args, "Relay password set");
            case ACTION_SET_RELAY_SEARCH_MODE -> mockSetIntParam(sn, action,
                    PARAM_RELAY_SEARCH_MODE, args, "Relay search mode set");

            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    // ==================== Mock 辅助方法 ====================

    /**
     * Mock 设置限高。
     */
    private Map<String, Object> mockSetHeightLimit(String sn, Map<String, Object> args) {
        int heightLimit = getIntArg(args, PARAM_HEIGHT_LIMIT, -1);
        if (heightLimit <= 0) {
            return errorResult("Missing or invalid height_limit",
                    "Provide height_limit (positive integer, in meters).");
        }
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_SET_HEIGHT_LIMIT, "height_limit", heightLimit),
                "Height limit set to " + heightLimit + "m");
    }

    /**
     * Mock 设置限远。
     */
    private Map<String, Object> mockSetDistanceLimit(String sn, Map<String, Object> args) {
        int state = getIntArg(args, PARAM_DISTANCE_LIMIT_STATE, -1);
        int value = getIntArg(args, PARAM_DISTANCE_LIMIT_VALUE, -1);
        if (state < 0) {
            return errorResult("Missing distance_limit_state",
                    "Provide distance_limit_state (0=off, 1=on).");
        }
        if (value < 0) {
            return errorResult("Missing distance_limit_value",
                    "Provide distance_limit_value (meters).");
        }
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_SET_DISTANCE_LIMIT,
                        "distance_limit_state", state, "distance_limit_value", value),
                "Distance limit set: state=" + state + ", value=" + value + "m");
    }

    /**
     * Mock 通用整型参数设置。
     */
    private Map<String, Object> mockSetIntParam(String sn, String action, String paramKey,
                                                Map<String, Object> args, String successMsg) {
        int value = getIntArg(args, paramKey, -1);
        if (value < 0) {
            return errorResult("Missing or invalid parameter: " + paramKey,
                    "Provide " + paramKey + " (non-negative integer).");
        }
        return successResult(
                Map.of("device_sn", sn, "action", action, paramKey, value),
                successMsg + " to " + value);
    }

    /**
     * Mock 通用字符串参数设置。
     */
    private Map<String, Object> mockSetStringParam(String sn, String action, String paramKey,
                                                   Map<String, Object> args, String successMsg) {
        String value = getStringArg(args, paramKey, null);
        if (value == null) {
            return errorResult("Missing parameter: " + paramKey,
                    "Provide " + paramKey + " (non-empty string).");
        }
        return successResult(
                Map.of("device_sn", sn, "action", action, paramKey, value),
                successMsg + ": " + value);
    }

    /**
     * Mock SIM 卡槽切换。
     */
    private Map<String, Object> mockSimSlotSwitch(String sn, Map<String, Object> args) {
        String imei = getStringArg(args, PARAM_IMEI, null);
        int simSlot = getIntArg(args, PARAM_SIM_SLOT, -1);
        String deviceType = getStringArg(args, PARAM_DEVICE_TYPE, null);
        if (imei == null) {
            return errorResult("Missing parameter: imei", "Provide device IMEI.");
        }
        if (simSlot < 0) {
            return errorResult("Missing parameter: sim_slot", "Provide SIM slot index.");
        }
        if (deviceType == null) {
            return errorResult("Missing parameter: device_type", "Provide device type.");
        }
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_SIM_SLOT_SWITCH,
                        "imei", imei, "sim_slot", simSlot, "device_type", deviceType),
                "SIM slot switched: imei=" + imei + ", slot=" + simSlot);
    }

    /**
     * Mock eSIM 运营商切换。
     */
    private Map<String, Object> mockEsimOperatorSwitch(String sn, Map<String, Object> args) {
        String imei = getStringArg(args, PARAM_IMEI, null);
        int operatorType = getIntArg(args, PARAM_OPERATOR_TYPE, -1);
        String deviceType = getStringArg(args, PARAM_DEVICE_TYPE, null);
        if (imei == null) {
            return errorResult("Missing parameter: imei", "Provide device IMEI.");
        }
        if (operatorType < 0) {
            return errorResult("Missing parameter: operator_type", "Provide eSIM operator type.");
        }
        if (deviceType == null) {
            return errorResult("Missing parameter: device_type", "Provide device type.");
        }
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_ESIM_OPERATOR_SWITCH,
                        "imei", imei, "operator_type", operatorType, "device_type", deviceType),
                "eSIM operator switched: imei=" + imei + ", operator=" + operatorType);
    }

    // ==================== 真实 SDK 调用 ====================

    /**
     * 真实 SDK 模式下执行设备调试指令。
     * 通过 dock.getDeviceDebugModule() / dock.getRtkCalibrationModule() / dock.getNetworkTestModule() 路由到对应模块。
     *
     * @param deviceSn 目标设备 SN（dock SN）
     * @param action   操作类型
     * @param args     原始参数 Map
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(String deviceSn, String action, Map<String, Object> args) {
        try {
            Dock dock = sdkService.getDock(deviceSn);
            if (dock == null) {
                return errorResult("Dock instance not found: " + deviceSn,
                        "Ensure dock is online. Use fh_device_list to check.");
            }

            return dispatchRealSdk(dock, deviceSn, action, args);

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("设备调试超时: device={}, action={}: {}", deviceSn, action, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("设备调试 SDK 错误: device={}, action={}: [{}] {}",
                    deviceSn, action, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check device state and retry.");
        } catch (Exception e) {
            log.error("设备调试异常: device={}, action={}: {}", deviceSn, action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check device connectivity and try again.");
        }
    }

    /**
     * 根据 action 分发真实 SDK 调用。
     */
    private Map<String, Object> dispatchRealSdk(Dock dock, String sn,
                                                String action, Map<String, Object> args) {
        DeviceDebugModule module = dock.getDeviceDebugModule();
        if (module == null) {
            return errorResult("DeviceDebugModule not available for: " + sn,
                    "This dock model may not support debug mode. Check device capabilities.");
        }
        return switch (action) {
            // ---- Debug Mode ----
            case ACTION_OPEN_DEBUG -> {
                awaitDebugOp(f -> module.openDebugMode(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "debug_mode", true),
                        "Debug mode opened");
            }
            case ACTION_CLOSE_DEBUG -> {
                awaitDebugOp(f -> module.closeDebugMode(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "debug_mode", false),
                        "Debug mode closed");
            }

            // ---- Dock Cover ----
            case ACTION_OPEN_COVER -> {
                awaitDebugOp(f -> module.openDockCover(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "cover", "open"),
                        "Dock cover opened");
            }
            case ACTION_CLOSE_COVER -> {
                awaitDebugOp(f -> module.closeDockCover(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "cover", "closed"),
                        "Dock cover closed");
            }

            // ---- Charge ----
            case ACTION_OPEN_CHARGE -> {
                awaitDebugOp(f -> module.openCharge(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "charging", true),
                        "Charging started");
            }
            case ACTION_CLOSE_CHARGE -> {
                awaitDebugOp(f -> module.closeCharge(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "charging", false),
                        "Charging stopped");
            }

            // ---- Drone Power ----
            case ACTION_OPEN_DRONE -> {
                awaitDebugOp(f -> module.openDrone(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "drone_power", true),
                        "Drone powered on");
            }
            case ACTION_CLOSE_DRONE -> {
                awaitDebugOp(f -> module.closeDrone(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "drone_power", false),
                        "Drone powered off");
            }

            // ---- Format ----
            case ACTION_FORMAT_DEVICE -> {
                awaitDebugOp(f -> module.deviceFormat(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "status", "formatting"),
                        "Device storage format initiated");
            }
            case ACTION_FORMAT_DRONE -> {
                awaitDebugOp(f -> module.droneFormat(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "status", "formatting"),
                        "Drone storage format initiated");
            }

            // ---- Reboot ----
            case ACTION_REBOOT_DEVICE -> {
                awaitDebugOp(f -> module.deviceReboot(sn, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "status", "rebooting"),
                        "Device reboot initiated");
            }

            // ---- Network Test ----
            case ACTION_NETWORK_TEST_START -> {
                awaitDebugOp(f -> dock.getNetworkTestModule().startNetworkTest((err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "network_test", "started"),
                        "Network speed test started");
            }
            case ACTION_NETWORK_TEST_STOP -> {
                awaitDebugOp(f -> dock.getNetworkTestModule().stopNetworkTest(err -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "network_test", "stopped"),
                        "Network speed test stopped");
            }

            // ---- RTK Calibration ----
            case ACTION_RTK_CALIBRATE_MANUAL -> realRtkCalibrateManual(dock, sn, args);
            case ACTION_RTK_CALIBRATE_NETWORK -> realRtkCalibrateNetwork(dock, sn, args);

            // ---- Flight Params (L0) ----
            case ACTION_SET_HEIGHT_LIMIT -> realSetHeightLimit(dock, sn, args);
            case ACTION_SET_DISTANCE_LIMIT -> realSetDistanceLimit(dock, sn, args);
            case ACTION_SET_OBSTACLE_AVOIDANCE -> {
                int state = getIntArg(args, PARAM_OBSTACLE_AVOIDANCE_STATE, -1);
                if (state < 0) {
                    yield errorResult("Missing obstacle_avoidance_state", "Provide obstacle_avoidance_state.");
                }
                awaitDebugOp(f -> module.setObstacleAvoidance(sn, state, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "obstacle_avoidance_state", state),
                        "Obstacle avoidance state set to " + state);
            }
            case ACTION_SET_NIGHT_LIGHTS -> {
                int state = getIntArg(args, PARAM_NIGHT_LIGHTS_STATE, -1);
                if (state < 0) {
                    yield errorResult("Missing night_lights_state", "Provide night_lights_state.");
                }
                awaitDebugOp(f -> module.setNightLightsState(sn, state, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "night_lights_state", state),
                        "Night lights state set to " + state);
            }

            // ---- Battery (L0) ----
            case ACTION_SET_BATTERY_STORE_MODE -> {
                int mode = getIntArg(args, PARAM_BATTERY_STORE_MODE, -1);
                if (mode < 0) {
                    yield errorResult("Missing battery_store_mode", "Provide battery_store_mode.");
                }
                awaitDebugOp(f -> module.setBatteryStoreMode(sn, mode, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "battery_store_mode", mode),
                        "Battery store mode set to " + mode);
            }
            case ACTION_SET_REMAINING_POWER_RTH -> {
                int power = getIntArg(args, PARAM_REMAINING_POWER_RTH, -1);
                if (power < 0) {
                    yield errorResult("Missing remaining_power_for_rth", "Provide remaining_power_for_rth.");
                }
                awaitDebugOp(f -> module.setRemainingPowerForRth(sn, power, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "remaining_power_for_rth", power),
                        "Remaining power for RTH set to " + power + "%");
            }

            // ---- Environment (L0) ----
            case ACTION_SET_AIR_CONDITIONER -> {
                int mode = getIntArg(args, PARAM_AIR_CONDITIONER_MODE, -1);
                if (mode < 0) {
                    yield errorResult("Missing air_conditioner_mode", "Provide air_conditioner_mode.");
                }
                SdkCallbackBridge.awaitVoid(cb -> module.setDockAirConditionerMode(sn, mode, cb));
                yield successResult(Map.of("device_sn", sn, "action", action, "air_conditioner_mode", mode),
                        "Air conditioner mode set to " + mode);
            }
            case ACTION_SET_SILENT_MODE -> {
                int mode = getIntArg(args, PARAM_SILENT_MODE, -1);
                if (mode < 0) {
                    yield errorResult("Missing silent_mode", "Provide silent_mode.");
                }
                SdkCallbackBridge.awaitVoid(cb -> module.setDockSilentMode(sn, mode, cb));
                yield successResult(Map.of("device_sn", sn, "action", action, "silent_mode", mode),
                        "Silent mode set to " + mode);
            }
            case ACTION_SWITCH_ALARM_LIGHT -> {
                int state = getIntArg(args, PARAM_ALARM_STATE, -1);
                if (state < 0) {
                    yield errorResult("Missing alarm_state", "Provide alarm_state.");
                }
                awaitDebugOp(f -> module.switchAlarmLightState(sn, state, (err, res) -> completeFuture(f, err)));
                yield successResult(Map.of("device_sn", sn, "action", action, "alarm_state", state),
                        "Alarm light state set to " + state);
            }

            // ---- SIM/eSIM ----
            case ACTION_SIM_SLOT_SWITCH -> realSimSlotSwitch(dock, sn, args);
            case ACTION_ESIM_OPERATOR_SWITCH -> realEsimOperatorSwitch(dock, sn, args);

            // ---- Relay ----
            // TODO: SDK v2.0.0 API 变更 — 中继控制方法已从 DeviceDebugModule 迁移到 RelayControlModule，
            //       但 Dock 类未暴露 getRelayControlModule() 方法（已通过 javap 验证）。
            //       在 SDK 对外开放该接口之前，此类操作暂不支持。
            case ACTION_RELAY_REBOOT -> {
                yield errorResult(
                        "Relay reboot not supported in SDK v2.0.0",
                        "RelayControlModule is not exposed on the Dock object in SDK v2.0.0. "
                                + "Please perform relay operations through the FlightHub web console.");
            }
            case ACTION_RELAY_DISCONNECT -> {
                yield errorResult(
                        "Relay disconnect not supported in SDK v2.0.0",
                        "RelayControlModule is not exposed on the Dock object in SDK v2.0.0. "
                                + "Please perform relay operations through the FlightHub web console.");
            }
            case ACTION_SET_RELAY_SSID -> {
                yield errorResult(
                        "Set relay SSID not supported in SDK v2.0.0",
                        "RelayControlModule is not exposed on the Dock object in SDK v2.0.0. "
                                + "Please configure relay SSID through the FlightHub web console.");
            }
            case ACTION_SET_RELAY_PASSWORD -> {
                yield errorResult(
                        "Set relay password not supported in SDK v2.0.0",
                        "RelayControlModule is not exposed on the Dock object in SDK v2.0.0. "
                                + "Please configure relay password through the FlightHub web console.");
            }
            case ACTION_SET_RELAY_SEARCH_MODE -> {
                yield errorResult(
                        "Set relay search mode not supported in SDK v2.0.0",
                        "RelayControlModule is not exposed on the Dock object in SDK v2.0.0. "
                                + "Please configure relay search mode through the FlightHub web console.");
            }

            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    // ==================== 真实 SDK 复杂参数子方法 ====================

    /**
     * 真实 SDK 设置限高。
     */
    private Map<String, Object> realSetHeightLimit(Dock dock, String sn,
                                                   Map<String, Object> args) {
        int heightLimit = getIntArg(args, PARAM_HEIGHT_LIMIT, -1);
        if (heightLimit <= 0) {
            return errorResult("Missing or invalid height_limit",
                    "Provide height_limit (positive integer, in meters).");
        }
        DeviceDebugModule module = dock.getDeviceDebugModule();
        awaitDebugOp(f -> module.setHeightLimit(sn, heightLimit, (err, res) -> completeFuture(f, err)));
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_SET_HEIGHT_LIMIT, "height_limit", heightLimit),
                "Height limit set to " + heightLimit + "m");
    }

    /**
     * 真实 SDK 设置限远。
     */
    private Map<String, Object> realSetDistanceLimit(Dock dock, String sn,
                                                     Map<String, Object> args) {
        int state = getIntArg(args, PARAM_DISTANCE_LIMIT_STATE, -1);
        int value = getIntArg(args, PARAM_DISTANCE_LIMIT_VALUE, -1);
        if (state < 0) {
            return errorResult("Missing distance_limit_state",
                    "Provide distance_limit_state (0=off, 1=on).");
        }
        if (value < 0) {
            return errorResult("Missing distance_limit_value",
                    "Provide distance_limit_value (meters).");
        }
        DeviceDebugModule module = dock.getDeviceDebugModule();
        awaitDebugOp(f -> module.setDistanceLimit(sn, state, value, (err, res) -> completeFuture(f, err)));
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_SET_DISTANCE_LIMIT,
                        "distance_limit_state", state, "distance_limit_value", value),
                "Distance limit set: state=" + state + ", value=" + value + "m");
    }

    /**
     * 真实 SDK SIM 卡槽切换。
     */
    private Map<String, Object> realSimSlotSwitch(Dock dock, String sn,
                                                  Map<String, Object> args) {
        String imei = getStringArg(args, PARAM_IMEI, null);
        int simSlot = getIntArg(args, PARAM_SIM_SLOT, -1);
        String deviceType = getStringArg(args, PARAM_DEVICE_TYPE, null);
        if (imei == null) {
            return errorResult("Missing parameter: imei", "Provide device IMEI.");
        }
        if (simSlot < 0) {
            return errorResult("Missing parameter: sim_slot", "Provide SIM slot index.");
        }
        if (deviceType == null) {
            return errorResult("Missing parameter: device_type", "Provide device type.");
        }
        DeviceDebugModule module = dock.getDeviceDebugModule();
        awaitDebugOp(f -> module.simSlotSwitch(sn, imei, simSlot, deviceType,
                (err, res) -> completeFuture(f, err)));
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_SIM_SLOT_SWITCH,
                        "imei", imei, "sim_slot", simSlot, "device_type", deviceType),
                "SIM slot switched: imei=" + imei + ", slot=" + simSlot);
    }

    /**
     * 真实 SDK eSIM 运营商切换。
     */
    private Map<String, Object> realEsimOperatorSwitch(Dock dock, String sn,
                                                       Map<String, Object> args) {
        String imei = getStringArg(args, PARAM_IMEI, null);
        int operatorType = getIntArg(args, PARAM_OPERATOR_TYPE, -1);
        String deviceType = getStringArg(args, PARAM_DEVICE_TYPE, null);
        if (imei == null) {
            return errorResult("Missing parameter: imei", "Provide device IMEI.");
        }
        if (operatorType < 0) {
            return errorResult("Missing parameter: operator_type", "Provide eSIM operator type.");
        }
        if (deviceType == null) {
            return errorResult("Missing parameter: device_type", "Provide device type.");
        }
        DeviceDebugModule module = dock.getDeviceDebugModule();
        awaitDebugOp(f -> module.esimOperatorSwitch(sn, imei, operatorType, deviceType,
                (err, res) -> completeFuture(f, err)));
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_ESIM_OPERATOR_SWITCH,
                        "imei", imei, "operator_type", operatorType, "device_type", deviceType),
                "eSIM operator switched: imei=" + imei + ", operator=" + operatorType);
    }

    // ==================== SDK 回调桥接辅助方法 ====================

    // ==================== RTK 定标私有方法 ====================

    /**
     * Mock 模式：RTK 手动定标参数校验 + 模拟返回。
     */
    private Map<String, Object> mockRtkCalibrateManual(String sn, Map<String, Object> args) {
        double longitude = getDoubleArg(args, PARAM_RTK_LONGITUDE, Double.NaN);
        double latitude = getDoubleArg(args, PARAM_RTK_LATITUDE, Double.NaN);
        double height = getDoubleArg(args, PARAM_RTK_HEIGHT, Double.NaN);
        if (Double.isNaN(longitude)) {
            return errorResult("Missing longitude", "Provide longitude (decimal degrees) for rtk_calibrate_manual.");
        }
        if (Double.isNaN(latitude)) {
            return errorResult("Missing latitude", "Provide latitude (decimal degrees) for rtk_calibrate_manual.");
        }
        if (Double.isNaN(height)) {
            return errorResult("Missing height", "Provide height (meters) for rtk_calibrate_manual.");
        }
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_RTK_CALIBRATE_MANUAL,
                        "longitude", longitude, "latitude", latitude, "height", height,
                        "bid", "mock-bid-manual"),
                "RTK manual calibration started (mock)");
    }

    /**
     * Mock 模式：RTK 网络定标参数校验 + 模拟返回。
     */
    private Map<String, Object> mockRtkCalibrateNetwork(String sn, Map<String, Object> args) {
        String host = getStringArg(args, PARAM_RTK_HOST, null);
        int port = getIntArg(args, PARAM_RTK_PORT, -1);
        String account = getStringArg(args, PARAM_RTK_ACCOUNT, null);
        String password = getStringArg(args, PARAM_RTK_PASSWORD, null);
        String mountPoint = getStringArg(args, PARAM_RTK_MOUNT_POINT, null);
        if (host == null) {
            return errorResult("Missing rtk_host", "Provide rtk_host (NTRIP server host) for rtk_calibrate_network.");
        }
        if (port < 0) {
            return errorResult("Missing rtk_port", "Provide rtk_port (NTRIP server port) for rtk_calibrate_network.");
        }
        if (account == null) {
            return errorResult("Missing rtk_account", "Provide rtk_account for rtk_calibrate_network.");
        }
        if (password == null) {
            return errorResult("Missing rtk_password", "Provide rtk_password for rtk_calibrate_network.");
        }
        if (mountPoint == null) {
            return errorResult("Missing rtk_mount_point", "Provide rtk_mount_point for rtk_calibrate_network.");
        }
        return successResult(
                Map.of("device_sn", sn, "action", ACTION_RTK_CALIBRATE_NETWORK,
                        "host", host, "port", port, "mount_point", mountPoint,
                        "bid", "mock-bid-network"),
                "RTK network calibration started (mock)");
    }

    /**
     * 真实模式：RTK 手动定标 — 构建参数并调用 SDK。
     *
     * @param dock  Dock 设备实例
     * @param sn    设备序列号（用于日志）
     * @param args  工具调用参数
     * @return 调用结果
     */
    private Map<String, Object> realRtkCalibrateManual(Dock dock, String sn, Map<String, Object> args) {
        double longitude = getDoubleArg(args, PARAM_RTK_LONGITUDE, Double.NaN);
        double latitude = getDoubleArg(args, PARAM_RTK_LATITUDE, Double.NaN);
        double height = getDoubleArg(args, PARAM_RTK_HEIGHT, Double.NaN);
        if (Double.isNaN(longitude)) {
            return errorResult("Missing longitude", "Provide longitude (decimal degrees) for rtk_calibrate_manual.");
        }
        if (Double.isNaN(latitude)) {
            return errorResult("Missing latitude", "Provide latitude (decimal degrees) for rtk_calibrate_manual.");
        }
        if (Double.isNaN(height)) {
            return errorResult("Missing height", "Provide height (meters) for rtk_calibrate_manual.");
        }

        ManulRtkCalibrationParams params = new ManulRtkCalibrationParams();
        params.setLongitude(longitude);
        params.setLatitude(latitude);
        params.setHeight(height);

        var rtkModule = dock.getRtkCalibrationModule();
        if (rtkModule == null) {
            return errorResult("RTK calibration module not available",
                    "This dock model may not support RTK calibration.");
        }

        CompletableFuture<RtkCalibrationResult> future = new CompletableFuture<>();
        rtkModule.calibrationByManual(params, (error, result) -> {
            if (error.isSuccess()) {
                future.complete(result);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getServerCode(), formatSdkError(error)));
            }
        });
        RtkCalibrationResult result = SdkCallbackBridge.await(future);

        return successResult(
                Map.of("device_sn", sn, "action", ACTION_RTK_CALIBRATE_MANUAL,
                        "longitude", longitude, "latitude", latitude, "height", height,
                        "bid", result != null && result.getBid() != null ? result.getBid() : "",
                        "message", result != null && result.getMessage() != null ? result.getMessage() : ""),
                "RTK manual calibration started");
    }

    /**
     * 真实模式：RTK 网络定标 — 构建参数并调用 SDK。
     *
     * @param dock  Dock 设备实例
     * @param sn    设备序列号（用于日志）
     * @param args  工具调用参数
     * @return 调用结果
     */
    private Map<String, Object> realRtkCalibrateNetwork(Dock dock, String sn, Map<String, Object> args) {
        String host = getStringArg(args, PARAM_RTK_HOST, null);
        int port = getIntArg(args, PARAM_RTK_PORT, -1);
        String account = getStringArg(args, PARAM_RTK_ACCOUNT, null);
        String password = getStringArg(args, PARAM_RTK_PASSWORD, null);
        String mountPoint = getStringArg(args, PARAM_RTK_MOUNT_POINT, null);
        int version = getIntArg(args, PARAM_RTK_VERSION, 1);
        if (host == null) {
            return errorResult("Missing rtk_host", "Provide rtk_host (NTRIP server host) for rtk_calibrate_network.");
        }
        if (port < 0) {
            return errorResult("Missing rtk_port", "Provide rtk_port (NTRIP server port) for rtk_calibrate_network.");
        }
        if (account == null) {
            return errorResult("Missing rtk_account", "Provide rtk_account for rtk_calibrate_network.");
        }
        if (password == null) {
            return errorResult("Missing rtk_password", "Provide rtk_password for rtk_calibrate_network.");
        }
        if (mountPoint == null) {
            return errorResult("Missing rtk_mount_point", "Provide rtk_mount_point for rtk_calibrate_network.");
        }

        NetworkRtkCalibrationParams params = new NetworkRtkCalibrationParams();
        params.setHost(host);
        params.setPort(port);
        params.setAccount(account);
        params.setPassword(password);
        params.setMountPoint(mountPoint);
        params.setVersion(version);

        var rtkModule = dock.getRtkCalibrationModule();
        if (rtkModule == null) {
            return errorResult("RTK calibration module not available",
                    "This dock model may not support RTK calibration.");
        }

        CompletableFuture<RtkCalibrationResult> future = new CompletableFuture<>();
        rtkModule.calibrationByNetwork(params, (error, result) -> {
            if (error.isSuccess()) {
                future.complete(result);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getServerCode(), formatSdkError(error)));
            }
        });
        RtkCalibrationResult result = SdkCallbackBridge.await(future);

        return successResult(
                Map.of("device_sn", sn, "action", ACTION_RTK_CALIBRATE_NETWORK,
                        "host", host, "port", port, "mount_point", mountPoint,
                        "bid", result != null && result.getBid() != null ? result.getBid() : "",
                        "message", result != null && result.getMessage() != null ? result.getMessage() : ""),
                "RTK network calibration started");
    }

    /**
     * 统一的异步转同步桥接方法：创建 CompletableFuture，执行 SDK 调用，同步等待结果。
     * 减少每个 action 重复编写 future 创建和 await 逻辑的样板代码。
     *
     * @param sdkCall 接受 future 并注册 SDK 回调的函数
     */
    private void awaitDebugOp(Consumer<CompletableFuture<Void>> sdkCall) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sdkCall.accept(future);
        SdkCallbackBridge.await(future);
    }

    /**
     * 根据 FlightHubError 完成或异常终止 future。
     * 成功时 complete(null)，失败时用 SdkException 包裹错误信息。
     *
     * @param future CompletableFuture 实例
     * @param error  SDK 回调返回的错误对象
     */
    private void completeFuture(CompletableFuture<Void> future, FlightHubError error) {
        if (error.isSuccess()) {
            future.complete(null);
        } else {
            future.completeExceptionally(
                    new SdkCallbackBridge.SdkException(error.getServerCode(), formatSdkError(error)));
        }
    }
}
