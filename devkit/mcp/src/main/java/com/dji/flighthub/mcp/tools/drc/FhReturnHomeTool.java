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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_return_home — 命令无人机返航（飞回起飞点或机巢），或取消正在进行中的返航。
 * 返航使用管理通道，不需要 DRC 链路，只需持有飞行控制权。
 *
 * <p>安全等级：L2（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhReturnHomeTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhReturnHomeTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_return_home";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** confirm 参数名 */
    private static final String PARAM_CONFIRM = "confirm";

    /** action 参数名 */
    private static final String PARAM_ACTION = "action";

    /** 操作标识，用于 requireConfirm 提示 */
    private static final String ACTION_RETURN_HOME = "return_home";

    /** 返航动作：开始 */
    private static final String ACTION_START = "start";

    /** 返航动作：取消 */
    private static final String ACTION_CANCEL = "cancel";

    /** 默认返航动作 */
    private static final String DEFAULT_ACTION = ACTION_START;

    /** 有效的 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_START, ACTION_CANCEL);

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhReturnHomeTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 返回本工具的 MCP 协议定义，包含名称、描述和 inputSchema。
     *
     * @return MCP 工具定义 Map
     */

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.FLIGHT;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Command drone to return home (fly back to takeoff point or dock). "
                        + "Can also cancel an in-progress return. "
                        + "Prerequisites: fh_control_acquire (DRC link not required).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the return-home command"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "start: initiate return | cancel: abort in-progress return (default: start)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_CONFIRM));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行返航操作。
     * 前置条件：持有控制权 + confirm。安全等级 L2。
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

        // 2. 校验 drone_sn 参数
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 3. 校验 action 参数（先于确认，避免操作员白确认无效操作）
        String action = getStringArg(args, PARAM_ACTION, DEFAULT_ACTION);
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult(
                    "Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 4. 飞行操作前置人工确认：返航先确认再抢控
        //    dry_run 模式无需确认（见 step 5）
        if (!isDryRun(args)) {
            Map<String, Object> preConfirmError = requireHumanConfirm(args, ACTION_RETURN_HOME, droneSn,
                    String.format("⚠️ 无人机 %s 即将执行返航操作（%s），是否确认？", droneSn, action));
            if (preConfirmError != null) {
                return preConfirmError;
            }
        }

        // 5. 确保飞行控制权（RTH 使用管理通道，无需 DRC，自动获取控制权）— 确认通过后才执行
        Map<String, Object> controlError = ensureControlAuthority(session, droneSn, sdkService);
        if (controlError != null) {
            return controlError;
        }

        // 6. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute return-home '" + action + "' on drone " + droneSn,
                    buildDryRunPreview(TOOL_NAME, action, droneSn,
                            Map.of("action", action),
                            List.of("session_active", "security_level_allowed", "control_acquired", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 7. 人工确认已在 step 4 完成

        // 8. Mock 模式：直接执行
        if (sdkService.isMockMode()) {
            return executeInMockMode(session, droneSn, action);
        }

        // 9. 真实 SDK 调用（生产路径，需要真实 SDK 环境）
        return executeWithRealSdk(session, droneSn, action);
    }

    /**
     * Mock 模式下执行返航指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / cancel）
     * @return 成功响应
     */
    private Map<String, Object> executeInMockMode(SessionContext session, String droneSn, String action) {
        if (ACTION_START.equals(action)) {
            session.setFlightPhase(FlightPhase.RETURNING);
            log.info("[MOCK] RTH initiated: drone={}", droneSn);
        } else {
            session.setFlightPhase(FlightPhase.HOVERING);
            log.info("[MOCK] RTH cancelled: drone={}", droneSn);
        }
        Map<String, Object> result = buildRthResult(droneSn, action, session.getFlightPhase(), "[MOCK] ");
        // 向 data 注入 mock 标记
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        data.put("_mock", true);
        return result;
    }

    /**
     * 真实 SDK 模式下执行返航指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / cancel）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(SessionContext session, String droneSn, String action) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            if (ACTION_START.equals(action)) {
                drone.getFlightControlModule().returnHome(error -> {
                    if (error.isSuccess()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(
                                new SdkCallbackBridge.SdkException(
                                        error.getServerCode(), formatSdkError(error)));
                    }
                });
            } else {
                drone.getFlightControlModule().returnHomeCancel(error -> {
                    if (error.isSuccess()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(
                                new SdkCallbackBridge.SdkException(
                                        error.getServerCode(), formatSdkError(error)));
                    }
                });
            }
            SdkCallbackBridge.await(future);

            if (ACTION_START.equals(action)) {
                session.setFlightPhase(FlightPhase.RETURNING);
            } else {
                session.setFlightPhase(FlightPhase.HOVERING);
            }
            return buildRthResult(droneSn, action, session.getFlightPhase(), "");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_return_home timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_return_home SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            log.error("fh_return_home error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 构建返航操作成功响应（mock 和真实 SDK 路径复用）。
     *
     * @param droneSn      目标无人机 SN
     * @param action       操作类型（start / cancel）
     * @param phase        执行后的飞行阶段（由调用方设置后传入）
     * @param msgPrefix    消息前缀（mock 模式传 "[MOCK] "，真实模式传 ""）
     * @return 成功结果 Map
     */
    private Map<String, Object> buildRthResult(
            String droneSn, String action, FlightPhase phase, String msgPrefix) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", action);

        String message;
        if (FlightPhase.RETURNING.equals(phase)) {
            data.put("status", "returning");
            message = msgPrefix + "Drone " + droneSn + " returning to home point.";
        } else {
            data.put("status", "hovering");
            message = msgPrefix + "Return home cancelled. Drone " + droneSn + " hovering.";
        }
        return successResult(data, message);
    }
}
