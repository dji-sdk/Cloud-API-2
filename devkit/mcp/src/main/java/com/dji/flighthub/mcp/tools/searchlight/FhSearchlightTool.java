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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * fh_searchlight — 探照灯高级控制工具。
 * 覆盖探照灯的模式设置、校准、微调、云台控制、类型和宽幅模式等高级功能。
 * 基础的开关功能通过 mode_set(mode=0/1) 实现。
 *
 * <p>安全等级：L1（强光发射类操作，需 elicitation 确认）
 * <p>前置条件：必须已获取飞行控制权（fh_control_acquire）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhSearchlightTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhSearchlightTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_searchlight";

    // ==================== 参数常量 ====================
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_POSITION = "position";
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_SAVED = "saved";
    private static final String PARAM_GIMBAL_CONTROL = "gimbal_control";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_WIDE_FIELD_MODE = "wide_field_mode";

    // ==================== Action 常量 ====================
    private static final String ACTION_MODE_SET = "mode_set";
    private static final String ACTION_CALIBRATION = "calibration";
    private static final String ACTION_FINE_TUNING = "fine_tuning";
    private static final String ACTION_GIMBAL_CONTROL = "gimbal_control";
    private static final String ACTION_TYPE_SET = "type_set";
    private static final String ACTION_WIDE_FIELD_MODE = "wide_field_mode";

    /** 有效 action 集合 */
    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_MODE_SET, ACTION_CALIBRATION, ACTION_FINE_TUNING,
            ACTION_GIMBAL_CONTROL, ACTION_TYPE_SET, ACTION_WIDE_FIELD_MODE
    );

    /** 有效 action 排序列表 */
    private static final List<String> VALID_ACTIONS_SORTED = VALID_ACTIONS.stream()
            .sorted().toList();

    /** 默认负载索引（探照灯类型） */
    private static final String DEFAULT_PAYLOAD_INDEX = "81-0-0";

    /** 探照灯模式：0=关闭 1=常亮 2=爆闪 3=快闪 4=交替 */
    private static final int MODE_MIN = 0;
    private static final int MODE_MAX = 4;

    /** payload_index 解析失败哨兵 */
    private static final int INVALID_PAYLOAD_INDEX = -1;
    private static final int PAYLOAD_INDEX_MIN_SEGMENTS = 2;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhSearchlightTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 探照灯操作为强光发射类，需要人工确认。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Advanced searchlight control: mode switching (off/constant/strobe/flash/alternate), "
                        + "calibration, fine-tuning, gimbal control, type and wide-field mode. "
                        + "Use mode_set(mode=0) for off, mode_set(mode=1) for constant on. "
                        + "Requires flight control authority.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Searchlight action to execute"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm searchlight command"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Payload index (e.g. '81-0-0'), default: primary searchlight"
        ));
        props.put(PARAM_MODE, Map.of(
                "type", "integer",
                "description", "Searchlight mode for mode_set: 0=off, 1=constant, 2=strobe, 3=fast-flash, 4=alternate"
        ));
        props.put(PARAM_POSITION, Map.of(
                "type", "integer",
                "description", "Fine-tuning position (for fine_tuning)"
        ));
        props.put(PARAM_VALUE, Map.of(
                "type", "number",
                "description", "Fine-tuning value (for fine_tuning)"
        ));
        props.put(PARAM_SAVED, Map.of(
                "type", "boolean",
                "description", "Whether to save fine-tuning value persistently (for fine_tuning)"
        ));
        props.put(PARAM_GIMBAL_CONTROL, Map.of(
                "type", "integer",
                "description", "Gimbal control value (for gimbal_control)"
        ));
        props.put(PARAM_TYPE, Map.of(
                "type", "integer",
                "description", "Searchlight type value (for type_set)"
        ));
        props.put(PARAM_WIDE_FIELD_MODE, Map.of(
                "type", "integer",
                "description", "Wide-field mode value (for wide_field_mode)"
        ));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_ACTION));
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

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L1 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 2. 校验 drone_sn
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 3. 校验飞行控制权
        Map<String, Object> controlError = requireControlAuthority(session, droneSn);
        if (controlError != null) {
            return controlError;
        }

        // 4. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        // 5. 人工确认（所有探照灯操作均为强光发射类）
        Map<String, Object> confirmError = requireHumanConfirm(args, action, droneSn,
                String.format("⚠️ 无人机 %s 即将执行探照灯操作（%s），是否确认？", droneSn, action));
        if (confirmError != null) {
            return confirmError;
        }

        // 6. 解析 payload_index
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        // 7. Mock 模式（含 workspace 为 null 的降级场景）
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return dispatchMock(droneSn, action, payloadIndex, args);
        }

        // 8. 真实 SDK 调用
        return executeWithRealSdk(droneSn, action, payloadIndex, args);
    }

    /**
     * Mock 模式分发。
     */
    private Map<String, Object> dispatchMock(String sn, String action, String idx, Map<String, Object> args) {
        return switch (action) {
            case ACTION_MODE_SET -> {
                int mode = getIntArg(args, PARAM_MODE, -1);
                if (mode < MODE_MIN || mode > MODE_MAX) {
                    yield errorResult("Invalid mode: " + mode,
                            "Must be 0~4 (0=off, 1=constant, 2=strobe, 3=fast-flash, 4=alternate).");
                }
                yield successResult(
                        Map.of("drone_sn", sn, "action", action, "mode", mode, "payload_index", idx),
                        "Searchlight mode set to " + describeModeValue(mode));
            }
            case ACTION_CALIBRATION -> successResult(
                    Map.of("drone_sn", sn, "action", action, "payload_index", idx, "status", "started"),
                    "Searchlight calibration started");
            case ACTION_FINE_TUNING -> {
                int position = getIntArg(args, PARAM_POSITION, -1);
                double value = getDoubleArg(args, PARAM_VALUE, Double.NaN);
                boolean saved = getBooleanArg(args, PARAM_SAVED);
                if (position < 0) {
                    yield errorResult("Missing or invalid position parameter",
                            "Provide position (integer >= 0) for fine-tuning.");
                }
                if (Double.isNaN(value)) {
                    yield errorResult("Missing value parameter",
                            "Provide value (number) for fine-tuning.");
                }
                yield successResult(
                        Map.of("drone_sn", sn, "action", action, "position", position,
                                "value", value, "saved", saved),
                        String.format("Fine-tuning set: position=%d, value=%.2f, saved=%s",
                                position, value, saved));
            }
            case ACTION_GIMBAL_CONTROL -> {
                if (!args.containsKey(PARAM_GIMBAL_CONTROL)) {
                    yield errorResult("Missing gimbal_control parameter",
                            "Provide gimbal_control value (supports negative for counterclockwise).");
                }
                int ctrl = getIntArg(args, PARAM_GIMBAL_CONTROL, 0);
                yield successResult(
                        Map.of("drone_sn", sn, "action", action, "gimbal_control", ctrl),
                        "Searchlight gimbal control set to " + ctrl);
            }
            case ACTION_TYPE_SET -> {
                int type = getIntArg(args, PARAM_TYPE, -1);
                if (type < 0) {
                    yield errorResult("Missing type parameter",
                            "Provide type value for searchlight type.");
                }
                yield successResult(
                        Map.of("drone_sn", sn, "action", action, "type", type),
                        "Searchlight type set to " + type);
            }
            case ACTION_WIDE_FIELD_MODE -> {
                int wfm = getIntArg(args, PARAM_WIDE_FIELD_MODE, -1);
                if (wfm < 0) {
                    yield errorResult("Missing wide_field_mode parameter",
                            "Provide wide_field_mode value.");
                }
                yield successResult(
                        Map.of("drone_sn", sn, "action", action, "wide_field_mode", wfm),
                        "Wide-field mode set to " + wfm);
            }
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    /**
     * 真实 SDK 调用分发。
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(
            String droneSn, String action, String payloadIndex, Map<String, Object> args) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            int intIdx = parseIntPayloadIndex(payloadIndex);
            if (intIdx == INVALID_PAYLOAD_INDEX) {
                return errorResult("Invalid payload_index format: " + payloadIndex,
                        "Expected format: 'type-index-subIndex' (e.g. '81-0-0').");
            }

            return switch (action) {
                case ACTION_MODE_SET -> {
                    int mode = getIntArg(args, PARAM_MODE, -1);
                    if (mode < MODE_MIN || mode > MODE_MAX) {
                        yield errorResult("Invalid mode: " + mode,
                                "Must be 0~4.");
                    }
                    SdkCallbackBridge.awaitVoid(cb -> drone.getPsdkControlModule().searchlightModeSet(intIdx, mode, cb));
                    yield successResult(
                            Map.of("drone_sn", droneSn, "action", action, "mode", mode),
                            "Searchlight mode set to " + describeModeValue(mode));
                }
                case ACTION_CALIBRATION -> {
                    SdkCallbackBridge.awaitVoid(cb -> drone.getPsdkControlModule().searchlightCalibration(intIdx, cb));
                    yield successResult(
                            Map.of("drone_sn", droneSn, "action", action, "status", "started"),
                            "Searchlight calibration started");
                }
                case ACTION_FINE_TUNING -> {
                    int position = getIntArg(args, PARAM_POSITION, -1);
                    double value = getDoubleArg(args, PARAM_VALUE, Double.NaN);
                    boolean saved = getBooleanArg(args, PARAM_SAVED);
                    if (position < 0 || Double.isNaN(value)) {
                        yield errorResult("Missing position or value",
                                "Provide position and value for fine-tuning.");
                    }
                    SdkCallbackBridge.awaitVoid(
                            cb -> drone.getPsdkControlModule().searchlightFineTuningSet(intIdx, position, value, saved, cb));
                    yield successResult(
                            Map.of("drone_sn", droneSn, "action", action,
                                    "position", position, "value", value, "saved", saved),
                            "Fine-tuning applied");
                }
                case ACTION_GIMBAL_CONTROL -> {
                    if (!args.containsKey(PARAM_GIMBAL_CONTROL)) {
                        yield errorResult("Missing gimbal_control parameter",
                                "Provide gimbal_control value (supports negative for counterclockwise).");
                    }
                    int ctrl = getIntArg(args, PARAM_GIMBAL_CONTROL, 0);
                    SdkCallbackBridge.awaitVoid(
                            cb -> drone.getPsdkControlModule().searchlightLightGimbalControlSet(intIdx, ctrl, cb));
                    yield successResult(
                            Map.of("drone_sn", droneSn, "action", action, "gimbal_control", ctrl),
                            "Gimbal control set");
                }
                case ACTION_TYPE_SET -> {
                    int type = getIntArg(args, PARAM_TYPE, -1);
                    if (type < 0) {
                        yield errorResult("Missing type parameter", "Provide type value.");
                    }
                    SdkCallbackBridge.awaitVoid(cb -> drone.getPsdkControlModule().searchlightTypeSet(intIdx, type, cb));
                    yield successResult(
                            Map.of("drone_sn", droneSn, "action", action, "type", type),
                            "Searchlight type set");
                }
                case ACTION_WIDE_FIELD_MODE -> {
                    int wfm = getIntArg(args, PARAM_WIDE_FIELD_MODE, -1);
                    if (wfm < 0) {
                        yield errorResult("Missing wide_field_mode parameter", "Provide value.");
                    }
                    SdkCallbackBridge.awaitVoid(
                            cb -> drone.getPsdkControlModule().searchlightWideFieldModeSet(intIdx, wfm, cb));
                    yield successResult(
                            Map.of("drone_sn", droneSn, "action", action, "wide_field_mode", wfm),
                            "Wide-field mode set");
                }
                default -> errorResult("Unhandled action: " + action, "Report this bug.");
            };

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("Searchlight timeout: drone={}, action={}: {}", droneSn, action, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("Searchlight SDK error: drone={}, action={}: [{}] {}",
                    droneSn, action, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check and retry.");
        } catch (Exception e) {
            log.error("Searchlight error: drone={}, action={}: {}", droneSn, action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(), "Check drone connectivity.");
        }
    }

    /**
     * 从 "type-index-subIndex" 格式解析 int 索引。
     */
    private static int parseIntPayloadIndex(String payloadIndex) {
        if (payloadIndex == null || payloadIndex.isBlank()) {
            return INVALID_PAYLOAD_INDEX;
        }
        String[] parts = payloadIndex.split("-");
        if (parts.length < PAYLOAD_INDEX_MIN_SEGMENTS) {
            return INVALID_PAYLOAD_INDEX;
        }
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return INVALID_PAYLOAD_INDEX;
        }
    }

    /**
     * 将模式数值转为人可读描述。
     */
    private static String describeModeValue(int mode) {
        return switch (mode) {
            case 0 -> "off";
            case 1 -> "constant";
            case 2 -> "strobe";
            case 3 -> "fast-flash";
            case 4 -> "alternate";
            default -> "unknown(" + mode + ")";
        };
    }
}
