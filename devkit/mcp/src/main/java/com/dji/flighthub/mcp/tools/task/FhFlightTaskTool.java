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

package com.dji.flighthub.mcp.tools.task;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.EditTaskReq;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_flight_task — 航线任务管理（CRUD + 执行 + 状态变更）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>list — 分页查询任务列表（L0）</li>
 *   <li>detail — 查询单个任务详情（L0）</li>
 *   <li>create — 创建航线任务（L1，需 confirm）</li>
 *   <li>edit — 编辑任务（L1，需 confirm）</li>
 *   <li>delete — 删除任务（L2，需 confirm）</li>
 *   <li>execute — 执行任务（L1，需 confirm）</li>
 *   <li>update_status — 更新任务状态（L1，需 confirm）</li>
 * </ul>
 *
 * <p>混合安全等级，不在工具级拦截 confirm，由内部各 action 分别门控。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlightTaskTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlightTaskTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flight_task";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            "list", "detail", "create", "edit", "delete", "execute", "update_status");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 各 action 要求的安全等级（未列出的 action 为 L0，不需要拦截） */
    private static final Map<String, SecurityLevel> ACTION_SECURITY_LEVELS = Map.of(
            "create", SecurityLevel.REVERSIBLE,
            "edit", SecurityLevel.REVERSIBLE,
            "execute", SecurityLevel.REVERSIBLE,
            "update_status", SecurityLevel.REVERSIBLE,
            "delete", SecurityLevel.FLIGHT  // L2：不可逆删除
    );

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_TASK_UUID = "task_uuid";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SN = "sn";
    private static final String PARAM_TASK_TYPE = "task_type";
    private static final String PARAM_WAYLINE_UUID = "wayline_uuid";
    private static final String PARAM_RTH_ALTITUDE = "rth_altitude";
    private static final String PARAM_TIME_ZONE = "time_zone";
    private static final String PARAM_OUT_OF_CONTROL_ACTION = "out_of_control_action";
    private static final String PARAM_BEGIN_AT = "begin_at";
    private static final String PARAM_END_AT = "end_at";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 失控动作枚举值 */
    private static final int OUT_OF_CONTROL_RETURN_HOME = 1;
    private static final int OUT_OF_CONTROL_CONTINUE = 2;

    /** 失控动作默认值：返航 */
    private static final int DEFAULT_OUT_OF_CONTROL_ACTION = OUT_OF_CONTROL_RETURN_HOME;

    /** 默认时区 */
    private static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** RTH 高度约束 */
    private static final double RTH_ALTITUDE_MIN = 15.0;
    private static final double RTH_ALTITUDE_MAX = 1500.0;

    /**
     * 支持的任务类型（对齐 es-task-management 的 {@code po.TaskType} iota）。
     *
     * <p>1 = 立即执行；2 = 定时执行，需同时给出 begin_at 与 end_at。
     *
     * <p><b>语义警示</b>：不同层的注释对 2 的定义并不一致 ——
     * 内核 {@code po.TaskType} 叫 {@code RepeatedTask}，PaaS 网关 proto 又称之为
     * 「条件任务」，本工具此前称之为 "scheduled"。在实际调度行为上，
     * 2 表现为「按 begin_at 起、到 end_at 止的单次任务」。
     *
     * <p>更要紧的是：**PaaS/SDK 通道无法表达重复计划**。SDK 的
     * {@code CreateTaskReq}/{@code EditTaskReq} 没有 {@code repeat_type}/{@code interval}
     * 等 setter，协议层也无承载位，BFF 只能丢弃这些字段，服务端最终按单次任务调度
     * ——即界面上的「重复定时」永远不会按周期反复触发。
     * 需要重复执行时，请指导用户改用 SaaS 通道或逐次创建，不要口头承诺周期触发。
     */
    private static final int TASK_TYPE_IMMEDIATE = 1;
    private static final int TASK_TYPE_SCHEDULED = 2;

    /** SDK 执行中状态码 */
    private static final int STATUS_EXECUTING = 2;

    /** 可派发飞行任务的网关设备类别（大小写不敏感匹配） */
    private static final Set<String> GATEWAY_DEVICE_CLASSES = Set.of("DOCK", "AIRPORT", "BASE_STATION");

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhFlightTaskTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Manage flight tasks (CRUD + execute + status update). "
                + "Actions: list, detail, create, edit, delete, execute, update_status. "
                + "Use 'list'/'detail' for read-only queries; write actions require confirm=true. "
                + "WHEN TO USE (action=create): You have a wayline_uuid from the workspace library. "
                + "DO NOT USE WHEN: You only have a KMZ file URL (use fh_wayline_upload instead) "
                + "or a local KMZ file (use fh_workspace action=submit first to get wayline_uuid). "
                + "REQUIRES for create: wayline_uuid + dock sn (NOT drone sn). "
                + "TYPICAL FLOW: fh_workspace(action=list) -> get wayline_uuid -> fh_flight_task(action=create).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation to perform"
        ));
        props.put(PARAM_TASK_UUID, Map.of(
                "type", "string",
                "description", "Task UUID (required for detail/edit/delete/execute/update_status)"
        ));
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Task name (required for create)"
        ));
        props.put(PARAM_SN, Map.of(
                "type", "string",
                "description", "Dock/airport device SN (NOT drone SN). Required for create; recommended for list to filter tasks by device."
        ));
        props.put(PARAM_TASK_TYPE, Map.of(
                "type", "integer",
                "description", "Task type: 1=immediate (starts on creation), 2=time-windowed "
                        + "(runs once at begin_at, bounded by end_at). NOTE: the PaaS/SDK channel "
                        + "cannot express a recurring schedule - there is no repeat_type/interval "
                        + "field end-to-end, so a task can never repeat on a cycle. Do not promise "
                        + "periodic execution to the user. (required for create)"
        ));
        props.put(PARAM_WAYLINE_UUID, Map.of(
                "type", "string",
                "description", "Wayline UUID to bind (required for create)"
        ));
        props.put(PARAM_RTH_ALTITUDE, Map.of(
                "type", "number",
                "description", "Return-to-home altitude in meters (15~1500, required for create)"
        ));
        props.put(PARAM_TIME_ZONE, Map.of(
                "type", "string",
                "description", "Timezone for task scheduling, e.g. Asia/Shanghai, UTC (default: Asia/Shanghai)"
        ));
        props.put(PARAM_OUT_OF_CONTROL_ACTION, Map.of(
                "type", "integer",
                "description", "Action when drone loses signal during flight: 1=return_home (default), 2=continue"
        ));
        props.put(PARAM_BEGIN_AT, Map.of(
                "type", "integer",
                "description", "Task start time as Unix timestamp (seconds). Required when task_type=2."
        ));
        props.put(PARAM_END_AT, Map.of(
                "type", "integer",
                "description", "Task end time as Unix timestamp (seconds). Required when task_type=2."
        ));
        props.put(PARAM_STATUS, Map.of(
                "type", "integer",
                "description", "Target status code (for update_status)"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number for list (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Items per page for list (default: 20, max: 100)"
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Set true to confirm write operations"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action",
                    "Valid actions: " + VALID_ACTIONS);
        }

        // 统一安全等级拦截：create/edit/execute/update_status=L1，delete=L2
        SecurityLevel requiredLevel = ACTION_SECURITY_LEVELS.get(action);
        if (requiredLevel != null) {
            Map<String, Object> denied = enforceSecurityLevel(requiredLevel);
            if (denied != null) {
                return denied;
            }
        }

        switch (action) {
            case "list":
                return executeList(args);
            case "detail":
                return executeDetail(args);
            case "create":
                return executeCreate(args);
            case "edit":
                return executeEdit(args);
            case "delete":
                return executeDelete(args);
            case "execute":
                return executeExecute(args);
            case "update_status":
                return executeUpdateStatus(args);
            default:
                return errorResult("Unhandled action: " + action, null);
        }
    }

    /**
     * 不在工具级拦截 confirm（混合安全等级）。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== list（L0） ====================

    private Map<String, Object> executeList(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        String sn = getStringArg(args, PARAM_SN, null);

        log.info("[{}] list: page={}, pageSize={}, sn={}", TOOL_NAME, page, pageSize, sn);

        return sdkService.isMockMode()
                ? listInMockMode(page, pageSize)
                : listWithRealSdk(page, pageSize, sn);
    }

    private Map<String, Object> listInMockMode(int page, int pageSize) {
        Map<String, Object> task1 = new LinkedHashMap<>();
        task1.put("task_uuid", "task_001");
        task1.put("name", "Daily Inspection");
        task1.put("sn", "DOCK-001");
        task1.put("status", "SUCCESS");
        task1.put("task_type", 1);

        Map<String, Object> task2 = new LinkedHashMap<>();
        task2.put("task_uuid", "task_002");
        task2.put("name", "Perimeter Patrol");
        task2.put("sn", "DOCK-001");
        task2.put("status", "CREATED");
        task2.put("task_type", 2);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tasks", List.of(task1, task2));
        data.put("total", 2);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Task list retrieved.");
    }

    @Generated
    private Map<String, Object> listWithRealSdk(int page, int pageSize, String sn) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            FlightTaskListParams params = new FlightTaskListParams();
            params.setPage(page);
            params.setPageSize(pageSize);

            List<String> sns;
            if (sn != null && !sn.isBlank()) {
                sns = List.of(sn);
            } else {
                sns = getOnlineGatewaySns();
                if (sns.isEmpty()) {
                    log.info("[{}] 无在线网关设备，跳过任务查询", TOOL_NAME);
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("tasks", List.of());
                    data.put("total", 0);
                    data.put("page", page);
                    data.put("page_size", pageSize);
                    return successResult(data,
                            "No online gateway devices (airport/dock) found. Task list requires at least one online device.");
                }
                log.info("[{}] sn 未指定，自动填充 {} 个在线网关 SN", TOOL_NAME, sns.size());
            }
            params.setSn(sns);

            CompletableFuture<FlightTaskListResult> future = new CompletableFuture<>();
            ftm.getFlightTaskList(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getFlightTaskList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            FlightTaskListResult result = SdkCallbackBridge.await(future);
            if (result == null) {
                return errorResult("SDK returned null result", "Retry or check device connectivity.");
            }
            List<Map<String, Object>> tasks = mapTaskList(result);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tasks", tasks);
            data.put("total", result.getPagination() != null ? result.getPagination().getTotal() : tasks.size());
            data.put("page", page);
            data.put("page_size", pageSize);
            return successResult(data, "Task list retrieved (" + tasks.size() + " items).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check error details.");
        } catch (Exception e) {
            log.error("[{}] list error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list tasks: " + e.getMessage(),
                    "Check logs for details.");
        }
    }

    // ==================== detail（L0） ====================

    private Map<String, Object> executeDetail(Map<String, Object> args) {
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid for detail query.");
        }

        log.info("[{}] detail: taskUuid={}", TOOL_NAME, taskUuid);

        return sdkService.isMockMode()
                ? detailInMockMode(taskUuid)
                : detailWithRealSdk(taskUuid);
    }

    private Map<String, Object> detailInMockMode(String taskUuid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("name", "Daily Inspection");
        data.put("sn", "DOCK-001");
        data.put("wayline_uuid", "wl_abc123");
        data.put("task_type", 1);
        data.put("status", "EXECUTING");
        data.put("progress", 65);
        data.put("rth_altitude", 120.0);
        data.put("out_of_control_action", 1);
        data.put("_mock", true);
        return successResult(data, "Task detail retrieved.");
    }

    @Generated
    private Map<String, Object> detailWithRealSdk(String taskUuid) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<TaskDetail> future = new CompletableFuture<>();
            ftm.getFlightTaskDetail(taskUuid, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getFlightTaskDetail failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            TaskDetail detail = SdkCallbackBridge.await(future);
            Map<String, Object> data = mapTaskDetail(detail);
            return successResult(data, "Task detail retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] detail timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] detail SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] detail error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get task detail: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== create（L1） ====================

    private Map<String, Object> executeCreate(Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, null);
        if (name == null) {
            return errorResult("Missing required parameter: name",
                    "Provide task name for creation.");
        }
        String sn = getStringArg(args, PARAM_SN, null);
        if (sn == null) {
            return errorResult("Missing required parameter: sn",
                    "Provide dock device SN.");
        }
        int taskType = getIntArg(args, PARAM_TASK_TYPE, -1);
        if (taskType != TASK_TYPE_IMMEDIATE && taskType != TASK_TYPE_SCHEDULED) {
            return errorResult("Invalid or missing task_type: must be 1 (immediate) or 2 (time-windowed)",
                    "Current value: " + taskType);
        }
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResultWithHint(
                    "Missing required parameter: wayline_uuid",
                    "Provide a wayline_uuid from the workspace.",
                    "Get wayline_uuid: (1) local KMZ -> fh_workspace(action=submit). "
                    + "(2) public URL -> fh_wayline_upload. "
                    + "(3) already uploaded -> fh_workspace(action=list).",
                    List.of(
                            Map.of("tool", "fh_workspace",
                                   "params_hint", Map.of("action", "submit"),
                                   "when", "local KMZ file"),
                            Map.of("tool", "fh_wayline_upload",
                                   "params_hint", Map.of("action", "create"),
                                   "when", "public URL to KMZ"),
                            Map.of("tool", "fh_workspace",
                                   "params_hint", Map.of("action", "list"),
                                   "when", "wayline already uploaded")
                    ));
        }
        double rthAltitude = getDoubleArg(args, PARAM_RTH_ALTITUDE, -1.0);
        if (rthAltitude < RTH_ALTITUDE_MIN || rthAltitude > RTH_ALTITUDE_MAX) {
            return errorResult("rth_altitude must be between 15 and 1500 meters",
                    "Current value: " + rthAltitude);
        }
        String timeZone = getStringArg(args, PARAM_TIME_ZONE, DEFAULT_TIME_ZONE);
        int outOfControlAction = getIntArg(args, PARAM_OUT_OF_CONTROL_ACTION, DEFAULT_OUT_OF_CONTROL_ACTION);
        if (outOfControlAction != OUT_OF_CONTROL_RETURN_HOME && outOfControlAction != OUT_OF_CONTROL_CONTINUE) {
            return errorResult("Invalid out_of_control_action: must be 1 (return_home) or 2 (continue)",
                    "Current value: " + outOfControlAction);
        }

        // 时间窗任务（task_type=2）必须提供合法的执行时间窗口
        long beginAt = getLongArg(args, PARAM_BEGIN_AT, -1L);
        long endAt = getLongArg(args, PARAM_END_AT, -1L);
        if (taskType == TASK_TYPE_SCHEDULED && (beginAt <= 0 || endAt <= 0)) {
            return errorResult("Missing required parameters: begin_at and end_at are required when task_type=2",
                    "Provide Unix timestamps in seconds.");
        }
        if (taskType == TASK_TYPE_SCHEDULED && endAt <= beginAt) {
            return errorResult("Invalid time window: end_at must be greater than begin_at",
                    "begin_at=" + beginAt + ", end_at=" + endAt);
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "create", "flight_task", "Create flight task: " + name);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] create: name={}, sn={}, taskType={}, waylineUuid={}, timeZone={}, outOfControlAction={}, beginAt={}, endAt={}",
                TOOL_NAME, name, sn, taskType, waylineUuid, timeZone, outOfControlAction, beginAt, endAt);

        return sdkService.isMockMode()
                ? createInMockMode(name, sn, taskType, waylineUuid, rthAltitude, timeZone, outOfControlAction, beginAt, endAt)
                : createWithRealSdk(name, sn, taskType, waylineUuid, rthAltitude, timeZone, outOfControlAction, beginAt, endAt);
    }

    private Map<String, Object> createInMockMode(String name, String sn, int taskType,
                                                  String waylineUuid, double rthAltitude,
                                                  String timeZone, int outOfControlAction,
                                                  long beginAt, long endAt) {
        String taskUuid = "task_new_" + System.currentTimeMillis() % 10000;
        Map<String, Object> data = buildCreateResponseData(
                taskUuid, name, sn, taskType, waylineUuid, rthAltitude, timeZone, outOfControlAction, beginAt, endAt);
        data.put("_mock", true);
        return successResultWithNextSteps(data, "Flight task created.", buildCreateNextSteps(taskUuid));
    }

    @Generated
    private Map<String, Object> createWithRealSdk(String name, String sn, int taskType,
                                                   String waylineUuid, double rthAltitude,
                                                   String timeZone, int outOfControlAction,
                                                   long beginAt, long endAt) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CreateTaskReq req = new CreateTaskReq();
            req.setName(name);
            req.setSn(sn);
            req.setTaskType(taskType);
            req.setWaylineUuid(waylineUuid);
            req.setRthAltitude(rthAltitude);
            req.setTimeZone(timeZone);
            req.setOutOfControlActionInFlight(outOfControlAction);
            if (taskType == TASK_TYPE_SCHEDULED) {
                req.setBeginAt(beginAt);
                req.setEndAt(endAt);
            }

            CompletableFuture<CreateTaskResp> future = new CompletableFuture<>();
            ftm.createFlightTask(req, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "createFlightTask failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            CreateTaskResp resp = SdkCallbackBridge.await(future);

            String taskUuid = resp.getUuid();
            Map<String, Object> data = buildCreateResponseData(
                    taskUuid, name, sn, taskType, waylineUuid, rthAltitude, timeZone, outOfControlAction, beginAt, endAt);
            return successResultWithNextSteps(data, "Flight task created.", buildCreateNextSteps(taskUuid));

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] create timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] create SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] create error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to create task: " + e.getMessage(), "Check logs for details.");
        }
    }

    /**
     * 构建 create 成功后的 next_steps 引导列表。
     *
     * @param taskUuid 新创建的任务 UUID
     * @return next_steps 列表
     */
    private List<Map<String, Object>> buildCreateNextSteps(String taskUuid) {
        return List.of(
                Map.of("action", "Check task details",
                       "tool", TOOL_NAME,
                       "params_hint", Map.of("action", "detail", "task_uuid", taskUuid)),
                Map.of("action", "Execute via fh_wayline_execute (requires L2 security level)",
                       "tool", "fh_wayline_execute",
                       "params_hint", Map.of("task_uuid", taskUuid, "confirm", true))
        );
    }

    // ==================== edit（L1） ====================

    private Map<String, Object> executeEdit(Map<String, Object> args) {
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid to edit.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "edit", "flight_task", "Edit flight task: " + taskUuid);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] edit: taskUuid={}", TOOL_NAME, taskUuid);

        return sdkService.isMockMode()
                ? editInMockMode(taskUuid)
                : editWithRealSdk(taskUuid, args);
    }

    private Map<String, Object> editInMockMode(String taskUuid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("status", "updated");
        data.put("_mock", true);
        return successResult(data, "Flight task updated.");
    }

    @Generated
    private Map<String, Object> editWithRealSdk(String taskUuid, Map<String, Object> args) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            EditTaskReq req = new EditTaskReq();
            // 将传入的编辑参数映射到 EditTaskReq
            String name = getStringArg(args, PARAM_NAME, null);
            if (name != null) {
                req.setName(name);
            }
            double rthAltitude = getDoubleArg(args, PARAM_RTH_ALTITUDE, -1.0);
            if (rthAltitude >= RTH_ALTITUDE_MIN && rthAltitude <= RTH_ALTITUDE_MAX) {
                req.setRthAltitude(rthAltitude);
            }

            CompletableFuture<CreateTaskResp> future = new CompletableFuture<>();
            ftm.editFlightTask(taskUuid, req, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "editFlightTask failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            CreateTaskResp resp = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_uuid", taskUuid);
            data.put("status", "updated");
            return successResult(data, "Flight task updated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] edit timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] edit SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] edit error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to edit task: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== delete（L2） ====================

    private Map<String, Object> executeDelete(Map<String, Object> args) {
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid to delete.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "delete", "flight_task", "Delete flight task: " + taskUuid);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] delete: taskUuid={}", TOOL_NAME, taskUuid);

        return sdkService.isMockMode()
                ? deleteInMockMode(taskUuid)
                : deleteWithRealSdk(taskUuid);
    }

    private Map<String, Object> deleteInMockMode(String taskUuid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("status", "deleted");
        data.put("_mock", true);
        return successResult(data, "Flight task deleted.");
    }

    @Generated
    private Map<String, Object> deleteWithRealSdk(String taskUuid) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            SdkCallbackBridge.awaitVoid(cb -> ftm.deleteFlightTask(taskUuid, cb));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_uuid", taskUuid);
            data.put("status", "deleted");
            return successResult(data, "Flight task deleted.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] delete timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] delete SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] delete error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to delete task: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== execute（L1） ====================

    private Map<String, Object> executeExecute(Map<String, Object> args) {
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid to execute.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "execute", "flight_task", "Execute flight task: " + taskUuid);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] execute: taskUuid={}", TOOL_NAME, taskUuid);

        // session 已在 execute() 入口验证过，此处安全获取
        SessionContext session = requireSession();
        return sdkService.isMockMode()
                ? executeInMockMode(session, taskUuid)
                : executeWithRealSdk(session, taskUuid);
    }

    private Map<String, Object> executeInMockMode(SessionContext session, String taskUuid) {
        // 更新 session 飞行状态，确保安全监控能感知通过此路径启动的飞行
        session.setActiveWaylineTaskUuid(taskUuid);
        session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("status", "EXECUTING");
        data.put("_mock", true);
        return successResult(data, "Flight task execution started.");
    }

    @Generated
    private Map<String, Object> executeWithRealSdk(SessionContext session, String taskUuid) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            // 将任务状态设为执行中（status=2），触发无人机飞行
            SdkCallbackBridge.awaitVoid(cb ->
                    ftm.updateFlightTaskStatus(taskUuid, STATUS_EXECUTING, cb));

            // SDK 调用成功后更新 session 飞行状态，确保安全监控能感知
            session.setActiveWaylineTaskUuid(taskUuid);
            session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_uuid", taskUuid);
            data.put("status", "EXECUTING");
            return successResult(data, "Flight task execution started.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] execute timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] execute SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] execute error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to execute task: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== update_status（L1） ====================

    private Map<String, Object> executeUpdateStatus(Map<String, Object> args) {
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid to update status.");
        }
        int status = getIntArg(args, PARAM_STATUS, -1);
        if (status < 0) {
            return errorResult("Missing or invalid parameter: status",
                    "Provide a valid status code.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "update_status", "flight_task",
                "Update task " + taskUuid + " status to " + status);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] update_status: taskUuid={}, status={}", TOOL_NAME, taskUuid, status);

        return sdkService.isMockMode()
                ? updateStatusInMockMode(taskUuid, status)
                : updateStatusWithRealSdk(taskUuid, status);
    }

    private Map<String, Object> updateStatusInMockMode(String taskUuid, int status) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("new_status", status);
        data.put("_mock", true);
        return successResult(data, "Task status updated.");
    }

    @Generated
    private Map<String, Object> updateStatusWithRealSdk(String taskUuid, int status) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            SdkCallbackBridge.awaitVoid(cb ->
                    ftm.updateFlightTaskStatus(taskUuid, status, cb));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_uuid", taskUuid);
            data.put("new_status", status);
            return successResult(data, "Task status updated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] update_status timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] update_status SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] update_status error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to update status: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 构建创建任务的响应数据 Map，统一 Mock 和真实 SDK 路径的字段集。
     *
     * @param taskUuid           任务 UUID
     * @param name               任务名称
     * @param sn                 机场序列号
     * @param taskType           任务类型
     * @param waylineUuid        航线 UUID
     * @param rthAltitude        返航高度
     * @param timeZone           时区
     * @param outOfControlAction 失控动作
     * @param beginAt            计划任务开始时间（非计划任务传 -1）
     * @param endAt              计划任务结束时间（非计划任务传 -1）
     * @return 响应数据 Map
     */
    private Map<String, Object> buildCreateResponseData(String taskUuid, String name, String sn,
                                                        int taskType, String waylineUuid,
                                                        double rthAltitude, String timeZone,
                                                        int outOfControlAction,
                                                        long beginAt, long endAt) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("name", name);
        data.put("sn", sn);
        data.put("task_type", taskType);
        data.put("wayline_uuid", waylineUuid);
        data.put("rth_altitude", rthAltitude);
        data.put("time_zone", timeZone);
        data.put("out_of_control_action", outOfControlAction);
        if (taskType == TASK_TYPE_SCHEDULED) {
            data.put("begin_at", beginAt);
            data.put("end_at", endAt);
        }
        data.put("status", "CREATED");
        return data;
    }

    /**
     * 获取 FlightTaskModule 实例。
     *
     * @return FlightTaskModule，不可用时返回 null
     */
    private FlightTaskModule getFlightTaskModule() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getFlightTaskModule();
    }

    /**
     * 获取当前所有在线网关设备（airport/dock/base_station）的 SN 列表。
     *
     * <p>SDK 要求 FlightTaskListParams 至少传入 1 个网关 SN 才返回数据，
     * 此方法在 Agent 未显式传入 sn 时自动填充。
     *
     * @return 在线网关 SN 列表，无设备或查询失败时返回空列表
     */
    private List<String> getOnlineGatewaySns() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return List.of();
        }

        CompletableFuture<List<String>> future = new CompletableFuture<>();
        workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
            if (!error.isSuccess()) {
                log.warn("[{}] getDeviceList 失败: {}", TOOL_NAME, error.getErrorCode());
                future.complete(List.of());
                return;
            }

            List<String> sns = new ArrayList<>();
            if (devices != null) {
                for (DeviceTopo topo : devices) {
                    Device device = topo.getDevice();
                    if (device != null && device.isOnline() && isGatewayDevice(device.getDeviceClass())) {
                        sns.add(device.getDeviceSn());
                    }
                }
            }
            future.complete(sns);
        });

        try {
            return SdkCallbackBridge.await(future);
        } catch (Exception e) {
            log.warn("[{}] 获取在线网关 SN 异常: {}", TOOL_NAME, e.getMessage());
            return List.of();
        }
    }

    /**
     * 判断设备是否为可派发任务的网关类型（大小写不敏感）。
     */
    private static boolean isGatewayDevice(String deviceClass) {
        return deviceClass != null && GATEWAY_DEVICE_CLASSES.contains(deviceClass.toUpperCase());
    }

    /**
     * 将 SDK 返回的 FlightTaskListResult 映射为 MCP 列表响应格式。
     */
    private List<Map<String, Object>> mapTaskList(FlightTaskListResult result) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        if (result == null || result.getList() == null) {
            return tasks;
        }
        for (TaskDetail detail : result.getList()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("task_uuid", detail.getUuid());
            item.put("name", detail.getName());
            item.put("sn", detail.getSn());
            item.put("status", detail.getStatus());
            item.put("task_type", detail.getTaskType());
            item.put("wayline_uuid", detail.getWaylineUuid());
            item.put("created_at", detail.getCreatedAt());
            tasks.add(item);
        }
        return tasks;
    }

    /**
     * 将 SDK 返回的 TaskDetail 映射为 MCP 详情响应格式。
     */
    private Map<String, Object> mapTaskDetail(TaskDetail detail) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (detail == null) {
            return data;
        }
        data.put("task_uuid", detail.getUuid());
        data.put("name", detail.getName());
        data.put("sn", detail.getSn());
        data.put("wayline_uuid", detail.getWaylineUuid());
        data.put("task_type", detail.getTaskType());
        data.put("status", detail.getStatus());
        data.put("rth_altitude", detail.getRthAltitude());
        data.put("out_of_control_action", detail.getOutOfControlActionInFlight());
        data.put("current_waypoint_index", detail.getCurrentWaypointIndex());
        data.put("total_waypoints", detail.getTotalWaypoints());
        data.put("created_at", detail.getCreatedAt());
        data.put("completed_at", detail.getCompletedAt());
        return data;
    }
}
