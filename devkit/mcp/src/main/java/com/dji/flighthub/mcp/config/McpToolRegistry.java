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

package com.dji.flighthub.mcp.config;

import com.dji.flighthub.mcp.audit.AuditService;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.ratelimit.ToolRateLimiter;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * MCP tool 注册中心 — 持有所有 tool 的定义和执行 handler。
 * 供 stdio 和 SSE 两种传输层使用，统一处理 tool 请求。
 *
 * <p>拦截层（executeTool 中按序执行）：
 * <ol>
 *   <li>幂等检查：重复请求直接返回缓存结果</li>
 *   <li>安全等级门控：session 上限低于工具声明等级直接拒绝（sim_* 与 CRITICAL 自检工具豁免）</li>
 *   <li>工具级限流：超出 {@link SecurityLevel} 档位配额直接拒绝（不进入 handler）</li>
 *   <li>执行 tool handler</li>
 *   <li>输出体积预算：超阈值结果截断后返回（并注入截断元信息）</li>
 *   <li>判断执行结果是否成功</li>
 *   <li>成功结果写入幂等缓存</li>
 *   <li>审计日志记录</li>
 * </ol>
 *
 * @author DJI FlightHub SDK Team
 */
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    /** 限流拒绝的错误码（McpErrorCode 体系落地前先用字面常量） */
    public static final String ERROR_CODE_RATE_LIMITED = "ERR_RATE_LIMITED";

    /** 安全等级拒绝的错误码（与 McpErrorCode 体系落地前先用字面常量） */
    public static final String ERROR_CODE_SECURITY_LEVEL = "ERR_SECURITY_LEVEL";

    /** sim 工具名称前缀 —— sim 工具由 AbstractSimTool 自检安全等级，派发层门控放行 */
    private static final String SIM_TOOL_PREFIX = "sim_";

    private SessionManager sessionManager;

    private final List<ToolEntry> tools = new ArrayList<>();
    private AuditService auditService;
    private IdempotencyCache idempotencyCache;
    private OutputBudgetGuard outputBudgetGuard;
    private ToolRateLimiter toolRateLimiter;

    /**
     * 注册 tool 定义和执行 handler（带安全等级）。
     *
     * @param definition                tool 定义（必须包含 "name" 字段）
     * @param handler                   tool 执行函数
     * @param requiresHumanConfirmation 是否需要人工确认（L1+ 危险操作）
     * @param securityLevel             工具安全等级，决定限流档位
     */
    public void register(Map<String, Object> definition,
                         Function<Map<String, Object>, Map<String, Object>> handler,
                         boolean requiresHumanConfirmation,
                         SecurityLevel securityLevel) {
        String name = (String) definition.get("name");
        tools.add(new ToolEntry(name, definition, handler, requiresHumanConfirmation, securityLevel));
    }

    /**
     * 注册 tool 定义和执行 handler。
     *
     * @param definition                tool 定义（必须包含 "name" 字段）
     * @param handler                   tool 执行函数
     * @param requiresHumanConfirmation 是否需要人工确认（L1+ 危险操作）
     */
    public void register(Map<String, Object> definition,
                         Function<Map<String, Object>, Map<String, Object>> handler,
                         boolean requiresHumanConfirmation) {
        register(definition, handler, requiresHumanConfirmation, SecurityLevel.READ_ONLY);
    }

    /**
     * 注册 tool 定义和执行 handler（默认不需要人工确认）。
     *
     * @param definition tool 定义（必须包含 "name" 字段）
     * @param handler    tool 执行函数
     */
    public void register(Map<String, Object> definition, Function<Map<String, Object>, Map<String, Object>> handler) {
        register(definition, handler, false);
    }

    /**
     * 获取所有 tool 定义（用于 tools/list 响应）。
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return tools.stream().map(ToolEntry::definition).toList();
    }

    /**
     * 根据名称执行指定 tool，传入参数。
     *
     * <p>执行前进行幂等检查（防重放），执行后记录审计日志。
     *
     * @param name tool 名称
     * @param args tool 参数
     * @return tool 执行结果
     */
    public Map<String, Object> executeTool(String name, Map<String, Object> args) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        boolean idempotentHit = false;
        boolean rateLimited = false;
        boolean securityDeniedFlag = false;
        String idempotencyKey = buildIdempotencyKey();

        try {
            // 幂等命中优先于安全门控：会话上限被下调后重放相同 requestId 仍返回已缓存结果
            // （幂等语义优先，已评估可接受）
            // [1] 幂等检查：sentinel 模式抢占槽位，命中则直接返回（命中不消耗限流配额）
            Map<String, Object> cached = tryGetCachedResult(idempotencyKey);
            if (cached != null) {
                idempotentHit = true;
                success = true;
                log.debug("幂等缓存命中, 跳过执行: tool={}, key={}", name, idempotencyKey);
                return cached;
            }

            ToolEntry entry = findTool(name).orElse(null);

            // [2] 安全等级门控：session 上限低于工具声明等级时拒绝执行（fail-closed 第二道闸）。
            //     sim_* 工具与 CRITICAL 自检工具豁免（见 enforceSecurityGate）。
            Map<String, Object> gateDenied = enforceSecurityGate(entry, name);
            if (gateDenied != null) {
                securityDeniedFlag = true;
                errorMessage = Objects.toString(gateDenied.get("error_message"), null);
                // 拒绝不是业务结果，释放 [1] 抢占的 PENDING 占位，避免死循环占满幂等缓存
                cleanupPendingQuietly(idempotencyKey);
                return gateDenied;
            }

            // [3] 工具级限流：未知 tool 不消耗配额，拒绝时不进入 handler
            ToolRateLimiter.Decision decision = tryAcquireRateLimit(entry, args);
            if (decision != null && !decision.allowed()) {
                rateLimited = true;
                errorMessage = formatRateLimitedMessage(name, decision.limitRpm());
                // 拒绝不是业务结果，必须释放 [1] 抢占的 PENDING 占位，
                // 否则死循环会把幂等缓存占满，使后续重放保护静默失效
                cleanupPendingQuietly(idempotencyKey);
                return buildRateLimitedResponse(name, decision);
            }

            // [4] 执行 tool handler
            Map<String, Object> result = entry != null
                    ? entry.handler().apply(args)
                    : buildUnknownToolResponse(name);

            // [5] 输出体积预算：超阈值结果在返回前统一截断，避免撑爆 LLM 上下文
            result = applyOutputBudget(name, result);

            // [6] 判断执行是否成功
            success = Boolean.TRUE.equals(result.get("success"));
            if (!success) {
                Object errMsg = result.get("error_message");
                errorMessage = errMsg != null ? errMsg.toString() : null;
            }

            // [7] 缓存成功结果 / 清理失败占位（缓存的是截断后的结果，避免内存放大）
            cacheResultOrCleanup(idempotencyKey, success, result);

            return result;
        } catch (Exception e) {
            cleanupPendingQuietly(idempotencyKey);
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // [8] 审计日志（无论成功失败都记录）
            long durationMs = System.currentTimeMillis() - startTime;
            logAudit(name, success, errorMessage, durationMs, idempotentHit, rateLimited,
                    securityDeniedFlag);
        }
    }

    /**
     * 查找已注册的 tool。
     *
     * @param name tool 名称
     * @return 匹配的条目，未注册时返回空
     */
    private Optional<ToolEntry> findTool(String name) {
        return tools.stream().filter(t -> t.name().equals(name)).findFirst();
    }

    /**
     * 执行派发层安全等级门控。
     *
     * <p>放行条件（按检查顺序，任一满足即放行）：
     * <ol>
     *   <li>未知 tool（entry == null）——交给 unknown-tool 报错路径，不改变现有语义</li>
     *   <li>未注入 sessionManager——无会话上限可比较，跳过门控</li>
     *   <li>{@code name} 以 {@link #SIM_TOOL_PREFIX} 开头——sim 工具由
     *       {@code AbstractSimTool.enforceSimSecurityLevel} 按 {@code FH_MAX_SECURITY_LEVEL} 自检，
     *       不受 session 上限约束</li>
     *   <li>工具声明等级为 {@code CRITICAL}——在读取活跃 session 之前提前返回，L3 工具由
     *       {@code execute()} 内自检（{@code FhEmergencyStopTool.precheck} 首行即
     *       {@code enforceToolSecurityLevel()}），派发层豁免以避免紧急场景被默认 L2 会话预拒</li>
     *   <li>无活跃 session——建连/握手前放行（{@code fh_connect} 是建立链路的必要前置，
     *       不能被自身门控拦下）</li>
     *   <li>{@code entry.securityLevel().isAllowedBy(session.getMaxSecurityLevel())} 为 true</li>
     * </ol>
     *
     * @param entry 目标 tool 条目，null（未知 tool）时放行
     * @param name  tool 名称
     * @return null=放行; 非 null=拒绝响应（success=false）
     */
    private Map<String, Object> enforceSecurityGate(ToolEntry entry, String name) {
        if (entry == null || sessionManager == null) {
            return null;
        }
        if (name.startsWith(SIM_TOOL_PREFIX)) {
            return null;
        }
        SecurityLevel required = entry.securityLevel();
        if (required == SecurityLevel.CRITICAL) {
            return null;
        }
        SessionContext session = sessionManager.getActiveSession();
        if (session == null) {
            return null;
        }
        if (required.isAllowedBy(session.getMaxSecurityLevel())) {
            return null;
        }
        return buildSecurityDeniedResponse(name, required, session.getMaxSecurityLevel());
    }

    /**
     * 构建安全等级拒绝响应（仿 buildRateLimitedResponse 结构）。
     *
     * @param name           tool 名称
     * @param required       工具声明等级
     * @param sessionMaxLevel session 允许的最高等级
     * @return 结构化拒绝响应（success=false，走 isError 内容块）
     */
    private Map<String, Object> buildSecurityDeniedResponse(String name,
                                                            SecurityLevel required,
                                                            int sessionMaxLevel) {
        Map<String, Object> response = new LinkedHashMap<>(5);
        response.put("success", false);
        response.put("error_code", ERROR_CODE_SECURITY_LEVEL);
        response.put("error_message", "Operation denied: insufficient security level");
        response.put("suggestion", String.format(
                "This operation requires %s, but session allows max %s. "
                + "Reconnect with FH_MAX_SECURITY_LEVEL raised, or use a different tool.",
                required.getLabel(), SecurityLevel.labelFor(sessionMaxLevel)));
        response.put("security_denied", true);
        return response;
    }

    /**
     * 对本次调用执行工具级限流检查。
     *
     * <p>参数需下传给限流器：设备序列号决定计数键，action 决定是否跳过 per-tool 覆盖
     * （中止 / 查询类 action 不能被启动类的高成本配额挡住）。
     *
     * @param entry 目标 tool 条目，null（未知 tool）时跳过限流
     * @param args  tool 调用参数，可为 null
     * @return 限流决策；未注入限流器或未知 tool 时返回 null（视为放行）
     */
    private ToolRateLimiter.Decision tryAcquireRateLimit(ToolEntry entry, Map<String, Object> args) {
        if (entry == null || toolRateLimiter == null) {
            return null;
        }
        RequestContext ctx = RequestContext.current();
        String sessionId = ctx != null ? ctx.getSessionId() : null;
        return toolRateLimiter.tryAcquire(sessionId, entry.name(), entry.securityLevel(), args);
    }

    /**
     * 构建限流拒绝响应。
     *
     * <p>文案遵循「指示停，不指示等」原则：死循环中的模型对「重试」类提示极为敏感，
     * 若提示「稍后重试」，模型只会在 {@code retry_after_ms} 后再次调用。
     * {@code retry_after_ms} 仅保留给人和调试脚本使用。
     *
     * @param name     tool 名称
     * @param decision 限流决策
     * @return 结构化拒绝响应（success=false，走 isError 内容块，不抛 JSON-RPC error）
     */
    private Map<String, Object> buildRateLimitedResponse(String name, ToolRateLimiter.Decision decision) {
        Map<String, Object> response = new LinkedHashMap<>(8);
        response.put("success", false);
        response.put("error_code", ERROR_CODE_RATE_LIMITED);
        // 不说 "for this session"：无 sessionId 时（stdio / 握手前）计数粒度是全局的
        response.put("error_message", formatRateLimitedMessage(name, decision.limitRpm()));
        response.put("suggestion", "You appear to be calling this tool in a loop. STOP calling it and "
                + "report the current drone state to the user instead of retrying.");
        response.put("rate_limited", true);
        response.put("retry_after_ms", decision.retryAfterMs());
        response.put("limit_rpm", decision.limitRpm());
        response.put("loop_hint", "If you intended to repeat this action, use a single call with a longer "
                + "duration or switch to a batch/wayline tool instead of calling repeatedly.");
        return response;
    }

    /**
     * 构建限流错误文案 — 拒绝响应与审计日志共用，避免两处拼写漂移。
     *
     * @param name tool 名称
     * @param rpm  生效的每分钟配额
     * @return 统一格式的错误信息
     */
    private static String formatRateLimitedMessage(String name, int rpm) {
        return "Tool '" + name + "' rate limit exceeded (max " + rpm + " calls/min).";
    }

    /**
     * 判断指定 tool 是否需要人工确认通道（L1+ 危险操作）。
     *
     * @param name tool 名称
     * @return true 表示需要 elicitation 通道才允许执行
     */
    public boolean isHumanConfirmRequired(String name) {
        return tools.stream()
                .filter(t -> t.name().equals(name))
                .findFirst()
                .map(ToolEntry::requiresHumanConfirmation)
                .orElse(false);
    }

    /**
     * 获取已注册 tool 数量。
     *
     * @return tool 数量
     */
    public int getToolCount() {
        return tools.size();
    }

    // --- 依赖注入（通过 setter，因为本类非 Spring @Component） ---

    /**
     * 注入审计服务。
     *
     * @param auditService 审计服务实例
     */
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 注入幂等缓存。
     *
     * @param idempotencyCache 幂等缓存实例
     */
    public void setIdempotencyCache(IdempotencyCache idempotencyCache) {
        this.idempotencyCache = idempotencyCache;
    }

    /**
     * 注入输出体积预算守卫。
     *
     * @param outputBudgetGuard 输出预算守卫实例
     */
    public void setOutputBudgetGuard(OutputBudgetGuard outputBudgetGuard) {
        this.outputBudgetGuard = outputBudgetGuard;
    }

    /**
     * 注入工具级限流器。
     *
     * @param toolRateLimiter 限流器实例；为 null 时不限流
     */
    public void setToolRateLimiter(ToolRateLimiter toolRateLimiter) {
        this.toolRateLimiter = toolRateLimiter;
    }

    /**
     * 注入 session 管理器，供派发层安全等级门控读取会话上限。
     *
     * @param sessionManager session 管理器实例；为 null 时跳过门控
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // --- 私有辅助方法 ---

    /**
     * 对 tool 结果施加输出体积预算。未注入守卫时原样返回。
     *
     * @param toolName tool 名称
     * @param result   tool 执行结果
     * @return 原始结果或截断后的结果
     */
    private Map<String, Object> applyOutputBudget(String toolName, Map<String, Object> result) {
        if (outputBudgetGuard == null || result == null) {
            return result;
        }
        try {
            return outputBudgetGuard.apply(toolName, result);
        } catch (Exception e) {
            // 兜底逻辑异常不应影响业务结果
            log.warn("输出预算守卫异常，返回原始结果: tool={}, error={}", toolName, e.getMessage());
            return result;
        }
    }

    /**
     * 尝试从幂等缓存获取结果（sentinel 模式：未命中时抢占槽位）。
     *
     * @param key 幂等 key，null 时跳过检查
     * @return 缓存的真实结果，未命中或无需检查时返回 null
     */
    private Map<String, Object> tryGetCachedResult(String key) {
        if (key == null || idempotencyCache == null) {
            return null;
        }
        return idempotencyCache.getOrMarkPending(key);
    }

    /**
     * 缓存成功结果或清理失败占位。异常不影响主流程。
     *
     * @param key     幂等 key，null 时跳过
     * @param success 执行是否成功
     * @param result  执行结果
     */
    private void cacheResultOrCleanup(String key, boolean success, Map<String, Object> result) {
        if (key == null || idempotencyCache == null) {
            return;
        }
        try {
            if (success) {
                idempotencyCache.put(key, result);
            } else {
                idempotencyCache.removePending(key);
            }
        } catch (Exception e) {
            log.warn("幂等缓存写入异常: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 静默清理 pending 占位（异常路径调用，失败忽略）。
     *
     * @param key 幂等 key，null 时跳过
     */
    private void cleanupPendingQuietly(String key) {
        if (key == null || idempotencyCache == null) {
            return;
        }
        try {
            idempotencyCache.removePending(key);
        } catch (Exception ignored) {
            // 清理失败不影响异常传播
        }
    }

    /**
     * 从当前 RequestContext 构建幂等 key。
     *
     * @return 幂等 key（clientId:toolName:requestId），无上下文时返回 null
     */
    private String buildIdempotencyKey() {
        RequestContext ctx = RequestContext.current();
        if (ctx == null) {
            return null;
        }
        return ctx.buildIdempotencyKey();
    }

    /**
     * 记录审计日志（委托给 AuditService）。
     *
     * @param toolName     工具名
     * @param success      执行是否成功
     * @param errorMessage 错误信息
     * @param durationMs   执行耗时
     * @param idempotentHit 是否命中幂等缓存
     * @param rateLimited  是否被限流拒绝
     * @param securityDenied 是否被派发层安全等级门控拒绝
     */
    private void logAudit(String toolName, boolean success, String errorMessage,
                          long durationMs, boolean idempotentHit, boolean rateLimited,
                          boolean securityDenied) {
        if (auditService != null) {
            try {
                auditService.logExecution(toolName, success, errorMessage, durationMs,
                        idempotentHit, rateLimited, securityDenied);
            } catch (Exception e) {
                // 审计日志记录失败不应影响工具执行
                log.warn("审计日志记录异常: tool={}, error={}", toolName, e.getMessage());
            }
        }
    }

    // --- 不支持能力提示：模糊匹配与友好错误构建 ---

    /** 模糊匹配最多返回的相似 tool 数量 */
    private static final int MAX_SUGGESTIONS = 3;

    /** Levenshtein 距离阈值，超过此值不视为相似 */
    private static final int MAX_LEVENSHTEIN_DISTANCE = 3;

    /**
     * 构建 unknown tool 的友好错误响应，包含模糊匹配建议。
     *
     * @param name 客户端请求的不存在的 tool 名称
     * @return 包含 similar_tools 的错误响应
     */
    private Map<String, Object> buildUnknownToolResponse(String name) {
        List<String> similarTools = findSimilarTools(name);

        Map<String, Object> error = new LinkedHashMap<>(5);
        error.put("success", false);
        error.put("error_message",
                "Capability not available: '" + name + "' is not a supported tool.");

        if (similarTools.isEmpty()) {
            error.put("suggestion", "Call tools/list to see all available capabilities.");
        } else {
            String joined = String.join(", ", similarTools);
            error.put("suggestion",
                    "Did you mean: " + joined + "? Call tools/list for all available capabilities.");
            error.put("similar_tools", similarTools);
        }

        log.info("未知 tool 调用: name={}, 建议={}", name, similarTools);
        return error;
    }

    /**
     * 模糊匹配相似的 tool 名称。
     *
     * <p>匹配策略（按优先级）：
     * <ol>
     *   <li>前缀匹配：已注册 tool 名称以输入开头</li>
     *   <li>包含匹配：已注册 tool 名称包含输入（去除 "fh_" 前缀后）</li>
     *   <li>Levenshtein 距离 ≤ 3：编辑距离最近的 tool</li>
     * </ol>
     *
     * @param name 客户端请求的 tool 名称
     * @return 最多 {@value #MAX_SUGGESTIONS} 个相似 tool 名称列表
     */
    private List<String> findSimilarTools(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        String lowerName = name.toLowerCase();
        // 去除常见前缀以提取核心关键词（如 "fh_video" → "video"）
        String keyword = lowerName.startsWith("fh_")
                ? lowerName.substring(3) : lowerName;

        List<String> prefixMatches = new ArrayList<>();
        List<String> containsMatches = new ArrayList<>();
        List<ScoredTool> levenshteinMatches = new ArrayList<>();

        for (ToolEntry tool : tools) {
            String toolName = tool.name();
            String toolLower = toolName.toLowerCase();

            // 策略 1：前缀匹配（tool 名称以输入开头）
            if (toolLower.startsWith(lowerName)) {
                prefixMatches.add(toolName);
                continue;
            }

            // 策略 2：包含匹配（tool 名称包含核心关键词）
            if (!keyword.isEmpty() && toolLower.contains(keyword)) {
                containsMatches.add(toolName);
                continue;
            }

            // 策略 3：Levenshtein 距离
            int distance = levenshteinDistance(lowerName, toolLower);
            if (distance <= MAX_LEVENSHTEIN_DISTANCE) {
                levenshteinMatches.add(new ScoredTool(toolName, distance));
            }
        }

        // 按优先级合并结果，去重，限制数量
        List<String> result = new ArrayList<>(MAX_SUGGESTIONS);
        // 优先加入前缀匹配
        for (String match : prefixMatches) {
            if (result.size() >= MAX_SUGGESTIONS) {
                break;
            }
            result.add(match);
        }
        // 再加入包含匹配
        for (String match : containsMatches) {
            if (result.size() >= MAX_SUGGESTIONS) {
                break;
            }
            if (!result.contains(match)) {
                result.add(match);
            }
        }
        // 最后加入 Levenshtein 匹配（按距离升序）
        levenshteinMatches.sort(Comparator.comparingInt(ScoredTool::distance));
        for (ScoredTool scored : levenshteinMatches) {
            if (result.size() >= MAX_SUGGESTIONS) {
                break;
            }
            if (!result.contains(scored.name())) {
                result.add(scored.name());
            }
        }

        return result;
    }

    /**
     * 计算两个字符串的 Levenshtein 编辑距离。
     * 使用滚动数组优化空间复杂度为 O(min(m,n))。
     *
     * @param s 源字符串
     * @param t 目标字符串
     * @return 编辑距离（插入、删除、替换操作次数）
     */
    private static int levenshteinDistance(String s, String t) {
        if (s.equals(t)) {
            return 0;
        }
        if (s.isEmpty()) {
            return t.length();
        }
        if (t.isEmpty()) {
            return s.length();
        }

        // 保证 s 是较短的字符串以优化空间
        if (s.length() > t.length()) {
            String tmp = s;
            s = t;
            t = tmp;
        }

        int[] prev = new int[s.length() + 1];
        int[] curr = new int[s.length() + 1];

        for (int i = 0; i <= s.length(); i++) {
            prev[i] = i;
        }

        for (int j = 1; j <= t.length(); j++) {
            curr[0] = j;
            for (int i = 1; i <= s.length(); i++) {
                int cost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                curr[i] = Math.min(
                        Math.min(curr[i - 1] + 1, prev[i] + 1),
                        prev[i - 1] + cost);
            }
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }

        return prev[s.length()];
    }

    /** Levenshtein 匹配的临时得分记录 */
    private record ScoredTool(String name, int distance) {}

    /**
     * 已注册 tool 的元数据。
     *
     * @param name                      tool 名称
     * @param definition                tool 定义（tools/list 返回的 schema）
     * @param handler                   执行函数
     * @param requiresHumanConfirmation 是否需要人工确认
     * @param securityLevel             安全等级，决定限流档位
     */
    public record ToolEntry(
            String name,
            Map<String, Object> definition,
            Function<Map<String, Object>, Map<String, Object>> handler,
            boolean requiresHumanConfirmation,
            SecurityLevel securityLevel
    ) {}
}
