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

package com.dji.flighthub.mcp.tools.workspace;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam;
import com.dji.flighthubsdk.bean.DuplicateWaylineParam;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.SubmitWaylineInfoParam;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.core.WaylineManageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_workspace — 航线文件（Wayline）管理工具。
 * 支持航线列表查询、详情查看、上传、更新、复制、批量删除和批量下载。
 *
 * <p>安全等级：L0（大部分操作为只读/创建），batch_delete 为 L1（不可逆删除，需确认）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWorkspaceTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWorkspaceTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_workspace";

    // ==================== 参数常量 ====================
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_WAYLINE_UUID = "wayline_uuid";
    private static final String PARAM_WAYLINE_UUIDS = "wayline_uuids";
    private static final String PARAM_WAYLINE_NAME = "wayline_name";
    private static final String PARAM_FILE_PATH = "file_path";
    private static final String PARAM_DRONE_TYPE = "drone_type";
    private static final String PARAM_PAYLOAD_TYPE = "payload_type";
    private static final String PARAM_DRONE_TYPES = "drone_types";
    private static final String PARAM_PAYLOAD_TYPES = "payload_types";
    private static final String PARAM_DATE_START = "date_start";
    private static final String PARAM_DATE_END = "date_end";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";
    private static final String PARAM_CONFIRM = "confirm";

    // ==================== Action 常量 ====================
    private static final String ACTION_LIST = "list";
    private static final String ACTION_DETAIL = "detail";
    private static final String ACTION_SUBMIT = "submit";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DUPLICATE = "duplicate";
    private static final String ACTION_BATCH_DELETE = "batch_delete";
    private static final String ACTION_BATCH_DOWNLOAD = "batch_download";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_LIST, ACTION_DETAIL, ACTION_SUBMIT, ACTION_UPDATE,
            ACTION_DUPLICATE, ACTION_BATCH_DELETE, ACTION_BATCH_DOWNLOAD
    );

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 分页上限 */
    private static final int MAX_PAGE_SIZE = 100;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhWorkspaceTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Manage wayline files in the workspace library. "
                + "Actions: list, detail, submit, update, duplicate, batch_delete, batch_download. "
                + "WHEN TO USE (action=submit): You have a LOCAL KMZ file to upload to workspace. "
                + "AFTER submit: use returned wayline_uuid with fh_flight_task(action=create). "
                + "WHEN TO USE (action=list): Browse all workspace waylines (returns all, no device/distance filter). "
                + "DO NOT USE (action=list) WHEN: user has a specific dock and wants flyable/reachable waylines "
                + "→ use fh_wayline_recommend instead (it filters by drone model + distance; list returns all "
                + "waylines regardless of dock, which may recommend a faraway wayline the dock cannot reach).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Action to perform: list, detail, submit, update, duplicate, batch_delete, batch_download"
        ));
        props.put(PARAM_WAYLINE_UUID, Map.of(
                "type", "string",
                "description", "Wayline UUID (required for detail, update, duplicate)"
        ));
        props.put(PARAM_WAYLINE_UUIDS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "List of wayline UUIDs (required for batch_delete, batch_download)"
        ));
        props.put(PARAM_WAYLINE_NAME, Map.of(
                "type", "string",
                "description", "Wayline name (required for submit, update)"
        ));
        props.put(PARAM_FILE_PATH, Map.of(
                "type", "string",
                "description", "Path to wayline KMZ file (required for submit)"
        ));
        props.put(PARAM_DRONE_TYPE, Map.of(
                "type", "string",
                "description", "Drone type filter for list"
        ));
        props.put(PARAM_PAYLOAD_TYPE, Map.of(
                "type", "string",
                "description", "Payload type filter for list"
        ));
        props.put(PARAM_DRONE_TYPES, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Drone types filter for list (multi-select)"
        ));
        props.put(PARAM_PAYLOAD_TYPES, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Payload types filter for list (multi-select)"
        ));
        props.put(PARAM_DATE_START, Map.of(
                "type", "string",
                "description", "Start date filter for list (ISO format)"
        ));
        props.put(PARAM_DATE_END, Map.of(
                "type", "string",
                "description", "End date filter for list (ISO format)"
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Page size (default: 20)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Confirmation flag for destructive actions (batch_delete)"
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
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 3. 分发到对应操作
        return switch (action) {
            case ACTION_LIST -> executeList(args);
            case ACTION_DETAIL -> executeDetail(args);
            case ACTION_SUBMIT -> executeSubmit(args);
            case ACTION_UPDATE -> executeUpdate(args);
            case ACTION_DUPLICATE -> executeDuplicate(args);
            case ACTION_BATCH_DELETE -> executeBatchDelete(args);
            case ACTION_BATCH_DOWNLOAD -> executeBatchDownload(args);
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    /**
     * 列出航线文件（支持可选过滤和分页）。
     */
    private Map<String, Object> executeList(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            long now = System.currentTimeMillis() / 1000;
            List<Map<String, Object>> waylines = List.of(
                    Map.of(
                            "uuid", "wl_mock_001",
                            "name", "Survey Route A",
                            "drone_type", "M350 RTK",
                            "payload_type", "Zenmuse L2",
                            "created_at", now - 86400,
                            "updated_at", now - 3600
                    ),
                    Map.of(
                            "uuid", "wl_mock_002",
                            "name", "Inspection Path B",
                            "drone_type", "Matrice 30T",
                            "payload_type", "Zenmuse H20T",
                            "created_at", now - 172800,
                            "updated_at", now - 7200
                    )
            );

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("page", page);
            data.put("page_size", pageSize);
            data.put("total", 2);
            data.put("waylines", waylines);
            return successResult(data, "Found 2 wayline(s)");
        }

        // 真实 SDK 调用
        return executeRealList(args, page, pageSize);
    }

    /**
     * 查询航线详情。
     */
    private Map<String, Object> executeDetail(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Provide wayline_uuid for detail action.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            long now = System.currentTimeMillis() / 1000;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("uuid", waylineUuid);
            data.put("name", "Survey Route A");
            data.put("md5", "a1b2c3d4e5f6789012345678abcdef01");
            data.put("kmz_key", "waylines/" + waylineUuid + "/route.kmz");
            data.put("download_url", "https://flighthub.example.com/waylines/" + waylineUuid + "/download");
            data.put("created_at", now - 86400);
            data.put("updated_at", now - 3600);
            return successResult(data, "Wayline detail for " + waylineUuid);
        }

        // 真实 SDK 调用
        return executeRealDetail(waylineUuid);
    }

    /**
     * 上传航线文件。
     */
    private Map<String, Object> executeSubmit(Map<String, Object> args) {
        String waylineName = getStringArg(args, PARAM_WAYLINE_NAME, null);
        if (waylineName == null) {
            return errorResult("Missing required parameter: wayline_name",
                    "Provide wayline_name for the uploaded wayline.");
        }

        String filePath = getStringArg(args, PARAM_FILE_PATH, null);
        if (filePath == null) {
            return errorResult("Missing required parameter: file_path",
                    "Provide file_path to the KMZ wayline file.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            String mockWaylineUuid = "wl_" + System.currentTimeMillis();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("request_id", "req_" + System.currentTimeMillis());
            data.put("wayline_uuid", mockWaylineUuid);
            data.put("status", "uploaded");
            List<Map<String, Object>> nextSteps = List.of(
                    Map.of("action", "Create a flight task using this wayline",
                           "tool", "fh_flight_task",
                           "params_hint", Map.of(
                                   "action", "create",
                                   "wayline_uuid", mockWaylineUuid,
                                   "sn", "<DOCK_SN>",
                                   "task_type", 1,
                                   "rth_altitude", 120.0))
            );
            return successResultWithNextSteps(data,
                    "Wayline '" + waylineName + "' submitted successfully.", nextSteps);
        }

        // 真实 SDK 调用
        return executeRealSubmit(waylineName, filePath, args);
    }

    /**
     * 更新航线信息。
     */
    private Map<String, Object> executeUpdate(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Provide wayline_uuid for the wayline to update.");
        }

        String waylineName = getStringArg(args, PARAM_WAYLINE_NAME, null);
        if (waylineName == null) {
            return errorResult("Missing required parameter: wayline_name",
                    "Provide the new wayline_name.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("uuid", waylineUuid);
            data.put("name", waylineName);
            data.put("status", "updated");
            return successResult(data, "Wayline " + waylineUuid + " updated");
        }

        // 真实 SDK 调用
        return executeRealUpdate(waylineUuid, waylineName);
    }

    /**
     * 复制航线。
     */
    private Map<String, Object> executeDuplicate(Map<String, Object> args) {
        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Provide wayline_uuid of the wayline to duplicate.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("source_uuid", waylineUuid);
            data.put("new_uuid", "wl_dup_" + System.currentTimeMillis());
            data.put("status", "duplicated");
            return successResult(data, "Wayline " + waylineUuid + " duplicated");
        }

        // 真实 SDK 调用
        return executeRealDuplicate(waylineUuid);
    }

    /**
     * 批量删除航线（不可逆，需确认）。
     */
    private Map<String, Object> executeBatchDelete(Map<String, Object> args) {
        List<String> uuids = getStringListArg(args, PARAM_WAYLINE_UUIDS);
        if (uuids == null || uuids.isEmpty()) {
            return errorResult("Missing required parameter: wayline_uuids",
                    "Provide a list of wayline UUIDs to delete.");
        }

        // 安全等级 L1 硬拦截 + 确认校验
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Map<String, Object> confirmResult = requireHumanConfirm(args, "batch_delete",
                uuids.size() + " wayline(s)",
                "Permanently delete " + uuids.size() + " wayline(s). This action cannot be undone.");
        if (confirmResult != null) {
            return confirmResult;
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deleted_count", uuids.size());
            data.put("deleted_uuids", uuids);
            return successResult(data, "Deleted " + uuids.size() + " wayline(s)");
        }

        // 真实 SDK 调用
        return executeRealBatchDelete(uuids);
    }

    /**
     * 批量下载航线。
     */
    private Map<String, Object> executeBatchDownload(Map<String, Object> args) {
        List<String> uuids = getStringListArg(args, PARAM_WAYLINE_UUIDS);
        if (uuids == null || uuids.isEmpty()) {
            return errorResult("Missing required parameter: wayline_uuids",
                    "Provide a list of wayline UUIDs to download.");
        }

        // Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            List<Map<String, Object>> downloads = new ArrayList<>();
            for (String uuid : uuids) {
                downloads.add(Map.of(
                        "uuid", uuid,
                        "download_url", "https://flighthub.example.com/waylines/" + uuid + "/download"
                ));
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", uuids.size());
            data.put("downloads", downloads);
            return successResult(data, "Download URLs generated for " + uuids.size() + " wayline(s)");
        }

        // 真实 SDK 调用
        return executeRealBatchDownload(uuids);
    }

    // ==================== 真实 SDK 调用（待实现） ====================

    /**
     * 真实 SDK 列表查询。
     */
    private Map<String, Object> executeRealList(Map<String, Object> args, int page, int pageSize) {
        try {
            log.info("[REAL] listWaylines: page={}/{}", page, pageSize);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            GetWaylineListParam param = new GetWaylineListParam();
            param.setPage(page);
            param.setPageSize(pageSize);

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            wmm.getWaylineList(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getWaylineList failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("items", result.getItems());
                data.put("pagination", result.getPagination());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Wayline list retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline list request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline list error: {}", e.getMessage(), e);
            return errorResult("List failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 详情查询。
     */
    private Map<String, Object> executeRealDetail(String waylineUuid) {
        try {
            log.info("[REAL] getWaylineDetail: uuid={}", waylineUuid);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            wmm.getWaylineDetail(waylineUuid, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getWaylineDetail failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("detail", result.toString());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Wayline detail retrieved for " + waylineUuid);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline detail request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline detail error: {}", e.getMessage(), e);
            return errorResult("Detail failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 上传航线。
     */
    private Map<String, Object> executeRealSubmit(String waylineName, String filePath,
                                                  Map<String, Object> args) {
        try {
            log.info("[REAL] submitWayline: name={}, file={}", waylineName, filePath);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            SubmitWaylineInfoParam param = new SubmitWaylineInfoParam();
            param.setKmzObjectKey(filePath);
            param.setWaylineName(waylineName);
            param.setTimestamp(System.currentTimeMillis() / 1000);

            CompletableFuture<Void> future = new CompletableFuture<>();
            wmm.submitWaylineInfo(param, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "submitWaylineInfo failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("wayline_name", waylineName);
            data.put("file_path", filePath);
            data.put("status", "submitted");
            List<Map<String, Object>> nextSteps = List.of(
                    Map.of("action", "List waylines to get the wayline_uuid, then create a flight task",
                           "tool", "fh_workspace",
                           "params_hint", Map.of("action", "list"))
            );
            return successResultWithNextSteps(data,
                    "Wayline '" + waylineName + "' submitted successfully.", nextSteps);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline submit request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline submit error: {}", e.getMessage(), e);
            return errorResult("Submit failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 更新航线。
     */
    private Map<String, Object> executeRealUpdate(String waylineUuid, String waylineName) {
        try {
            log.info("[REAL] updateWayline: uuid={}, name={}", waylineUuid, waylineName);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            CompletableFuture<Void> future = new CompletableFuture<>();
            wmm.updateWayline(waylineUuid, waylineName, (error, result) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "updateWayline failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("wayline_uuid", waylineUuid);
            data.put("wayline_name", waylineName);
            data.put("status", "updated");
            return successResult(data, "Wayline '" + waylineUuid + "' updated to name '" + waylineName + "'.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline update request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline update error: {}", e.getMessage(), e);
            return errorResult("Update failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 复制航线。
     */
    private Map<String, Object> executeRealDuplicate(String waylineUuid) {
        try {
            log.info("[REAL] duplicateWayline: uuid={}", waylineUuid);
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            DuplicateWaylineParam param = new DuplicateWaylineParam();
            param.setOriginalWaylineUuid(waylineUuid);

            CompletableFuture<Object> future = new CompletableFuture<>();
            wmm.duplicateWayline(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "duplicateWayline failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(result);
            });

            Object result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("source_uuid", waylineUuid);
            data.put("status", "duplicated");
            if (result != null) {
                data.put("result", result.toString());
            }
            return successResult(data, "Wayline '" + waylineUuid + "' duplicated successfully.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline duplicate request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline duplicate error: {}", e.getMessage(), e);
            return errorResult("Duplicate failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 批量删除。
     */
    private Map<String, Object> executeRealBatchDelete(List<String> uuids) {
        try {
            log.info("[REAL] batchDeleteWaylines: count={}", uuids.size());
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            CompletableFuture<Object> future = new CompletableFuture<>();
            wmm.batchDeleteWaylines(uuids, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "batchDeleteWaylines failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(result);
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deleted_uuids", uuids);
            data.put("count", uuids.size());
            data.put("status", "deleted");
            return successResult(data, uuids.size() + " wayline(s) deleted successfully.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline batch delete request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline batch delete error: {}", e.getMessage(), e);
            return errorResult("Batch delete failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }

    /**
     * 真实 SDK 批量下载。
     */
    private Map<String, Object> executeRealBatchDownload(List<String> uuids) {
        try {
            log.info("[REAL] batchDownloadWaylines: count={}", uuids.size());
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            BatchDownloadWaylinesParam param = new BatchDownloadWaylinesParam();
            param.setWaylineUuids(uuids);

            CompletableFuture<List<?>> future = new CompletableFuture<>();
            wmm.batchDownloadWaylines(param, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "batchDownloadWaylines failed: " + formatSdkError(error)));
                    return;
                }
                future.complete(results);
            });

            List<?> results = SdkCallbackBridge.await(future);
            List<Map<String, Object>> downloads = new ArrayList<>();
            if (results != null) {
                for (Object item : results) {
                    downloads.add(Map.of("item", item.toString()));
                }
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", uuids.size());
            data.put("downloads", downloads);
            return successResult(data, "Download URLs generated for " + uuids.size() + " wayline(s).");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline batch download request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("Wayline batch download error: {}", e.getMessage(), e);
            return errorResult("Batch download failed: " + e.getMessage(), "Retry or check connectivity.");
        }
    }
}
