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

package com.dji.flighthub.mcp.annotation;

import com.dji.flighthub.mcp.audit.AuditService;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.config.OutputBudgetGuard;
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.ratelimit.ToolRateLimiter;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.drc.FhControlReleaseTool;
import com.dji.flighthub.mcp.tools.drc.FhDrcDisconnectTool;
import com.dji.flighthub.mcp.tools.drc.FhEmergencyStopTool;
import com.dji.flighthub.mcp.tools.drc.FhPayloadAcquireTool;
import com.dji.flighthub.mcp.tools.drc.FhPayloadReleaseTool;
import com.dji.flighthub.mcp.tools.livesync.FhLiveSyncTool;
import com.dji.flighthub.mcp.tools.psdk.FhPsdkWidgetTool;
import com.dji.flighthub.mcp.tools.simulator.AbstractSimTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP Tool 自动注册器。
 * 在所有 singleton bean 初始化完成后，自动扫描 AbstractMcpTool 实例并注册到 Registry。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class McpToolAutoRegistrar implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(McpToolAutoRegistrar.class);

    /**
     * 有意声明 CRITICAL 的工具类集合。
     *
     * <p>CRITICAL 语义为「未分类 / 自门控」：派发层不对其按 session 上限门控，
     * 由工具在 {@code execute()} 内自检（如 {@code FhEmergencyStopTool.precheck}
     * 首行即 {@code enforceToolSecurityLevel()}）。这些是有意为之，不应计入兜底告警。
     */
    private static final Set<Class<?>> INTENTIONALLY_CRITICAL = Set.of(FhEmergencyStopTool.class);

    /**
     * 有意不要求人工确认的 L1（REVERSIBLE）工具类集合（2026-10-30 安全复审 DK-P3-03）。
     *
     * <p>这些工具都是「释放 / 断开 / 同步 / 派发」语义：调用要么把系统退回安全状态，
     * 要么只是刷新远端状态，且常出现在失败清理路径上——强制确认会让调用方卡在
     * 「想释放却先要确认」的死循环里。它们仍受派发层 L1 门控、限流与前置条件链约束。
     *
     * <p>{@link AbstractSimTool} 的模拟器工具不在此列：其取值只影响模拟数据，
     * 且各自已按 {@code FH_MAX_SECURITY_LEVEL} 自检，不参与本告警。
     */
    private static final Set<Class<?>> INTENTIONALLY_UNCONFIRMED_REVERSIBLE = Set.of(
            FhControlReleaseTool.class,
            FhDrcDisconnectTool.class,
            FhLiveSyncTool.class,
            FhPayloadAcquireTool.class,
            FhPayloadReleaseTool.class,
            FhPsdkWidgetTool.class);

    private final List<AbstractMcpTool> tools;
    private final McpToolRegistry registry;
    private final AuditService auditService;
    private final IdempotencyCache idempotencyCache;
    private final OutputBudgetGuard outputBudgetGuard;
    private final ToolRateLimiter toolRateLimiter;
    private final SessionManager sessionManager;

    /**
     * 构造注入所有依赖。
     *
     * @param tools             所有 AbstractMcpTool 子类 bean（Spring 自动收集）
     * @param registry          MCP 工具注册中心
     * @param auditService      审计服务
     * @param idempotencyCache  幂等缓存
     * @param outputBudgetGuard 输出体积预算守卫
     * @param toolRateLimiter   工具级限流器
     * @param sessionManager    session 管理器（供派发层安全门控读取会话上限）
     */
    public McpToolAutoRegistrar(List<AbstractMcpTool> tools,
                                McpToolRegistry registry,
                                AuditService auditService,
                                IdempotencyCache idempotencyCache,
                                OutputBudgetGuard outputBudgetGuard,
                                ToolRateLimiter toolRateLimiter,
                                SessionManager sessionManager) {
        this.tools = tools;
        this.registry = registry;
        this.auditService = auditService;
        this.idempotencyCache = idempotencyCache;
        this.outputBudgetGuard = outputBudgetGuard;
        this.toolRateLimiter = toolRateLimiter;
        this.sessionManager = sessionManager;
    }

    /**
     * 所有 singleton 初始化完成后执行，自动将每个 tool 注册到 Registry。
     * 同时注入横切关注点：审计服务、幂等缓存、输出体积预算守卫和工具级限流器。
     */
    @Override
    public void afterSingletonsInstantiated() {
        registry.setAuditService(auditService);
        registry.setIdempotencyCache(idempotencyCache);
        registry.setOutputBudgetGuard(outputBudgetGuard);
        registry.setToolRateLimiter(toolRateLimiter);
        // 注入 session 管理器后，派发层安全门控才真正生效（未注入时门控直接放行）
        registry.setSessionManager(sessionManager);

        for (AbstractMcpTool tool : tools) {
            try {
                // 启动时断言：tool 必须满足其一 —— (a) 标注 @McpTool 或 (b) 覆写了 execute()
                validateToolConfiguration(tool);
                // 启动时安全契约断言：FLIGHT 等级工具必须声明人工确认（fail-closed）
                validateFlightNeedsConfirmation(tool);
                Map<String, Object> definition = tool.getToolDefinition();
                // 安全等级在此刻捕获，作为限流档位的唯一元数据来源
                registry.register(definition, tool::execute,
                        tool.requiresHumanConfirmation(), tool.getSecurityLevel());
            } catch (Exception e) {
                log.error("Tool 注册失败，跳过: {}", tool.getClass().getSimpleName(), e);
            }
        }

        warnFallbackCriticalTools();
        warnUnconfirmedReversibleTools();

        log.info("Auto-registered {} MCP tools: {}", registry.getToolCount(),
                registry.getToolDefinitions().stream()
                        .map(d -> d.get("name")).toList());
    }

    /**
     * 启动告警：统计声明 CRITICAL 的 fh 工具（排除有意自门控的工具）。
     * CRITICAL 表示「未分类 / 自门控」：派发层不按 session 上限对其实施门控，
     * 若确为遗漏覆写 {@code getSecurityLevel()}，则该工具完全依赖自身逻辑防护，需人工确认。
     */
    private void warnFallbackCriticalTools() {
        List<String> fallbackCriticalTools = tools.stream()
                .filter(tool -> tool.getSecurityLevel() == SecurityLevel.CRITICAL)
                .filter(tool -> !INTENTIONALLY_CRITICAL.contains(tool.getClass()))
                .map(tool -> tool.getClass().getSimpleName())
                .toList();
        if (!fallbackCriticalTools.isEmpty()) {
            log.warn("以下 {} 个 fh 工具声明等级为 CRITICAL（可能未显式覆写 getSecurityLevel()），"
                            + "派发层不按 session 上限对其实施门控，请确认其已在各 action 内自行门控：{}",
                    fallbackCriticalTools.size(), fallbackCriticalTools);
        }
    }

    /**
     * 启动告警：找出声明 L1（REVERSIBLE）却完全不要求人工确认的 fh 工具（DK-P3-03）。
     *
     * <p>只靠安全等级门控意味着：SSE 下不会弹窗，调用方传 {@code confirm=true} 也不被读取，
     * 唯一的拦截是 {@code FH_MAX_SECURITY_LEVEL} / session 上限。若确为遗漏，
     * 输出体积、误操作面都缺少最后一道真人防线；若是有意为之（释放类），
     * 必须登记到 {@link #INTENTIONALLY_UNCONFIRMED_REVERSIBLE} 才能消除告警。
     *
     * <p>与 {@link #validateFlightNeedsConfirmation(AbstractMcpTool)} 的分工：
     * L2 及以上是 fail-closed（拒绝注册），L1 只告警不阻断，避免既有部署升级即失败。
     *
     * <p>已知盲区：类级声明 L0、但在 execute() 内用 {@code enforceSecurityLevel(REVERSIBLE)}
     * 抬升的 {@code fh_maintenance} / {@code fh_diagnosis} / {@code fh_camera} 无法通过反射识别，
     * 这三处仅由 README「无人工确认的写操作」表格披露。
     */
    private void warnUnconfirmedReversibleTools() {
        List<String> unconfirmedTools = tools.stream()
                .filter(tool -> tool.getSecurityLevel() == SecurityLevel.REVERSIBLE)
                .filter(tool -> !(tool instanceof AbstractSimTool))
                .filter(tool -> !INTENTIONALLY_UNCONFIRMED_REVERSIBLE.contains(tool.getClass()))
                .filter(tool -> !tool.requiresHumanConfirmation())
                .map(tool -> tool.getClass().getSimpleName())
                .toList();
        if (!unconfirmedTools.isEmpty()) {
            log.warn("以下 {} 个 L1（REVERSIBLE）工具既无类级人工确认、也未登记为有意豁免，"
                            + "SSE 下不会弹窗，仅由安全等级门控拦截，请确认或加入 "
                            + "INTENTIONALLY_UNCONFIRMED_REVERSIBLE：{}",
                    unconfirmedTools.size(), unconfirmedTools);
        }
    }

    /**
     * 启动时安全契约断言：FLIGHT 等级工具必须声明人工确认（fail-closed）。
     *
     * <p>FLIGHT 级别的操作可改变无人机飞行行为（起飞/指点/接管/航线执行等），
     * 若该工具未声明 {@code requiresHumanConfirmation()=true}，则缺少「真人确认」这一
     * 最终防线——SSE 传输下不会有弹窗，stdio 下也不会要求 confirm 参数。
     * 因此注册期直接抛异常拒绝注册，避免高等级工具静默漏配确认通道。
     *
     * <p>CRITICAL 语义为「未分类 / 自门控」，由工具在 execute() 内自行门控，不在此列。
     *
     * @param tool 待注册工具
     * @throws IllegalStateException 当工具等级为 FLIGHT 且未声明人工确认时抛出
     */
    private void validateFlightNeedsConfirmation(AbstractMcpTool tool) {
        if (tool.getSecurityLevel() != SecurityLevel.FLIGHT) {
            return;
        }
        if (INTENTIONALLY_CRITICAL.contains(tool.getClass())) {
            return;
        }
        if (tool.requiresHumanConfirmation()) {
            return;
        }
        throw new IllegalStateException(
                tool.getClass().getSimpleName() + " 声明安全等级为 FLIGHT（L2），"
                        + "但 requiresHumanConfirmation() 返回 false。"
                        + "L2 接管级操作必须要求人工确认，请同时声明两者或下调安全等级。");
    }

    /**
     * 启动时断言：tool 必须满足以下条件之一，否则抛异常阻止注册：
     * <ol>
     *   <li>标注 @McpTool 注解（注解驱动模式）</li>
     *   <li>覆写了 execute() 方法（Legacy 手写模式）</li>
     * </ol>
     * 防止因移除 abstract 后遗漏覆写导致运行时才爆炸。
     */
    private void validateToolConfiguration(AbstractMcpTool tool) {
        Class<?> toolClass = tool.getClass();
        boolean hasAnnotation = toolClass.isAnnotationPresent(McpTool.class);
        if (hasAnnotation) {
            return;
        }
        // 检查是否覆写了 execute()
        try {
            Method executeMethod = toolClass.getMethod("execute", Map.class);
            if (executeMethod.getDeclaringClass() == AbstractMcpTool.class) {
                throw new IllegalStateException(
                        toolClass.getSimpleName() + " 既没有 @McpTool 注解也未覆写 execute()，"
                                + "无法注册为 MCP Tool");
            }
        } catch (NoSuchMethodException e) {
            // 不应发生（AbstractMcpTool 肯定有 execute 方法）
            throw new IllegalStateException("反射 execute() 失败: " + toolClass.getSimpleName(), e);
        }
    }
}
