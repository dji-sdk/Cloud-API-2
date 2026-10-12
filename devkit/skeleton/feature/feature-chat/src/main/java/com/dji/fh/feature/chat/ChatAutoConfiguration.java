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

package com.dji.fh.feature.chat;

import com.dji.fh.feature.chat.controller.ChatController;
import com.dji.fh.feature.chat.guard.ChatGuardChain;
import com.dji.fh.feature.chat.guard.ContentModerator;
import com.dji.fh.feature.chat.guard.DefaultContentModerator;
import com.dji.fh.feature.chat.guard.PromptInjectionDetector;
import com.dji.fh.feature.chat.guard.ResourceQuotaManager;
import com.dji.fh.feature.chat.mcp.McpClient;
import com.dji.fh.feature.chat.mcp.McpClientProperties;
import com.dji.fh.feature.chat.mcp.McpOutboundAuthValidator;
import com.dji.fh.feature.chat.mcp.McpProxyChatTool;
import com.dji.fh.feature.chat.mcp.ToolMapping;
import com.dji.fh.feature.chat.provider.ChatProviderFactory;
import com.dji.fh.feature.chat.provider.ProviderType;
import com.dji.fh.feature.chat.rag.DocumentInitializer;
import com.dji.fh.feature.chat.rag.KnowledgeAdvisor;
import com.dji.fh.feature.chat.tool.ChatSecurityLevel;
import com.dji.fh.feature.chat.tool.ChatToolRegistry;
import com.dji.fh.feature.chat.tool.SecurityGatedCallback;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

