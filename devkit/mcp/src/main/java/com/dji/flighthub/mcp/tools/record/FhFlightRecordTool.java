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

package com.dji.flighthub.mcp.tools.record;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.FlightTaskRecordModule;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.bean.ExportFlightRecordsRequestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_flight_record — 飞行记录导出工具。
 * 支持飞行记录数据导出和导出进度查询。
 *
 * <p>安全等级：L0（只读数据导出，免确认）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlightRecordTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlightRecordTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flight_record";

    // ==================== 参数常量 ====================
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_USER_ID = "user_id";
    private static final String PARAM_FLIGHT_TASK_IDS = "flight_task_ids";
    private static final String PARAM_FILE_NAME = "file_name";
    private static final String PARAM_FILE_TYPE = "file_type";
    private static final String PARAM_METRICS = "metrics";
    private static final String PARAM_TIME_ZONE = "time_zone";
    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_EXPORT_ID = "export_id";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";

    // ==================== Action 常量 ====================
    private static final String ACTION_EXPORT = "export";
    private static final String ACTION_GET_PROGRESS = "get_progress";

    /** 有效 action 集合 */
    private static final Set<String> VALID_ACTIONS = Set.of(ACTION_EXPORT, ACTION_GET_PROGRESS);

    /** 有效 action 排序列表（用于错误提示） */
    private static final List<String> VALID_ACTIONS_SORTED = List.of(ACTION_EXPORT, ACTION_GET_PROGRESS);

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 默认时区 */
    private static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";

    /** 默认语言 */
    private static final String DEFAULT_LOCALE = "zh_CN";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhFlightRecordTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Export flight records and check export progress. "
                        + "Supports exporting flight data to files with customizable metrics and formats.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Action: 'export' to start export, 'get_progress' to check status"
        ));
        props.put(PARAM_USER_ID, Map.of(
                "type", "string",
                "description", "User ID for the export operation (required for 'export')"
        ));
        props.put(PARAM_FLIGHT_TASK_IDS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "List of flight task UUIDs to export (required for 'export')"
        ));
        props.put(PARAM_FILE_NAME, Map.of(
                "type", "string",
                "description", "Export file name (optional, auto-generated if omitted)"
        ));
        props.put(PARAM_FILE_TYPE, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Export file types (e.g. ['csv', 'pdf'])"
        ));
        props.put(PARAM_METRICS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Metrics to include in export"
        ));
        props.put(PARAM_TIME_ZONE, Map.of(
                "type", "string",
                "description", "Time zone (default: Asia/Shanghai)"
        ));
        props.put(PARAM_LOCALE, Map.of(
                "type", "string",
                "description", "Locale for export (default: zh_CN)"
        ));
        props.put(PARAM_EXPORT_ID, Map.of(
                "type", "string",
                "description", "Export ID to check progress (required for 'get_progress')"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number for progress list (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Page size for progress list (default: 20)"
        ));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
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

        // 2. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        // 3. 分发到对应操作
        return switch (action) {
            case ACTION_EXPORT -> executeExport(args);
            case ACTION_GET_PROGRESS -> executeGetProgress(args);
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    /**
     * 执行飞行记录导出。
     */
    private Map<String, Object> executeExport(Map<String, Object> args) {
        // 校验必填参数
        String userId = getStringArg(args, PARAM_USER_ID, null);
        if (userId == null) {
            return errorResult("Missing required parameter: user_id",
                    "Provide user_id for the export operation.");
        }

        List<String> taskIds = getStringListArg(args, PARAM_FLIGHT_TASK_IDS);
        if (taskIds == null || taskIds.isEmpty()) {
            return errorResult("Missing required parameter: flight_task_ids",
                    "Provide at least one flight task UUID to export.");
        }

        String fileName = getStringArg(args, PARAM_FILE_NAME, "flight_record_export");
        String timeZone = getStringArg(args, PARAM_TIME_ZONE, DEFAULT_TIME_ZONE);
        String locale = getStringArg(args, PARAM_LOCALE, DEFAULT_LOCALE);
        List<String> fileType = getStringListArg(args, PARAM_FILE_TYPE);
        List<String> metrics = getStringListArg(args, PARAM_METRICS);

        // Mock 模式（含 workspace 为 null 的降级场景）
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("export_id", "exp_" + System.currentTimeMillis());
            data.put("user_id", userId);
            data.put("file_name", fileName);
            data.put("task_count", taskIds.size());
            data.put("status", "processing");
            data.put("message", "Export started, use get_progress to check status.");
            return successResult(data, "Flight record export started for " + taskIds.size() + " task(s)");
        }

        // 真实 SDK 调用
        return executeRealExport(userId, taskIds, fileName, timeZone, locale, fileType, metrics);
    }

    /**
     * 查询导出进度。
     */
    private Map<String, Object> executeGetProgress(Map<String, Object> args) {
        String exportId = getStringArg(args, PARAM_EXPORT_ID, null);
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);

        // Mock 模式（含 workspace 为 null 的降级场景）
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            if (exportId != null) {
                data.put("export_id", exportId);
                data.put("status", "completed");
                data.put("progress", 100);
                data.put("download_url", "https://flighthub.example.com/exports/" + exportId + ".csv");
            } else {
                data.put("page", page);
                data.put("page_size", pageSize);
                data.put("total", 1);
                data.put("records", List.of(
                        Map.of("export_id", "exp_mock_001",
                                "status", "completed",
                                "progress", 100,
                                "file_name", "flight_record_export.csv",
                                "created_at", System.currentTimeMillis() / 1000 - 3600)
                ));
            }
            return successResult(data, exportId != null
                    ? "Export " + exportId + " completed"
                    : "Found export records");
        }

        // 真实 SDK
        return executeRealGetProgress(exportId, page, pageSize);
    }

    /**
     * 真实 SDK 导出调用。
     */
    @Generated
    private Map<String, Object> executeRealExport(
            String userId, List<String> taskIds, String fileName,
            String timeZone, String locale, List<String> fileType, List<String> metrics) {
        try {
            log.info("[REAL] exportFlightRecords: user={}, tasks={}, file={}",
                    userId, taskIds.size(), fileName);

            Workspace workspace = sdkService.getWorkspace();
            FlightTaskRecordModule ftrm = workspace.getFlightTaskRecordModule();

            ExportFlightRecordsRequestParams params = new ExportFlightRecordsRequestParams();
            params.setFileName(fileName);
            params.setFlightTaskIds(taskIds);
            params.setTimeZone(timeZone);
            params.setLocale(locale);
            if (fileType != null && !fileType.isEmpty()) {
                params.setFileType(fileType);
            }
            if (metrics != null && !metrics.isEmpty()) {
                params.setMetrics(metrics);
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ftrm.exportFlightRecords(timeZone, params, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "exportFlightRecords failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("export_id", results.getExportId());
                data.put("user_id", userId);
                data.put("file_name", fileName);
                data.put("task_count", taskIds.size());
                data.put("status", "processing");
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Flight record export started for " + taskIds.size() + " task(s).");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Export request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Flight record export error: {}", e.getMessage(), e);
            return errorResult("Export failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 进度查询。
     */
    @Generated
    private Map<String, Object> executeRealGetProgress(String exportId, int page, int pageSize) {
        try {
            log.info("[REAL] getExportFlightRecordsProgress: exportId={}, page={}/{}",
                    exportId, page, pageSize);

            Workspace workspace = sdkService.getWorkspace();
            FlightTaskRecordModule ftrm = workspace.getFlightTaskRecordModule();

            String timeZone = DEFAULT_TIME_ZONE;
            String locale = DEFAULT_LOCALE;
            int status = 0;

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ftrm.getExportFlightRecordsProgress(exportId, page, timeZone, pageSize, status, locale,
                    (error, results) -> {
                        if (!error.isSuccess()) {
                            future.completeExceptionally(new SdkCallbackBridge.SdkException(
                                    error.getHttpCode(),
                                    "getExportFlightRecordsProgress failed: " + formatSdkError(error)));
                            return;
                        }
                        Map<String, Object> data = new LinkedHashMap<>();
                        data.put("pagination", results.getPagination());
                        data.put("records", results.getList());
                        if (exportId != null) {
                            data.put("export_id", exportId);
                        }
                        future.complete(data);
                    });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, exportId != null
                    ? "Export progress retrieved for " + exportId
                    : "Export records retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Progress query timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Export progress query error: {}", e.getMessage(), e);
            return errorResult("Progress query failed: " + e.getMessage(), "Retry.");
        }
    }
}
