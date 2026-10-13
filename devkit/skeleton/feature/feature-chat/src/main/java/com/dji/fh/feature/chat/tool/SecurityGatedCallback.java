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

package com.dji.fh.feature.chat.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 安全网关包装器 — 根据 Tool 的 {@link ChatSecurityLevel} 决定放行或拒绝。
 *
 * <ul>
 *   <li>L0 READ_ONLY：直接委托给被包装的 {@link ToolCallback} 执行</li>
 *   <li>L1 DEVICE_CONTROL：需已认证且在 {@code fh.chat.tool-access.device-control-roles} 白名单内的角色；
 *       匿名及普通 user 角色默认拒绝</li>
 *   <li>L2 FLIGHT_CONTROL：返回错误信息，引导用户通过飞行舱页面操作</li>
 *   <li>L3 EMERGENCY：返回错误信息，拒绝执行</li>
 * </ul>
 *
 * <p>L1/L2/L3 级别通过返回结构化错误字符串（而非抛出异常）来拒绝执行。
 * 这样 Spring AI 的 {@code DefaultToolCallingManager} 可以正常处理 Tool 结果，
 * 将错误信息作为 Tool Response 发回 LLM，由 LLM 用自然语言向用户解释。</p>
 *
 * <p>此类实现 Spring AI 1.0.0 的 {@link ToolCallback} 接口，
 * 通过装饰器模式在不修改原始 Tool 的前提下叠加安全门控逻辑。</p>
 *
 * @author DJI DevKit Team
 */
