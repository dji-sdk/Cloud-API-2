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

package com.dji.flighthub.mcp.tools;

import com.dji.flighthub.mcp.elicit.ClientBridge;
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.elicit.ElicitResult;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tool 基础抽象类，提供通用能力：
 * <ul>
 *   <li>统一的错误响应构建</li>
 *   <li>Session 有效性检查</li>
 *   <li>成功响应构建</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
public abstract class AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(AbstractMcpTool.class);

    protected final SessionManager sessionManager;

    protected AbstractMcpTool(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 获取 Tool 定义（名称、描述、输入 schema）。
     *
     * <p>Legacy 模式：子类直接覆写此方法手动构建 definition。
     * <p>注解驱动模式：子类无需覆写，框架根据 @McpTool/@McpParam 自动生成。
     *
     * @return MCP tool definition
     */
    public Map<String, Object> getToolDefinition() {
        com.dji.flighthub.mcp.annotation.McpTool meta =
                this.getClass().getAnnotation(com.dji.flighthub.mcp.annotation.McpTool.class);
        if (meta != null) {
            return com.dji.flighthub.mcp.annotation.SchemaGenerator.generate(
                    this.getClass().asSubclass(AbstractMcpTool.class));
        }
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + ": override getToolDefinition() or use @McpTool annotation");
    }

    /**
     * 执行 Tool 逻辑。
     *
     * <p>Legacy 模式：子类直接覆写此方法编排参数校验、前置条件、业务逻辑。
     * <p>注解驱动模式：子类无需覆写，框架自动执行前置条件链 → 参数绑定 → doExecute()。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    public Map<String, Object> execute(Map<String, Object> args) {
        return annotationDrivenExecute(args);
    }

    /**
     * 注解驱动 Tool 的业务逻辑入口。
     * 调用时保证：session 有效、安全等级通过、confirm 完成、参数已绑定到 ToolContext。
     *
     * @param ctx 当前执行上下文
     * @return 执行结果
     */
    protected Map<String, Object> doExecute(com.dji.flighthub.mcp.annotation.ToolContext ctx) {
        throw new UnsupportedOperationException(
                "Annotated tools must override doExecute(ToolContext)");
    }

    /**
     * 此 tool 是否需要真人确认（L1+ 危险操作）。
     * 注解驱动模式：读取 @McpTool#requiresConfirm()。
     * Legacy 模式：默认 false，需要确认的 tool 覆写返回 true。
     * 用于 SSE 等无反向弹窗通道的传输层硬拦截。
     *
     * @return true 表示必须有人工确认通道才能执行
     */
    public boolean requiresHumanConfirmation() {
        com.dji.flighthub.mcp.annotation.McpTool meta =
                this.getClass().getAnnotation(com.dji.flighthub.mcp.annotation.McpTool.class);
        if (meta != null) {
            return meta.requiresConfirm();
        }
        return false;
    }

    // ==================== 注解驱动执行引擎 ====================

    /**
     * 注解驱动执行主流程：前置条件链 → 参数绑定 → dry_run 短路 → doExecute()。
     * 仅当子类未覆写 execute() 且类上标注了 @McpTool 时触发。
     *
     * @param args 原始输入参数
     * @return 执行结果
     */
    private Map<String, Object> annotationDrivenExecute(Map<String, Object> args) {
        com.dji.flighthub.mcp.annotation.McpTool meta =
                this.getClass().getAnnotation(com.dji.flighthub.mcp.annotation.McpTool.class);
        if (meta == null) {
            throw new UnsupportedOperationException(
                    getClass().getSimpleName() + ": override execute() or use @McpTool annotation");
        }

        try {
            // ① 前置条件链
            Map<String, Object> preError = executeAnnotatedPrerequisites(meta, args);
            if (preError != null) {
                return preError;
            }

            // ② 参数绑定 + 校验
            Map<String, Object> mutableArgs = args != null
                    ? new java.util.HashMap<>(args) : new java.util.HashMap<>();
            Map<String, Object> bindError = com.dji.flighthub.mcp.annotation.ParamBinder.bind(
                    this.getClass().asSubclass(AbstractMcpTool.class), mutableArgs);
            if (bindError != null) {
                return bindError;
            }

            // ③ 初始化 ToolContext
            SessionContext session = sessionManager.getActiveSession();
            com.dji.flighthub.mcp.annotation.ToolContext.init(mutableArgs, session);

            // ④ dry_run 短路
            if (getBooleanArg(mutableArgs, "dry_run")) {
                return buildAnnotatedDryRunPreview(meta, mutableArgs);
            }

            // ⑤ 调用业务逻辑
            return doExecute(com.dji.flighthub.mcp.annotation.ToolContext.current());
        } finally {
            com.dji.flighthub.mcp.annotation.ToolContext.clear();
        }
    }

    /**
     * 按 @McpTool#prerequisites 声明顺序执行前置条件检查。
     *
     * @param meta 当前 Tool 的 @McpTool 注解元数据
     * @param args 原始输入参数（用于提取 targetSn 和 confirm）
     * @return null=全部通过; 非 null=第一个失败的错误响应
     */
    private Map<String, Object> executeAnnotatedPrerequisites(
            com.dji.flighthub.mcp.annotation.McpTool meta, Map<String, Object> args) {
        String targetSn = getStringArg(args, meta.targetSnParam(), null);

        for (com.dji.flighthub.mcp.annotation.Prerequisite pre : meta.prerequisites()) {
            Map<String, Object> error = switch (pre) {
                case SESSION -> {
                    SessionContext s = requireSessionOrAutoConnect();
                    yield s == null ? noSessionError() : null;
                }
                case SECURITY_LEVEL -> enforceSecurityLevel(meta.securityLevel());
                case CONTROL_AUTHORITY -> {
                    SessionContext s = sessionManager.getOrAutoConnect();
                    if (s == null) {
                        yield noSessionError();
                    }
                    s.touch();
                    yield requireControlAuthority(s, targetSn);
                }
                case DRC_LINK -> {
                    SessionContext s = sessionManager.getOrAutoConnect();
                    if (s == null) {
                        yield noSessionError();
                    }
                    s.touch();
                    yield requireDrcLink(s, targetSn);
                }
            };
            if (error != null) {
                return error;
            }
        }

        // confirm 检查（位于前置条件之后）— dry_run 模式无需确认，直接走后续预览分支
        if (meta.requiresConfirm() && !getBooleanArg(args, DRY_RUN_PARAM)) {
            String confirmMsg = String.format("About to execute '%s' on %s. Confirm?",
                    meta.name(), targetSn != null ? targetSn : "target");
            Map<String, Object> confirmError = requireHumanConfirm(
                    args, meta.name(), targetSn != null ? targetSn : "", confirmMsg);
            if (confirmError != null) {
                return confirmError;
            }
        }

        return null;
    }

    /**
     * 构建 dry_run 模式的预览响应。
     *
     * @param meta 当前 Tool 的 @McpTool 注解元数据
     * @param args 绑定后的参数 Map
     * @return dry_run 预览结果
     */
    private Map<String, Object> buildAnnotatedDryRunPreview(
            com.dji.flighthub.mcp.annotation.McpTool meta, Map<String, Object> args) {
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("tool", meta.name());
        preview.put("security_level", meta.securityLevel().getLabel());
        preview.put("requires_confirm", meta.requiresConfirm());

        Map<String, Object> params = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            if (!"dry_run".equals(entry.getKey()) && entry.getValue() != null) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        preview.put("parameters", params);

        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("success", true);
        result.put("_dry_run", true);
        result.put("message", "Would execute " + meta.name() + " with validated parameters");
        result.put("data", preview);
        return result;
    }

    // ==================== SecurityLevel 声明与强制检查 ====================

    /**
     * 声明此 tool 整体的安全等级。
     * 注解驱动模式：读取 @McpTool#securityLevel()。
     * Legacy 模式：默认 L0（只读），L1+ 工具覆写此方法返回对应等级。
     *
     * <p>对于混合等级工具（部分 action L0，部分 L1+），
     * 工具级保持 L0，在各 action 方法内调用 {@link #enforceSecurityLevel(SecurityLevel)}。
     *
     * @return 此工具整体要求的安全等级
     */
    public SecurityLevel getSecurityLevel() {
        com.dji.flighthub.mcp.annotation.McpTool meta =
                this.getClass().getAnnotation(com.dji.flighthub.mcp.annotation.McpTool.class);
        if (meta != null) {
            return meta.securityLevel();
        }
        return SecurityLevel.READ_ONLY;
    }

    /**
     * 检查 session 是否允许执行指定安全等级的操作（硬拦截）。
     * 返回 null 表示允许；非 null 为不可绕过的拒绝错误响应。
     *
     * <p>此方法用于混合等级工具的 per-action 级别检查。
     * 统一等级工具使用 {@link #enforceToolSecurityLevel()} 更简洁。
     *
     * <p>与 {@link #requireHumanConfirm} 的关系：
     * <ul>
     *   <li>enforceSecurityLevel = 授权检查（"允许吗？"）— 硬拒绝，confirm 无法绕过</li>
     *   <li>requireHumanConfirm = 确认门控（"确定吗？"）— 软拦截，用户可确认放行</li>
     * </ul>
     * 调用顺序：先 enforceSecurityLevel，再 requireHumanConfirm。
     *
     * @param required 本次操作要求的安全等级
     * @return null=通过; 非 null=硬拒绝响应
     */
    protected Map<String, Object> enforceSecurityLevel(SecurityLevel required) {
        SessionContext session = sessionManager.getActiveSession();
        if (session == null) {
            return noSessionError();
        }
        if (!required.isAllowedBy(session.getMaxSecurityLevel())) {
            return errorResult(
                    "Operation denied: insufficient security level",
                    String.format("This operation requires %s, but session allows max %s.",
                            required.getLabel(),
                            SecurityLevel.labelFor(session.getMaxSecurityLevel())));
        }
        return null;
    }

    /**
     * 使用 tool 声明的 {@link #getSecurityLevel()} 进行工具级安全检查。
     * 推荐在 execute() 开头、requireSession() 之后立即调用。
     *
     * @return null=通过; 非 null=硬拒绝响应
     */
    protected Map<String, Object> enforceToolSecurityLevel() {
        return enforceSecurityLevel(getSecurityLevel());
    }

    // ==================== Session 辅助方法 ====================

    /**
     * 检查当前是否有活跃 session，若有则刷新活跃时间并返回。
     *
     * @return session 上下文，若无活跃 session 返回 null
     */
    protected SessionContext requireSession() {
        SessionContext session = sessionManager.getActiveSession();
        if (session != null) {
            session.touch();
        }
        return session;
    }

    /**
     * 获取活跃 session，若已过期则自动重连。
     * 用于前置条件链，对调用方透明地恢复过期 session。
     *
     * @return session 上下文；自动重连也失败时返回 null
     */
    protected SessionContext requireSessionOrAutoConnect() {
        SessionContext session = sessionManager.getOrAutoConnect();
        if (session != null) {
            session.touch();
        }
        return session;
    }

    /**
     * 无 session 时的标准错误响应。
     *
     * @return 错误结果 Map
     */
    protected Map<String, Object> noSessionError() {
        return errorResult("No active session", "Call fh_connect first.");
    }

    // ==================== 响应构建辅助方法 ====================

    /**
     * 构建错误响应。
     *
     * @param error      错误信息
     * @param suggestion 建议操作
     * @return 错误结果 Map
     */
    protected static Map<String, Object> errorResult(String error, String suggestion) {
        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("success", false);
        result.put("error_message", error);
        result.put("suggestion", suggestion);
        return result;
    }

    /**
     * 构建带引导提示的错误响应。
     * 当 AI 选错 Tool 或缺少前置条件时，hint 和 next_steps 引导到正确路径。
     *
     * @param error      错误信息
     * @param suggestion 建议操作（向后兼容原有字段）
     * @param hint       自然语言引导（≤200 字符，面向 AI 的路由纠正）
     * @param nextSteps  下一步操作建议列表
     * @return 错误结果 Map
     */
    protected static Map<String, Object> errorResultWithHint(
            String error, String suggestion, String hint, List<Map<String, Object>> nextSteps) {
        Map<String, Object> result = new LinkedHashMap<>(6);
        result.put("success", false);
        result.put("error_message", error);
        result.put("suggestion", suggestion);
        if (hint != null && !hint.isBlank()) {
            result.put("hint", hint);
        }
        if (nextSteps != null && !nextSteps.isEmpty()) {
            result.put("next_steps", nextSteps);
        }
        return result;
    }

    /**
     * 构建成功响应。
     *
     * @param data     数据负载
     * @param message  摘要消息
     * @return 成功结果 Map
     */
    protected static Map<String, Object> successResult(Map<String, Object> data, String message) {
        Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("success", true);
        result.put("data", data);
        result.put("message", message);
        return result;
    }

    /**
     * 构建带 workflow 引导的成功响应。
     * next_steps 为空时不输出该字段，避免干扰简单查询结果。
     *
     * @param data      数据负载
     * @param message   摘要消息
     * @param nextSteps 下一步操作建议列表（每项含 tool/action/params_hint）
     * @return 成功结果 Map
     */
    protected static Map<String, Object> successResultWithNextSteps(
            Map<String, Object> data, String message, List<Map<String, Object>> nextSteps) {
        Map<String, Object> result = new LinkedHashMap<>(5);
        result.put("success", true);
        result.put("data", data);
        result.put("message", message);
        if (nextSteps != null && !nextSteps.isEmpty()) {
            result.put("next_steps", nextSteps);
        }
        return result;
    }

    // ==================== 参数解析辅助方法 ====================

    /**
     * 从参数 Map 中获取 String 类型参数。
     *
     * @param args     参数 Map
     * @param key      参数名
     * @param fallback 默认值
     * @return 参数值
     */
    protected static String getStringArg(Map<String, Object> args, String key, String fallback) {
        Object val = args != null ? args.get(key) : null;
        if (val instanceof String s && !s.isBlank()) {
            return s;
        }
        return fallback;
    }

    /**
     * 从参数 Map 中获取 int 类型参数。
     *
     * @param args     参数 Map
     * @param key      参数名
     * @param fallback 默认值
     * @return 参数值
     */
    protected static int getIntArg(Map<String, Object> args, String key, int fallback) {
        Object val = args != null ? args.get(key) : null;
        if (val instanceof Number n) {
            return n.intValue();
        }
        if (val instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                // 解析失败，返回默认值
            }
        }
        return fallback;
    }

    /**
     * 从参数 Map 中获取 boolean 类型参数。
     *
     * @param args 参数 Map
     * @param key  参数名
     * @return 参数值（默认 false）
     */
    protected static boolean getBooleanArg(Map<String, Object> args, String key) {
        Object val = args != null ? args.get(key) : null;
        if (val instanceof Boolean b) {
            return b;
        }
        if (val instanceof String s) {
            return "true".equalsIgnoreCase(s.trim());
        }
        return false;
    }

    /**
     * 从参数 Map 中获取 String 列表参数。
     * 遍历 List 中的元素，过滤 null 和空白字符串。
     *
     * @param args 参数 Map
     * @param key  参数名
     * @return 非空字符串列表；参数缺失或非 List 类型时返回 null
     */
    protected static List<String> getStringListArg(Map<String, Object> args, String key) {
        if (args == null) {
            return null;
        }
        Object val = args.get(key);
        if (val instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s && !s.isBlank()) {
                    result.add(s);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 从参数 Map 中获取 long 类型参数。
     *
     * @param args     参数 Map
     * @param key      参数名
     * @param fallback 默认值
     * @return 参数值
     */
    protected static long getLongArg(Map<String, Object> args, String key, long fallback) {
        Object val = args != null ? args.get(key) : null;
        if (val instanceof Number n) {
            return n.longValue();
        }
        if (val instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                // 解析失败，返回默认值
            }
        }
        return fallback;
    }

    /**
     * 从参数中解析字符串列表。
     * 过滤 null 和空白元素，结果为空时返回 null。
     *
     * @param args 输入参数 Map
     * @param key  参数名
     * @return 解析成功返回 List<String>，参数不存在或为空返回 null
     */
    protected static List<String> parseStringList(Map<String, Object> args, String key) {
        Object value = args != null ? args.get(key) : null;
        if (value == null) {
            return null;
        }
        if (!(value instanceof List)) {
            return null;
        }
        List<?> rawList = (List<?>) value;
        if (rawList.isEmpty()) {
            return null;
        }
        List<String> result = new java.util.ArrayList<>(rawList.size());
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            String s = item.toString().trim();
            if (!s.isEmpty()) {
                result.add(s);
            }
        }
        return result.isEmpty() ? null : result;
    }

    // ==================== 飞行控制前置条件校验 ====================

    /**
     * 校验是否持有指定设备的飞行控制权。
     *
     * @param session 当前 session 上下文
     * @param droneSn 目标设备 SN
     * @return null 表示校验通过，非 null 为错误响应
     */
    protected Map<String, Object> requireControlAuthority(SessionContext session, String droneSn) {
        if (!session.hasControlOf(droneSn)) {
            return errorResult("No control authority for " + droneSn,
                    "Call fh_control_acquire(drone_sn='" + droneSn + "') first.");
        }
        return null;
    }

    /**
     * 校验是否有到指定设备的 DRC 链路。
     *
     * @param session 当前 session 上下文
     * @param droneSn 目标设备 SN
     * @return null 表示校验通过，非 null 为错误响应
     */
    protected Map<String, Object> requireDrcLink(SessionContext session, String droneSn) {
        if (!session.hasDrcLinkTo(droneSn)) {
            return errorResult("No DRC link to " + droneSn,
                    "Call fh_drc_connect(drone_sn='" + droneSn + "') first.");
        }
        return null;
    }

    // ==================== 飞行控制前置条件自动获取 ====================

    /**
     * 确保持有指定设备的飞行控制权，未持有时自动静默获取。
     * 可逆操作，无需人工确认。
     *
     * @param session    当前 session 上下文
     * @param droneSn    目标设备 SN
     * @param sdkService SDK 服务实例
     * @return null 表示已持有控制权，非 null 为获取失败的错误响应
     */
    protected Map<String, Object> ensureControlAuthority(
            SessionContext session, String droneSn, SdkService sdkService) {
        if (session.hasControlOf(droneSn)) {
            return null;
        }
        if (session.hasControlAuthority()) {
            String currentSn = session.getActiveControlDeviceSn();
            return errorResult("Already controlling device: " + currentSn,
                    "Call fh_control_release(drone_sn='" + currentSn + "') first.");
        }
        if (sdkService.isMockMode()) {
            session.setActiveControlDevice(droneSn);
            return null;
        }
        return acquireControlWithSdk(session, droneSn, sdkService);
    }

    /**
     * 确保已建立到指定设备的 DRC 链路，未建立时自动静默获取控制权并连接 DRC。
     * 可逆操作，无需人工确认。
     *
     * @param session    当前 session 上下文
     * @param droneSn    目标设备 SN
     * @param sdkService SDK 服务实例
     * @return null 表示 DRC 链路已就绪，非 null 为获取失败的错误响应
     */
    protected Map<String, Object> ensureDrcLink(
            SessionContext session, String droneSn, SdkService sdkService) {
        Map<String, Object> controlError = ensureControlAuthority(session, droneSn, sdkService);
        if (controlError != null) {
            return controlError;
        }
        if (session.hasDrcLinkTo(droneSn)) {
            return null;
        }
        if (sdkService.isMockMode()) {
            session.setActiveDrcDevice(droneSn);
            return null;
        }
        return connectDrcWithSdk(session, droneSn, sdkService);
    }

    /**
     * 通过 SDK 获取飞行控制权（内部方法，不含确认逻辑）。
     */
    private Map<String, Object> acquireControlWithSdk(
            SessionContext session, String droneSn, SdkService sdkService) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }
            CompletableFuture<Void> future = new CompletableFuture<>();
            drone.getControlAuthModule().acquireFlightControlAuthority(error -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(
                                    error.getServerCode(), formatSdkError(error)));
                }
            });
            SdkCallbackBridge.await(future);
            session.setActiveControlDevice(droneSn);
            return null;
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("Failed to acquire control (timeout): " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Failed to acquire control [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            return errorResult("Failed to acquire control: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 通过 SDK 建立 DRC 链路（内部方法，不含确认逻辑）。
     */
    private Map<String, Object> connectDrcWithSdk(
            SessionContext session, String droneSn, SdkService sdkService) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }
            CompletableFuture<Void> future = new CompletableFuture<>();
            drone.getDrcModule().connectDrcLink(error -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(
                                    error.getServerCode(), formatSdkError(error)));
                }
            });
            SdkCallbackBridge.await(future);
            session.setActiveDrcDevice(droneSn);
            return null;
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("Failed to connect DRC (timeout): " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Failed to connect DRC [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            return errorResult("Failed to connect DRC: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    // ==================== DRC 指令自动重试 ====================

    /**
     * 执行依赖 DRC 链路的指令，失败时自动断开→重连 DRC→重试（最多 1 次）。
     *
     * <p>适用于 FhFlytoTool、FhStickMoveTool、FhPoiTool 等通过 DRC 链路下发的飞行指令。
     * 当 SDK 返回 SdkException（通常为 ERROR_OPERATION_FAILED 等 DRC 通道异常）时：
     * <ol>
     *   <li>断开当前 DRC 链路（best-effort，失败不中断）</li>
     *   <li>重新建立 DRC 连接</li>
     *   <li>以完全相同的参数重试原指令</li>
     * </ol>
     *
     * <p>若重连失败，直接返回重连错误（基础设施问题，调用方无需处理）。
     * 若重试仍抛出异常，则重新抛出，由调用方自行包装错误文案。
     *
     * @param session    当前 session 上下文
     * @param droneSn    目标设备 SN
     * @param sdkService SDK 服务实例
     * @param operation  实际 SDK 指令逻辑（返回 successResult 或抛出 SdkException）
     * @return 执行结果（成功响应或重连失败的错误响应）
     * @throws SdkCallbackBridge.SdkException        重试后仍失败时抛出
     * @throws SdkCallbackBridge.SdkTimeoutException  首次或重试超时时抛出
     */
    protected Map<String, Object> executeWithDrcRetry(
            SessionContext session, String droneSn, SdkService sdkService,
            Supplier<Map<String, Object>> operation) {
        try {
            return operation.get();
        } catch (SdkCallbackBridge.SdkException firstError) {
            log.warn("DRC 指令失败 [{}]: {}，尝试重连 DRC 并重试: drone={}",
                    firstError.getErrorCode(), firstError.getMessage(), droneSn);

            Map<String, Object> reconnectError = reconnectDrc(session, droneSn, sdkService);
            if (reconnectError != null) {
                log.error("DRC 重连失败，放弃重试: drone={}", droneSn);
                return reconnectError;
            }

            log.info("DRC 重连成功，重试指令: drone={}", droneSn);
            return operation.get();
        }
    }

    /**
     * 断开并重新建立 DRC 链路。
     * 断开阶段 best-effort（失败仅记日志），连接阶段失败返回错误。
     *
     * @param session    当前 session 上下文
     * @param droneSn    目标设备 SN
     * @param sdkService SDK 服务实例
     * @return null=重连成功; 非 null=重连失败的错误响应
     */
    private Map<String, Object> reconnectDrc(
            SessionContext session, String droneSn, SdkService sdkService) {
        // 第一步：断开 DRC（best-effort）
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone != null) {
                SdkCallbackBridge.awaitVoidRc(
                        cb -> drone.getDrcModule().disconnectDrcLink(cb), 5);
            }
        } catch (Exception e) {
            log.warn("DRC 断开失败（best-effort，继续重连）: drone={}, error={}", droneSn, e.getMessage());
        }
        session.clearDrcDevice();

        // 第二步：重新建立 DRC 链路
        return connectDrcWithSdk(session, droneSn, sdkService);
    }

    /**
     * L1+ 操作确认校验。
     *
     * @param args   输入参数
     * @param action 操作描述（用于提示信息）
     * @param target 目标描述（设备 SN）
     * @return null 表示校验通过，非 null 为错误响应
     */
    protected Map<String, Object> requireConfirm(Map<String, Object> args, String action, String target) {
        if (!getBooleanArg(args, "confirm")) {
            return errorResult("Confirmation required",
                    String.format("About to execute '%s' on %s. Pass confirm=true to proceed.", action, target));
        }
        return null;
    }

    // ==================== Elicitation 人工确认（第1层 A 方案） ====================

    /** elicitation 反向请求等待超时（毫秒），60 秒 */
    protected static final long ELICIT_TIMEOUT_MILLIS = 60_000L;

    /** 纯确认场景的 boolean 字段名 */
    private static final String CONFIRM_FIELD = "confirmed";

    /**
     * 当前调用链上是否可用 client elicitation 反向弹窗。
     * 需同时满足：绑定了 bridge（工作线程执行）且 client 声明了 elicitation 能力。
     *
     * @return true 表示可发起 elicitation 弹窗
     */
    protected boolean elicitationAvailable() {
        ClientBridge bridge = ClientBridgeHolder.get();
        return bridge != null && bridge.supportsElicitation();
    }

    /**
     * 构建纯确认场景的 requestedSchema。
     *
     * <p>MCP 协议规范要求 requestedSchema 为 {@code type:object} + properties，
     * 但 properties 允许为空对象（TypeScript index signature {@code [key: string]}）。
     * 空 properties 意味着只展示 message + Accept/Decline 按钮，无额外表单字段。
     *
     * <p>若 client 不支持空 properties 渲染（auto-cancel），则触发 CANCEL 分支，
     * server 返回错误提示用户换支持 elicitation 的 client。
     *
     * @param prompt 未使用（保留签名以兼容调用方）
     * @return 空 properties 的合法 requestedSchema
     */
    private static Map<String, Object> buildConfirmSchema(String prompt) {
        Map<String, Object> schema = new LinkedHashMap<>(2);
        schema.put("type", "object");
        schema.put("properties", Map.of());
        return schema;
    }

    /**
     * 通过 client 原生弹窗请求真人确认（纯确认，无补输入）。
     *
     * <p>安全语义：真人 ACCEPT 即视为确认（点击接受按钮是主确认动作）；
     * DECLINE / CANCEL / TIMEOUT 一律返回错误响应，绝不放行。
     * 若 client 渲染了 confirmed checkbox 但真人未勾选（显式 false），同样拦截。
     * 调用方必须在 {@link #elicitationAvailable()} 为 true 时才调用。
     *
     * @param message 展示给真人的确认提示
     * @return null 表示真人确认通过，非 null 为错误响应
     */
    protected Map<String, Object> elicitConfirm(String message) {
        ClientBridge bridge = ClientBridgeHolder.get();
        if (bridge == null) {
            return errorResult("Confirmation channel unavailable",
                    "No client bridge bound for elicitation.");
        }

        ElicitResult result = bridge.elicit(message, buildConfirmSchema(message), ELICIT_TIMEOUT_MILLIS);
        return switch (result.action()) {
            case ACCEPT -> confirmedInContent(result) ? null
                    : errorResult("Operation not confirmed",
                    "The confirmation checkbox was not checked; operation not executed.");
            case DECLINE -> errorResult("Operation declined by operator",
                    "The human operator declined this operation.");
            case CANCEL -> errorResult("Operation cancelled",
                    "The confirmation was cancelled; operation not executed.");
            case TIMEOUT -> errorResult("Confirmation timed out",
                    "No response from operator within the time limit; operation not executed.");
        };
    }

    /**
     * 判断 ACCEPT 结果中真人是否确认。
     *
     * <p>ACCEPT 本身即为确认（真人主动点击了接受按钮）；
     * confirmed 字段缺失时按"已确认"处理——适配只有 Accept/Decline 按钮的简易 client。
     * 仅当字段显式为 false 时视为未确认（真人看到 checkbox 但未勾选）。
     *
     * <p>安全边界说明：若存在 client 不渲染 UI 就自动返回 accept，
     * 这属于 client 侧协议违规，非 server schema 层面可防御（恶意 client
     * 同样可发 {@code confirmed:true}）。真正的防线是传输层信任（stdio 本地可信）
     * 和 SSE 硬拦截（远程 client 无法执行 L1+ 操作）。
     *
     * @param result elicitation 结果
     * @return true 表示已确认
     */
    private static boolean confirmedInContent(ElicitResult result) {
        Object confirmed = result.content().get(CONFIRM_FIELD);
        if (confirmed == null) {
            return true;
        }
        return Boolean.TRUE.equals(confirmed);
    }

    /**
     * 统一人工确认入口：优先走 client 原生弹窗（elicitation），
     * SSE 远程模式下回退 confirm 参数校验，stdio 本地模式同样回退参数校验。
     *
     * <p>安全策略：
     * <ul>
     *   <li>elicitation 可用 → 发起弹窗等待真人确认（正常路径）</li>
     *   <li>SSE 远程模式但 client 未声明 elicitation → 回退 confirm 参数校验
     *       （Chatbot 等集成方通过 system prompt 引导 AI 执行 dry_run→用户确认→confirm=true 流程，
     *       配合 Chatbot 端角色鉴权确保安全；每次放行记录审计日志）</li>
     *   <li>stdio 本地模式（无 bridge）→ 回退 confirm 参数校验（本地可信）</li>
     * </ul>
     *
     * @param args    输入参数（回退分支读取 confirm）
     * @param action  操作描述
     * @param target  目标描述（设备 SN）
     * @param message elicitation 弹窗提示信息
     * @return null 表示确认通过，非 null 为错误响应
     */
    protected Map<String, Object> requireHumanConfirm(Map<String, Object> args, String action,
                                                      String target, String message) {
        if (elicitationAvailable()) {
            return elicitConfirm(message);
        }
        // 远程传输（SSE）且 client 不支持 elicitation → 回退 confirm 参数校验
        // Chatbot 等集成方的安全保障：system prompt 强制 dry_run 预览 + 用户自然语言确认 + 角色鉴权
        ClientBridge bridge = ClientBridgeHolder.get();
        if (bridge != null && bridge.isRemote()) {
            if (getBooleanArg(args, "confirm")) {
                log.warn("L1+ 操作通过 confirm 参数放行（远程 client 无 elicitation）: action={}, target={}",
                        action, target);
                return null;
            }
            return errorResult("Confirmation required",
                    "This operation requires human confirmation. "
                            + "First call with dry_run=true to preview, then pass confirm=true after user approval. "
                            + "Action: '" + action + "', target: " + target);
        }
        // 本地传输（stdio）或无 bridge：信任本地调用方
        return requireConfirm(args, action, target);
    }

    /**
     * 通过 client 原生表单请求真人补充结构化输入（如指点飞行坐标）。
     *
     * <p>安全语义：仅 ACCEPT 返回真人填写的 content；DECLINE / CANCEL / TIMEOUT、
     * 以及无 bridge / client 不支持 elicitation，一律返回 null（调用方视为"未提供输入"，
     * 回退到自身的缺参报错分支）。
     *
     * <p>与 {@link #elicitConfirm} 的分工：确认场景返回错误响应 Map（放行/拦截二选一），
     * 补输入场景返回 null 或真人填写的 content（错误信息由各调用方自行构建）。
     *
     * @param message         展示给真人的表单提示
     * @param requestedSchema 结构化字段 schema（须为 {@code type:object} + properties）
     * @return 真人填写的字段 Map；未提供或未接受时返回 null
     */
    protected Map<String, Object> requireInput(String message, Map<String, Object> requestedSchema) {
        ClientBridge bridge = ClientBridgeHolder.get();
        if (bridge == null || !bridge.supportsElicitation()) {
            return null;
        }

        ElicitResult result = bridge.elicit(message, requestedSchema, ELICIT_TIMEOUT_MILLIS);
        return result.action() == ElicitResult.Action.ACCEPT ? result.content() : null;
    }

    // ==================== SDK 错误格式化 ====================

    /**
     * 格式化 SDK 错误为人可读消息。
     * 优先使用 serverMessage；无消息时拼接各层错误码。
     *
     * @param error SDK 返回的分层错误对象
     * @return 格式化后的错误描述
     */
    protected String formatSdkError(FlightHubError error) {
        if (error == null) {
            return "Unknown SDK error (null)";
        }
        String serverMsg = error.getServerMessage();
        if (serverMsg != null && !serverMsg.isBlank()) {
            return serverMsg;
        }
        return "errorCode=" + error.getErrorCode()
                + ", http=" + error.getHttpCode()
                + ", server=" + error.getServerCode()
                + ", device=" + error.getDeviceCode();
    }

    // ==================== dry_run 模式支持 ====================

    /** dry_run 参数名 */
    private static final String DRY_RUN_PARAM = "dry_run";

    /**
     * 判断当前调用是否为 dry_run 模式。
     * dry_run 模式下参数校验和前置条件检查照常执行，但跳过实际 SDK 调用。
     *
     * @param args 输入参数
     * @return true 表示 dry_run 模式
     */
    protected static boolean isDryRun(Map<String, Object> args) {
        return getBooleanArg(args, DRY_RUN_PARAM);
    }

    /**
     * 构建 dry_run 模式的预览响应。
     * 响应标记 {@code _dry_run: true}，描述"如果真正执行会做什么"。
     *
     * @param description 操作描述（如 "Would take off drone DRONE-001 to altitude 20m"）
     * @param preview     预览信息 Map（包含 tool、action、target、parameters 等）
     * @return dry_run 预览结果
     */
    protected static Map<String, Object> dryRunResult(String description, Map<String, Object> preview) {
        Map<String, Object> result = new LinkedHashMap<>(5);
        result.put("success", true);
        result.put("_dry_run", true);
        result.put("description", description);
        result.put("would_execute", preview);
        result.put("message", "Dry run: no changes were made. Pass dry_run=false or omit to execute.");
        return result;
    }

    /**
     * 构建 dry_run 预览 Map，消除各 Tool 中的重复代码。
     * 统一字段格式：tool、action、target、parameters、prerequisites_checked、security_level、requires_confirm。
     *
     * @param toolName             工具名称
     * @param action               操作名称
     * @param target               操作目标（设备 SN / task UUID 等）
     * @param params               参数 Map
     * @param prerequisitesChecked 已通过的前置条件列表
     * @param securityLevel        安全等级（使用 {@link SecurityLevel#name()} 获取）
     * @param requiresConfirm      是否需要人工确认
     * @return 预览信息 Map
     */
    protected static Map<String, Object> buildDryRunPreview(
            String toolName, String action, String target,
            Map<String, Object> params, List<String> prerequisitesChecked,
            SecurityLevel securityLevel, boolean requiresConfirm) {
        Map<String, Object> preview = new LinkedHashMap<>(7);
        preview.put("tool", toolName);
        preview.put("action", action);
        preview.put("target", target);
        preview.put("parameters", params);
        preview.put("prerequisites_checked", prerequisitesChecked);
        preview.put("security_level", securityLevel.name());
        preview.put("requires_confirm", requiresConfirm);
        return preview;
    }

    // ==================== GPS 距离计算辅助方法 ====================

    /** 地球平均半径（米），用于 Haversine 公式 */
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /**
     * 使用 Haversine 公式计算两个 GPS 坐标之间的水平距离（不含高度差）。
     *
     * @param lat1 起点纬度（度）
     * @param lon1 起点经度（度）
     * @param lat2 终点纬度（度）
     * @param lon2 终点经度（度）
     * @return 两点之间的水平距离（米）
     */
    protected static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    // ==================== GPS 坐标校验辅助方法 ====================

    /**
     * 校验 GPS 坐标参数有效性。
     *
     * @param lat    纬度
     * @param lng    经度
     * @param height 高度（米）
     * @return null 表示校验通过，非 null 为错误响应
     */
    protected Map<String, Object> validateGpsCoordinates(double lat, double lng, double height) {
        if (Double.isNaN(lat) || lat < -90 || lat > 90) {
            return errorResult("Invalid latitude: " + lat, "Must be between -90 and 90.");
        }
        if (Double.isNaN(lng) || lng < -180 || lng > 180) {
            return errorResult("Invalid longitude: " + lng, "Must be between -180 and 180.");
        }
        if (Double.isNaN(height) || height < 2 || height > 10000) {
            return errorResult("Invalid height: " + height, "Must be between 2 and 10000 meters.");
        }
        return null;
    }

    /**
     * 从参数 Map 中获取 double 类型参数。
     *
     * @param args     参数 Map
     * @param key      参数名
     * @param fallback 默认值
     * @return 参数值
     */
    protected static double getDoubleArg(Map<String, Object> args, String key, double fallback) {
        Object val = args != null ? args.get(key) : null;
        return toDouble(val, fallback);
    }

    /**
     * 将单个值安全转为 double（支持 Number 和 String 含前后空白）。
     * 供 getDoubleArg 和 elicitation 补输入后的值转换复用。
     *
     * @param value    待转换值
     * @param fallback 解析失败时的回退值
     * @return double 值
     */
    protected static double toDouble(Object value, double fallback) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException ignored) {
                // 解析失败，返回默认值
            }
        }
        return fallback;
    }
}
