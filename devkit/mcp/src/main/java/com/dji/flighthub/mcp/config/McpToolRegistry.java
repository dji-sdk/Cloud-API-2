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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * MCP tool 注册中心 — 持有所有 tool 的定义和执行 handler。
 * 供 stdio 和 SSE 两种传输层使用，统一处理 tool 请求。
 *
 * <p>拦截层（executeTool 中按序执行）：
 * <ol>
 *   <li>幂等检查：重复请求直接返回缓存结果</li>
 *   <li>执行 tool handler</li>
 *   <li>成功结果写入幂等缓存</li>
 *   <li>审计日志记录</li>
 * </ol>
 *
 * @author DJI FlightHub SDK Team
 */
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    private final List<ToolEntry> tools = new ArrayList<>();
    private AuditService auditService;
    private IdempotencyCache idempotencyCache;

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
        String name = (String) definition.get("name");
        tools.add(new ToolEntry(name, definition, handler, requiresHumanConfirmation));
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
        String idempotencyKey = buildIdempotencyKey();

        try {
            // [1] 幂等检查：sentinel 模式抢占槽位，命中则直接返回
            Map<String, Object> cached = tryGetCachedResult(idempotencyKey);
            if (cached != null) {
                idempotentHit = true;
                success = true;
                log.debug("幂等缓存命中, 跳过执行: tool={}, key={}", name, idempotencyKey);
                return cached;
            }

            // [2] 查找并执行 tool handler
            Map<String, Object> result = tools.stream()
                    .filter(t -> t.name().equals(name))
                    .findFirst()
                    .map(t -> t.handler().apply(args))
                    .orElseGet(() -> buildUnknownToolResponse(name));

            // [3] 判断执行是否成功
            success = Boolean.TRUE.equals(result.get("success"));
            if (!success) {
                Object errMsg = result.get("error_message");
                errorMessage = errMsg != null ? errMsg.toString() : null;
            }

            // [4] 缓存成功结果 / 清理失败占位
            cacheResultOrCleanup(idempotencyKey, success, result);

            return result;
        } catch (Exception e) {
            cleanupPendingQuietly(idempotencyKey);
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // [5] 审计日志（无论成功失败都记录）
            long durationMs = System.currentTimeMillis() - startTime;
            logAudit(name, success, errorMessage, durationMs, idempotentHit);
        }
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

    // --- 私有辅助方法 ---

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
     */
    private void logAudit(String toolName, boolean success, String errorMessage,
                          long durationMs, boolean idempotentHit) {
        if (auditService != null) {
            try {
                auditService.logExecution(toolName, success, errorMessage, durationMs, idempotentHit);
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

    public record ToolEntry(
            String name,
            Map<String, Object> definition,
            Function<Map<String, Object>, Map<String, Object>> handler,
            boolean requiresHumanConfirmation
    ) {}
}
