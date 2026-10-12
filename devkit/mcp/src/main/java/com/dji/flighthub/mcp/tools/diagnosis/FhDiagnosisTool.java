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

package com.dji.flighthub.mcp.tools.diagnosis;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.CreateSelfDiagnosisParams;
import com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListParams;
import com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults;
import com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults;
import com.dji.flighthubsdk.bean.OperateSelfDiagnosisParams;
import com.dji.flighthubsdk.core.DiagnosisManagementModule;
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
 * fh_diagnosis — 设备自诊断管理。
 *
 * <p>支持 action：
 * <ul>
 *   <li>create — 创建自诊断任务（L0）</li>
 *   <li>get_report — 获取单份诊断报告（L0）</li>
 *   <li>list_reports — 分页查询诊断报告列表（L0）</li>
 *   <li>operate — 操作诊断（如取消）（L0）</li>
 * </ul>
 *
 * <p>所有操作均为 L0，无需人工确认。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDiagnosisTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDiagnosisTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_diagnosis";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            "create", "get_report", "list_reports", "operate");

    /** 有效 action 集合 */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_PART = "part";
    private static final String PARAM_INDEX = "index";
    private static final String PARAM_REPORT_UUID = "report_uuid";
    private static final String PARAM_CMD_METHOD = "cmd_method";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";
    private static final String PARAM_STATUS = "status";

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /** SDK 回调默认超时（秒） */
    private static final int SDK_TIMEOUT_SECONDS = 15;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhDiagnosisTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Device self-diagnosis management: create diagnosis task, get/list diagnosis reports, "
                + "operate on diagnosis (cancel, etc). "
                + "Actions: create, get_report, list_reports, operate.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of("type", "string", "enum", VALID_ACTIONS, "description", "Operation to perform"));
        props.put(PARAM_DEVICE_SN, Map.of("type", "string", "description", "Device serial number (for create, list_reports filter)"));
        props.put(PARAM_TYPE, Map.of("type", "integer", "description", "Diagnosis type (for create): 0=comprehensive, 1=module"));
        props.put(PARAM_PART, Map.of("type", "integer", "description", "Part to diagnose (for create): 0=whole, 1=flight, 2=camera, etc."));
        props.put(PARAM_INDEX, Map.of("type", "integer", "description", "Part index (for create, default 0)"));
        props.put(PARAM_REPORT_UUID, Map.of("type", "string", "description", "Report UUID (for get_report, operate)"));
        props.put(PARAM_CMD_METHOD, Map.of("type", "string", "description", "Operation command method (for operate): e.g. 'cancel'"));
        props.put(PARAM_STATUS, Map.of("type", "array", "items", Map.of("type", "string"), "description", "Status filter list (for list_reports)"));
        props.put(PARAM_PAGE, Map.of("type", "integer", "description", "Page number (default: 1)"));
        props.put(PARAM_PAGE_SIZE, Map.of("type", "integer", "description", "Items per page (default: 20)"));

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
            return errorResult("Invalid or missing action", "Valid actions: " + VALID_ACTIONS);
        }

        switch (action) {
            case "create":
                return executeCreate(args);
            case "get_report":
                return executeGetReport(args);
            case "list_reports":
                return executeListReports(args);
            case "operate":
                return executeOperate(args);
            default:
                return errorResult("Unhandled action: " + action, null);
        }
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== create（L0） ====================

    private Map<String, Object> executeCreate(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide the device serial number for diagnosis.");
        }

        int type = getIntArg(args, PARAM_TYPE, 0);
        int part = getIntArg(args, PARAM_PART, 0);
        int index = getIntArg(args, PARAM_INDEX, 0);

        log.info("[{}] create: deviceSn={}, type={}, part={}, index={}",
                TOOL_NAME, deviceSn, type, part, index);

        return sdkService.isMockMode()
                ? createInMockMode(deviceSn, type, part, index)
                : createWithRealSdk(deviceSn, type, part, index);
    }

    private Map<String, Object> createInMockMode(String deviceSn, int type, int part, int index) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("type", type);
        data.put("part", part);
        data.put("index", index);
        data.put("status", "pending");
        data.put("message", "Self-diagnosis task created successfully");
        data.put("_mock", true);
        return successResult(data, "Diagnosis task created.");
    }

    @Generated
    private Map<String, Object> createWithRealSdk(String deviceSn, int type, int part, int index) {
        try {
            DiagnosisManagementModule dmm = getDiagnosisModule();
            if (dmm == null) {
                return errorResult("DiagnosisManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CreateSelfDiagnosisParams params = new CreateSelfDiagnosisParams();
            params.setDeviceSn(deviceSn);
            params.setType(type);
            params.setPart(part);
            params.setIndex(index);

            // CreateSelfDiagnosisCallback 只有 onResult(FlightHubError)，无返回数据
            CompletableFuture<Void> future = new CompletableFuture<>();
            dmm.createSelfDiagnosis(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "createSelfDiagnosis failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future, SDK_TIMEOUT_SECONDS);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("type", type);
            data.put("part", part);
            data.put("status", "created");
            return successResult(data, "Diagnosis task created successfully.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] create timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] create SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] create error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to create diagnosis: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== get_report（L0） ====================

    private Map<String, Object> executeGetReport(Map<String, Object> args) {
        String reportUuid = getStringArg(args, PARAM_REPORT_UUID, null);
        if (reportUuid == null) {
            return errorResult("Missing required parameter: report_uuid",
                    "Provide the diagnosis report UUID.");
        }

        log.info("[{}] get_report: reportUuid={}", TOOL_NAME, reportUuid);

        return sdkService.isMockMode()
                ? getReportInMockMode(reportUuid)
                : getReportWithRealSdk(reportUuid);
    }

    private Map<String, Object> getReportInMockMode(String reportUuid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report_uuid", reportUuid);
        data.put("device_sn", "DRONE-M3D-001");
        data.put("status", "completed");
        data.put("result_code", 0);
        data.put("create_time", 1718433600000L);
        data.put("part", 0);
        data.put("origin", 1);
        data.put("_mock", true);
        return successResult(data, "Diagnosis report retrieved.");
    }

    @Generated
    private Map<String, Object> getReportWithRealSdk(String reportUuid) {
        try {
            DiagnosisManagementModule dmm = getDiagnosisModule();
            if (dmm == null) {
                return errorResult("DiagnosisManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<GetSelfDiagnosisReportResults> future = new CompletableFuture<>();
            dmm.getSelfDiagnosisReport(reportUuid, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getSelfDiagnosisReport failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetSelfDiagnosisReportResults report = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("report_uuid", report.getReportUuid());
            data.put("device_sn", report.getDeviceSn());
            data.put("status", report.getStatus());
            data.put("result_code", report.getResultCode());
            data.put("create_time", report.getCreateTime());
            data.put("part", report.getPart());
            data.put("origin", report.getOrigin());
            data.put("device_model_key", report.getDeviceModelKey());
            data.put("bid", report.getBid());
            return successResult(data, "Diagnosis report retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] get_report timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] get_report SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] get_report error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get diagnosis report: " + e.getMessage(), "Check logs for details.");
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

        log.info("[{}] list_reports: page={}, pageSize={}", TOOL_NAME, page, pageSize);

        return sdkService.isMockMode()
                ? listReportsInMockMode(page, pageSize)
                : listReportsWithRealSdk(args, page, pageSize);
    }

    private Map<String, Object> listReportsInMockMode(int page, int pageSize) {
        Map<String, Object> report1 = new LinkedHashMap<>();
        report1.put("report_uuid", "diag-report-001");
        report1.put("device_sn", "DRONE-M3D-001");
        report1.put("status", "completed");
        report1.put("result_code", 0);
        report1.put("create_time", 1718433600000L);

        Map<String, Object> report2 = new LinkedHashMap<>();
        report2.put("report_uuid", "diag-report-002");
        report2.put("device_sn", "DOCK-002");
        report2.put("status", "in_progress");
        report2.put("result_code", -1);
        report2.put("create_time", 1718520000000L);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reports", List.of(report1, report2));
        data.put("total", 2);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Diagnosis reports retrieved.");
    }

    @Generated
    private Map<String, Object> listReportsWithRealSdk(Map<String, Object> args, int page, int pageSize) {
        try {
            DiagnosisManagementModule dmm = getDiagnosisModule();
            if (dmm == null) {
                return errorResult("DiagnosisManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            GetSelfDiagnosisReportListParams params = new GetSelfDiagnosisReportListParams();
            params.setPage(page);
            params.setPageSize(pageSize);

            // 可选过滤条件
            String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
            if (deviceSn != null) {
                params.setDeviceSn(deviceSn);
            }

            List<String> statusFilter = getStringListArg(args, PARAM_STATUS);
            if (statusFilter != null && !statusFilter.isEmpty()) {
                params.setStatus(statusFilter);
            }

            CompletableFuture<GetSelfDiagnosisReportListResults> future = new CompletableFuture<>();
            dmm.getSelfDiagnosisReportList(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getSelfDiagnosisReportList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetSelfDiagnosisReportListResults result = SdkCallbackBridge.await(future);

            List<Map<String, Object>> reports = new ArrayList<>();
            if (result.getList() != null) {
                for (GetSelfDiagnosisReportResults item : result.getList()) {
                    Map<String, Object> reportMap = new LinkedHashMap<>();
                    reportMap.put("report_uuid", item.getReportUuid());
                    reportMap.put("device_sn", item.getDeviceSn());
                    reportMap.put("status", item.getStatus());
                    reportMap.put("result_code", item.getResultCode());
                    reportMap.put("create_time", item.getCreateTime());
                    reportMap.put("part", item.getPart());
                    reports.add(reportMap);
                }
            }

            int total = (result.getPagination() != null) ? result.getPagination().getTotal() : reports.size();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("reports", reports);
            data.put("total", total);
            data.put("page", page);
            data.put("page_size", pageSize);
            return successResult(data, "Diagnosis reports retrieved (" + reports.size() + " items).");

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

    // ==================== operate（L0） ====================

    private Map<String, Object> executeOperate(Map<String, Object> args) {
        String reportUuid = getStringArg(args, PARAM_REPORT_UUID, null);
        if (reportUuid == null) {
            return errorResult("Missing required parameter: report_uuid",
                    "Provide the diagnosis report UUID.");
        }

        String cmdMethod = getStringArg(args, PARAM_CMD_METHOD, null);
        if (cmdMethod == null) {
            return errorResult("Missing required parameter: cmd_method",
                    "Provide the operation command method (e.g. 'cancel').");
        }

        log.info("[{}] operate: reportUuid={}, cmdMethod={}", TOOL_NAME, reportUuid, cmdMethod);

        return sdkService.isMockMode()
                ? operateInMockMode(reportUuid, cmdMethod)
                : operateWithRealSdk(reportUuid, cmdMethod);
    }

    private Map<String, Object> operateInMockMode(String reportUuid, String cmdMethod) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report_uuid", reportUuid);
        data.put("cmd_method", cmdMethod);
        data.put("status", "success");
        data.put("_mock", true);
        return successResult(data, "Diagnosis operation executed.");
    }

    @Generated
    private Map<String, Object> operateWithRealSdk(String reportUuid, String cmdMethod) {
        try {
            DiagnosisManagementModule dmm = getDiagnosisModule();
            if (dmm == null) {
                return errorResult("DiagnosisManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            OperateSelfDiagnosisParams params = new OperateSelfDiagnosisParams();
            params.setReportUuid(reportUuid);
            params.setReportCmdMethod(cmdMethod);

            // OperateSelfDiagnosisCallback 只有 onResult(FlightHubError)，无返回数据
            CompletableFuture<Void> future = new CompletableFuture<>();
            dmm.operateSelfDiagnosis(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "operateSelfDiagnosis failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future, SDK_TIMEOUT_SECONDS);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("report_uuid", reportUuid);
            data.put("cmd_method", cmdMethod);
            data.put("status", "success");
            return successResult(data, "Diagnosis operation executed.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] operate timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] operate SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] operate error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to operate diagnosis: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 DiagnosisManagementModule 实例。
     *
     * @return DiagnosisManagementModule，不可用时返回 null
     */
    private DiagnosisManagementModule getDiagnosisModule() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getDiagnosisManagementModule();
    }
}
