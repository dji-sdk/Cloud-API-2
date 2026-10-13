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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
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
import java.util.concurrent.CompletableFuture;

/**
 * fh_wayline_status — 查询航线任务状态或列表。
 * 提供 task_uuid 时返回单个任务详情；否则返回任务列表（支持过滤和分页）。
 *
 * <p>安全等级：L0（只读查询，无需 confirm）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWaylineStatusTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWaylineStatusTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_wayline_status";

    /** 参数名常量 */
    private static final String PARAM_TASK_UUID = "task_uuid";
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 默认分页参数 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 有效状态过滤值 */
    private static final List<String> VALID_STATUSES = List.of(
            "CREATED", "EXECUTING", "PAUSED", "SUCCESS", "FAILED", "CANCELLED");

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhWaylineStatusTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
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
                "Query wayline task status. If task_uuid is provided, returns detail of that task. "
                + "Otherwise returns a paginated list of tasks, optionally filtered by drone_sn or status.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_TASK_UUID, Map.of(
                "type", "string",
                "description", "Task UUID to query detail. If omitted, returns task list."
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Filter task list by drone serial number (only for list mode)"
        ));
        props.put(PARAM_STATUS, Map.of(
                "type", "string",
                "enum", VALID_STATUSES,
                "description", "Filter task list by status (only for list mode)"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number (default: 1, only for list mode)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Items per page (default: 20, max: 100, only for list mode)"
        ));
        schema.put("properties", props);
        // 所有参数可选
        schema.put("required", List.of());
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 查询航线任务状态或列表。
     *
     * @param args 输入参数
     * @return 查询结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 分发：有 task_uuid → 查详情；否则 → 查列表
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid != null) {
            return queryTaskDetail(taskUuid);
        }
        return queryTaskList(args);
    }

    /**
     * 查询单个任务详情。
     */
    private Map<String, Object> queryTaskDetail(String taskUuid) {
        if (sdkService.isMockMode()) {
            return queryDetailInMockMode(taskUuid);
        }
        return queryDetailWithRealSdk(taskUuid);
    }

    /**
     * Mock 模式下查询任务详情。
     */
    private Map<String, Object> queryDetailInMockMode(String taskUuid) {
        log.info("[MOCK] Querying wayline task detail: uuid={}", taskUuid);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("task_name", "Mock-Wayline-Task");
        data.put("drone_sn", "MOCK-DRONE-001");
        data.put("status", "EXECUTING");
        data.put("progress", 45);
        data.put("out_of_control_action", "return_home");
        data.put("created_time", System.currentTimeMillis() - 300000L);
        data.put("started_time", System.currentTimeMillis() - 240000L);
        data.put("_mock", true);

        return successResult(data, "Task detail retrieved.");
    }

    /**
     * 真实 SDK 模式下查询任务详情。
     * 通过 FlightTaskModule.getFlightTaskDetail 获取完整任务信息。
     */
    @Generated
    private Map<String, Object> queryDetailWithRealSdk(String taskUuid) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            CompletableFuture<TaskDetail> future = new CompletableFuture<>();
            ftm.getFlightTaskDetail(taskUuid, (error, detail) -> {
                if (error.isSuccess()) {
                    future.complete(detail);
                } else {
                    String message = (error.getServerMessage() != null && !error.getServerMessage().isBlank())
                            ? error.getServerMessage()
                            : "errorCode=" + error.getErrorCode() + ", server=" + error.getServerCode();
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(), message));
                }
            });
            TaskDetail detail = SdkCallbackBridge.await(future);

            // 将 TaskDetail 映射到响应字段
            Map<String, Object> data = mapTaskDetailToResponse(detail);
            return successResult(data, "Task detail retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_wayline_status detail timeout for task {}: {}", taskUuid, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_wayline_status detail SDK error for task {}: [{}] {}", taskUuid, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Retry or check connectivity.");
        } catch (Exception e) {
            log.error("fh_wayline_status detail error for task {}: {}", taskUuid, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 将 TaskDetail 对象映射为 MCP 响应数据。
     */
    private Map<String, Object> mapTaskDetailToResponse(TaskDetail detail) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", detail.getUuid());
        data.put("task_name", detail.getName());
        data.put("task_type", detail.getTaskType());
        data.put("status", detail.getStatus());
        data.put("drone_sn", detail.getSn());
        data.put("wayline_uuid", detail.getWaylineUuid());
        data.put("begin_at", detail.getBeginAt());
        data.put("end_at", detail.getEndAt());
        data.put("current_waypoint_index", detail.getCurrentWaypointIndex());
        data.put("total_waypoints", detail.getTotalWaypoints());
        data.put("rth_altitude", detail.getRthAltitude());
        data.put("rth_mode", detail.getRthMode());
        data.put("out_of_control_action", detail.getOutOfControlActionInFlight());
        data.put("created_at", detail.getCreatedAt());
        data.put("completed_at", detail.getCompletedAt());
        return data;
    }

    /**
     * 查询任务列表。
     */
    private Map<String, Object> queryTaskList(Map<String, Object> args) {
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        String status = getStringArg(args, PARAM_STATUS, null);
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);

        // 校验分页范围
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        if (sdkService.isMockMode()) {
            return queryListInMockMode(droneSn, status, page, pageSize);
        }
        return queryListWithRealSdk(droneSn, status, page, pageSize);
    }

    /**
     * Mock 模式下查询任务列表。
     */
    private Map<String, Object> queryListInMockMode(String droneSn, String status, int page, int pageSize) {
        log.info("[MOCK] Querying wayline task list: droneSn={}, status={}, page={}, pageSize={}",
                droneSn, status, page, pageSize);

        // 返回 mock 示例数据
        Map<String, Object> task1 = new LinkedHashMap<>();
        task1.put("task_uuid", "task_mock_001");
        task1.put("task_name", "Inspection Route A");
        task1.put("drone_sn", droneSn != null ? droneSn : "MOCK-DRONE-001");
        task1.put("status", status != null ? status : "EXECUTING");
        task1.put("progress", 72);

        Map<String, Object> task2 = new LinkedHashMap<>();
        task2.put("task_uuid", "task_mock_002");
        task2.put("task_name", "Mapping Mission B");
        task2.put("drone_sn", droneSn != null ? droneSn : "MOCK-DRONE-001");
        task2.put("status", "SUCCESS");
        task2.put("progress", 100);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tasks", List.of(task1, task2));
        data.put("total", 2);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);

        return successResult(data, "Task list retrieved.");
    }

    /**
     * 真实 SDK 模式下查询任务列表。
     * 通过 FlightTaskModule.getFlightTaskList 分页获取任务列表。
     */
    @Generated
    private Map<String, Object> queryListWithRealSdk(String droneSn, String status, int page, int pageSize) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            FlightTaskListParams params = new FlightTaskListParams();
            params.setPage(page);
            params.setPageSize(pageSize);
            if (droneSn != null) {
                params.setSn(List.of(droneSn));
            }
            if (status != null) {
                params.setStatus(List.of(mapStatusToCode(status)));
            }

            CompletableFuture<FlightTaskListResult> future = new CompletableFuture<>();
            ftm.getFlightTaskList(params, (error, result) -> {
                if (error.isSuccess()) {
                    future.complete(result);
                } else {
                    String message = (error.getServerMessage() != null && !error.getServerMessage().isBlank())
                            ? error.getServerMessage()
                            : "errorCode=" + error.getErrorCode() + ", server=" + error.getServerCode();
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(), message));
                }
            });
            FlightTaskListResult listResult = SdkCallbackBridge.await(future);

            // 映射任务列表
            List<Map<String, Object>> tasks = new ArrayList<>();
            if (listResult.getList() != null) {
                for (TaskDetail detail : listResult.getList()) {
                    tasks.add(mapTaskDetailToResponse(detail));
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tasks", tasks);
            data.put("total", listResult.getPagination() != null
                    ? listResult.getPagination().getTotal() : tasks.size());
            data.put("page", page);
            data.put("page_size", pageSize);

            return successResult(data, "Task list retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_wayline_status list timeout: {}", e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_wayline_status list SDK error: [{}] {}", e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Retry or check connectivity.");
        } catch (Exception e) {
            log.error("fh_wayline_status list error: {}", e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 将状态字符串映射为 SDK 使用的整型状态码。
     * CREATED=1, EXECUTING=2, PAUSED=3, CANCELLED=4, SUCCESS=5, FAILED=6
     * （参照 Go SDK POITaskStatus 枚举语义）
     */
    private int mapStatusToCode(String status) {
        return switch (status) {
            case "CREATED" -> 1;
            case "EXECUTING" -> 2;
            case "PAUSED" -> 3;
            case "CANCELLED" -> 4;
            case "SUCCESS" -> 5;
            case "FAILED" -> 6;
            default -> 0;
        };
    }
}
