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
import com.dji.flighthubsdk.bean.CommandTaskListItemData;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.EditTaskReq;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.FlightTrackInfo;
import com.dji.flighthubsdk.bean.FlightTrackPoint;
import com.dji.flighthubsdk.bean.GetCommandTaskListParams;
import com.dji.flighthubsdk.bean.GetCommandTaskListResults;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackParams;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackResults;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
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
 *   <li>list_command — 分页查询一键起飞指令任务列表（L0）</li>
 *   <li>track — 查询任务飞行轨迹（L0）</li>
 *   <li>create — 创建航线任务（L1，需 confirm）</li>
 *   <li>edit — 编辑任务（L1，需 confirm）</li>
 *   <li>delete — 删除任务（L2，需 confirm）</li>
 *   <li>execute — 执行任务（L2，需 confirm）</li>
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
            "list", "detail", "list_command", "track", "create", "edit", "delete", "execute", "update_status");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 各 action 要求的安全等级（未列出的 action 为 L0，不需要拦截）
     *  execute 为实际下发飞行任务，按接管级操作（L2）门控；delete 为不可逆删除（L2）。 */
    private static final Map<String, SecurityLevel> ACTION_SECURITY_LEVELS = Map.of(
            "create", SecurityLevel.REVERSIBLE,
            "edit", SecurityLevel.REVERSIBLE,
            "execute", SecurityLevel.FLIGHT,  // L2：实际下发飞行任务，需人工确认
            "update_status", SecurityLevel.REVERSIBLE,
            "delete", SecurityLevel.FLIGHT  // L2：不可逆删除
    );

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_TASK_UUID = "task_uuid";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SN = "sn";
    private static final String PARAM_DRONE_SN = "drone_sn";
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
    private static final int MAX_PAGE_SIZE = 100;

    /** list_command 分页默认值与边界 */
    private static final int DEFAULT_COMMAND_PAGE_SIZE = 10;
    private static final int COMMAND_PAGE_SIZE_MIN = 1;
    private static final int COMMAND_PAGE_SIZE_MAX = 100;

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

    /** SDK 任务状态码（与 Go SDK 枚举语义一致） */
    private static final int STATUS_CREATED = 1;
    private static final int STATUS_EXECUTING = 2;
    private static final int STATUS_SUCCESS = 5;

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

    /**
     * 声明工具级静态安全等级：REVERSIBLE。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Manage flight tasks (CRUD+execute+status) and query one-key-takeoff command tasks/track. "
                + "Read-only: list, detail, list_command, track. "
                + "Writes (create/edit/delete/execute/update_status) need confirm=true. "
                + "WHEN TO USE (action=create): have wayline_uuid from workspace. "
                + "DO NOT USE WHEN: only have KMZ URL (use fh_wayline_upload) or local KMZ "
                + "(fh_workspace action=submit first). "
                + "REQUIRES for create: wayline_uuid + dock sn (NOT drone sn). "
                + "FLOW: fh_workspace(list) -> wayline_uuid -> fh_flight_task(create).");

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
                "description", "Task UUID (required for detail/track/edit/delete/execute/update_status)"
        ));
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Task name (required for create)"
        ));
        props.put(PARAM_SN, Map.of(
                "type", "string",
                "description", "Dock/airport device SN (NOT drone SN). Required for create; "
                        + "recommended for list to filter tasks by device; optional for edit to re-bind the task; "
                        + "optional GATEWAY SN filter for list_command (at least one of sn/drone_sn should be provided)."
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "DRONE serial number filter for list_command. This is a different filter "
                        + "dimension from sn (GATEWAY SN) and both may be supplied together."
        ));
        props.put(PARAM_TASK_TYPE, Map.of(
                "type", "integer",
                "description", "Task type: 1=immediate (starts on creation), 2=time-windowed "
                        + "(runs once at begin_at, bounded by end_at). NOTE: the PaaS/SDK channel "
                        + "cannot express a recurring schedule - there is no repeat_type/interval "
                        + "field end-to-end, so a task can never repeat on a cycle. Do not promise "
                        + "periodic execution to the user. (required for create; optional for edit)"
        ));
        props.put(PARAM_WAYLINE_UUID, Map.of(
                "type", "string",
                "description", "Wayline UUID to bind (required for create; optional for edit to re-bind)"
        ));
        props.put(PARAM_RTH_ALTITUDE, Map.of(
                "type", "number",
                "description", "Return-to-home altitude in meters (15~1500). Required for create; "
                        + "optional for edit."
        ));
        props.put(PARAM_TIME_ZONE, Map.of(
                "type", "string",
                "description", "Timezone for task scheduling, e.g. Asia/Shanghai, UTC "
                        + "(default for create: Asia/Shanghai; optional for edit)"
        ));
        props.put(PARAM_OUT_OF_CONTROL_ACTION, Map.of(
                "type", "integer",
                "description", "Action when drone loses signal during flight: 1=return_home (default), "
                        + "2=continue (optional for edit)"
        ));
        props.put(PARAM_BEGIN_AT, Map.of(
                "type", "integer",
                "description", "For create: task start time as Unix timestamp (seconds), required when task_type=2; "
                        + "for edit, when changing the window, send both begin_at and end_at. "
                        + "For list_command: start of the time filter window; must be provided together with end_at. "
                        + "NOTE: this is the write (create/edit) direction only - detail/list return "
                        + "begin_at as the server's original string, not as a Unix timestamp."
        ));
        props.put(PARAM_END_AT, Map.of(
                "type", "integer",
                "description", "For create: task end time as Unix timestamp (seconds), required when task_type=2; "
                        + "for edit, when changing the window, send both begin_at and end_at. "
                        + "For list_command: end of the time filter window; must be provided together with begin_at. "
                        + "NOTE: this is the write (create/edit) direction only - detail/list return "
                        + "end_at as the server's original string, not as a Unix timestamp."
        ));
        props.put(PARAM_STATUS, Map.of(
                "type", List.of("integer", "array"),
                "items", Map.of("type", "integer"),
                "description", "For update_status: target status code (required, a single integer), "
                        + "1=CREATED, 2=EXECUTING, 3=PAUSED, 4=CANCELLED, 5=SUCCESS, 6=FAILED. "
                        + "For action=list: optional filter, accepts a single code or an array of codes "
                        + "(e.g. [1, 2]) using the same 1-6 scheme as update_status (an array is only valid "
                        + "for action=list). For list_command: status filter list, each value in "
                        + "0=pending,1=running,2=completed,3=failed,4=timeout."
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number for list/list_command (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Items per page for list (default: 20, max: 100) "
                        + "or list_command (default: 10, max: 100)"
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
            case "list_command":
                return executeListCommand(args);
            case "track":
                return executeTrack(args);
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
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        String sn = getStringArg(args, PARAM_SN, null);
        List<Integer> statusFilter = getStatusFilter(args);

        log.info("[{}] list: page={}, pageSize={}, sn={}, status={}",
                TOOL_NAME, page, pageSize, sn, statusFilter);

        return sdkService.isMockMode()
                ? listInMockMode(page, pageSize, statusFilter)
                : listWithRealSdk(page, pageSize, sn, statusFilter);
    }

    /**
     * 解析 list 的状态过滤参数，兼容单个整数与整数数组两种写法。
     *
     * @param args 工具入参
     * @return 状态码列表；未传时返回空列表表示不过滤
     */
    private List<Integer> getStatusFilter(Map<String, Object> args) {
        Object raw = args == null ? null : args.get(PARAM_STATUS);
        List<Integer> statuses = new ArrayList<>();
        if (raw instanceof Number number) {
            statuses.add(number.intValue());
            return statuses;
        }
        if (raw instanceof String text) {
            Integer status = parseStatus(text);
            if (status != null) {
                statuses.add(status);
            }
            return statuses;
        }
        if (raw instanceof Collection<?> values) {
            for (Object value : values) {
                if (value instanceof Number number) {
                    statuses.add(number.intValue());
                } else if (value instanceof String text) {
                    Integer status = parseStatus(text);
                    if (status != null) {
                        statuses.add(status);
                    }
                }
            }
        }
        return statuses;
    }

    /**
     * 解析单个状态码，非数字输入返回 {@code null} 并记录告警。
     *
     * @param text 状态码文本
     * @return 解析后的状态码；非法时返回 null
     */
    private Integer parseStatus(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            log.warn("[{}] 忽略非法 status 值: {}", TOOL_NAME, text);
            return null;
        }
    }

    /**
     * 判断调用方是否显式传入了某个参数（且值不为 {@code null}）。
     *
     * <p>edit 流程用它替代 {@code -1} 之类的哨兵值判断：哨兵值无法区分
     * “未传”和“显式传了一个非法值”，会把后者静默当成“未传”吞掉。
     *
     * @param args 工具入参
     * @param key  参数名
     * @return 显式传入且非 null 时返回 true
     */
    private static boolean hasArg(Map<String, Object> args, String key) {
        return args != null && args.get(key) != null;
    }

    /**
     * 取参数原始值用于错误信息回显，便于调用方看到自己到底传了什么。
     *
     * @param args 工具入参
     * @param key  参数名
     * @return 原始值；未传时返回 {@code null}
     */
    private static Object rawValue(Map<String, Object> args, String key) {
        return args == null ? null : args.get(key);
    }

    private Map<String, Object> listInMockMode(int page, int pageSize, List<Integer> statusFilter) {
        Map<String, Object> task1 = new LinkedHashMap<>();
        task1.put("task_uuid", "task_001");
        task1.put("name", "Daily Inspection");
        task1.put("sn", "DOCK-001");
        task1.put("status", "SUCCESS");
        task1.put("status_code", STATUS_SUCCESS);
        task1.put("task_type", 1);

        Map<String, Object> task2 = new LinkedHashMap<>();
        task2.put("task_uuid", "task_002");
        task2.put("name", "Perimeter Patrol");
        task2.put("sn", "DOCK-001");
        task2.put("status", "CREATED");
        task2.put("status_code", STATUS_CREATED);
        task2.put("task_type", 2);

        List<Map<String, Object>> tasks = new ArrayList<>();
        for (Map<String, Object> task : List.of(task1, task2)) {
            if (statusFilter.isEmpty() || statusFilter.contains(task.get("status_code"))) {
                tasks.add(task);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tasks", tasks);
        data.put("total", tasks.size());
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Task list retrieved.");
    }

    @Generated
    private Map<String, Object> listWithRealSdk(int page, int pageSize, String sn, List<Integer> statusFilter) {
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
            if (statusFilter != null && !statusFilter.isEmpty()) {
                params.setStatus(statusFilter);
            }

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

    /**
     * Mock 模式下的任务详情。
     *
     * <p>字段集与 {@link #mapTaskDetail(TaskDetail)} 保持一致，避免调用方按 mock 响应开发、
     * 到真实环境才发现字段对不上。时间字段为示意值，仅用于演示「原始字符串」语义。
     */
    private Map<String, Object> detailInMockMode(String taskUuid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("name", "Daily Inspection");
        data.put("sn", "DOCK-001");
        data.put("wayline_uuid", "wl_abc123");
        data.put("task_type", 1);
        data.put("status", "EXECUTING");
        data.put("rth_altitude", 120.0);
        data.put("out_of_control_action", 1);
        data.put("current_waypoint_index", 5);
        data.put("total_waypoints", 11);
        data.put("time_zone", "Asia/Shanghai");
        data.put("begin_at", "2025-01-20T10:00:00Z");
        data.put("end_at", "2025-01-20T10:30:00Z");
        data.put("tags", List.of("mock"));
        data.put("created_at", "2025-01-20T09:50:00Z");
        data.put("updated_at", "2025-01-20T10:05:00Z");
        data.put("completed_at", null);
        data.put("deleted_at", null);
        // mock 专属的展示字段，真实路径没有对应字段
        data.put("progress", 65);
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

    // ==================== list_command（L0） ====================

    /**
     * 查询一键起飞指令任务列表。
     *
     * <p>sn（网关 SN）与 drone_sn（无人机 SN）是两个不同维度的过滤条件，
     * 二者可同时提供，不是互斥关系。begin_at/end_at 必须同时提供或同时省略。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeListCommand(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_COMMAND_PAGE_SIZE);
        if (pageSize < COMMAND_PAGE_SIZE_MIN || pageSize > COMMAND_PAGE_SIZE_MAX) {
            pageSize = DEFAULT_COMMAND_PAGE_SIZE;
        }

        String sn = getStringArg(args, PARAM_SN, null);
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        long beginAt = getLongArg(args, PARAM_BEGIN_AT, -1L);
        long endAt = getLongArg(args, PARAM_END_AT, -1L);
        if ((beginAt > 0) != (endAt > 0)) {
            return errorResult("begin_at and end_at must be provided together",
                    "Provide both Unix timestamps in seconds, or omit both to disable the time filter.");
        }
        List<Integer> status = parseIntList(args, PARAM_STATUS);

        log.info("[{}] list_command: page={}, pageSize={}, sn={}, droneSn={}, beginAt={}, endAt={}, status={}",
                TOOL_NAME, page, pageSize, sn, droneSn, beginAt, endAt, status);

        return sdkService.isMockMode()
                ? listCommandInMockMode(page, pageSize)
                : listCommandWithRealSdk(page, pageSize, sn, droneSn, beginAt, endAt, status);
    }

    private Map<String, Object> listCommandInMockMode(int page, int pageSize) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("uuid", "cmd_task_001");
        item.put("sn", "DOCK-001");
        item.put("drone_sn", "DRONE-001");
        item.put("status", 2);
        item.put("err_code", 0);
        item.put("takeoff_mode", "manual");
        item.put("security_takeoff_height", 20.0);
        item.put("max_speed", 10);
        item.put("rth_altitude", 100);
        item.put("created_at", "2026-01-01T00:00:00Z");
        item.put("user_id", "user_001");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tasks", List.of(item));
        data.put("total", 1);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Command task list retrieved.");
    }

    @Generated
    private Map<String, Object> listCommandWithRealSdk(int page, int pageSize, String sn, String droneSn,
                                                         long beginAt, long endAt, List<Integer> status) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            GetCommandTaskListParams params = new GetCommandTaskListParams();
            params.setPage(page);
            params.setPageSize(pageSize);
            params.setSn(sn);
            params.setDroneSn(droneSn);
            params.setBeginAt(beginAt);
            params.setEndAt(endAt);
            params.setStatus(status);

            CompletableFuture<GetCommandTaskListResults> future = new CompletableFuture<>();
            ftm.getCommandTaskList(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getCommandTaskList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetCommandTaskListResults result = SdkCallbackBridge.await(future);
            List<Map<String, Object>> tasks = mapCommandTaskList(result);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tasks", tasks);
            data.put("total", result != null && result.getPagination() != null
                    ? result.getPagination().getTotal() : tasks.size());
            data.put("page", page);
            data.put("page_size", pageSize);
            return successResult(data, "Command task list retrieved (" + tasks.size() + " items).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list_command timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list_command SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] list_command error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list command tasks: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== track（L0） ====================

    /**
     * 查询指定任务的飞行轨迹。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeTrack(Map<String, Object> args) {
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid for track query.");
        }

        log.info("[{}] track: taskUuid={}", TOOL_NAME, taskUuid);

        return sdkService.isMockMode()
                ? trackInMockMode(taskUuid)
                : trackWithRealSdk(taskUuid);
    }

    private Map<String, Object> trackInMockMode(String taskUuid) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("timestamp", 1700000000L);
        point.put("latitude", 31.2);
        point.put("longitude", 121.5);
        point.put("height", 50.0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("track_id", "track_001");
        data.put("drone_sn", "DRONE-001");
        data.put("flight_distance", 1200);
        data.put("flight_duration", 300);
        data.put("points", List.of(point));
        data.put("_mock", true);
        return successResult(data, "Flight task track retrieved.");
    }

    @Generated
    private Map<String, Object> trackWithRealSdk(String taskUuid) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            GetFlightTaskTrackParams params = new GetFlightTaskTrackParams();
            params.setTaskUuid(taskUuid);

            CompletableFuture<GetFlightTaskTrackResults> future = new CompletableFuture<>();
            ftm.getFlightTaskTrack(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getFlightTaskTrack failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetFlightTaskTrackResults result = SdkCallbackBridge.await(future);
            Map<String, Object> data = mapFlightTaskTrack(taskUuid, result);
            return successResult(data, "Flight task track retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] track timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] track SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] track error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get flight task track: " + e.getMessage(), "Check logs for details.");
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
        data.put("wayline_validity_check_codes", new ArrayList<Integer>());
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
            data.put("wayline_validity_check_codes", resp.getWaylineValidityCheckCodes());
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

        Map<String, Object> changes = new LinkedHashMap<>();
        String name = getStringArg(args, PARAM_NAME, null);
        if (name != null) {
            changes.put(PARAM_NAME, name);
        }
        String sn = getStringArg(args, PARAM_SN, null);
        if (sn != null) {
            changes.put(PARAM_SN, sn);
        }
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid != null) {
            changes.put(PARAM_WAYLINE_UUID, waylineUuid);
        }
        String timeZone = getStringArg(args, PARAM_TIME_ZONE, null);
        if (timeZone != null) {
            changes.put(PARAM_TIME_ZONE, timeZone);
        }

        // 数值字段一律用「参数是否存在」判断，而不是用 -1 / -1.0 之类的哨兵值：
        // 调用方显式传 0 时哨兵判断会把它当成「未传」而静默丢弃，
        // 最终只报一句“No editable field provided”，掩盖了真正的问题（取值越界）
        Integer taskType = null;
        if (hasArg(args, PARAM_TASK_TYPE)) {
            int value = getIntArg(args, PARAM_TASK_TYPE, -1);
            if (value != TASK_TYPE_IMMEDIATE && value != TASK_TYPE_SCHEDULED) {
                return errorResult("Invalid task_type: must be 1 (immediate) or 2 (time-windowed)",
                        "Current value: " + rawValue(args, PARAM_TASK_TYPE));
            }
            taskType = value;
            changes.put(PARAM_TASK_TYPE, value);
        }

        if (hasArg(args, PARAM_OUT_OF_CONTROL_ACTION)) {
            int value = getIntArg(args, PARAM_OUT_OF_CONTROL_ACTION, -1);
            if (value != OUT_OF_CONTROL_RETURN_HOME && value != OUT_OF_CONTROL_CONTINUE) {
                return errorResult("Invalid out_of_control_action: must be 1 (return_home) or 2 (continue)",
                        "Current value: " + rawValue(args, PARAM_OUT_OF_CONTROL_ACTION));
            }
            changes.put(PARAM_OUT_OF_CONTROL_ACTION, value);
        }

        if (hasArg(args, PARAM_RTH_ALTITUDE)) {
            double value = getDoubleArg(args, PARAM_RTH_ALTITUDE, -1.0);
            if (value < RTH_ALTITUDE_MIN || value > RTH_ALTITUDE_MAX) {
                return errorResult("rth_altitude must be between 15 and 1500 meters",
                        "Current value: " + rawValue(args, PARAM_RTH_ALTITUDE));
            }
            changes.put(PARAM_RTH_ALTITUDE, value);
        }

        boolean hasBeginAt = hasArg(args, PARAM_BEGIN_AT);
        boolean hasEndAt = hasArg(args, PARAM_END_AT);
        long beginAt = getLongArg(args, PARAM_BEGIN_AT, -1L);
        long endAt = getLongArg(args, PARAM_END_AT, -1L);
        if (hasBeginAt != hasEndAt) {
            return errorResult("begin_at and end_at must be provided together",
                    "begin_at=" + rawValue(args, PARAM_BEGIN_AT)
                            + ", end_at=" + rawValue(args, PARAM_END_AT));
        }
        if (hasBeginAt) {
            if (beginAt <= 0 || endAt <= 0) {
                return errorResult("Invalid time window: begin_at and end_at must be positive "
                                + "Unix timestamps (seconds)",
                        "begin_at=" + rawValue(args, PARAM_BEGIN_AT)
                                + ", end_at=" + rawValue(args, PARAM_END_AT));
            }
            if (endAt <= beginAt) {
                return errorResult("Invalid time window: end_at must be greater than begin_at",
                        "begin_at=" + beginAt + ", end_at=" + endAt);
            }
            changes.put(PARAM_BEGIN_AT, beginAt);
            changes.put(PARAM_END_AT, endAt);
        }
        if (taskType != null && taskType == TASK_TYPE_SCHEDULED && !hasBeginAt) {
            return errorResult("Missing required parameters: begin_at and end_at are required "
                            + "when editing a task to task_type=2",
                    "Provide Unix timestamps in seconds.");
        }

        if (changes.isEmpty()) {
            return errorResult("No editable field provided",
                    "Provide at least one of: name, sn, wayline_uuid, time_zone, task_type, "
                    + "out_of_control_action, rth_altitude, begin_at/end_at.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "edit", "flight_task", "Edit flight task: " + taskUuid);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] edit: taskUuid={}, fields={}", TOOL_NAME, taskUuid, changes.keySet());

        return sdkService.isMockMode()
                ? editInMockMode(taskUuid, changes)
                : editWithRealSdk(taskUuid, changes);
    }

    private Map<String, Object> editInMockMode(String taskUuid, Map<String, Object> changes) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("status", "updated");
        data.put("updated_fields", new ArrayList<>(changes.keySet()));
        data.put("_mock", true);
        return successResult(data, "Flight task updated.");
    }

    /**
     * 通过真实 SDK 编辑任务，把所有通过校验的字段映射到 {@link EditTaskReq}。
     *
     * <p>只设置调用方显式传入的字段，未传入的字段保持 SDK 默认值，
     * 避免把空串/0 覆盖到服务端已有配置上。
     *
     * @param taskUuid 任务 UUID
     * @param changes  已通过校验的待更新字段
     * @return 工具响应
     */
    @Generated
    private Map<String, Object> editWithRealSdk(String taskUuid, Map<String, Object> changes) {
        try {
            FlightTaskModule ftm = getFlightTaskModule();
            if (ftm == null) {
                return errorResult("FlightTaskModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            EditTaskReq req = buildEditRequest(taskUuid, changes);

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
            data.put("updated_fields", new ArrayList<>(changes.keySet()));
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

    /**
     * 把已校验的编辑字段映射为 SDK 的 {@link EditTaskReq}。
     *
     * @param taskUuid 任务 UUID
     * @param changes  已通过校验的待更新字段
     * @return SDK 请求对象
     */
    private EditTaskReq buildEditRequest(String taskUuid, Map<String, Object> changes) {
        EditTaskReq req = new EditTaskReq();
        req.setUuid(taskUuid);

        String name = getStringArg(changes, PARAM_NAME, null);
        if (name != null) {
            req.setName(name);
        }
        String sn = getStringArg(changes, PARAM_SN, null);
        if (sn != null) {
            req.setSn(sn);
        }
        String waylineUuid = getStringArg(changes, PARAM_WAYLINE_UUID, null);
        if (waylineUuid != null) {
            req.setWaylineUuid(waylineUuid);
        }
        String timeZone = getStringArg(changes, PARAM_TIME_ZONE, null);
        if (timeZone != null) {
            req.setTimeZone(timeZone);
        }
        // changes 中只会出现 executeEdit 校验通过并写入的字段，
        // 因此这里同样用 containsKey 判断，与上游保持一致，不再依赖 -1 哨兵
        if (changes.containsKey(PARAM_TASK_TYPE)) {
            req.setTaskType(getIntArg(changes, PARAM_TASK_TYPE, -1));
        }
        if (changes.containsKey(PARAM_OUT_OF_CONTROL_ACTION)) {
            req.setOutOfControlActionInFlight(getIntArg(changes, PARAM_OUT_OF_CONTROL_ACTION, -1));
        }
        if (changes.containsKey(PARAM_RTH_ALTITUDE)) {
            req.setRthAltitude(getDoubleArg(changes, PARAM_RTH_ALTITUDE, -1.0));
        }
        if (changes.containsKey(PARAM_BEGIN_AT)) {
            req.setBeginAt(getLongArg(changes, PARAM_BEGIN_AT, -1L));
        }
        if (changes.containsKey(PARAM_END_AT)) {
            req.setEndAt(getLongArg(changes, PARAM_END_AT, -1L));
        }
        return req;
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
        // schema 中 status 同时声明了 integer 与 array（array 仅供 action=list 作过滤）。
        // 数组直接交给 getIntArg 会落到 fallback，报出与真实原因无关的“缺少参数”，因此这里先拦下
        Object rawStatus = args == null ? null : args.get(PARAM_STATUS);
        if (rawStatus instanceof Collection<?>) {
            return errorResult("Invalid parameter: status must be a single code for update_status",
                    "Arrays are only supported by action=list as a status filter. "
                            + "Provide a single status code (e.g. status=5) to set the task status.");
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
     *
     * <p><strong>时间字段读/写不对称</strong>：写（create/edit）走 {@code CreateTaskReq}/{@code EditTaskReq}，
     * {@code begin_at}/{@code end_at} 是 Unix 秒级时间戳（{@code Long}）；读走 {@code TaskDetail}，
     * SDK 里 {@code beginAt}/{@code endAt} 只有 {@code String} getter，SDK 原样透传服务端返回值。
     * 因此本方法不对时间字段做任何格式化/解析，直接把服务端原始字符串返回，
     * 调用方需要自行按服务端实际格式解析（MCP 层无法保证是 ISO-8601）。
     *
     * <p>与 {@code fh_wayline_status} 的详情映射保持同一字段集与同一时间语义。
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
        data.put("time_zone", detail.getTimeZone());
        // 服务端原始字符串，不做时间戳转换（SDK 只提供 String getter，见方法注释）
        data.put("begin_at", detail.getBeginAt());
        data.put("end_at", detail.getEndAt());
        data.put("tags", detail.getTags());
        data.put("created_at", detail.getCreatedAt());
        data.put("updated_at", detail.getUpdatedAt());
        data.put("completed_at", detail.getCompletedAt());
        // 服务端执行的是软删除：已删除任务仍可查到，调用方需依据 deleted_at 自行过滤
        data.put("deleted_at", detail.getDeletedAt());
        return data;
    }

    /**
     * 将 SDK 返回的一键起飞指令任务列表映射为 MCP 响应格式。
     *
     * @param result SDK 返回的指令任务列表结果
     * @return 映射后的任务列表
     */
    private List<Map<String, Object>> mapCommandTaskList(GetCommandTaskListResults result) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        if (result == null || result.getList() == null) {
            return tasks;
        }
        for (CommandTaskListItemData item : result.getList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("uuid", item.getUuid());
            map.put("sn", item.getSn());
            map.put("drone_sn", item.getDroneSn());
            map.put("status", item.getStatus());
            map.put("err_code", item.getErrCode());
            map.put("takeoff_mode", item.getTakeoffMode());
            map.put("security_takeoff_height", item.getSecurityTakeoffHeight());
            map.put("max_speed", item.getMaxSpeed());
            map.put("rth_altitude", item.getRthAltitude());
            map.put("created_at", item.getCreatedAt());
            map.put("user_id", item.getUserId());
            tasks.add(map);
        }
        return tasks;
    }

    /**
     * 将 SDK 返回的飞行轨迹结果映射为 MCP 响应格式。
     *
     * @param taskUuid 任务 UUID（用于响应中回显）
     * @param result   SDK 返回的轨迹查询结果
     * @return 映射后的轨迹数据
     */
    private Map<String, Object> mapFlightTaskTrack(String taskUuid, GetFlightTaskTrackResults result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);

        FlightTrackInfo track = result != null ? result.getTrack() : null;
        List<Map<String, Object>> points = new ArrayList<>();
        if (track == null) {
            data.put("points", points);
            return data;
        }

        data.put("track_id", track.getTrackId());
        data.put("drone_sn", track.getDroneSn());
        data.put("flight_distance", track.getFlightDistance());
        data.put("flight_duration", track.getFlightDuration());

        if (track.getPoints() != null) {
            for (FlightTrackPoint point : track.getPoints()) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("timestamp", point.getTimestamp());
                p.put("latitude", point.getLatitude());
                p.put("longitude", point.getLongitude());
                p.put("height", point.getHeight());
                points.add(p);
            }
        }
        data.put("points", points);
        return data;
    }

    /**
     * 从参数中解析整数列表（用于 list_command 的 status 过滤条件）。
     *
     * @param args 输入参数 Map
     * @param key  参数名
     * @return 整数列表；参数缺失或非 List 类型时返回 null
     */
    private static List<Integer> parseIntList(Map<String, Object> args, String key) {
        Object val = args != null ? args.get(key) : null;
        if (val instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof Number)
                    .map(item -> ((Number) item).intValue())
                    .toList();
        }
        return null;
    }
}