/**
 * Chat Bot feature 的 AutoConfiguration。
 *
 * <p>条件：{@code fh.chat.enabled=true}（默认关闭，ISV 主动开启）。
 *
 * <p>Tool 注册策略：通过 MCP 透传模式，从 MCP Server 动态获取 Tool schema，
 * 按 {@link #TOOL_MAPPINGS} 过滤后生成 {@link McpProxyChatTool} 实例，
 * 包装 {@link SecurityGatedCallback} 后注册到 {@link ChatToolRegistry}。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@ConditionalOnProperty(name = "fh.chat.enabled", havingValue = "true")
@EnableConfigurationProperties({ChatProperties.class, McpClientProperties.class})
public class ChatAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ChatAutoConfiguration.class);

    /**
     * Chat Tool 到 MCP Tool 的映射配置。
     *
     * <p>定义哪些 MCP Tool 暴露给 chatbot，以及对应的安全等级。
     * 新增 Tool 只需在此列表追加一行即可，无需编写新的 Java 类。
     *
     * <p>安全等级说明：
     * <ul>
     *   <li>L0 READ_ONLY：直接执行，无需确认（查询类）</li>
     *   <li>L1 DEVICE_CONTROL：需用户二次确认（设备控制、飞行任务、航线执行）——MCP Server 侧已有 confirm=true 保护</li>
     *   <li>L2 FLIGHT_CONTROL：拒绝执行，引导到功能页面（裸摇杆等高风险实时操作）</li>
     *   <li>L3 EMERGENCY：拒绝执行（紧急停机）</li>
     * </ul>
     */
    private static final List<ToolMapping> TOOL_MAPPINGS = List.of(
            // ===== L0 只读查询 =====
            new ToolMapping("fh_device_list", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_device_manage", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_device_recommend", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_drone_snapshot", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_dock_snapshot", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_osd_snapshot", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_hms", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_wayline_status", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_flight_task", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_flight_area_list", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_media_list", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_media_download", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_workspace", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_firmware", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_diagnosis", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_maintenance", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_flight_record", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_log", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_flysafe", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_tcas", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_terra", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_offline_map", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_unlock_license", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_session_info", ChatSecurityLevel.READ_ONLY),
            new ToolMapping("fh_connect", ChatSecurityLevel.READ_ONLY),

            // ===== L1 设备控制（需用户确认） =====
            new ToolMapping("fh_takeoff", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_dock_control", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_live_start", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_media_record", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_media_delete", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_camera", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_speaker", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_searchlight", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_psdk_widget", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_device_debug", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_flight_area_manage", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_flight_area_sync", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_wayline_upload", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_control_acquire", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_control_release", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_drc_connect", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_drc_disconnect", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_flyto", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_poi", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_return_home", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_wayline_control", ChatSecurityLevel.DEVICE_CONTROL),
            new ToolMapping("fh_wayline_execute", ChatSecurityLevel.DEVICE_CONTROL),

            // ===== L2 飞行控制（拒绝执行，引导到页面） =====
            // fh_stick_move：裸摇杆控制无目标坐标，风险高，不允许通过对话执行
            new ToolMapping("fh_stick_move", ChatSecurityLevel.FLIGHT_CONTROL),

            // ===== L3 紧急操作（绝对拒绝） =====
            new ToolMapping("fh_emergency_stop", ChatSecurityLevel.EMERGENCY)
    );

    /**
     * 注册 ChatProviderFactory，用于从 ApplicationContext 中选取正确的 ChatModel bean。
     *
     * @param properties         Chat 配置属性
     * @param applicationContext Spring 应用上下文
     * @return ChatProviderFactory 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatProviderFactory chatProviderFactory(ChatProperties properties,
                                                   ApplicationContext applicationContext) {
        return new ChatProviderFactory(properties, applicationContext);
    }

    /**
     * 注册 McpClient，负责与 MCP Server 通信。
     *
     * @param mcpProperties MCP 连接配置
     * @return McpClient 实例
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public McpClient mcpClient(McpClientProperties mcpProperties) {
        return new McpClient(mcpProperties);
    }

    /**
     * 注册 McpOutboundAuthValidator，提醒 MCP 出站鉴权 token 缺失 —— 安全审计整改（F-04，P2）。
     *
     * @param mcpProperties MCP 连接配置
     * @return McpOutboundAuthValidator 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public McpOutboundAuthValidator mcpOutboundAuthValidator(McpClientProperties mcpProperties) {
        return new McpOutboundAuthValidator(mcpProperties);
    }

    /**
     * 注册 ChatToolRegistry，通过 MCP 透传模式动态生成 Tool。
     *
     * <p>启动时连接 MCP Server，获取 Tool schema，按 TOOL_MAPPINGS 过滤后
     * 为每个 Tool 创建 {@link McpProxyChatTool}，包装安全等级后注册。
     *
     * @param mcpClient  MCP Client
     * @param extraTools ISV 注入的额外 ToolCallback（可选）
     * @return ChatToolRegistry 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatToolRegistry chatToolRegistry(McpClient mcpClient,
                                             McpClientProperties mcpProperties,
                                             ChatProperties chatProperties,
                                             @Nullable List<ToolCallback> extraTools) {
        List<ToolCallback> tools = new ArrayList<>();

        if (!mcpProperties.isEnabled()) {
            LOG.info("MCP 透传模式已禁用（fh.chat.mcp.enabled=false），跳过 Tool 注册");
            if (extraTools != null) {
                tools.addAll(extraTools);
            }
            return new ChatToolRegistry(tools);
        }

        // 安全发布专项修复：DEVICE_CONTROL 角色白名单注入
        java.util.Set<String> deviceControlRoles = chatProperties.getToolAccess().getDeviceControlRoles();
        if (deviceControlRoles == null || deviceControlRoles.isEmpty()) {
            LOG.warn("fh.chat.tool-access.device-control-roles 为空，DEVICE_CONTROL 级工具将全部拒绝执行（fail-closed）");
        }

        try {
            mcpClient.connectAndInitialize();
            JsonNode toolsResult = mcpClient.listTools();

            for (ToolMapping mapping : TOOL_MAPPINGS) {
                String name = mapping.mcpToolName();
                String schema = McpClient.extractInputSchema(name, toolsResult);
                String description = McpClient.extractDescription(name, toolsResult);

                McpProxyChatTool proxyTool = new McpProxyChatTool(
                        name, description, schema, mcpClient);
                tools.add(new SecurityGatedCallback(proxyTool, mapping.securityLevel(),
                        deviceControlRoles));
                LOG.debug("注册 MCP 透传 Tool: name={}, level={}", name, mapping.securityLevel());
            }
            LOG.info("ChatToolRegistry：从 MCP Server 注册 {} 个透传 Tool", tools.size());
            autoConnectFlightHub(mcpClient);
            mcpClient.setOnReconnectedCallback(() -> autoConnectFlightHub(mcpClient));
        } catch (Exception e) {
            LOG.error("MCP Server 连接失败，Chat Tool 注册为空。错误: {}", e.getMessage());
            LOG.info("请确保 MCP Server 已启动且 fh.chat.mcp.url 配置正确");
        }

        // ISV 注入的额外 Tool
        if (extraTools != null) {
            tools.addAll(extraTools);
        }

        return new ChatToolRegistry(tools);
    }

    /**
     * 注册主 ChatClient，绑定 LLM、Tool 和 RAG advisor。
     *
     * @param factory         ChatModel provider 工厂
     * @param toolRegistry    Tool 注册表
     * @param knowledgeAdvisor RAG advisor（可选，当 RAG 未启用时为 null）
     * @return ChatClient 实例
     */
    @Bean
    @ConditionalOnMissingBean(ChatClient.class)
    public ChatClient chatClient(ChatProviderFactory factory,
                                 ChatToolRegistry toolRegistry,
                                 @Nullable KnowledgeAdvisor knowledgeAdvisor) {
        ChatModel chatModel = factory.resolve();
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolRegistry.all());

        if (knowledgeAdvisor != null) {
            builder.defaultAdvisors(knowledgeAdvisor);
            LOG.info("ChatClient：已启用 RAG KnowledgeAdvisor");
        } else {
            LOG.info("ChatClient：RAG 未启用，跳过 KnowledgeAdvisor");
        }

        return builder.build();
    }

    /**
     * 注册轻量 ChatClient，使用 haiku 模型处理 L0 只读查询。
     *
     * <p>仅注册 READ_ONLY 工具，防止轻量模型意外调用设备控制指令。
     * 仅在 provider=anthropic 时切换 haiku 模型；其他 provider 沿用主模型配置。</p>
     *
     * @param factory          ChatModel provider 工厂
     * @param properties       Chat 配置属性
     * @param toolRegistry     Tool 注册表
     * @param knowledgeAdvisor RAG advisor（可选）
     * @return 轻量 ChatClient 实例
     */
    @Bean("lightChatClient")
    @ConditionalOnMissingBean(name = "lightChatClient")
    public ChatClient lightChatClient(ChatProviderFactory factory,
                                      ChatProperties properties,
                                      ChatToolRegistry toolRegistry,
                                      @Nullable KnowledgeAdvisor knowledgeAdvisor) {
        ChatModel chatModel = factory.resolve();
        List<ToolCallback> readOnlyTools = toolRegistry.readOnly();

        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultToolCallbacks(readOnlyTools);

        if (properties.getProvider() == ProviderType.ANTHROPIC
                && properties.getLightModel() != null
                && !properties.getLightModel().isBlank()) {
            builder.defaultOptions(AnthropicChatOptions.builder()
                    .model(properties.getLightModel())
                    .build());
            LOG.info("lightChatClient：使用 haiku 模型={}, 只读工具数={}",
                    properties.getLightModel(), readOnlyTools.size());
        } else {
            LOG.info("lightChatClient：provider={} 不支持 haiku 模型切换，沿用主模型，只读工具数={}",
                    properties.getProvider(), readOnlyTools.size());
        }

        if (knowledgeAdvisor != null) {
            builder.defaultAdvisors(knowledgeAdvisor);
        }

        return builder.build();
    }

    /**
     * 注册 ChatController，提供 SSE 流式对话接口。
     *
     * @param chatClient      Chat 主客户端（sonnet，全工具集）
     * @param lightChatClient 轻量 Chat 客户端（haiku，只读工具集）
     * @param properties      Chat 配置属性
     * @param objectMapper    Jackson ObjectMapper
     * @param guardChain      安全防护链（可选，未启用时为 null）
     * @return ChatController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatController chatController(@Qualifier("chatClient") ChatClient chatClient,
                                         @Qualifier("lightChatClient") ChatClient lightChatClient,
                                         ChatProperties properties,
                                         ObjectMapper objectMapper,
                                         @Nullable ChatGuardChain guardChain) {
        return new ChatController(chatClient, lightChatClient, properties, objectMapper, guardChain);
    }

    // ===== 安全防护相关 Bean =====

    /**
     * 注册 Prompt 注入检测器。
     *
     * <p>条件：{@code fh.chat.guard.enabled=true} 且 {@code fh.chat.guard.injection.enabled=true}。
     *
     * @param properties Chat 配置属性
     * @return PromptInjectionDetector 实例
     */
    @Bean
    @ConditionalOnProperty(name = "fh.chat.guard.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public PromptInjectionDetector promptInjectionDetector(ChatProperties properties) {
        ChatProperties.Guard.Injection injection = properties.getGuard().getInjection();
        if (!injection.isEnabled()) {
            return null;
        }
        LOG.info("注册 PromptInjectionDetector（自定义规则数={}）", injection.getExtraPatterns().size());
        return new PromptInjectionDetector(injection.getExtraPatterns());
    }

    /**
     * 注册默认内容审核器（关键词正则匹配）。
     *
     * <p>条件：{@code fh.chat.guard.enabled=true} 且 {@code fh.chat.guard.moderation.enabled=true}。
     * ISV 可通过注册自己的 {@code ContentModerator} Bean 覆盖。
     *
     * @param properties Chat 配置属性
     * @return ContentModerator 实例
     */
    @Bean
    @ConditionalOnProperty(name = "fh.chat.guard.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public ContentModerator contentModerator(ChatProperties properties) {
        ChatProperties.Guard.Moderation moderation = properties.getGuard().getModeration();
        if (!moderation.isEnabled()) {
            return null;
        }
        LOG.info("注册 DefaultContentModerator（关键词数={}）", moderation.getKeywords().size());
        return new DefaultContentModerator(moderation.getKeywords());
    }

    /**
     * 注册资源配额管理器。
     *
     * <p>条件：{@code fh.chat.quota.enabled=true}。
     *
     * @param properties Chat 配置属性
     * @return ResourceQuotaManager 实例
     */
    @Bean
    @ConditionalOnProperty(name = "fh.chat.quota.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public ResourceQuotaManager resourceQuotaManager(ChatProperties properties) {
        ChatProperties.Quota quota = properties.getQuota();
        long windowMs = 60 * 60 * 1000L;
        LOG.info("注册 ResourceQuotaManager（maxCalls={}/h, maxConcurrent={}, dailyTokens={}）",
                quota.getMaxCallsPerHour(), quota.getMaxConcurrentStreams(), quota.getMaxDailyTokens());
        return new ResourceQuotaManager(
                quota.getMaxCallsPerHour(),
                windowMs,
                quota.getMaxConcurrentStreams(),
                quota.getMaxDailyTokens());
    }

    /**
     * 注册安全防护链编排器。
     *
     * <p>当任一子组件存在时自动创建（guard.enabled 或 quota.enabled 任一为 true 即可）。
     * 所有子组件为 nullable，未启用的子功能不会被调用。
     * 若所有子组件均为 null，则不创建此 bean（Controller 中 guardChain 为 null）。
     *
     * @param properties        Chat 配置属性
     * @param quotaManager      配额管理器（可选）
     * @param injectionDetector 注入检测器（可选）
     * @param contentModerator  内容审核器（可选）
     * @return ChatGuardChain 实例，或 null（无任何子组件时）
     */
    @Bean
    @ConditionalOnMissingBean
    @Nullable
    public ChatGuardChain chatGuardChain(ChatProperties properties,
                                         @Nullable ResourceQuotaManager quotaManager,
                                         @Nullable PromptInjectionDetector injectionDetector,
                                         @Nullable ContentModerator contentModerator) {
        if (quotaManager == null && injectionDetector == null && contentModerator == null) {
            LOG.debug("ChatGuardChain：所有子组件均为 null，跳过注册");
            return null;
        }
        boolean outputCheck = properties.getGuard().getModeration().isOutputCheckEnabled();
        LOG.info("注册 ChatGuardChain（quota={}, injection={}, moderation={}, outputCheck={}）",
                quotaManager != null, injectionDetector != null, contentModerator != null, outputCheck);
        return new ChatGuardChain(quotaManager, injectionDetector, contentModerator, outputCheck);
    }

    /**
     * 注册 ApplicationRunner，在应用启动后异步初始化 RAG 文档。
     *
     * @param documentInitializer RAG 文档初始化器（可选）
     * @return ApplicationRunner 实例
     */
    @Bean
    public ApplicationRunner ragDocumentLoader(@Nullable DocumentInitializer documentInitializer) {
        return args -> {
            if (documentInitializer == null) {
                LOG.debug("RAG DocumentInitializer 不存在，跳过文档加载");
                return;
            }
            LOG.info("开始加载 RAG 文档...");
            documentInitializer.initialize();
            LOG.info("RAG 文档加载完成");
        };
    }

    /**
     * 自动调用 MCP Server 的 fh_connect 建立 FlightHub session。
     *
     * <p>启动时和 SSE 重连后调用，确保 chatbot 就绪后即可直接执行 L0 查询类 Tool。
     * 连接失败不影响启动——AI 在工具返回 session 缺失错误时会自动触发重连。</p>
     *
     * @param mcpClient MCP Client 实例
     */
    private void autoConnectFlightHub(McpClient mcpClient) {
        try {
            String result = mcpClient.callTool("fh_connect", Map.of());
            String preview = result != null && result.length() > 200
                    ? result.substring(0, 200) + "..." : result;
            LOG.info("ChatAutoConfiguration：FlightHub 预连接完成: {}", preview);
        } catch (Exception e) {
            LOG.warn("ChatAutoConfiguration：FlightHub 预连接失败（不影响启动，AI 可在需要时重连）: {}",
                    e.getMessage());
        }
    }
}
