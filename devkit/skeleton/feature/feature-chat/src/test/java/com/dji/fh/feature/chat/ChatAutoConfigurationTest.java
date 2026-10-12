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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dji.fh.feature.chat.mcp.McpClient;
import com.dji.fh.feature.chat.mcp.McpClientProperties;
import com.dji.fh.feature.chat.rag.DocumentInitializer;
import com.dji.fh.feature.chat.rag.KnowledgeAdvisor;
import com.dji.fh.feature.chat.tool.ChatToolRegistry;
import com.dji.fh.feature.chat.tool.SecurityGatedCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

/**
 * {@link ChatAutoConfiguration} 单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>chatToolRegistry 正确创建 MCP 透传 Tool</li>
 *   <li>chatToolRegistry 在 MCP 不可用时返回空注册表</li>
 *   <li>chatToolRegistry 接受额外 Tool 列表</li>
 *   <li>ragDocumentLoader 生命周期</li>
 *   <li>chatClient 创建</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
class ChatAutoConfigurationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ChatAutoConfiguration config = new ChatAutoConfiguration();

    // -----------------------------------------------------------------------
    // 测试替身
    // -----------------------------------------------------------------------

    /**
     * 记录 initialize() 是否被调用的 DocumentInitializer 存根。
     */
    static class TrackingDocumentInitializer extends DocumentInitializer {

        private final AtomicBoolean initialized = new AtomicBoolean(false);

        TrackingDocumentInitializer() {
            super("classpath:nonexistent-path/**/*.md", new NoOpVectorStore());
        }

        @Override
        public void initialize() {
            initialized.set(true);
        }

        boolean isInitialized() {
            return initialized.get();
        }
    }

    /**
     * 空操作 VectorStore 存根。
     */
    static class NoOpVectorStore implements VectorStore {

        @Override
        public void add(List<Document> documents) {
        }

        @Override
        public void delete(List<String> idList) {
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return List.of();
        }
    }

    /**
     * 模拟已完成初始化的 McpClient — 覆写关键方法避免真实网络调用。
     */
    static class FakeMcpClient extends McpClient {

        private final ObjectNode fakeToolsResult;

        FakeMcpClient() {
            super(createTestProps());
            this.fakeToolsResult = buildFakeToolsResult();
        }

        @Override
        public void connectAndInitialize() {
            // 跳过真实连接
        }

        @Override
        public com.fasterxml.jackson.databind.JsonNode listTools() {
            return fakeToolsResult;
        }

        @Override
        public String callTool(String toolName, Map<String, Object> arguments) {
            return "{\"mock\":true,\"tool\":\"" + toolName + "\"}";
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        private static McpClientProperties createTestProps() {
            McpClientProperties props = new McpClientProperties();
            props.setUrl("http://localhost:9999");
            return props;
        }

        /**
         * 构建模拟的 tools/list 响应，包含 TOOL_MAPPINGS 中配置的所有 tool name。
         */
        private static ObjectNode buildFakeToolsResult() {
            ObjectNode result = MAPPER.createObjectNode();
            ArrayNode tools = MAPPER.createArrayNode();

            String[] toolNames = {
                // L0
                "fh_device_list", "fh_device_manage", "fh_device_recommend",
                "fh_drone_snapshot", "fh_dock_snapshot", "fh_osd_snapshot",
                "fh_hms", "fh_wayline_status", "fh_flight_task", "fh_flight_area_list",
                "fh_media_list", "fh_media_download", "fh_workspace", "fh_firmware",
                "fh_diagnosis", "fh_maintenance", "fh_flight_record", "fh_log",
                "fh_flysafe", "fh_tcas", "fh_terra", "fh_offline_map",
                "fh_unlock_license", "fh_session_info",
                // L1
                "fh_dock_control", "fh_live_start", "fh_media_record", "fh_media_delete",
                "fh_camera", "fh_speaker", "fh_searchlight", "fh_psdk_widget",
                "fh_device_debug", "fh_flight_area_manage", "fh_flight_area_sync",
                "fh_wayline_upload", "fh_connect",
                // L2
                "fh_takeoff", "fh_flyto", "fh_poi", "fh_stick_move",
                "fh_return_home", "fh_wayline_control", "fh_wayline_execute",
                "fh_control_acquire", "fh_control_release", "fh_drc_connect", "fh_drc_disconnect",
                // L3
                "fh_emergency_stop"
            };

            for (String name : toolNames) {
                ObjectNode tool = MAPPER.createObjectNode();
                tool.put("name", name);
                tool.put("description", "Mock " + name);
                ObjectNode schema = MAPPER.createObjectNode();
                schema.put("type", "object");
                schema.set("properties", MAPPER.createObjectNode());
                tool.set("inputSchema", schema);
                tools.add(tool);
            }

            result.set("tools", tools);
            return result;
        }
    }

    /**
     * 模拟连接失败的 McpClient。
     */
    static class FailingMcpClient extends McpClient {

        FailingMcpClient() {
            super(FakeMcpClient.createTestProps());
        }

        @Override
        public void connectAndInitialize() {
            throw new com.dji.fh.feature.chat.mcp.McpClientException("连接失败");
        }
    }

    // -----------------------------------------------------------------------
    // chatToolRegistry
    // -----------------------------------------------------------------------

    private static McpClientProperties enabledProps() {
        McpClientProperties props = new McpClientProperties();
        props.setEnabled(true);
        return props;
    }

    private static McpClientProperties disabledProps() {
        McpClientProperties props = new McpClientProperties();
        props.setEnabled(false);
        return props;
    }

    @Test
    void chatToolRegistry_shouldContainAllMappedTools_whenMcpConnected() {
        // when: 使用 FakeMcpClient 模拟 MCP Server 已连接
        ChatToolRegistry registry = config.chatToolRegistry(new FakeMcpClient(), enabledProps(), new ChatProperties(), null);

        // then: TOOL_MAPPINGS 中有 49 个映射（24 L0 + 13 L1 + 11 L2 + 1 L3）
        assertThat(registry.all()).hasSize(49);
    }

    @Test
    void chatToolRegistry_allTools_shouldBeSecurityGated() {
        // when
        ChatToolRegistry registry = config.chatToolRegistry(new FakeMcpClient(), enabledProps(), new ChatProperties(), null);

        // then
        for (ToolCallback tool : registry.all()) {
            assertThat(tool).isInstanceOf(SecurityGatedCallback.class);
        }
    }

    @Test
    void chatToolRegistry_shouldBeEmpty_whenMcpConnectionFails() {
        // when: MCP 连接失败，应优雅降级为空注册表
        ChatToolRegistry registry = config.chatToolRegistry(new FailingMcpClient(), enabledProps(), new ChatProperties(), null);

        // then
        assertThat(registry.all()).isEmpty();
    }

    @Test
    void chatToolRegistry_shouldBeEmpty_whenMcpDisabled() {
        // when: MCP 透传模式被禁用
        ChatToolRegistry registry = config.chatToolRegistry(new FakeMcpClient(), disabledProps(), new ChatProperties(), null);

        // then
        assertThat(registry.all()).isEmpty();
    }

    @Test
    void chatToolRegistry_shouldIncludeExtraTools_whenProvided() {
        // given
        ToolCallback extraTool = new ToolCallback() {
            @Override
            public String call(String input) {
                return "{}";
            }

            @Override
            public org.springframework.ai.tool.definition.ToolDefinition getToolDefinition() {
                return org.springframework.ai.tool.definition.ToolDefinition.builder()
                        .name("extra_tool")
                        .description("test")
                        .inputSchema("{\"type\":\"object\",\"properties\":{}}")
                        .build();
            }
        };

        // when
        ChatToolRegistry registry = config.chatToolRegistry(new FakeMcpClient(), enabledProps(), new ChatProperties(), List.of(extraTool));

        // then: 49 MCP Tool + 1 额外 = 50
        assertThat(registry.all()).hasSize(50);
        assertThat(registry.all()).contains(extraTool);
    }

    // -----------------------------------------------------------------------
    // chatClient
    // -----------------------------------------------------------------------

    @Test
    void chatClient_shouldBeCreated_withoutKnowledgeAdvisor() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatProperties props = new ChatProperties();
        ApplicationContext ctx = mock(ApplicationContext.class);
        // 默认 provider=openai，工厂会查找 openAiChatModel bean
        when(ctx.containsBean("openAiChatModel")).thenReturn(true);
        when(ctx.getBean("openAiChatModel", ChatModel.class)).thenReturn(chatModel);

        var factory = config.chatProviderFactory(props, ctx);
        ChatToolRegistry registry = new ChatToolRegistry(List.of());

        ChatClient client = config.chatClient(factory, registry, null);
        assertThat(client).isNotNull();
    }

    @Test
    void chatClient_shouldBeCreated_withKnowledgeAdvisor() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatProperties props = new ChatProperties();
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.containsBean("openAiChatModel")).thenReturn(true);
        when(ctx.getBean("openAiChatModel", ChatModel.class)).thenReturn(chatModel);

        var factory = config.chatProviderFactory(props, ctx);
        ChatToolRegistry registry = new ChatToolRegistry(List.of());
        KnowledgeAdvisor advisor = new KnowledgeAdvisor(new NoOpVectorStore(), 5, 0.7);

        ChatClient client = config.chatClient(factory, registry, advisor);
        assertThat(client).isNotNull();
    }


    // -----------------------------------------------------------------------
    // lightChatClient
    // -----------------------------------------------------------------------

    @Test
    void lightChatClient_shouldBeCreated_withReadOnlyToolsOnly() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatProperties props = new ChatProperties();
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.containsBean("openAiChatModel")).thenReturn(true);
        when(ctx.getBean("openAiChatModel", ChatModel.class)).thenReturn(chatModel);

        var factory = config.chatProviderFactory(props, ctx);
        ChatToolRegistry registry = config.chatToolRegistry(new FakeMcpClient(), enabledProps(), new ChatProperties(), null);

        ChatClient lightClient = config.lightChatClient(factory, props, registry, null);
        assertThat(lightClient).isNotNull();
    }

    // -----------------------------------------------------------------------
    // ragDocumentLoader
    // -----------------------------------------------------------------------

    @Test
    void ragDocumentLoader_shouldBeNoOp_whenNull() throws Exception {
        ApplicationRunner runner = config.ragDocumentLoader(null);
        runner.run(null);
        // 无异常即通过
    }

    @Test
    void ragDocumentLoader_shouldCallInitialize() throws Exception {
        TrackingDocumentInitializer initializer = new TrackingDocumentInitializer();
        ApplicationRunner runner = config.ragDocumentLoader(initializer);
        runner.run(null);
        assertThat(initializer.isInitialized()).isTrue();
    }
}
