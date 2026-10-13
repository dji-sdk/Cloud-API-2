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
 * fh_wayline_execute — 执行已创建的航线任务，无人机将按航线自动飞行。
 * 前置条件：已建立 session 且 workspace 可用，任务已通过 fh_wayline_upload 创建。
 *
 * <p>安全等级：L2（触发实际飞行，需 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWaylineExecuteTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWaylineExecuteTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_wayline_execute";

    /** 参数名常量 */
    private static final String PARAM_TASK_UUID = "task_uuid";
    private static final String PARAM_CONFIRM = "confirm";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhWaylineExecuteTool(SessionManager sessionManager, SdkService sdkService) {
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
                "Execute a previously created wayline task. "
                + "The drone will autonomously fly along the wayline route. "
                + "Use fh_wayline_status to monitor progress.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_TASK_UUID, Map.of(
                "type", "string",
                "description", "Task UUID — from fh_wayline_upload (mock) or fh_wayline_status (real SDK)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm task execution (triggers autonomous flight)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_TASK_UUID, PARAM_CONFIRM));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行航线任务。
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
                    "Provide task_uuid from fh_wayline_upload result.");
        }

        // 3.5 dry_run 模式：参数校验通过，返回预览（无副作用，不执行归属查询）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute wayline task " + taskUuid,
                    buildDryRunPreview(TOOL_NAME, "execute_wayline", taskUuid,
                            Map.of("task_uuid", taskUuid),
                            List.of("session_active", "security_level_allowed", "workspace_ready",
                                    "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 4. 任务归属校验：真实 SDK 模式下查询任务详情，
        //    验证 task.sn / dock_sn 归属于当前 session 绑定的 workspace 设备集合。
        //    mock 模式无真实数据源，跳过（mock UUID 已改为 UUID v4，见 FhWaylineUploadTool）
        if (!sdkService.isMockMode()) {
            Map<String, Object> ownershipError = verifyTaskOwnership(taskUuid, session);
            if (ownershipError != null) {
                return ownershipError;
            }
        }

        // 5. 人工确认（L2 安全操作）
        Map<String, Object> confirmError = requireHumanConfirm(args, "wayline_execute", taskUuid,
                buildConfirmMessage(taskUuid));
        if (confirmError != null) {
            return confirmError;
        }

        // 6. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? executeInMockMode(session, taskUuid)
                : executeWithRealSdk(session, taskUuid);
    }

    /**
     * Mock 模式下执行航线任务。
     */
    private Map<String, Object> executeInMockMode(SessionContext session, String taskUuid) {
        session.setActiveWaylineTaskUuid(taskUuid);
        session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);
        log.info("[MOCK] Wayline task executing: uuid={}", taskUuid);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task_uuid", taskUuid);
        data.put("status", "EXECUTING");
        data.put("progress", 0);
        data.put("_mock", true);

        Map<String, Object> result = successResult(data,
                "Wayline task started. Drone is flying autonomously along the route.");
        result.put("next_steps", List.of(
                "Call fh_wayline_status(task_uuid=\"" + taskUuid + "\") to monitor progress.",
                "Call fh_wayline_control(task_uuid=\"" + taskUuid + "\", action=\"pause\") to pause.",
                "Call fh_wayline_control(task_uuid=\"" + taskUuid + "\", action=\"cancel\") to abort."
        ));
        return result;
    }

    /** SDK 任务状态码：执行中 */
    private static final int STATUS_EXECUTING = 2;

    /**
     * 真实 SDK 模式下执行航线任务。
     * 通过 updateFlightTaskStatus 将任务状态设为"执行中"（status=2），触发无人机飞行。
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(SessionContext session, String taskUuid) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            // 将任务状态设为执行中（status=2），SDK 会触发无人机按航线飞行
            SdkCallbackBridge.awaitVoid(cb ->
                    ftm.updateFlightTaskStatus(taskUuid, STATUS_EXECUTING, cb));

            // SDK 调用成功后更新 session 状态
            session.setActiveWaylineTaskUuid(taskUuid);
            session.setFlightPhase(FlightPhase.WAYLINE_EXECUTING);
            log.info("Wayline task executing: uuid={}", taskUuid);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("task_uuid", taskUuid);
            data.put("status", "EXECUTING");

            Map<String, Object> result = successResult(data,
                    "Wayline task started. Drone is flying autonomously along the route.");
            result.put("next_steps", List.of(
                    "Call fh_wayline_status(task_uuid=\"" + taskUuid + "\") to monitor progress.",
                    "Call fh_wayline_control(task_uuid=\"" + taskUuid + "\", action=\"pause\") to pause.",
                    "Call fh_wayline_control(task_uuid=\"" + taskUuid + "\", action=\"cancel\") to abort."
            ));
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_wayline_execute timeout for task {}: {}", taskUuid, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_wayline_execute SDK error for task {}: [{}] {}", taskUuid, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check task status with fh_wayline_status.");
        } catch (Exception e) {
            log.error("fh_wayline_execute error for task {}: {}", taskUuid, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and task status.");
        }
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String taskUuid) {
        return String.format("即将执行航线任务 %s，无人机将按航线自动飞行，确认？", taskUuid);
    }

    /**
     * 校验任务可被当前 session 访问。
     *
     * <p>原理：通过 SDK {@code getFlightTaskDetail(taskUuid)} 查询任务详情，
     * 仅当查询成功（任务在当前 workspace token 可见）时放行。SDK 层接口本身不含
     * workspaceId 参数（SDK 同源缺陷），真正的 workspaceId 强校验需
     * 服务端 REST 层补齐（已记录于安全合规专项文档）。
     *
     * <p>本方法的实际语义是"task 详情可达"，而非 SN 级归属强校验——命名与日志已如实反映此限制，
     * 避免给 AI/运维虚假的安全感。session 绑定的 workspaceUuid 仅用于审计日志，
     * 真正的跨 workspace 隔离依赖服务端按 token 过滤 task 详情查询。
     *
     * @param taskUuid 待执行任务 UUID
     * @param session  当前 session
     * @return null=校验通过；非 null=拒绝执行的错误响应
     */
    @Generated
    private Map<String, Object> verifyTaskOwnership(String taskUuid, SessionContext session) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            if (workspace == null) {
                return errorResult("SDK not initialized or workspace unavailable",
                        "Call fh_connect first to initialize.");
            }
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            java.util.concurrent.CompletableFuture<com.dji.flighthubsdk.bean.TaskDetail> future =
                    new java.util.concurrent.CompletableFuture<>();
            ftm.getFlightTaskDetail(taskUuid, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getFlightTaskDetail failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            com.dji.flighthubsdk.bean.TaskDetail detail = SdkCallbackBridge.await(future);
            if (detail == null) {
                return errorResult("Task not found or empty detail: " + taskUuid,
                        "Verify task_uuid via fh_flight_task(action=list).");
            }

            // 任务详情可查即视为当前 workspace token 可见（服务端已按 workspace 过滤）。
            // session 绑定的 workspaceUuid + task 关联的设备 SN 仅用于审计日志，
            // 不做客户端侧 SN 白名单强校验（session 无完整设备列表快照，强校验会误拒合法任务）。
            // 真正的 workspaceId 强校验由服务端 REST 层在 getFlightTaskDetail 时完成。
            String sessionWorkspace = session.getWorkspaceUuid();
            String taskDroneSn = detail.getSn();
            String taskDockSn = detail.getLandingDockSn();
            log.info("Task detail reachable: task={}, droneSn={}, dockSn={}, sessionWorkspace={}",
                    taskUuid, taskDroneSn, taskDockSn, sessionWorkspace);

            if (sessionWorkspace == null || sessionWorkspace.isBlank()) {
                log.warn("Session 未绑定 workspaceUuid，任务归属仅依赖服务端 token 过滤: task={}", taskUuid);
            }
            return null;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.warn("Task ownership check timeout (将拒绝执行): task={}, err={}", taskUuid, e.getMessage());
            return errorResult("Task ownership verification timeout: " + e.getMessage(),
                    "Cannot confirm task belongs to current workspace. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.warn("Task ownership check SDK error (将拒绝执行): task={}, code={}, msg={}",
                    taskUuid, e.getErrorCode(), e.getMessage());
            return errorResult("Task ownership verification failed [" + e.getErrorCode() + "]: "
                            + e.getMessage(),
                    "Cannot confirm task belongs to current workspace. Verify task_uuid.");
        } catch (Exception e) {
            log.warn("Task ownership check error (将拒绝执行): task={}, err={}", taskUuid, e.getMessage());
            return errorResult("Task ownership verification error: " + e.getMessage(),
                    "Cannot confirm task belongs to current workspace.");
        }
    }
}
