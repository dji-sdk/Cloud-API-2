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
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.SubmitWaylineInfoParam;
import com.dji.flighthubsdk.core.WaylineManageModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * fh_wayline_upload — 创建航线任务（上传航线文件到云端并生成任务）。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L1（创建任务但不触发飞行，可逆操作，需 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWaylineUploadTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWaylineUploadTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_wayline_upload";

    /** 参数名常量 */
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_WAYLINE_URL = "wayline_url";
    private static final String PARAM_TASK_NAME = "task_name";
    private static final String PARAM_OUT_OF_CONTROL_ACTION = "out_of_control_action";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_CONFIRM = "confirm";

    /** 失控动作有效值 */
    private static final List<String> VALID_OOC_ACTIONS = List.of("return_home", "hover", "continue");

    /** 默认失控动作 */
    private static final String DEFAULT_OOC_ACTION = "return_home";

    /** 导入结果事件等待超时（秒）：服务端异步解析 KMZ，需要比普通回调更长时间 */
    private static final int IMPORT_EVENT_TIMEOUT_SECONDS = 30;

    /** 导入结果事件成功码 */
    private static final int IMPORT_CODE_SUCCESS = 0;

    /** 响应中的导入状态值 */
    private static final String STATUS_SUBMITTED = "submitted";
    private static final String STATUS_IMPORTED = "imported";
    private static final String STATUS_IMPORT_FAILED = "import_failed";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhWaylineUploadTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
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
                "Import a wayline from a public URL into the workspace. "
                + "WHEN TO USE: You have a KMZ file accessible via public URL (https://...). "
                + "DO NOT USE WHEN: You have a local file path (use fh_workspace action=submit first), "
                + "or wayline already in workspace (use fh_flight_task action=create directly). "
                + "NOTE: This tool takes drone_sn (NOT dock_sn), unlike fh_flight_task. "
                + "Returns the imported wayline_uuid (or async_request_id when the import event times out); "
                + "create the flight task afterwards with fh_flight_task action=create.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN) to assign this wayline task"
        ));
        props.put(PARAM_WAYLINE_URL, Map.of(
                "type", "string",
                "description", "URL of the wayline file (must be accessible from the cloud)"
        ));
        props.put(PARAM_TASK_NAME, Map.of(
                "type", "string",
                "description", "Optional task name. Auto-generated if not provided."
        ));
        props.put(PARAM_OUT_OF_CONTROL_ACTION, Map.of(
                "type", "string",
                "enum", VALID_OOC_ACTIONS,
                "description", "Action when drone loses connection: return_home (default), hover, or continue"
        ));
        props.put(PARAM_DESCRIPTION, Map.of(
                "type", "string",
                "description", "Optional task description for audit purposes"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm task creation"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_WAYLINE_URL, PARAM_CONFIRM));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行航线上传（创建任务）操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    @SuppressWarnings("unchecked")
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

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 解析并校验参数
        Object parsed = parseAndValidateParams(args);
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        UploadParams params = (UploadParams) parsed;

        // 4. 人工确认
        Map<String, Object> confirmError = requireHumanConfirm(args, "wayline_upload",
                params.droneSn, buildConfirmMessage(params.droneSn, params.waylineUrl));
        if (confirmError != null) {
            return confirmError;
        }

        // 5. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? uploadInMockMode(session, params.droneSn, params.waylineUrl,
                        params.taskName, params.oocAction, params.description)
                : uploadWithRealSdk(session, params.droneSn, params.waylineUrl,
                        params.taskName, params.oocAction, params.description);
    }

    /**
     * 解析并校验上传参数。
     * 成功返回 UploadParams 实例，失败返回错误 Map。
     */
    private Object parseAndValidateParams(Map<String, Object> args) {
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        String waylineUrl = getStringArg(args, PARAM_WAYLINE_URL, null);
        if (waylineUrl == null) {
            return errorResult("Missing required parameter: wayline_url",
                    "Provide a valid URL to the wayline file.");
        }

        String taskName = getStringArg(args, PARAM_TASK_NAME, null);
        String oocAction = getStringArg(args, PARAM_OUT_OF_CONTROL_ACTION, DEFAULT_OOC_ACTION);
        String description = getStringArg(args, PARAM_DESCRIPTION, null);

        if (!VALID_OOC_ACTIONS.contains(oocAction)) {
            return errorResult("Invalid out_of_control_action: " + oocAction,
                    "Must be one of: " + VALID_OOC_ACTIONS);
        }

        return new UploadParams(droneSn, waylineUrl, taskName, oocAction, description);
    }

    /** 上传参数值对象 */
    private record UploadParams(String droneSn, String waylineUrl,
                                String taskName, String oocAction, String description) {
    }

    /**
     * Mock 模式下创建航线任务。
     */
    private Map<String, Object> uploadInMockMode(
            SessionContext session, String droneSn, String waylineUrl,
            String taskName, String oocAction, String description) {
        // mock 模式 UUID 改用 UUID v4，消除时间戳可猜测性
        String taskUuid = "task_" + java.util.UUID.randomUUID().toString().replace("-", "");
        String resolvedName = (taskName != null) ? taskName : "Wayline-" + droneSn + "-" + taskUuid.substring(5, 13);

        // 注意：不设置 session.setActiveWaylineTaskUuid —— CREATED 状态不是 active，
        // 仅在 FhWaylineExecuteTool 执行成功后才标记。
        log.info("[MOCK] Wayline task created: uuid={}, drone={}, url={}", taskUuid, droneSn, waylineUrl);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("task_name", resolvedName);
        data.put("drone_sn", droneSn);
        data.put("wayline_url", waylineUrl);
        data.put("out_of_control_action", oocAction);
        data.put("status", "CREATED");
        data.put("_mock", true);

        List<Map<String, Object>> nextSteps = List.of(
                Map.of("action", "Execute the mission to start flight",
                       "tool", "fh_flight_task",
                       "params_hint", Map.of("action", "execute", "task_uuid", taskUuid, "confirm", true)),
                Map.of("action", "Check task status",
                       "tool", "fh_flight_task",
                       "params_hint", Map.of("action", "detail", "task_uuid", taskUuid))
        );
        return successResultWithNextSteps(data,
                "Wayline task created. Use fh_flight_task to execute or check status.", nextSteps);
    }

    /**
     * 真实 SDK 模式下创建航线任务。
     * 通过 WaylineManageModule.submitWaylineInfo 提交航线文件到云端。
     *
     * <p>提交请求需携带 asyncRequestId（服务端 {@code ImportRequest.AsyncRequestId} 必填），
     * wayline_uuid 由 listenImportWaylineEvent 异步事件下发，通过 asyncRequestId 配对获取。
     */
    @Generated
    private Map<String, Object> uploadWithRealSdk(
            SessionContext session, String droneSn, String waylineUrl,
            String taskName, String oocAction, String description) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            WaylineManageModule wmm = workspace.getWaylineManageModule();

            String resolvedName = (taskName != null) ? taskName
                    : "Wayline-" + droneSn + "-" + System.currentTimeMillis();

            // 异步请求标识：服务端 ImportRequest.AsyncRequestId 必填，用于配对导入结果事件
            String asyncRequestId = UUID.randomUUID().toString();

            // 先注册导入事件监听，再提交，避免事件先于监听到达而丢失
            CompletableFuture<ImportWaylineEventResult> importFuture = new CompletableFuture<>();
            int listenerId = wmm.listenImportWaylineEvent(event -> {
                if (asyncRequestId.equals(event.getAsyncRequestId())) {
                    importFuture.complete(event);
                }
            });
            // 监听注册失败时回调永远不会触发：不能提交（否则调用方无法拿到导入结果），
            // 也不能进入 30 秒的无谓等待，应直接返回可诊断的错误
            if (listenerId < 0) {
                log.error("listenImportWaylineEvent failed: drone={}, listenerId={}", droneSn, listenerId);
                return errorResult("Failed to register wayline import event listener",
                        "SDK returned listener id " + listenerId
                                + ". Retry the call; if it persists, check SDK native library state.");
            }
            try {
                submitWaylineInfo(wmm, waylineUrl, resolvedName, asyncRequestId);
                log.info("Wayline info submitted: drone={}, url={}, name={}, asyncRequestId={}",
                        droneSn, waylineUrl, resolvedName, asyncRequestId);
                return buildUploadResult(droneSn, waylineUrl, resolvedName, oocAction,
                        asyncRequestId, importFuture);
            } finally {
                wmm.cancelListenImportWaylineEvent(listenerId);
            }
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_wayline_upload timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Platform may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_wayline_upload SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check wayline file URL and try again.");
        } catch (Exception e) {
            log.error("fh_wayline_upload error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 提交航线导入请求（服务端异步受理）。
     *
     * @param wmm            航线管理模块
     * @param waylineUrl     KMZ 对象键 / URL
     * @param waylineName    航线名称
     * @param asyncRequestId 异步请求标识
     */
    private void submitWaylineInfo(WaylineManageModule wmm, String waylineUrl,
                                   String waylineName, String asyncRequestId) {
        SubmitWaylineInfoParam param = new SubmitWaylineInfoParam();
        param.setKmzObjectKey(waylineUrl);
        param.setWaylineName(waylineName);
        param.setAsyncRequestId(asyncRequestId);
        param.setTimestamp(System.currentTimeMillis() / 1000);

        // submitWaylineInfo 使用 SubmitWaylineInfoCallback（void 回调）
        CompletableFuture<Void> future = new CompletableFuture<>();
        wmm.submitWaylineInfo(param, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                String message = (error.getServerMessage() != null && !error.getServerMessage().isBlank())
                        ? error.getServerMessage()
                        : "errorCode=" + error.getErrorCode() + ", server=" + error.getServerCode();
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getServerCode(), message));
            }
        });
        SdkCallbackBridge.await(future);
    }

    /**
     * 组装提交结果，并等待导入事件回填 wayline_uuid。
     *
     * <p>提交已被受理但事件未在超时内到达时不视为失败，仅标记 {@code import_event_timeout}，
     * 调用方可通过 {@code fh_workspace(action=list)} 复查。
     *
     * @param droneSn        无人机序列号
     * @param waylineUrl     航线文件来源
     * @param resolvedName   航线名称
     * @param oocAction      失控动作
     * @param asyncRequestId 异步请求标识
     * @param importFuture   导入结果事件
     * @return 工具返回结果
     */
    private Map<String, Object> buildUploadResult(String droneSn, String waylineUrl, String resolvedName,
                                                  String oocAction, String asyncRequestId,
                                                  CompletableFuture<ImportWaylineEventResult> importFuture) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_name", resolvedName);
        data.put("drone_sn", droneSn);
        data.put("wayline_url", waylineUrl);
        data.put("out_of_control_action", oocAction);
        data.put("status", STATUS_SUBMITTED);
        data.put("async_request_id", asyncRequestId);
        data.put("wayline_uuid", null);

        try {
            ImportWaylineEventResult event =
                    SdkCallbackBridge.await(importFuture, IMPORT_EVENT_TIMEOUT_SECONDS);
            data.put("wayline_uuid", event.getWaylineUuid());
            if (event.getCode() != IMPORT_CODE_SUCCESS) {
                // 服务端导入失败（如 KMZ 格式非法），透出错误码与信息
                data.put("status", STATUS_IMPORT_FAILED);
                data.put("import_code", event.getCode());
                data.put("import_message", event.getMessage());
                return buildFailureResult(
                        "Import failed (code=" + event.getCode() + "): " + event.getMessage(),
                        "Check the KMZ file content, or retry with a valid wayline file.",
                        data);
            }
            data.put("status", STATUS_IMPORTED);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.warn("Import event not received in {}s for asyncRequestId={}",
                    IMPORT_EVENT_TIMEOUT_SECONDS, asyncRequestId);
            data.put("import_event_timeout", true);
        }

        List<Map<String, Object>> nextSteps = List.of(
                Map.of("action", "Create a flight task using this wayline",
                       "tool", "fh_flight_task",
                       "params_hint", Map.of("action", "create", "wayline_uuid",
                               data.get("wayline_uuid") != null
                                       ? data.get("wayline_uuid") : "<QUERY_VIA_fh_workspace_list>"))
        );
        return successResultWithNextSteps(data,
                "Wayline info submitted. wayline_uuid is returned from the import event "
                + "(or query via fh_workspace(action=list) if the event timed out).", nextSteps);
    }

    /**
     * 构建携带诊断数据的失败响应。
     *
     * <p>基类 {@code errorResult(error, suggestion)} 只返回三字段信封（不含 {@code data}），
     * 但导入失败时调用方需要拿到 {@code async_request_id}、{@code import_code} 等定位信息，
     * 因此本工具在本地组装失败信封。字段顺序与基类保持一致：{@code success} → {@code error_message}
     * → {@code suggestion} → {@code data}，调用方可按同一套逻辑解析。
     *
     * @param error      错误信息
     * @param suggestion 修复建议
     * @param data       诊断数据（已包含导入状态、错误码等）
     * @return 失败响应
     */
    private Map<String, Object> buildFailureResult(String error, String suggestion,
                                                   Map<String, Object> data) {
        Map<String, Object> failure = new LinkedHashMap<>(4);
        failure.put("success", false);
        failure.put("error_message", error);
        failure.put("suggestion", suggestion);
        failure.put("data", data);
        return failure;
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String droneSn, String waylineUrl) {
        return String.format("即将为无人机 %s 创建航线任务，航线来源: %s，确认？", droneSn, waylineUrl);
    }
}
