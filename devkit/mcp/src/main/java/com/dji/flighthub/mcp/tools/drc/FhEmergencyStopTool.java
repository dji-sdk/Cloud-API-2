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
import com.dji.flighthubsdk.FlightHubCallback.ResultCallbackWithError;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_emergency_stop — 紧急停机 / 强制降落 / 开伞 / 悬停保活（L3 最高安全等级）。
 *
 * <p>SDK 侧全部为 DRC 通道指令，但本工具不做 DRC 链路前置校验，
 * 以免紧急场景下因链路未建立而被拦下；无链路时 SDK 会直接返回错误。
 *
 * <p>操作不可逆，必须提供 reason 用于审计追踪。
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

    /** 悬停保活时长参数名（仅 hover_hold 使用，单位 ms） */
    private static final String PARAM_DURATION_MS = "duration_ms";

    /** 开伞强制标志参数名（仅 parachute 使用） */
    private static final String PARAM_PARACHUTE_FORCE = "parachute_force";

    /** 操作标识，用于 requireConfirm 提示 */
    private static final String ACTION_EMERGENCY_STOP = "EMERGENCY_STOP";

    /** 停机模式：停机悬停（不可持续，将自由下落） */
    private static final String MODE_HOVER = "hover";

    /** 停机模式：紧急降落（保留动力） */
    private static final String MODE_LANDING = "landing";

    /** 停机模式：强制降落（drcForceLanding，无参数） */
    private static final String MODE_FORCE_LANDING = "force_landing";

    /** 停机模式：开伞（drcOpenParachute） */
    private static final String MODE_PARACHUTE = "parachute";

    /**
     * 停机模式：悬停保活（drcSetDroneHover）。
     * 与 {@link #MODE_HOVER} 语义完全不同，严禁复用同一 mode 名。
     */
    private static final String MODE_HOVER_HOLD = "hover_hold";

    /** 默认停机模式 */
    private static final String DEFAULT_MODE = MODE_HOVER;

    /** 有效停机模式列表 */
    private static final List<String> VALID_MODES = List.of(
            MODE_HOVER, MODE_LANDING, MODE_FORCE_LANDING, MODE_PARACHUTE, MODE_HOVER_HOLD);

    /** 悬停保活时长下限（ms）：0 表示取消保活定时器 */
    private static final int HOVER_HOLD_MIN_DURATION_MS = 0;

    /** 悬停保活时长上限（ms）：30 分钟，SDK 对超范围值静默截断 */
    private static final int HOVER_HOLD_MAX_DURATION_MS = 1_800_000;

    /** duration_ms 未提供时的哨兵值（与合法的 0 区分） */
    private static final int DURATION_NOT_PROVIDED = Integer.MIN_VALUE;

    /** SDK 开伞入参：普通开伞 */
    private static final int PARACHUTE_FORCE_NORMAL = 0;

    /** SDK 开伞入参：强制开伞 */
    private static final int PARACHUTE_FORCE_FORCED = 1;

    /** 执行状态：紧急动作 */
    private static final String STATUS_EMERGENCY = "emergency";

    /** 执行状态：悬停保活 */
    private static final String STATUS_HOVERING = "hovering";

    /** 执行状态：降落 */
    private static final String STATUS_LANDING = "landing";

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
                "EMERGENCY: Irreversible flight-safety actions sent over the DRC channel. "
                        + "Actions: hover (cut motors in air, drone WILL fall) | landing (emergency landing, power kept) | "
                        + "force_landing (drone descends immediately) | parachute (deploy parachute) | "
                        + "hover_hold (extend the hover keep-alive timer, DJI Dock 4 drones only). "
                        + "If the DRC link is not established the SDK returns an error. "
                        + "L3: requires confirm, reason and operator_token.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm this IRREVERSIBLE emergency command"
        ));
        props.put(PARAM_OPERATOR_TOKEN, Map.of(
                "type", "string",
                "description", "Operator token (FH_OPERATOR_TOKEN) required for L3 operations. "
                        + "Must be supplied by a human operator out-of-band, NEVER auto-filled by the AI."
        ));
        props.put(PARAM_REASON, Map.of(
                "type", "string",
                "description", "Mandatory reason for the emergency action (required for audit trail)"
        ));
        props.put(PARAM_MODE, Map.of(
                "type", "string",
                "enum", VALID_MODES,
                "description", "hover: cut motors in air, the drone WILL fall | "
                        + "landing: emergency landing with power kept | "
                        + "force_landing: force the drone to land immediately | "
                        + "parachute: release the parachute | "
                        + "hover_hold: extend hover keep-alive timer, DJI Dock 4 only (default: hover)"
        ));
        props.put(PARAM_DURATION_MS, Map.of(
                "type", "integer",
                "minimum", HOVER_HOLD_MIN_DURATION_MS,
                "maximum", HOVER_HOLD_MAX_DURATION_MS,
                "description", "Hover keep-alive duration in milliseconds, required for mode=hover_hold. "
                        + "Range [0, 1800000]; 0 cancels the keep-alive timer. "
                        + "Values outside the range are rejected here because the SDK truncates them silently."
        ));
        props.put(PARAM_PARACHUTE_FORCE, Map.of(
                "type", "boolean",
                "description", "Only for mode=parachute. true forces parachute deployment even when the "
                        + "drone's safety logic would not; false (default) uses normal deployment."
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
     * 执行紧急动作。
     * 前置条件：持有控制权 + confirm + reason。安全等级 L3。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        Map<String, Object> precheckError = precheck(session, args);
        if (precheckError != null) {
            return precheckError;
        }

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        EmergencyAction action = resolveAction(args);
        String reason = getStringArg(args, PARAM_REASON, null);

        // dry_run：参数校验通过即返回预览（无需 confirm 与 operator token）
        if (isDryRun(args)) {
            return buildDryRunResult(droneSn, action, reason);
        }

        Map<String, Object> gateError = checkL3Gates(args, droneSn, action, reason);
        if (gateError != null) {
            return gateError;
        }

        log.warn("EMERGENCY action triggered: drone={}, action={}, reason={}", droneSn, action, reason);

        return dispatch(session, droneSn, action, reason);
    }

    /**
     * 前置校验链：安全等级 → drone_sn → 控制权 → 参数。
     *
     * <p>紧急动作故意不做 DRC 链路校验：链路缺失时由 SDK 返回错误，
     * 避免紧急场景被本地前置条件拦下。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @return 错误响应 Map，校验通过时返回 null
     */
    private Map<String, Object> precheck(SessionContext session, Map<String, Object> args) {
        // session.maxSecurityLevel < L3 时直接拒绝
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        Map<String, Object> controlError = requireControlAuthority(session, droneSn);
        if (controlError != null) {
            return controlError;
        }

        return validateEmergencyParams(args);
    }

    /**
     * L3 双重门禁：运营商 token 二次鉴权 + 人工确认。
     *
     * @param args    输入参数
     * @param droneSn 目标无人机 SN
     * @param action  已校验的动作参数
     * @param reason  审计原因
     * @return 错误响应 Map，门禁全部通过时返回 null
     */
    private Map<String, Object> checkL3Gates(Map<String, Object> args, String droneSn,
                                             EmergencyAction action, String reason) {
        // 运营商二次鉴权：L3 独立凭据，AI 不可代填。
        // operator token 未配置时同样拒绝执行——宁可不可用，不可让 L3 裸奔。
        if (!verifyOperatorToken(args)) {
            return errorResult(OPERATOR_AUTH_ERROR,
                    "L3 emergency action requires a valid operator_token "
                            + "(configured via FH_OPERATOR_TOKEN). This token must be provided "
                            + "by a human operator out-of-band and must NOT be auto-filled by the AI.");
        }

        // 人工确认：优先走 client 原生弹窗（elicitation），不可用时回退 confirm 参数校验
        return requireHumanConfirm(args, ACTION_EMERGENCY_STOP, droneSn,
                buildConfirmMessage(droneSn, action, reason));
    }

    /**
     * mock 模式下写入飞行阶段，真实模式下发 SDK 指令。
     *
     * @param session 当前 session
     * @param droneSn 目标无人机 SN
     * @param action  已校验的动作参数
     * @param reason  审计原因
     * @return 执行结果
     */
    private Map<String, Object> dispatch(SessionContext session, String droneSn,
                                         EmergencyAction action, String reason) {
        if (sdkService.isMockMode()) {
            return executeInMockMode(session, droneSn, action, reason);
        }
        return executeWithRealSdk(session, droneSn, action, reason);
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
            log.info("L3 operator token verified for emergency action (operator={})",
                    OperatorTokenAuthenticator.maskToken(provided));
            return true;
        }
        log.warn("L3 emergency action blocked: operator token missing or invalid "
                + "(operator_enabled={})", operatorAuthenticator.isEnabled());
        return false;
    }

    /**
     * Mock 模式下执行紧急动作（仅更新 session 状态，不触碰 SDK）。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   已校验的动作参数
     * @param reason   动作原因（审计用）
     * @return 成功响应
     */
    private Map<String, Object> executeInMockMode(SessionContext session, String droneSn,
                                                  EmergencyAction action, String reason) {
        session.setFlightPhase(action.phase());
        log.info("[MOCK] Emergency action executed: drone={}, mode={}", droneSn, action.mode());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("mode", action.mode());
        data.put("reason", reason);
        data.put("status", action.status());
        data.put("_mock", true);

        return successResult(data,
                "[MOCK] EMERGENCY action executed for drone " + droneSn + " (mode=" + action.mode() + ").");
    }

    /**
     * 真实 SDK 模式下执行紧急动作。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   已校验的动作参数
     * @param reason   动作原因（审计用）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(SessionContext session, String droneSn,
                                                   EmergencyAction action, String reason) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            dispatchEmergencyCommand(drone.getFlightControlModule(), action,
                    createSdkCallback(future));
            SdkCallbackBridge.await(future);

            session.setFlightPhase(action.phase());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("mode", action.mode());
            data.put("reason", reason);
            data.put("status", action.status());

            return successResult(data,
                    "EMERGENCY action executed for drone " + droneSn + " (mode=" + action.mode() + ").");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_emergency_stop timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "CRITICAL: Emergency command may not have been received. Verify drone status immediately.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_emergency_stop SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Emergency action failed. Retry immediately or use physical RC.");
        } catch (Exception e) {
            log.error("fh_emergency_stop error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 根据 mode 分发到对应的 SDK 紧急指令。
     *
     * <p>五种模式的物理后果完全不同，绝不可混用：
     * <ul>
     *   <li>{@code hover} → {@code drcEmergencyStop}：空中切断电机，无人机将自由坠落</li>
     *   <li>{@code landing} → {@code drcEmergencyLanding}：紧急降落（仍保有动力）</li>
     *   <li>{@code force_landing} → {@code drcForceLanding}：立即强制降落，无入参</li>
     *   <li>{@code parachute} → {@code drcOpenParachute}：开伞，入参为 int 强制标志</li>
     *   <li>{@code hover_hold} → {@code drcSetDroneHover}：续期悬停保活定时器，入参为毫秒</li>
     * </ul>
     *
     * @param module   飞行控制模块
     * @param action   已校验的动作参数
     * @param callback 统一的 SDK 回调
     */
    private void dispatchEmergencyCommand(FlightControlModule module, EmergencyAction action,
                                          ResultCallbackWithError callback) {
        switch (action.mode()) {
            case MODE_LANDING -> module.drcEmergencyLanding(callback);
            case MODE_FORCE_LANDING -> module.drcForceLanding(callback);
            case MODE_PARACHUTE -> module.drcOpenParachute(action.parachuteForce(), callback);
            case MODE_HOVER_HOLD -> module.drcSetDroneHover(action.hoverDurationMs(), callback);
            case MODE_HOVER -> module.drcEmergencyStop(callback);
            // mode 已在 validateEmergencyParams 中做过白名单校验，走到这里是编程错误
            default -> throw new IllegalStateException("Unvalidated emergency mode: " + action.mode());
        }
    }

    /**
     * 构建 dry-run 预览，列出本次将下发的动作及其关键参数。
     *
     * @param droneSn 目标无人机 SN
     * @param action  已校验的动作参数
     * @param reason  动作原因
     * @return dry-run 响应
     */
    private Map<String, Object> buildDryRunResult(String droneSn, EmergencyAction action, String reason) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("mode", action.mode());
        params.put("reason", reason);
        if (MODE_HOVER_HOLD.equals(action.mode())) {
            params.put(PARAM_DURATION_MS, action.hoverDurationMs());
        }
        if (MODE_PARACHUTE.equals(action.mode())) {
            params.put(PARAM_PARACHUTE_FORCE, action.parachuteForce() == PARACHUTE_FORCE_FORCED);
        }
        return dryRunResult(
                buildDryRunMessage(droneSn, action),
                buildDryRunPreview(TOOL_NAME, ACTION_EMERGENCY_STOP, droneSn, params,
                        List.of("session_active", "security_level_allowed", "control_acquired",
                                "params_valid", "operator_token_required"),
                        getSecurityLevel(), requiresHumanConfirmation()));
    }

    /**
     * 解析已通过校验的动作参数。
     *
     * @param args 输入参数
     * @return 动作参数
     */
    private EmergencyAction resolveAction(Map<String, Object> args) {
        String mode = getStringArg(args, PARAM_MODE, DEFAULT_MODE);
        int durationMs = MODE_HOVER_HOLD.equals(mode)
                ? getIntArg(args, PARAM_DURATION_MS, DURATION_NOT_PROVIDED)
                : 0;
        int parachuteForce = MODE_PARACHUTE.equals(mode) && getBooleanArg(args, PARAM_PARACHUTE_FORCE)
                ? PARACHUTE_FORCE_FORCED
                : PARACHUTE_FORCE_NORMAL;
        return new EmergencyAction(mode, durationMs, parachuteForce);
    }

    /**
     * 创建统一的 SDK 回调：成功则完成 future，失败则携带错误码异常完成。
     *
     * @param future 用于桥接异步回调的 future
     * @return SDK 回调
     */
    private ResultCallbackWithError createSdkCallback(CompletableFuture<Void> future) {
        return error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(
                                error.getServerCode(), formatSdkError(error)));
            }
        };
    }

    /**
     * 构建 dry-run 预览文案，按模式说明物理后果。
     *
     * @param droneSn 目标无人机 SN
     * @param action  动作参数
     * @return dry-run 预览文案
     */
    private String buildDryRunMessage(String droneSn, EmergencyAction action) {
        String effect = switch (action.mode()) {
            case MODE_LANDING -> "drone will descend immediately but keep power";
            case MODE_FORCE_LANDING -> "drone will be forced to land immediately";
            case MODE_PARACHUTE -> "parachute will be deployed";
            case MODE_HOVER_HOLD -> "hover keep-alive timer will be set to "
                    + action.hoverDurationMs() + " ms";
            default -> "motors will stop, drone WILL fall";
        };
        return "Would execute EMERGENCY " + action.mode() + " on drone " + droneSn
                + " — " + effect;
    }

    /**
     * 构建 elicitation 弹窗展示给真人的确认提示信息。
     *
     * @param droneSn 目标无人机 SN
     * @param action  动作参数
     * @param reason  动作原因
     * @return 面向真人的确认提示
     */
    private String buildConfirmMessage(String droneSn, EmergencyAction action, String reason) {
        String effect = switch (action.mode()) {
            case MODE_LANDING -> "紧急降落（保留动力）";
            case MODE_FORCE_LANDING -> "强制立即降落";
            case MODE_PARACHUTE -> "开伞"
                    + (action.parachuteForce() == PARACHUTE_FORCE_FORCED ? "（强制）" : "");
            case MODE_HOVER_HOLD -> "悬停保活续期 " + action.hoverDurationMs() + " 毫秒";
            default -> "紧急停机（切断电机，无人机将坠落）";
        };
        return String.format("⚠️ %s！无人机 %s（模式=%s）。此操作不可撤销！原因：%s。是否确认？",
                effect, droneSn, action.mode(), reason);
    }

    /**
     * 校验紧急动作参数（reason、mode、以及 mode 专属参数）。
     *
     * @param args 输入参数
     * @return 错误响应 Map，校验通过时返回 null
     */
    private Map<String, Object> validateEmergencyParams(Map<String, Object> args) {
        String reason = getStringArg(args, PARAM_REASON, null);
        if (reason == null || reason.isBlank()) {
            return errorResult("Emergency action requires a reason for the audit trail",
                    "Provide reason parameter describing why the emergency action is needed.");
        }
        String mode = getStringArg(args, PARAM_MODE, DEFAULT_MODE);
        if (!VALID_MODES.contains(mode)) {
            return errorResult(
                    "Invalid mode: " + mode,
                    "mode must be one of: " + VALID_MODES);
        }
        if (MODE_HOVER_HOLD.equals(mode)) {
            return validateHoverDuration(args);
        }
        return null;
    }

    /**
     * 校验悬停保活时长。
     *
     * <p>SDK 对超范围值静默截断且不报错，必须在 MCP 层拦截；
     * duration_ms 缺省不等于 0——0 是「取消保活定时器」的合法语义。
     *
     * @param args 输入参数
     * @return 错误响应 Map，校验通过时返回 null
     */
    private Map<String, Object> validateHoverDuration(Map<String, Object> args) {
        int durationMs = getIntArg(args, PARAM_DURATION_MS, DURATION_NOT_PROVIDED);
        if (durationMs == DURATION_NOT_PROVIDED) {
            return errorResult("mode=hover_hold requires duration_ms",
                    "Provide duration_ms in the range [0, " + HOVER_HOLD_MAX_DURATION_MS
                            + "]. Use 0 to cancel the keep-alive timer.");
        }
        if (durationMs < HOVER_HOLD_MIN_DURATION_MS || durationMs > HOVER_HOLD_MAX_DURATION_MS) {
            return errorResult(
                    "duration_ms out of range: " + durationMs,
                    "duration_ms must be within [" + HOVER_HOLD_MIN_DURATION_MS + ", "
                            + HOVER_HOLD_MAX_DURATION_MS + "]; the SDK silently truncates out-of-range values.");
        }
        return null;
    }

    /**
     * 已通过校验的紧急动作参数。
     *
     * @param mode            动作模式
     * @param hoverDurationMs 悬停保活时长（ms），仅 hover_hold 有意义
     * @param parachuteForce  开伞强制标志（0=正常，1=强制），仅 parachute 有意义
     */
    private record EmergencyAction(String mode, int hoverDurationMs, int parachuteForce) {

        /**
         * 动作成功后应写入的飞行阶段。
         *
         * @return 飞行阶段
         */
        FlightPhase phase() {
            return switch (mode) {
                case MODE_LANDING, MODE_FORCE_LANDING -> FlightPhase.LANDING;
                case MODE_HOVER_HOLD -> FlightPhase.HOVERING;
                default -> FlightPhase.EMERGENCY;
            };
        }

        /**
         * 动作成功后响应中的 status 字段。
         *
         * @return 状态字符串
         */
        String status() {
            return switch (mode) {
                case MODE_LANDING, MODE_FORCE_LANDING -> STATUS_LANDING;
                case MODE_HOVER_HOLD -> STATUS_HOVERING;
                default -> STATUS_EMERGENCY;
            };
        }
    }
}
