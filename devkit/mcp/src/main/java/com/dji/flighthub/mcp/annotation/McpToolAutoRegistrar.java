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
import com.dji.flighthub.mcp.idempotency.IdempotencyCache;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * MCP Tool 自动注册器。
 * 在所有 singleton bean 初始化完成后，自动扫描 AbstractMcpTool 实例并注册到 Registry。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class McpToolAutoRegistrar implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(McpToolAutoRegistrar.class);

    private final List<AbstractMcpTool> tools;
    private final McpToolRegistry registry;
    private final AuditService auditService;
    private final IdempotencyCache idempotencyCache;

    /**
     * 构造注入所有依赖。
     *
     * @param tools            所有 AbstractMcpTool 子类 bean（Spring 自动收集）
     * @param registry         MCP 工具注册中心
     * @param auditService     审计服务
     * @param idempotencyCache 幂等缓存
     */
    public McpToolAutoRegistrar(List<AbstractMcpTool> tools,
                                McpToolRegistry registry,
                                AuditService auditService,
                                IdempotencyCache idempotencyCache) {
        this.tools = tools;
        this.registry = registry;
        this.auditService = auditService;
        this.idempotencyCache = idempotencyCache;
    }

    /**
     * 所有 singleton 初始化完成后执行，自动将每个 tool 注册到 Registry。
     * 同时注入横切关注点：审计服务和幂等缓存。
     */
    @Override
    public void afterSingletonsInstantiated() {
        registry.setAuditService(auditService);
        registry.setIdempotencyCache(idempotencyCache);

        for (AbstractMcpTool tool : tools) {
            try {
                // 启动时断言：tool 必须满足其一 —— (a) 标注 @McpTool 或 (b) 覆写了 execute()
                validateToolConfiguration(tool);
                Map<String, Object> definition = tool.getToolDefinition();
                registry.register(definition, tool::execute, tool.requiresHumanConfirmation());
            } catch (Exception e) {
                log.error("Tool 注册失败，跳过: {}", tool.getClass().getSimpleName(), e);
            }
        }

        log.info("Auto-registered {} MCP tools: {}", registry.getToolCount(),
                registry.getToolDefinitions().stream()
                        .map(d -> d.get("name")).toList());
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