public class SecurityGatedCallback implements ToolCallback {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityGatedCallback.class);

    /** 安全拦截错误响应的 error 字段值。 */
    private static final String ERROR_SECURITY_BLOCKED = "security_blocked";

    /** READ_ONLY 工具结果缓存 TTL（毫秒），30 秒。 */
    private static final long CACHE_TTL_MS = 30_000L;

    /** 被装饰的原始 Tool 回调。 */
    private final ToolCallback delegate;

    /** 此 Tool 的安全等级。 */
    private final ChatSecurityLevel level;

    /** 允许调用 DEVICE_CONTROL（L1）工具的角色白名单。 */
    private final Set<String> deviceControlRoles;

    /** READ_ONLY 级别的工具结果缓存，key = 输入 JSON，避免重复调用 MCP Server。
     *  仅 READ_ONLY 实例分配（其他安全等级为 null）。 */
    private final Map<String, CacheEntry> readOnlyCache;

    /**
     * 构造函数（默认 DEVICE_CONTROL 仅允许 admin）。
     *
     * @param delegate 被装饰的原始 Tool 回调，不能为 null
     * @param level    此 Tool 的安全等级，不能为 null
     */
    public SecurityGatedCallback(ToolCallback delegate, ChatSecurityLevel level) {
        this(delegate, level, Set.of("admin"));
    }

    /**
     * 构造函数。
     *
     * @param delegate           被装饰的原始 Tool 回调，不能为 null
     * @param level              此 Tool 的安全等级，不能为 null
     * @param deviceControlRoles 允许 DEVICE_CONTROL 工具的角色白名单（不能为 null/空——空即拒绝一切）
     */
    public SecurityGatedCallback(ToolCallback delegate, ChatSecurityLevel level,
                                 Set<String> deviceControlRoles) {
        this.delegate = delegate;
        this.level = level;
        this.deviceControlRoles = deviceControlRoles == null ? Set.of() : Set.copyOf(deviceControlRoles);
        this.readOnlyCache = (level == ChatSecurityLevel.READ_ONLY) ? new ConcurrentHashMap<>() : null;
    }

    /**
     * 执行安全检查，通过后委托给被装饰的 Tool。
     *
     * @param input Tool 调用的 JSON 输入字符串
     * @return Tool 执行结果；安全检查未通过时返回错误信息字符串
     */
    @Override
    public String call(String input) {
        String blocked = checkSecurityPolicy();
        if (blocked != null) {
            return blocked;
        }
        return executeWithCache(input, () -> delegate.call(input));
    }

    /**
     * 执行安全检查，通过后委托给被装饰的 Tool（含 {@link ToolContext}）。
     *
     * @param input       Tool 调用的 JSON 输入字符串
     * @param toolContext Spring AI 工具上下文
     * @return Tool 执行结果；安全检查未通过时返回错误信息字符串
     */
    @Override
    public String call(String input, ToolContext toolContext) {
        String blocked = checkSecurityPolicy();
        if (blocked != null) {
            return blocked;
        }
        return executeWithCache(input, () -> delegate.call(input, toolContext));
    }

    /**
     * 对 READ_ONLY 级别的工具应用 TTL 缓存；其他级别直接执行。
     * 错误响应不会被缓存，以确保重连后的重试能正常执行。
     *
     * @param input    工具输入 JSON（作为 cache key）
     * @param supplier 实际执行逻辑
     * @return 工具执行结果（可能来自缓存）
     */
    private String executeWithCache(String input, Supplier<String> supplier) {
        if (level != ChatSecurityLevel.READ_ONLY) {
            return supplier.get();
        }
        // readOnlyCache != null guaranteed by constructor when level == READ_ONLY
        CacheEntry cached = readOnlyCache.get(input);
        if (cached != null && !cached.isExpired()) {
            LOG.debug("安全门控：缓存命中 tool={}", delegate.getToolDefinition().name());
            return cached.value();
        }
        String result = supplier.get();
        if (!isErrorResponse(result)) {
            readOnlyCache.put(input, new CacheEntry(result, System.currentTimeMillis()));
            // 驱逐过期条目，防止缓存无限增长
            readOnlyCache.entrySet().removeIf(e -> e.getValue().isExpired());
        } else {
            // 错误响应不缓存，同时移除可能存在的旧缓存条目
            readOnlyCache.remove(input);
        }
        return result;
    }

    /**
     * 判断工具调用结果是否为错误响应。
     * 错误响应格式为 JSON 且包含 "status":"error" 或 "error" 顶层字段。
     *
     * @param result 工具返回的字符串
     * @return true 表示错误响应
     */
    private static boolean isErrorResponse(String result) {
        if (result == null || result.isEmpty()) {
            return true;
        }
        return result.contains("\"status\":\"error\"") || result.contains("\"error\":");
    }

    /**
     * 返回被装饰 Tool 的定义（name、description、inputSchema）。
     *
     * @return ToolDefinition
     */
    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    /**
     * 返回被装饰 Tool 的元数据。
     *
     * @return ToolMetadata
     */
    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    /**
     * 返回此 Tool 的安全等级。
     *
     * @return 安全等级
     */
    public ChatSecurityLevel getSecurityLevel() {
        return level;
    }

    /**
     * 根据安全等级决定是否放行。
     *
     * <p>安全修复：DEVICE_CONTROL（L1）不再无条件放行，
     * 需当前请求角色命中 {@code deviceControlRoles} 白名单。
     *
     * @return null 表示放行；非 null 表示被拒绝，返回值为错误信息
     */
    private String checkSecurityPolicy() {
        ToolDefinition def = delegate.getToolDefinition();
        String toolName = (def != null && def.name() != null) ? def.name() : "unknown";
        switch (level) {
            case READ_ONLY -> {
                // 只读查询对匿名也开放（无副作用）
                return null;
            }
            case DEVICE_CONTROL -> {
                SecurityContext ctx = SecurityContextHolder.current();
                if (ctx == null || !ctx.isAuthenticated()) {
                    LOG.info("安全门控：L1 工具 {} 被拦截，未登录用户", toolName);
                    return buildBlockedResponse("DEVICE_CONTROL", toolName,
                            "此操作属于设备控制（起飞/返航/航线等），需要登录且具备授权角色后才能执行。"
                                    + "请完成登录后重试。");
                }
                boolean authorized = deviceControlRoles.stream().anyMatch(ctx::hasRole);
                if (!authorized) {
                    LOG.info("安全门控：L1 工具 {} 被拦截，角色不足 principal={}",
                            toolName, ctx.principalId());
                    return buildBlockedResponse("DEVICE_CONTROL", toolName,
                            "此操作属于设备控制（起飞/返航/航线等），当前账号角色无权执行。"
                                    + "请联系管理员开通授权角色。");
                }
                return null;
            }
            case FLIGHT_CONTROL -> {
                LOG.info("安全门控：L2 工具 {} 被拦截，需通过飞行舱操作", toolName);
                return buildBlockedResponse("FLIGHT_CONTROL", toolName,
                        "此操作属于飞行控制类操作，涉及无人机安全，不允许通过 Chat 执行。"
                                + "请引导用户前往飞行舱页面进行操作。");
            }
            case EMERGENCY -> {
                LOG.warn("安全门控：L3 工具 {} 被拦截，紧急操作禁止通过 Chat 执行", toolName);
                return buildBlockedResponse("EMERGENCY", toolName,
                        "此操作属于紧急操作（如紧急停机），绝对禁止通过 Chat 执行。"
                                + "请告知用户通过专用紧急操作界面处理。");
            }
            default -> throw new IllegalStateException("未知安全等级: " + level);
        }
    }

    /**
     * 构造安全拦截的 JSON 错误响应。
     *
     * @param level    安全等级名称
     * @param toolName 被拦截的工具名称
     * @param message  拦截原因说明
     * @return 结构化 JSON 错误字符串
     */
    private static String buildBlockedResponse(String level, String toolName, String message) {
        return "{\"error\": \"" + ERROR_SECURITY_BLOCKED + "\", "
                + "\"level\": \"" + level + "\", "
                + "\"tool\": \"" + toolName + "\", "
                + "\"message\": \"" + message + "\"}";
    }

    /**
     * 缓存条目，持有工具调用结果和写入时间戳。
     *
     * @param value     缓存的工具返回值
     * @param timestamp 写入时间（System.currentTimeMillis()）
     */
    private record CacheEntry(String value, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
}
