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
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * fh_wayline_control — 控制正在执行的航线任务：暂停、恢复或取消。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L2（影响飞行中任务，需 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWaylineControlTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWaylineControlTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_wayline_control";

    /** 参数名常量 */
    private static final String PARAM_TASK_UUID = "task_uuid";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_CONFIRM = "confirm";

    /** 操作：暂停任务 */
    private static final String ACTION_PAUSE = "pause";

    /** 操作：恢复任务 */
    private static final String ACTION_RESUME = "resume";

    /** 操作：取消任务 */
    private static final String ACTION_CANCEL = "cancel";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_PAUSE, ACTION_RESUME, ACTION_CANCEL);

    /** 操作对应的目标状态 */
    private static final Map<String, String> ACTION_TO_STATUS = Map.of(
            ACTION_PAUSE, "PAUSED",
            ACTION_RESUME, "EXECUTING",
            ACTION_CANCEL, "CANCELLED"
    );

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhWaylineControlTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.FLIGHT;
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
                "Control a running wayline task: pause (drone hovers in place), "
                + "resume (continue from pause point), or cancel (abort and return home).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_TASK_UUID, Map.of(
                "type", "string",
                "description", "Task UUID of the wayline task to control"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "pause: hover in place | resume: continue flight | cancel: abort and RTH"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the control action"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_TASK_UUID, PARAM_ACTION, PARAM_CONFIRM));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行航线控制操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L2 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 校验 task_uuid
        String taskUuid = getStringArg(args, PARAM_TASK_UUID, null);
        if (taskUuid == null) {
            return errorResult("Missing required parameter: task_uuid",
                    "Provide task_uuid of the running wayline task.");
        }

        // 4. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 5. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would '" + action + "' wayline task " + taskUuid,
                    buildDryRunPreview(TOOL_NAME, action, taskUuid,
                            Map.of("task_uuid", taskUuid, "action", action),
                            List.of("session_active", "security_level_allowed", "workspace_ready", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 6. 人工确认（L2 安全操作）
        Map<String, Object> confirmError = requireHumanConfirm(args,
                "wayline_" + action, taskUuid, buildConfirmMessage(taskUuid, action));
        if (confirmError != null) {
            return confirmError;
        }

        // 7. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? controlInMockMode(session, taskUuid, action)
                : controlWithRealSdk(session, taskUuid, action);
    }

    /**
     * Mock 模式下控制航线任务。
     */
    private Map<String, Object> controlInMockMode(SessionContext session, String taskUuid, String action) {
        String newStatus = ACTION_TO_STATUS.get(action);
        updateSessionState(session, action);
        log.info("[MOCK] Wayline task {}: uuid={}, newStatus={}", action, taskUuid, newStatus);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("action", action);
        data.put("status", newStatus);
        data.put("_mock", true);

        return successResult(data, buildSuccessMessage(action, taskUuid));
    }

    /** SDK 任务状态码：执行中（用于 resume） */
    private static final int SDK_STATUS_EXECUTING = 2;

    /** SDK 任务状态码：暂停 */
    private static final int SDK_STATUS_PAUSE = 3;

    /** SDK 任务状态码：取消 */
    private static final int SDK_STATUS_CANCEL = 4;

    /** 操作到 SDK 状态码的映射 */
    private static final Map<String, Integer> ACTION_TO_SDK_STATUS = Map.of(
            ACTION_PAUSE, SDK_STATUS_PAUSE,
            ACTION_RESUME, SDK_STATUS_EXECUTING,
            ACTION_CANCEL, SDK_STATUS_CANCEL
    );

    /**
     * 真实 SDK 模式下控制航线任务。
     * 通过 updateFlightTaskStatus 将任务设为暂停/恢复/取消。
     *
     * <p>注意：session 状态变更必须在 SDK 调用成功之后才执行，避免失败时状态污染。
     */
    @Generated
    private Map<String, Object> controlWithRealSdk(SessionContext session, String taskUuid, String action) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            int targetStatus = ACTION_TO_SDK_STATUS.get(action);
            SdkCallbackBridge.awaitVoid(cb ->
                    ftm.updateFlightTaskStatus(taskUuid, targetStatus, cb));

            // SDK 调用成功后，才更新 session 状态（防止失败时状态污染）
            String newStatus = ACTION_TO_STATUS.get(action);
            updateSessionState(session, action);
            log.info("Wayline task {}: uuid={}, newStatus={}", action, taskUuid, newStatus);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_uuid", taskUuid);
            data.put("action", action);
            data.put("status", newStatus);

            return successResult(data, buildSuccessMessage(action, taskUuid));

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_wayline_control timeout for task {} action {}: {}", taskUuid, action, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_wayline_control SDK error for task {} action {}: [{}] {}",
                    taskUuid, action, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check task status with fh_wayline_status.");
        } catch (Exception e) {
            // SDK 调用异常 —— session 状态未变更，保持一致性
            log.error("fh_wayline_control error for task {} action {}: {}", taskUuid, action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check task status with fh_wayline_status.");
        }
    }

    /**
     * 更新 session 的航线相关状态。
     */
    private void updateSessionState(SessionContext session, String action) {
        switch (action) {
            case ACTION_PAUSE -> session.setFlightPhase(FlightPhase.HOVERING);
            case ACTION_RESUME -> session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);
            case ACTION_CANCEL -> {
                session.setFlightPhase(FlightPhase.RETURNING);
                session.setActiveWaylineTaskUuid(null);
            }
            default -> { /* 不应到达 */ }
        }
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String taskUuid, String action) {
        return switch (action) {
            case ACTION_PAUSE -> String.format("即将暂停航线任务 %s，无人机将悬停在当前位置，确认？", taskUuid);
            case ACTION_RESUME -> String.format("即将恢复航线任务 %s，无人机将继续沿航线飞行，确认？", taskUuid);
            case ACTION_CANCEL -> String.format("即将取消航线任务 %s，无人机将返航，确认？", taskUuid);
            default -> "确认执行操作？";
        };
    }

    /**
     * 构建成功消息。
     */
    private String buildSuccessMessage(String action, String taskUuid) {
        return switch (action) {
            case ACTION_PAUSE -> "Task paused. Drone hovering at current position.";
            case ACTION_RESUME -> "Task resumed. Drone continuing along wayline.";
            case ACTION_CANCEL -> "Task cancelled. Drone returning to home point.";
            default -> "Control action completed.";
        };
    }
}
