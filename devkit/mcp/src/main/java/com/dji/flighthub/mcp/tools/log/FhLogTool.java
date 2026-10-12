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

package com.dji.flighthub.mcp.tools.log;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.CreateDeviceLogReportRequestParam;
import com.dji.flighthubsdk.bean.CreateDeviceLogReportResult;
import com.dji.flighthubsdk.bean.DeviceLogListResult;
import com.dji.flighthubsdk.bean.DeviceLogListResultFileItem;
import com.dji.flighthubsdk.bean.LogReportListResult;
import com.dji.flighthubsdk.bean.LogReportListResultItem;
import com.dji.flighthubsdk.core.LogManagementModule;
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
 * fh_log — 设备日志管理（查询/创建报告/列表/操作/上传）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>query — 查询设备可用日志模块（L0）</li>
 *   <li>create_report — 创建日志报告（L0）</li>
 *   <li>list_reports — 列出日志报告（L0）</li>
 *   <li>operate — 操作日志报告（下载/删除）（L1 delete 需 confirm）</li>
 *   <li>upload — 上传日志文件（L0）</li>
 * </ul>
 *
 * <p>工具级不需要 human confirmation（仅 delete 操作在 action 内部拦截）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhLogTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhLogTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_log";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            "query", "create_report", "list_reports", "operate", "upload");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_MODULE_LIST = "module_list";
    private static final String PARAM_START_TIME = "start_time";
    private static final String PARAM_END_TIME = "end_time";
    private static final String PARAM_REPORT_ID = "report_id";
    private static final String PARAM_OPERATION = "operation";
    private static final String PARAM_FILE_PATH = "file_path";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhLogTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Device log management: query available log modules, create/list log reports, "
                + "operate (download/delete) reports, upload log files. "
                + "Actions: query, create_report, list_reports, operate, upload.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation to perform"
        ));
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Device serial number (for query/create_report/list_reports)"
        ));
        props.put(PARAM_MODULE_LIST, Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Module IDs to include in log report (for create_report)"
        ));
        props.put(PARAM_START_TIME, Map.of(
                "type", "string",
                "description", "Start time for log collection (ISO format)"
        ));
        props.put(PARAM_END_TIME, Map.of(
                "type", "string",
                "description", "End time for log collection (ISO format)"
        ));
        props.put(PARAM_REPORT_ID, Map.of(
                "type", "string",
                "description", "Report ID (for operate action)"
        ));
        props.put(PARAM_OPERATION, Map.of(
                "type", "string",
                "description", "Operation type: download or delete (for operate action)"
        ));
        props.put(PARAM_FILE_PATH, Map.of(
                "type", "string",
                "description", "Local file path (for upload action)"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number for list_reports (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Items per page for list_reports (default: 20)"
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Set true to confirm destructive operations (e.g. delete)"
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

        switch (action) {
            case "query":
                return executeQuery(args);
            case "create_report":
                return executeCreateReport(args);
            case "list_reports":
                return executeListReports(args);
            case "operate":
                return executeOperate(args);
            case "upload":
                return executeUpload(args);
            default:
                return errorResult("Unhandled action: " + action, null);
        }
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== query（L0） ====================

    private Map<String, Object> executeQuery(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device SN to query available log modules.");
        }

        log.info("[{}] query: deviceSn={}", TOOL_NAME, deviceSn);

        return sdkService.isMockMode()
                ? queryInMockMode(deviceSn)
                : queryWithRealSdk(deviceSn, args);
    }

    private Map<String, Object> queryInMockMode(String deviceSn) {
        Map<String, Object> module1 = new LinkedHashMap<>();
        module1.put("module_id", 1);
        module1.put("module_name", "Flight Controller");

        Map<String, Object> module2 = new LinkedHashMap<>();
        module2.put("module_id", 2);
        module2.put("module_name", "Battery");

        Map<String, Object> module3 = new LinkedHashMap<>();
        module3.put("module_id", 3);
        module3.put("module_name", "Gimbal");

        Map<String, Object> module4 = new LinkedHashMap<>();
        module4.put("module_id", 4);
        module4.put("module_name", "Remote Controller");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("modules", List.of(module1, module2, module3, module4));
        data.put("_mock", true);
        return successResult(data, "Available log modules retrieved.");
    }

    @SuppressWarnings("unchecked")
    @Generated
    private Map<String, Object> queryWithRealSdk(String deviceSn, Map<String, Object> args) {
        try {
            LogManagementModule lmm = getLogManagementModule();
            if (lmm == null) {
                return errorResult("LogManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            // 解析 module_list 参数（可选，传空列表查询全部）
            List<Integer> moduleList = getIntListArg(args, PARAM_MODULE_LIST);

            CompletableFuture<DeviceLogListResult> future = new CompletableFuture<>();
            lmm.queryDeviceLogList(deviceSn, moduleList, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "queryDeviceLogList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            DeviceLogListResult result = SdkCallbackBridge.await(future);

            // 映射结果
            List<Map<String, Object>> files = new ArrayList<>();
            if (result.getFiles() != null) {
                for (DeviceLogListResultFileItem item : result.getFiles()) {
                    Map<String, Object> fileMap = new LinkedHashMap<>();
                    fileMap.put("device_sn", item.getDeviceSn());
                    fileMap.put("module", item.getModule());
                    fileMap.put("log_type", item.getLogType());
                    fileMap.put("object_key", item.getObjectKey());
                    files.add(fileMap);
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("files", files);
            return successResult(data, "Device log list retrieved (" + files.size() + " items).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] query timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] query SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] query error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to query device logs: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== create_report（L0） ====================

    private Map<String, Object> executeCreateReport(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device SN to create a log report.");
        }

        log.info("[{}] create_report: deviceSn={}", TOOL_NAME, deviceSn);

        return sdkService.isMockMode()
                ? createReportInMockMode(deviceSn)
                : createReportWithRealSdk(deviceSn, args);
    }

    private Map<String, Object> createReportInMockMode(String deviceSn) {
        String reportId = "RPT-" + System.currentTimeMillis();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("report_id", reportId);
        data.put("status", "generating");
        data.put("_mock", true);
        return successResult(data, "Log report creation initiated.");
    }

    @Generated
    private Map<String, Object> createReportWithRealSdk(String deviceSn, Map<String, Object> args) {
        try {
            LogManagementModule lmm = getLogManagementModule();
            if (lmm == null) {
                return errorResult("LogManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CreateDeviceLogReportRequestParam param = new CreateDeviceLogReportRequestParam();
            // 设置设备信息（通过 reportInformation 传入设备 SN）
            param.setReportInformation(deviceSn);

            CompletableFuture<CreateDeviceLogReportResult> future = new CompletableFuture<>();
            lmm.createDeviceLogReport(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "createDeviceLogReport failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            CreateDeviceLogReportResult result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("report_uuid", result.getReportUuid());
            data.put("track_number", result.getTrackNumber());
            data.put("status", "generating");
            return successResult(data, "Log report creation initiated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] create_report timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] create_report SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] create_report error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to create log report: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== list_reports（L0） ====================

    private Map<String, Object> executeListReports(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        log.info("[{}] list_reports: deviceSn={}, page={}, pageSize={}", TOOL_NAME, deviceSn, page, pageSize);

        return sdkService.isMockMode()
                ? listReportsInMockMode(deviceSn, page, pageSize)
                : listReportsWithRealSdk(deviceSn, page, pageSize);
    }

    private Map<String, Object> listReportsInMockMode(String deviceSn, int page, int pageSize) {
        Map<String, Object> report1 = new LinkedHashMap<>();
        report1.put("report_id", "RPT-20250601120000");
        report1.put("device_sn", deviceSn != null ? deviceSn : "DRONE-001");
        report1.put("status", "completed");
        report1.put("created_at", "2025-06-01T12:00:00Z");

        Map<String, Object> report2 = new LinkedHashMap<>();
        report2.put("report_id", "RPT-20250602150000");
        report2.put("device_sn", deviceSn != null ? deviceSn : "DRONE-002");
        report2.put("status", "generating");
        report2.put("created_at", "2025-06-02T15:00:00Z");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reports", List.of(report1, report2));
        data.put("total", 2);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Log reports listed.");
    }

    @Generated
    private Map<String, Object> listReportsWithRealSdk(String deviceSn, int page, int pageSize) {
        try {
            LogManagementModule lmm = getLogManagementModule();
            if (lmm == null) {
                return errorResult("LogManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            String sn = (deviceSn != null) ? deviceSn : "";

            CompletableFuture<LogReportListResult> future = new CompletableFuture<>();
            lmm.logReportList(sn, page, pageSize, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "logReportList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            LogReportListResult result = SdkCallbackBridge.await(future);

            List<Map<String, Object>> reports = new ArrayList<>();
            if (result.getList() != null) {
                for (LogReportListResultItem item : result.getList()) {
                    Map<String, Object> report = new LinkedHashMap<>();
                    report.put("report_uuid", item.getReportUuid());
                    report.put("report_time", item.getReportTime());
                    report.put("report_information", item.getReportInformation());
                    reports.add(report);
                }
            }

            int total = (result.getPagination() != null) ? result.getPagination().getTotal() : reports.size();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("reports", reports);
            data.put("total", total);
            data.put("page", page);
            data.put("page_size", pageSize);
            return successResult(data, "Log reports listed (" + reports.size() + " items).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list_reports timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list_reports SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] list_reports error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list reports: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== operate（L1 for delete） ====================

    private Map<String, Object> executeOperate(Map<String, Object> args) {
        String reportId = getStringArg(args, PARAM_REPORT_ID, null);
        if (reportId == null) {
            return errorResult("Missing required parameter: report_id",
                    "Provide report ID for the operation.");
        }

        String operation = getStringArg(args, PARAM_OPERATION, null);
        if (operation == null) {
            return errorResult("Missing required parameter: operation",
                    "Provide operation type: download or delete.");
        }

        // delete 操作需要安全等级 L1 + 人工确认
        if ("delete".equals(operation)) {
            Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
            if (denied != null) {
                return denied;
            }
            Map<String, Object> confirmResult = requireHumanConfirm(args,
                    "delete", reportId,
                    "Delete log report: " + reportId);
            if (confirmResult != null) {
                return confirmResult;
            }
        }

        if (!"download".equals(operation) && !"delete".equals(operation)) {
            return errorResult("Invalid operation: " + operation,
                    "Supported operations: download, delete.");
        }

        log.info("[{}] operate: reportId={}, operation={}", TOOL_NAME, reportId, operation);

        return sdkService.isMockMode()
                ? operateInMockMode(reportId, operation)
                : operateWithRealSdk(reportId, operation);
    }

    private Map<String, Object> operateInMockMode(String reportId, String operation) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report_id", reportId);
        data.put("operation", operation);

        if ("download".equals(operation)) {
            data.put("download_url", "https://storage.flighthub.dji.com/logs/" + reportId + ".zip");
            data.put("_mock", true);
            return successResult(data, "Log report download URL generated.");
        }

        // delete
        data.put("status", "deleted");
        data.put("_mock", true);
        return successResult(data, "Log report deleted.");
    }

    @Generated
    private Map<String, Object> operateWithRealSdk(String reportId, String operation) {
        try {
            LogManagementModule lmm = getLogManagementModule();
            if (lmm == null) {
                return errorResult("LogManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            lmm.logReportOperation(operation, reportId, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "logReportOperation failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });
            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("report_id", reportId);
            data.put("operation", operation);
            data.put("status", "delete".equals(operation) ? "deleted" : "completed");

            String message = "download".equals(operation)
                    ? "Log report download initiated."
                    : "Log report deleted.";
            return successResult(data, message);

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] operate timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] operate SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] operate error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to operate on report: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== upload（L0） ====================

    private Map<String, Object> executeUpload(Map<String, Object> args) {
        String filePath = getStringArg(args, PARAM_FILE_PATH, null);
        if (filePath == null) {
            return errorResult("Missing required parameter: file_path",
                    "Provide local file path for log upload.");
        }

        log.info("[{}] upload: filePath={}", TOOL_NAME, filePath);

        return sdkService.isMockMode()
                ? uploadInMockMode(filePath)
                : uploadWithRealSdk(filePath);
    }

    private Map<String, Object> uploadInMockMode(String filePath) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("file_path", filePath);
        data.put("status", "uploaded");
        data.put("object_key", "logs/uploaded_" + System.currentTimeMillis() % 10000 + ".log");
        data.put("_mock", true);
        return successResult(data, "Log file uploaded.");
    }

    @Generated
    private Map<String, Object> uploadWithRealSdk(String filePath) {
        try {
            LogManagementModule lmm = getLogManagementModule();
            if (lmm == null) {
                return errorResult("LogManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            lmm.uploadLogAppendFile(filePath, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "uploadLogAppendFile failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });
            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("file_path", filePath);
            data.put("status", "uploaded");
            return successResult(data, "Log file uploaded.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] upload timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "File upload may take longer. Retry with larger timeout.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] upload SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check file path and permissions.");
        } catch (Exception e) {
            log.error("[{}] upload error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to upload log file: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 LogManagementModule 实例。
     *
     * @return LogManagementModule，不可用时返回 null
     */
    private LogManagementModule getLogManagementModule() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getLogManagementModule();
    }

    /**
     * 从参数中提取整型列表。
     */
    @SuppressWarnings("unchecked")
    private List<Integer> getIntListArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof List) {
            List<?> rawList = (List<?>) value;
            List<Integer> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Number) {
                    result.add(((Number) item).intValue());
                }
            }
            return result;
        }
        return List.of();
    }
}
