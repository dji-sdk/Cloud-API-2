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

package com.dji.flighthub.mcp.tools.wayline;

import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo;
import com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskParams;
import com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult;
import com.dji.flighthubsdk.bean.InFlightWaylineTaskDetail;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.InFlightWaylineModule;
import com.dji.flighthubsdk.value.InFlightWaylineControlAction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 封装 {@code Drone.getInFlightWaylineModule()}，向已在空中的无人机下发/控制/查询航线任务。
 *
 * <p>与 {@code fh_wayline_control}（{@code Workspace.getFlightTaskModule().updateFlightTaskStatus}）
 * 并存：后者改平台侧任务状态，本工具直接对飞行中的飞机下发/控制航线，语义不同。</p>
 *
 * <p>混合安全等级工具：类级声明 L0（READ_ONLY，门控基线），
 * {@code create}/{@code control} 内部显式调用 {@link #enforceSecurityLevel(SecurityLevel)}
 * 升级到 L2（FLIGHT），{@link #requiresHumanConfirmation()} 覆写为 {@code false}
 * 以避免传输层硬性拦截只读的 {@code status} action。</p>
 *
 * <p>注意：{@code requiresHumanConfirmation() == false} 意味着传输层不做统一 confirm 拦截，
 * {@code create}/{@code control} 必须在业务方法内调用 {@link #requireHumanConfirm} 自行把关。</p>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhInFlightWaylineTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhInFlightWaylineTool.class);

    private static final String TOOL_NAME = "fh_inflight_wayline";

    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_WAYLINE_UUID = "wayline_uuid";
    private static final String PARAM_RTH_ALTITUDE = "rth_altitude";
    private static final String PARAM_CONTROL_ACTION = "control_action";
    private static final String PARAM_TASK_NAME = "task_name";
    private static final String PARAM_FAST_RECONSTRUCTION = "fast_reconstruction";
    private static final String PARAM_OUT_OF_CONTROL_ACTION = "out_of_control_action";
    private static final String PARAM_EXIT_WAYLINE_WHEN_RC_LOST = "exit_wayline_when_rc_lost";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_DRY_RUN = "dry_run";

    private static final String ACTION_CREATE = "create";
    private static final String ACTION_CONTROL = "control";
    private static final String ACTION_STATUS = "status";

    /** 有序列表：既用于参数校验，也直接作为 JSON Schema 的 enum 输出（保证顺序稳定）。 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_CREATE, ACTION_CONTROL, ACTION_STATUS);

    /** SDK 约定：-1 表示「不下发该字段」，不能用 0 代替（0 本身是合法枚举值）。 */
    private static final int SENTINEL_UNSET = -1;

    /** 飞控要求返航高度 >= 2 米（SDK message.h 注释）。 */
    private static final int MIN_RTH_ALTITUDE = 2;

    /**
     * MCP 侧防御性上限，与 {@code fh_takeoff}/{@code fh_flyto} 的高度范围（2~10000 米）保持一致。
     * 注意这是本项目的输入护栏，SDK 本身只约束下限 >= 2 米。
     */
    private static final int MAX_RTH_ALTITUDE = 10000;

    /**
     * 飞控失控动作（SDK {@code CreateInFlightWaylineTaskParams.out_of_control_action}）：
     * 0-返航，1-悬停，2-降落；-1 表示未设置。
     */
    private static final List<Integer> VALID_OUT_OF_CONTROL_ACTIONS = List.of(SENTINEL_UNSET, 0, 1, 2);

    /**
     * 航线失控动作（SDK {@code CreateInFlightWaylineTaskParams.exit_wayline_when_rc_lost}）：
     * 0-继续执行航线，1-执行飞控失控动作；-1 表示未设置。
     */
    private static final List<Integer> VALID_EXIT_WAYLINE_WHEN_RC_LOST = List.of(SENTINEL_UNSET, 0, 1);

    private static final String CONTROL_PAUSE = "pause";
    private static final String CONTROL_RESUME = "resume";
    private static final String CONTROL_CANCEL = "cancel";

    /** 有序列表：用于 Schema enum 与错误提示，避免 Map.of 的迭代顺序不稳定。 */
    private static final List<String> VALID_CONTROL_ACTIONS = List.of(CONTROL_PAUSE, CONTROL_RESUME, CONTROL_CANCEL);

    private static final Map<String, InFlightWaylineControlAction> CONTROL_ACTION_MAP = Map.of(
            CONTROL_PAUSE, InFlightWaylineControlAction.PAUSE,
            CONTROL_RESUME, InFlightWaylineControlAction.RESUME,
            CONTROL_CANCEL, InFlightWaylineControlAction.CANCEL);

    private static final String MOCK_FLIGHT_TASK_ID_PREFIX = "mock-flight-task-";
    private static final String MOCK_TASK_ID_PREFIX = "mock-ifw-";

    private final SdkService sdkService;

    public FhInFlightWaylineTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 声明工具级静态安全等级：READ_ONLY（L0）。
     *
     * <p>派发层按最低声明等级放行，{@code create}/{@code control} 在各自业务方法内
     * 调用 {@link #enforceSecurityLevel(SecurityLevel)} 升级到 L2（FLIGHT），
     * per-action 门控只会抬高实际要求，不会降低；{@code status} 保持只读可查。</p>
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Manage in-flight wayline tasks for an airborne drone: create a new in-flight wayline, "
                + "pause/resume/cancel the active one, or query its status. Unlike fh_wayline_control "
                + "(platform-side task status), this tool directly commands the drone while it is flying.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (must be airborne)"));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "create: dispatch new in-flight wayline | control: pause/resume/cancel "
                        + "active task | status: query active task"));
        props.put(PARAM_WAYLINE_UUID, Map.of(
                "type", "string",
                "description", "Wayline file UUID (required for create)"));
        props.put(PARAM_RTH_ALTITUDE, Map.of(
                "type", "integer",
                "description", "Return-to-home altitude in meters, >= " + MIN_RTH_ALTITUDE
                        + " (required for create)"));
        props.put(PARAM_CONTROL_ACTION, Map.of(
                "type", "string",
                "enum", VALID_CONTROL_ACTIONS,
                "description", "pause/resume/cancel the active in-flight wayline task (required for control)"));
        props.put(PARAM_TASK_NAME, Map.of(
                "type", "string",
                "description", "Optional display name for the task"));
        props.put(PARAM_FAST_RECONSTRUCTION, Map.of(
                "type", "boolean",
                "description", "Optional: enable fast 3D reconstruction"));
        props.put(PARAM_OUT_OF_CONTROL_ACTION, Map.of(
                "type", "integer",
                "description", "Optional flight-controller action when the control signal is lost: "
                        + "0=return_home, 1=hover, 2=land. Omit to keep the aircraft default."));
        props.put(PARAM_EXIT_WAYLINE_WHEN_RC_LOST, Map.of(
                "type", "integer",
                "description", "Optional wayline behaviour when RC signal is lost: 0=continue the wayline, "
                        + "1=execute the out-of-control action. Omit to keep the aircraft default."));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Required for create/control (not needed for status)"));
        props.put(PARAM_DRY_RUN, Map.of(
                "type", "boolean",
                "description", "If true, validate parameters without executing. Returns preview of what "
                        + "would happen."));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 混合安全等级工具：传输层不做统一 confirm 拦截，confirm 由 create/control 业务方法自行把关，
     * 避免只读的 status action 也被强制要求 confirm。
     *
     * @return 恒为 {@code false}
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "drone_sn is required to locate the Drone instance.");
        }

        // action 已在上方按 VALID_ACTIONS 校验过，这里用 if/else 而非 switch，避免出现不可达的 default 分支
        if (ACTION_CREATE.equals(action)) {
            return handleCreate(args, droneSn);
        }
        if (ACTION_CONTROL.equals(action)) {
            return handleControl(args, droneSn);
        }
        return handleStatus(droneSn);
    }

    private Map<String, Object> handleCreate(Map<String, Object> args, String droneSn) {
        CreateValidation validation = validateCreateParams(args);
        if (validation.error() != null) {
            return validation.error();
        }
        CreateInFlightWaylineTaskParams params = validation.params();

        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.FLIGHT);
        if (denied != null) {
            return denied;
        }

        if (isDryRun(args)) {
            return dryRunResult("Would create in-flight wayline task on drone " + droneSn,
                    buildDryRunPreview(TOOL_NAME, ACTION_CREATE, droneSn,
                            Map.of("wayline_uuid", params.getWaylineUuid(), "rth_altitude", params.getRthAltitude()),
                            List.of("session_active", "security_level_allowed", "params_valid"),
                            SecurityLevel.FLIGHT, true));
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "create_inflight_wayline", droneSn,
                "即将给飞行中的无人机 " + droneSn + " 下发新航线，确认？");
        if (confirmError != null) {
            return confirmError;
        }

        // mock 模式下 SdkService.getDrone() 恒返回 null，必须在解析 drone 之前分流
        if (sdkService.isMockMode()) {
            return createInMockMode(params);
        }

        Drone drone = sdkService.getDrone(droneSn);
        if (drone == null) {
            return droneNotFoundError(droneSn);
        }
        return runRealSdk("createInFlightWaylineTask", () -> dispatchRealSdkCreate(drone, params));
    }

    private Map<String, Object> handleControl(Map<String, Object> args, String droneSn) {
        String controlActionStr = getStringArg(args, PARAM_CONTROL_ACTION, null);
        if (controlActionStr == null) {
            return errorResult("Missing required parameter: control_action",
                    "control_action must be one of: " + VALID_CONTROL_ACTIONS);
        }
        InFlightWaylineControlAction controlAction = CONTROL_ACTION_MAP.get(controlActionStr.toLowerCase(Locale.ROOT));
        if (controlAction == null) {
            return errorResult("Invalid control_action: " + controlActionStr,
                    "control_action must be one of: " + VALID_CONTROL_ACTIONS);
        }

        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.FLIGHT);
        if (denied != null) {
            return denied;
        }

        if (isDryRun(args)) {
            return dryRunResult("Would " + controlActionStr + " in-flight wayline task on drone " + droneSn,
                    buildDryRunPreview(TOOL_NAME, ACTION_CONTROL, droneSn,
                            Map.of("control_action", controlActionStr),
                            List.of("session_active", "security_level_allowed", "params_valid"),
                            SecurityLevel.FLIGHT, true));
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "control_inflight_wayline", droneSn,
                "即将对飞行中的无人机 " + droneSn + " 执行 " + controlActionStr + " 操作，确认？");
        if (confirmError != null) {
            return confirmError;
        }

        // mock 模式下 SdkService.getDrone() 恒返回 null，必须在解析 drone 之前分流
        if (sdkService.isMockMode()) {
            return controlInMockMode(controlActionStr);
        }

        Drone drone = sdkService.getDrone(droneSn);
        if (drone == null) {
            return droneNotFoundError(droneSn);
        }
        return runRealSdk("controlInFlightWaylineTask", () -> dispatchRealSdkControl(drone, controlAction));
    }

    private Map<String, Object> handleStatus(String droneSn) {
        // mock 模式下 SdkService.getDrone() 恒返回 null，必须在解析 drone 之前分流
        if (sdkService.isMockMode()) {
            return statusInMockMode();
        }

        Drone drone = sdkService.getDrone(droneSn);
        if (drone == null) {
            return droneNotFoundError(droneSn);
        }
        return runRealSdk("getActiveInFlightWaylineTask", () -> dispatchRealSdkStatus(drone));
    }

    private CreateValidation validateCreateParams(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return CreateValidation.fail(errorResult("Missing required parameter: wayline_uuid",
                    "wayline_uuid is required for create action."));
        }
        if (!args.containsKey(PARAM_RTH_ALTITUDE) || args.get(PARAM_RTH_ALTITUDE) == null) {
            return CreateValidation.fail(errorResult("Missing required parameter: rth_altitude",
                    "rth_altitude is required for create action (meters, >= " + MIN_RTH_ALTITUDE + ")."));
        }
        Integer rthAltitude = parseStrictInt(args, PARAM_RTH_ALTITUDE);
        if (rthAltitude == null) {
            return CreateValidation.fail(errorResult("Invalid rth_altitude: " + args.get(PARAM_RTH_ALTITUDE),
                    "rth_altitude must be an integer between " + MIN_RTH_ALTITUDE
                            + " and " + MAX_RTH_ALTITUDE + " meters."));
        }
        if (rthAltitude < MIN_RTH_ALTITUDE || rthAltitude > MAX_RTH_ALTITUDE) {
            return CreateValidation.fail(errorResult("Invalid rth_altitude: " + rthAltitude,
                    "rth_altitude must be between " + MIN_RTH_ALTITUDE + " and " + MAX_RTH_ALTITUDE + " meters."));
        }

        Integer outOfControlAction = SENTINEL_UNSET;
        if (args.get(PARAM_OUT_OF_CONTROL_ACTION) != null) {
            outOfControlAction = parseStrictInt(args, PARAM_OUT_OF_CONTROL_ACTION);
            if (outOfControlAction == null || !VALID_OUT_OF_CONTROL_ACTIONS.contains(outOfControlAction)) {
                return CreateValidation.fail(errorResult(
                        "Invalid out_of_control_action: " + args.get(PARAM_OUT_OF_CONTROL_ACTION),
                        "out_of_control_action must be one of: " + VALID_OUT_OF_CONTROL_ACTIONS));
            }
        }

        Integer exitWaylineWhenRcLost = SENTINEL_UNSET;
        if (args.get(PARAM_EXIT_WAYLINE_WHEN_RC_LOST) != null) {
            exitWaylineWhenRcLost = parseStrictInt(args, PARAM_EXIT_WAYLINE_WHEN_RC_LOST);
            if (exitWaylineWhenRcLost == null || !VALID_EXIT_WAYLINE_WHEN_RC_LOST.contains(exitWaylineWhenRcLost)) {
                return CreateValidation.fail(errorResult(
                        "Invalid exit_wayline_when_rc_lost: " + args.get(PARAM_EXIT_WAYLINE_WHEN_RC_LOST),
                        "exit_wayline_when_rc_lost must be one of: " + VALID_EXIT_WAYLINE_WHEN_RC_LOST));
            }
        }

        CreateInFlightWaylineTaskParams params = new CreateInFlightWaylineTaskParams();
        params.setWaylineUuid(waylineUuid);
        params.setRthAltitude(rthAltitude);
        params.setWaylinePrecisionType(SENTINEL_UNSET);
        params.setRthMode(SENTINEL_UNSET);
        params.setOutOfControlAction(outOfControlAction);
        params.setExitWaylineWhenRcLost(exitWaylineWhenRcLost);
        String taskName = getStringArg(args, PARAM_TASK_NAME, null);
        if (taskName != null) {
            params.setTaskName(taskName);
        }
        if (args.get(PARAM_FAST_RECONSTRUCTION) instanceof Boolean fastReconstruction) {
            params.setFastReconstruction(fastReconstruction);
        }
        return CreateValidation.ok(params);
    }

    /**
     * 严格解析整型参数：只接受 {@link Number} 或可解析为整数的字符串。
     *
     * <p>不使用 {@link #getIntArg}，因为它在解析失败时会静默回退到默认值，
     * 导致 {@code rth_altitude="abc"} 之类的非法输入被当成合法值下发。</p>
     *
     * @param args 参数 Map
     * @param key  参数名
     * @return 解析后的整数；参数缺失或无法解析时返回 {@code null}
     */
    private static Integer parseStrictInt(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.valueOf(text.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> dispatchRealSdkCreate(Drone drone, CreateInFlightWaylineTaskParams params) {
        InFlightWaylineModule module = drone.getInFlightWaylineModule();
        CompletableFuture<CreateInFlightWaylineTaskResult> future = new CompletableFuture<>();
        module.createInFlightWaylineTask(params, (result, error) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(error.getHttpCode(),
                        "createInFlightWaylineTask failed: " + formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        CreateInFlightWaylineTaskResult result = SdkCallbackBridge.await(future);
        if (result == null) {
            throw new SdkCallbackBridge.SdkException(-1,
                    "createInFlightWaylineTask returned no result (SDK callback payload was null).");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("flight_task_id", result.getFlightTaskId());
        data.put("in_flight_wayline_task_id", result.getInFlightWaylineTaskId());
        data.put("wayline_uuid", params.getWaylineUuid());
        return successResult(data, "In-flight wayline task created.");
    }

    private Map<String, Object> dispatchRealSdkControl(Drone drone, InFlightWaylineControlAction controlAction) {
        InFlightWaylineModule module = drone.getInFlightWaylineModule();
        // SDK 的 InFlightWaylineControlCallback 与 ResultCallbackWithError 是签名相同的两个独立接口，
        // 这里通过方法引用把后者适配给前者，复用 awaitVoid 的「future + 回调 + await」模板
        SdkCallbackBridge.awaitVoid(callback ->
                module.controlInFlightWaylineTask(controlAction, callback::onResult));

        String actionName = controlAction.name().toLowerCase(Locale.ROOT);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("control_action", actionName);
        return successResult(data, "In-flight wayline task " + actionName + " requested.");
    }

    private Map<String, Object> dispatchRealSdkStatus(Drone drone) {
        InFlightWaylineModule module = drone.getInFlightWaylineModule();
        CompletableFuture<ActiveInFlightWaylineTaskInfo> future = new CompletableFuture<>();
        module.getActiveInFlightWaylineTask((info, error) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(error.getHttpCode(),
                        "getActiveInFlightWaylineTask failed: " + formatSdkError(error)));
                return;
            }
            future.complete(info);
        });
        ActiveInFlightWaylineTaskInfo info = SdkCallbackBridge.await(future);

        boolean active = isActiveTask(info);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("active", active);
        if (active) {
            data.put("flight_id", info.getFlightId());
            data.put("flight_type", info.getFlightType());
            data.put("sn", info.getSn());
            InFlightWaylineTaskDetail detail = info.getInFlightTask();
            if (detail != null) {
                data.put("in_flight_task", mapTaskDetail(detail));
            }
            if (info.getFlightTaskData() != null) {
                data.put("flight_task_data", info.getFlightTaskData());
            }
        }
        return successResult(data, active ? "Active in-flight wayline task found." : "No active in-flight wayline task.");
    }

    /**
     * 判断 SDK 回调是否真的带回了一个活跃任务。
     *
     * <p>不能以 {@code info.getInFlightTask() != null} 作为判据：SDK 的
     * {@code ActiveInFlightWaylineTaskInfo} 构造器与 setter 都会把 null 替换成
     * 空的 {@code InFlightWaylineTaskDetail}，因此该字段永远非 null。服务端返回空 data 时
     * SDK 仍会构造一个全默认值的 info（{@code parse_common_response_data(..., required=false)}），
     * 这里改用「飞行主任务 ID 或子任务 uuid 非空」作为判据。</p>
     *
     * @param info SDK 回调带回的任务信息
     * @return 存在活跃任务时返回 {@code true}
     */
    private static boolean isActiveTask(ActiveInFlightWaylineTaskInfo info) {
        if (info == null) {
            return false;
        }
        if (isNotBlank(info.getFlightId())) {
            return true;
        }
        InFlightWaylineTaskDetail detail = info.getInFlightTask();
        return detail != null && isNotBlank(detail.getUuid());
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Map<String, Object> mapTaskDetail(InFlightWaylineTaskDetail detail) {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("uuid", detail.getUuid());
        task.put("status", detail.getStatus());
        task.put("wayline_id", detail.getWaylineId());
        task.put("progress_percent", detail.getProgressPercent());
        task.put("way_point_index", detail.getWayPointIndex());
        task.put("err_code", detail.getErrCode());
        task.put("wayline_duration", detail.getWaylineDuration());
        task.put("wayline_length", detail.getWaylineLength());
        task.put("task_name", detail.getTaskName());
        return task;
    }

    private Map<String, Object> createInMockMode(CreateInFlightWaylineTaskParams params) {
        log.info("[MOCK] Create in-flight wayline task: waylineUuid={}, rthAltitude={}",
                params.getWaylineUuid(), params.getRthAltitude());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("wayline_uuid", params.getWaylineUuid());
        data.put("flight_task_id", MOCK_FLIGHT_TASK_ID_PREFIX + params.getWaylineUuid());
        data.put("in_flight_wayline_task_id", MOCK_TASK_ID_PREFIX + params.getWaylineUuid());
        data.put("_mock", true);
        return successResult(data, "In-flight wayline task created (mock).");
    }

    private Map<String, Object> controlInMockMode(String controlAction) {
        log.info("[MOCK] Control in-flight wayline task: action={}", controlAction);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("control_action", controlAction);
        data.put("_mock", true);
        return successResult(data, "In-flight wayline task " + controlAction + " requested (mock).");
    }

    private Map<String, Object> statusInMockMode() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("active", false);
        data.put("_mock", true);
        return successResult(data, "No active in-flight wayline task (mock).");
    }

    private static Map<String, Object> droneNotFoundError(String droneSn) {
        return errorResult("Drone instance not found: " + droneSn,
                "Ensure the drone is online and airborne. Use fh_device_list to check.");
    }

    private Map<String, Object> runRealSdk(String sdkMethodName, Supplier<Map<String, Object>> call) {
        try {
            return call.get();
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("SDK call timed out: {}", sdkMethodName, e);
            return errorResult("SDK timeout: " + sdkMethodName + " did not respond in time.",
                    "Check device connectivity and retry.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("SDK call failed: {}", sdkMethodName, e);
            return errorResult("SDK error: " + e.getMessage(), "Check drone status and retry.");
        } catch (Exception e) {
            log.error("Unexpected error calling {}", sdkMethodName, e);
            return errorResult("Failed to " + sdkMethodName + ": " + e.getMessage(), "");
        }
    }

    private record CreateValidation(CreateInFlightWaylineTaskParams params, Map<String, Object> error) {
        static CreateValidation ok(CreateInFlightWaylineTaskParams params) {
            return new CreateValidation(params, null);
        }

        static CreateValidation fail(Map<String, Object> error) {
            return new CreateValidation(null, error);
        }
    }
}
