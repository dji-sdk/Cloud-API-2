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
import com.dji.flighthub.mcp.security.OperatorTokenAuthenticator;
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
 * fh_emergency_stop — 紧急停机或强制降落指令（L3 最高安全等级）。
 * 通过管理通道发送，不需要 DRC 链路，只需飞行控制权。
 * 操作不可逆，必须提供 reason 用于审计追踪。
 *
 * <p>安全等级：L3（最高，不可逆操作）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhEmergencyStopTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhEmergencyStopTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_emergency_stop";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** confirm 参数名 */
    private static final String PARAM_CONFIRM = "confirm";

    /** reason 参数名（审计追踪必填） */
    private static final String PARAM_REASON = "reason";

    /** mode 参数名 */
    private static final String PARAM_MODE = "mode";

    /** 操作标识，用于 requireConfirm 提示 */
    private static final String ACTION_EMERGENCY_STOP = "EMERGENCY_STOP";

    /** 停机模式：停机悬停（不可持续，将自由下落） */
    private static final String MODE_HOVER = "hover";

    /** 停机模式：强制降落 */
    private static final String MODE_LANDING = "landing";

    /** 默认停机模式 */
    private static final String DEFAULT_MODE = MODE_HOVER;

    /** 有效停机模式列表 */
    private static final List<String> VALID_MODES = List.of(MODE_HOVER, MODE_LANDING);

    /** 运营商 token 参数名（L3 二次鉴权，AI 不可代填） */
    private static final String PARAM_OPERATOR_TOKEN = "operator_token";

    /** 二次鉴权失败时的通用错误提示（不泄露配置状态） */
    private static final String OPERATOR_AUTH_ERROR =
            "Operator token verification failed";

    private final SdkService sdkService;
    private final OperatorTokenAuthenticator operatorAuthenticator;

    /**
     * 构造函数。
     *
     * @param sessionManager        session 管理器
     * @param sdkService            SDK 服务
     * @param operatorAuthenticator 运营商 token 二次鉴权（L3 独立凭据）
     */
    public FhEmergencyStopTool(SessionManager sessionManager, SdkService sdkService,
                               OperatorTokenAuthenticator operatorAuthenticator) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.operatorAuthenticator = operatorAuthenticator;
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
        return SecurityLevel.CRITICAL;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "EMERGENCY: Immediately stop all motors or force landing. "
                        + "This is irreversible and should only be used in critical situations. "
                        + "Does NOT require DRC link (works over management channel).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm this IRREVERSIBLE emergency stop command"
        ));
        props.put(PARAM_OPERATOR_TOKEN, Map.of(
                "type", "string",
                "description", "Operator token (FH_OPERATOR_TOKEN) required for L3 operations. "
                        + "Must be supplied by a human operator out-of-band, NEVER auto-filled by the AI."
        ));
        props.put(PARAM_REASON, Map.of(
                "type", "string",
                "description", "Mandatory reason for the emergency stop (required for audit trail)"
        ));
        props.put(PARAM_MODE, Map.of(
                "type", "string",
                "enum", VALID_MODES,
                "description", "hover: cut motors in air | landing: force immediate descent (default: hover)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_CONFIRM, PARAM_REASON));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行紧急停止操作。
     * 前置条件：持有控制权 + confirm + reason。安全等级 L3。
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

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L3 时直接拒绝）
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

        // 3. 校验飞行控制权（紧急停机使用管理通道，无需 DRC 链路）
        Map<String, Object> controlError = requireControlAuthority(session, droneSn);
        if (controlError != null) {
            return controlError;
        }

        // 4. 校验 reason 和 mode 参数（先于 confirm，确保参数错误返回正确信息）
        Map<String, Object> paramError = validateEmergencyParams(args);
        if (paramError != null) {
            return paramError;
        }

        // 5. 读取已校验参数
        String reason = getStringArg(args, PARAM_REASON, null);
        String mode = getStringArg(args, PARAM_MODE, DEFAULT_MODE);

        // 6. dry_run 模式：参数校验通过，返回预览（无需 confirm 与 operator token）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute EMERGENCY STOP on drone " + droneSn
                            + " (mode=" + mode + ") — motors will stop, drone WILL fall",
                    buildDryRunPreview(TOOL_NAME, ACTION_EMERGENCY_STOP, droneSn,
                            Map.of("mode", mode, "reason", reason),
                            List.of("session_active", "security_level_allowed", "control_acquired",
                                    "params_valid", "operator_token_required"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 7. 运营商二次鉴权：L3 独立凭据，AI 不可代填。
        //    operator token 未配置时同样拒绝执行——宁可不可用，不可让 L3 裸奔。
        if (!verifyOperatorToken(args)) {
            return errorResult(OPERATOR_AUTH_ERROR,
                    "L3 emergency stop requires a valid operator_token "
                            + "(configured via FH_OPERATOR_TOKEN). This token must be provided "
                            + "by a human operator out-of-band and must NOT be auto-filled by the AI.");
        }

        // 8. 需要明确的人工确认（L3 最高安全操作，不可逆）
        //    优先走 client 原生弹窗（elicitation），不可用时回退 confirm 参数校验
        Map<String, Object> confirmError = requireHumanConfirm(args, ACTION_EMERGENCY_STOP, droneSn,
                buildConfirmMessage(droneSn, mode, reason));
        if (confirmError != null) {
            return confirmError;
        }

        // 9. 记录审计警告日志（L3 强制要求）
        log.warn("EMERGENCY STOP triggered: drone={}, mode={}, reason={}", droneSn, mode, reason);

        // 10. Mock 模式：直接执行
        if (sdkService.isMockMode()) {
            return executeInMockMode(session, droneSn, mode, reason);
        }

        // 11. 真实 SDK 调用（生产路径，需要真实 SDK 环境）
        return executeWithRealSdk(session, droneSn, mode, reason);
    }

    /**
     * 校验调用方提供的运营商 token（L3 二次鉴权）。
     *
     * <p>安全策略：token 未配置或缺失/不匹配一律拒绝。
     * 校验通过时记录脱敏日志，便于审计与排障。
     *
     * @param args 输入参数（读取 operator_token）
     * @return true 表示鉴权通过
     */
    private boolean verifyOperatorToken(Map<String, Object> args) {
        String provided = getStringArg(args, PARAM_OPERATOR_TOKEN, null);
        if (operatorAuthenticator.verify(provided)) {
            log.info("L3 operator token verified for emergency stop (operator={})",
                    OperatorTokenAuthenticator.maskToken(provided));
            return true;
        }
        log.warn("L3 emergency stop blocked: operator token missing or invalid "
                + "(operator_enabled={})", operatorAuthenticator.isEnabled());
        return false;
    }

    /**
     * Mock 模式下执行紧急停机指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param mode     停机模式（hover / landing）
     * @param reason   停机原因（审计用）
     * @return 成功响应
     */
    private Map<String, Object> executeInMockMode(SessionContext session, String droneSn,
                                                   String mode, String reason) {
        session.setFlightPhase(FlightPhase.EMERGENCY);
        log.info("[MOCK] Emergency stop executed: drone={}, mode={}", droneSn, mode);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("mode", mode);
        data.put("reason", reason);
        data.put("status", "emergency");
        data.put("_mock", true);

        return successResult(data,
                "[MOCK] EMERGENCY STOP executed for drone " + droneSn + " (mode=" + mode + ").");
    }

    /**
     * 真实 SDK 模式下执行紧急停机指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param mode     停机模式（hover / landing）
     * @param reason   停机原因（审计用）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(SessionContext session, String droneSn,
                                                    String mode, String reason) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            drone.getFlightControlModule().drcEmergencyStop(error -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(
                                    error.getServerCode(), formatSdkError(error)));
                }
            });
            SdkCallbackBridge.await(future);

            session.setFlightPhase(FlightPhase.EMERGENCY);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("mode", mode);
            data.put("reason", reason);
            data.put("status", "emergency");

            return successResult(data,
                    "EMERGENCY STOP executed for drone " + droneSn + " (mode=" + mode + ").");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_emergency_stop timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "CRITICAL: Emergency stop may not have been received. Verify drone status immediately.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_emergency_stop SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Emergency stop failed. Retry immediately or use physical RC.");
        } catch (Exception e) {
            log.error("fh_emergency_stop error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 构建 elicitation 弹窗展示给真人的确认提示信息。
     *
     * @param droneSn 目标无人机 SN
     * @param mode    停机模式
     * @param reason  停机原因
     * @return 面向真人的确认提示
     */
    private String buildConfirmMessage(String droneSn, String mode, String reason) {
        return String.format(
                "⚠️ 紧急停机！无人机 %s（模式=%s）。此操作不可撤销！原因：%s。是否确认？",
                droneSn, mode, reason);
    }

    /**
     * 校验紧急停止参数（reason 和 mode）。
     *
     * @param args 输入参数
     * @return 错误响应 Map，校验通过时返回 null
     */
    private Map<String, Object> validateEmergencyParams(Map<String, Object> args) {
        String reason = getStringArg(args, PARAM_REASON, null);
        if (reason == null || reason.isBlank()) {
            return errorResult("Emergency stop requires a reason for the audit trail",
                    "Provide reason parameter describing why the emergency stop is needed.");
        }
        String mode = getStringArg(args, PARAM_MODE, DEFAULT_MODE);
        if (!VALID_MODES.contains(mode)) {
            return errorResult(
                    "Invalid mode: " + mode,
                    "mode must be one of: " + VALID_MODES);
        }
        return null;
    }
}
