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
import com.dji.flighthubsdk.bean.SubmitWaylineInfoParam;
import com.dji.flighthubsdk.core.WaylineManageModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                "Upload a wayline from a public URL and create a flight task in one step. "
                + "WHEN TO USE: You have a KMZ file accessible via public URL (https://...). "
                + "DO NOT USE WHEN: You have a local file path (use fh_workspace action=submit first), "
                + "or wayline already in workspace (use fh_flight_task action=create directly). "
                + "NOTE: This tool takes drone_sn (NOT dock_sn), unlike fh_flight_task.");

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

            // 构建提交参数
            SubmitWaylineInfoParam param = new SubmitWaylineInfoParam();
            param.setKmzObjectKey(waylineUrl);
            param.setWaylineName(resolvedName);
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

            log.info("Wayline info submitted: drone={}, url={}, name={}", droneSn, waylineUrl, resolvedName);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_name", resolvedName);
            data.put("drone_sn", droneSn);
            data.put("wayline_url", waylineUrl);
            data.put("out_of_control_action", oocAction);
            data.put("status", "CREATED");
            data.put("task_uuid", null); // SDK void 回调不返回 UUID，需通过 fh_flight_task 查询

            List<Map<String, Object>> nextSteps = List.of(
                    Map.of("action", "List flight tasks to find the task_uuid, then execute",
                           "tool", "fh_flight_task",
                           "params_hint", Map.of("action", "list"))
            );
            return successResultWithNextSteps(data,
                    "Wayline info submitted. Call fh_flight_task(action=list) to get the task_uuid, "
                    + "then fh_wayline_execute to start the mission.", nextSteps);

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
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String droneSn, String waylineUrl) {
        return String.format("即将为无人机 %s 创建航线任务，航线来源: %s，确认？", droneSn, waylineUrl);
    }
}
