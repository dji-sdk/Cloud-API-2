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

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.FlightOutOfControlActionResult;
import com.dji.flighthubsdk.bean.FlightRthAltitudeResult;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightSettingsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_setting — 修改飞行器持久化参数（返航高度 / 失控动作）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>set_rth_altitude — 设置返航高度（米）</li>
 *   <li>set_out_of_control_action — 设置失控时的行为</li>
 * </ul>
 *
 * <p>安全等级：L1（{@link SecurityLevel#REVERSIBLE}）——
 * 这些设置会改变飞行器参数，但不会直接触发飞行，也不是实时飞控指令，
 * 属于可逆操作，需 confirm=true。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhSettingTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhSettingTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_setting";

    /** action: 设置返航高度 */
    private static final String ACTION_SET_RTH_ALTITUDE = "set_rth_altitude";

    /** action: 设置失控动作 */
    private static final String ACTION_SET_OUT_OF_CONTROL_ACTION = "set_out_of_control_action";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_SET_RTH_ALTITUDE, ACTION_SET_OUT_OF_CONTROL_ACTION);

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_ALTITUDE = "altitude";
    private static final String PARAM_OUT_OF_CONTROL_ACTION = "out_of_control_action";
    private static final String PARAM_CONFIRM = "confirm";

    /** 返航高度最小值（米） */
    private static final int MIN_RTH_ALTITUDE = 20;

    /** 返航高度最大值（米） */
    private static final int MAX_RTH_ALTITUDE = 1500;

    /**
     * 失控动作有效值，与 {@code FhWaylineUploadTool.VALID_OOC_ACTIONS} 保持一致
     * （return_home / hover / continue 三种，小写下划线风格）。
     */
    private static final List<String> VALID_OOC_ACTIONS = List.of("return_home", "hover", "continue");

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhSettingTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    /**
     * 返回本工具的 MCP 协议定义。
     *
     * @return MCP 工具定义 Map
     */
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Change persistent aircraft flight parameters (does not trigger flight by itself). "
                + "Actions: set_rth_altitude (return-to-home altitude in meters, "
                + MIN_RTH_ALTITUDE + "-" + MAX_RTH_ALTITUDE + "), "
                + "set_out_of_control_action (behavior when the drone loses signal: "
                + "return_home, hover, or continue). Write operation, requires confirm=true.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Setting to change"
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number"
        ));
        props.put(PARAM_ALTITUDE, Map.of(
                "type", "integer",
                "description", "Return-to-home altitude in meters ("
                        + MIN_RTH_ALTITUDE + "~" + MAX_RTH_ALTITUDE + ", required for set_rth_altitude)"
        ));
        props.put(PARAM_OUT_OF_CONTROL_ACTION, Map.of(
                "type", "string",
                "enum", VALID_OOC_ACTIONS,
                "description", "Action when drone loses signal (required for set_out_of_control_action)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Set true to confirm this write operation"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_DRONE_SN));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行设置变更操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action", "Valid actions: " + VALID_ACTIONS);
        }

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        if (ACTION_SET_RTH_ALTITUDE.equals(action)) {
            return executeSetRthAltitude(args, droneSn);
        }
        return executeSetOutOfControlAction(args, droneSn);
    }

    // ==================== set_rth_altitude ====================

    /**
     * 处理 set_rth_altitude action：校验高度范围、人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args    输入参数
     * @param droneSn 目标飞行器 SN
     * @return 执行结果
     */
    private Map<String, Object> executeSetRthAltitude(Map<String, Object> args, String droneSn) {
        int altitude = getIntArg(args, PARAM_ALTITUDE, Integer.MIN_VALUE);
        if (altitude < MIN_RTH_ALTITUDE || altitude > MAX_RTH_ALTITUDE) {
            return errorResult(
                    "Invalid or missing altitude: must be between "
                            + MIN_RTH_ALTITUDE + " and " + MAX_RTH_ALTITUDE + " meters",
                    "Current value: " + altitude);
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_SET_RTH_ALTITUDE, droneSn,
                "Set RTH altitude to " + altitude + "m for " + droneSn);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] set_rth_altitude: droneSn={}, altitude={}", TOOL_NAME, droneSn, altitude);

        return sdkService.isMockMode()
                ? setRthAltitudeInMockMode(droneSn, altitude)
                : setRthAltitudeWithRealSdk(droneSn, altitude);
    }

    /**
     * Mock 模式下设置返航高度。
     *
     * @param droneSn  目标飞行器 SN
     * @param altitude 返航高度（米）
     * @return 成功响应
     */
    private Map<String, Object> setRthAltitudeInMockMode(String droneSn, int altitude) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("rth_altitude", altitude);
        data.put("_mock", true);
        return successResult(data, "RTH altitude updated.");
    }

    /**
     * 通过真实 SDK 设置返航高度。
     *
     * @param droneSn  目标飞行器 SN
     * @param altitude 返航高度（米）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> setRthAltitudeWithRealSdk(String droneSn, int altitude) {
        try {
            FlightSettingsModule module = getFlightSettingsModule(droneSn);
            if (module == null) {
                return errorResult("FlightSettingsModule not available",
                        "Drone may be offline. Use fh_device_list to check, or call fh_connect first.");
            }

            CompletableFuture<FlightRthAltitudeResult> future = new CompletableFuture<>();
            module.setRthAltitude(altitude, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "setRthAltitude failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            FlightRthAltitudeResult result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("rth_altitude", result != null ? result.getRthAltitude() : altitude);
            return successResult(data, "RTH altitude updated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_rth_altitude timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_rth_altitude SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] set_rth_altitude error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set RTH altitude: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== set_out_of_control_action ====================

    /**
     * 处理 set_out_of_control_action action：校验取值、人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args    输入参数
     * @param droneSn 目标飞行器 SN
     * @return 执行结果
     */
    private Map<String, Object> executeSetOutOfControlAction(Map<String, Object> args, String droneSn) {
        String oocAction = getStringArg(args, PARAM_OUT_OF_CONTROL_ACTION, null);
        if (oocAction == null || !VALID_OOC_ACTIONS.contains(oocAction)) {
            return errorResult("Invalid or missing out_of_control_action",
                    "Must be one of: " + VALID_OOC_ACTIONS);
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_SET_OUT_OF_CONTROL_ACTION, droneSn,
                "Set out-of-control action to '" + oocAction + "' for " + droneSn);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] set_out_of_control_action: droneSn={}, action={}", TOOL_NAME, droneSn, oocAction);

        return sdkService.isMockMode()
                ? setOutOfControlActionInMockMode(droneSn, oocAction)
                : setOutOfControlActionWithRealSdk(droneSn, oocAction);
    }

    /**
     * Mock 模式下设置失控动作。
     *
     * @param droneSn   目标飞行器 SN
     * @param oocAction 失控动作取值
     * @return 成功响应
     */
    private Map<String, Object> setOutOfControlActionInMockMode(String droneSn, String oocAction) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("out_of_control_action", oocAction);
        data.put("_mock", true);
        return successResult(data, "Out-of-control action updated.");
    }

    /**
     * 通过真实 SDK 设置失控动作。
     *
     * <p>注意：SDK 的 String 取值拼写（return_home / hover / continue）沿用
     * {@code FhWaylineUploadTool.VALID_OOC_ACTIONS} 的既有约定直传给
     * {@code FlightSettingsModule.setOutOfControlAction}，接入真机前建议先用最小
     * e2e 验证一次实际拼写是否与底层 C++ SDK 的 {@code OutOfControlAction}
     * 枚举（HOVER=0, RETURN_HOME=1, CONTINUE=2）字符串映射一致，再用于生产环境。
     *
     * @param droneSn   目标飞行器 SN
     * @param oocAction 失控动作取值
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> setOutOfControlActionWithRealSdk(String droneSn, String oocAction) {
        try {
            FlightSettingsModule module = getFlightSettingsModule(droneSn);
            if (module == null) {
                return errorResult("FlightSettingsModule not available",
                        "Drone may be offline. Use fh_device_list to check, or call fh_connect first.");
            }

            CompletableFuture<FlightOutOfControlActionResult> future = new CompletableFuture<>();
            module.setOutOfControlAction(oocAction, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "setOutOfControlAction failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            FlightOutOfControlActionResult result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("out_of_control_action", result != null ? result.getOutOfControlAction() : oocAction);
            return successResult(data, "Out-of-control action updated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_out_of_control_action timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_out_of_control_action SDK error: [{}] {}",
                    TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] set_out_of_control_action error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set out-of-control action: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取指定飞行器的 FlightSettingsModule 实例。
     *
     * @param droneSn 目标飞行器 SN
     * @return FlightSettingsModule，飞行器不可用时返回 null
     */
    private FlightSettingsModule getFlightSettingsModule(String droneSn) {
        Drone drone = sdkService.getDrone(droneSn);
        if (drone == null) {
            return null;
        }
        return drone.getFlightSettingsModule();
    }
}
