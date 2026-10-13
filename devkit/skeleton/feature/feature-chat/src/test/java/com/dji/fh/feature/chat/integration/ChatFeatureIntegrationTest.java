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

package com.dji.fh.feature.chat.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.chat.ChatProperties;
import com.dji.fh.feature.chat.provider.ProviderType;
import com.dji.fh.feature.chat.tool.ChatSecurityLevel;
import com.dji.fh.feature.chat.tool.ChatToolRegistry;
import com.dji.fh.feature.chat.tool.SecurityGatedCallback;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * 核心组件集成测试 — 验证各组件协作正确。
 *
 * <p>不依赖外部 LLM 服务，仅测试内部逻辑。
 * 覆盖场景：
 * <ul>
 *   <li>SecurityGatedCallback 各安全等级的联动行为</li>
 *   <li>ChatProperties 属性绑定和默认值</li>
 *   <li>ChatToolRegistry 与 SecurityGatedCallback 的组合使用</li>
 * </ul>
 * </p>
 *
 * @author DJI DevKit Team
 */
class ChatFeatureIntegrationTest {

    // -------------------------------------------------------------------------
    // 内部 Stub：实现 ToolCallback 接口，无需 Mockito
    // -------------------------------------------------------------------------

    /**
     * 简单的 ToolCallback Stub，用于测试 SecurityGatedCallback 的委托行为。
     */
    private static class StubToolCallback implements ToolCallback {

        private static final String STUB_RESULT = "{\"status\":\"ok\"}";

        private final String name;
        private boolean called = false;

        StubToolCallback(String name) {
            this.name = name;
        }

        @Override
        public String call(String input) {
            this.called = true;
            return STUB_RESULT;
        }

        @Override
        public String call(String input, ToolContext toolContext) {
            this.called = true;
            return STUB_RESULT;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return ToolDefinition.builder()
                    .name(name)
                    .description("stub tool for testing")
                    .inputSchema("{}")
                    .build();
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return ToolMetadata.builder().build();
        }

        /** 返回 Stub 是否被实际调用过。 */
        boolean wasCalled() {
            return called;
        }
    }

    // -------------------------------------------------------------------------
    // 测试 1：安全等级联动行为
    // -------------------------------------------------------------------------

    @Test
    void securityLevel_L0_shouldPassThrough() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_device_list");
        SecurityGatedCallback callback = new SecurityGatedCallback(stub, ChatSecurityLevel.READ_ONLY);

        // when
        String result = callback.call("{}");

        // then — L0 直接委托执行，stub 被调用
        assertThat(stub.wasCalled()).isTrue();
        assertThat(result).isEqualTo("{\"status\":\"ok\"}");
    }

    @Test
    void securityLevel_L1_shouldDelegateExecution() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_device_restart");
        SecurityGatedCallback callback = new SecurityGatedCallback(stub, ChatSecurityLevel.DEVICE_CONTROL);
        String toolInput = "{\"device_sn\":\"ABC123\"}";

        // when — L1 需已认证 + admin 白名单角色
        SecurityContextHolder.set(new TestSecurityContext("admin-1", Set.of("admin")));
        try {
            String result = callback.call(toolInput);

            // then — L1 已认证白名单角色直接委托执行（MCP Server 侧通过 confirm 保障安全）
            assertThat(stub.wasCalled()).isTrue();
            assertThat(result).isEqualTo("{\"status\":\"ok\"}");
        } finally {
            SecurityContextHolder.clear();
        }
    }

    @Test
    void securityLevel_L1_shouldReturnDelegateResult() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_control_acquire");
        SecurityGatedCallback callback = new SecurityGatedCallback(stub, ChatSecurityLevel.DEVICE_CONTROL);

        // when — L1 需已认证 + admin 白名单角色
        SecurityContextHolder.set(new TestSecurityContext("admin-1", Set.of("admin")));
        try {
            String result = callback.call("{}");

            // then — 返回 delegate 的结果，而非安全拦截错误
            assertThat(stub.wasCalled()).isTrue();
            assertThat(result).doesNotContain("security_blocked");
        } finally {
            SecurityContextHolder.clear();
        }
    }

    @Test
    void securityLevel_L2_shouldReturnSecurityBlockedError() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_takeoff");
        SecurityGatedCallback callback = new SecurityGatedCallback(stub, ChatSecurityLevel.FLIGHT_CONTROL);

        // when
        String result = callback.call("{}");

        // then — L2 返回安全拦截错误信息
        assertThat(result).contains("security_blocked");
        assertThat(result).contains("FLIGHT_CONTROL");
        assertThat(result).contains("fh_takeoff");

        assertThat(stub.wasCalled()).isFalse();
    }

    @Test
    void securityLevel_L3_shouldReturnSecurityBlockedError() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_emergency_stop");
        SecurityGatedCallback callback = new SecurityGatedCallback(stub, ChatSecurityLevel.EMERGENCY);

        // when
        String result = callback.call("{}");

        // then — L3 返回安全拦截错误信息
        assertThat(result).contains("security_blocked");
        assertThat(result).contains("EMERGENCY");
        assertThat(result).contains("fh_emergency_stop");

        assertThat(stub.wasCalled()).isFalse();
    }

    // -------------------------------------------------------------------------
    // 测试 2：ChatProperties 默认值与属性绑定
    // -------------------------------------------------------------------------

    @Test
    void chatProperties_shouldHaveCorrectDefaults() {
        // given — 不通过 Spring 上下文，直接 new，验证类级别默认值
        ChatProperties props = new ChatProperties();

        // then
        assertThat(props.isEnabled()).isFalse();
        // 默认走 OpenAI 兼容协议接入 GLM 等模型网关
        assertThat(props.getProvider()).isEqualTo(ProviderType.OPENAI);
        assertThat(props.getMaxHistoryTurns()).isEqualTo(10);
        // GLM-5.2 等 reasoning 模型需较大 max-tokens
        assertThat(props.getMaxTokens()).isEqualTo(8192);
        assertThat(props.getSystemPromptPath()).isEqualTo("classpath:chat-system-prompt.txt");
    }

    @Test
    void chatProperties_shouldBindCorrectly() {
        // given
        ChatProperties props = new ChatProperties();

        // when
        props.setEnabled(true);
        props.setProvider(ProviderType.ANTHROPIC);
        props.setMaxHistoryTurns(20);
        props.setMaxTokens(8192);

        // then
        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getProvider()).isEqualTo(ProviderType.ANTHROPIC);
        assertThat(props.getMaxHistoryTurns()).isEqualTo(20);
        assertThat(props.getMaxTokens()).isEqualTo(8192);
    }

    @Test
    void chatProperties_rag_shouldHaveCorrectDefaults() {
        // given
        ChatProperties props = new ChatProperties();
        ChatProperties.Rag rag = props.getRag();

        // then
        assertThat(rag.isEnabled()).isTrue();
        assertThat(rag.getDocumentsPath()).isEqualTo("classpath:chat-docs/");
        assertThat(rag.getEmbeddingProvider()).isEqualTo("openai");
        assertThat(rag.getChunkSize()).isEqualTo(1000);
        assertThat(rag.getChunkOverlap()).isEqualTo(200);
        assertThat(rag.getTopK()).isEqualTo(5);
        assertThat(rag.getSimilarityThreshold()).isEqualTo(0.7);
    }

    // -------------------------------------------------------------------------
    // 测试 3：ChatToolRegistry + SecurityGatedCallback 组合
    // -------------------------------------------------------------------------

    @Test
    void toolRegistry_shouldAcceptEmptyList() {
        // given / when
        ChatToolRegistry registry = new ChatToolRegistry(List.of());

        // then
        assertThat(registry.all()).isEmpty();
    }

    @Test
    void toolRegistry_shouldHoldWrappedCallbacks() {
        // given — 模拟真实场景：L0 tool 直接注册，L1 tool 通过 SecurityGatedCallback 包装
        StubToolCallback readOnlyStub = new StubToolCallback("fh_device_list");
        StubToolCallback controlStub = new StubToolCallback("fh_device_restart");

        SecurityGatedCallback gatedL0 = new SecurityGatedCallback(readOnlyStub, ChatSecurityLevel.READ_ONLY);
        SecurityGatedCallback gatedL1 = new SecurityGatedCallback(controlStub, ChatSecurityLevel.DEVICE_CONTROL);

        ChatToolRegistry registry = new ChatToolRegistry(List.of(gatedL0, gatedL1));

        // when
        List<ToolCallback> tools = registry.all();

        // then
        assertThat(tools).hasSize(2);
        assertThat(tools).containsExactly(gatedL0, gatedL1);
    }

    @Test
    void toolRegistry_shouldReturnImmutableList() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_device_list");
        ChatToolRegistry registry = new ChatToolRegistry(List.of(stub));

        // when / then
        assertThat(registry.all()).isUnmodifiable();
    }

    @Test
    void toolRegistry_shouldPreserveToolDefinitions() {
        // given
        StubToolCallback stub = new StubToolCallback("fh_device_list");
        SecurityGatedCallback gated = new SecurityGatedCallback(stub, ChatSecurityLevel.READ_ONLY);
        ChatToolRegistry registry = new ChatToolRegistry(List.of(gated));

        // when
        ToolCallback tool = registry.all().get(0);

        // then — 通过 SecurityGatedCallback 仍能读到正确的 ToolDefinition
        assertThat(tool.getToolDefinition().name()).isEqualTo("fh_device_list");
        assertThat(tool.getToolDefinition().description()).isEqualTo("stub tool for testing");
    }

    /**
     * 测试用 SecurityContext 最小实现（角色守卫用）。
     */
    private record TestSecurityContext(String principal, Set<String> roles)
            implements SecurityContext {
        @Override
        public String principalId() {
            return principal;
        }

        @Override
        public Set<String> roles() {
            return roles;
        }

        @Override
        public Map<String, Object> attributes() {
            return Map.of();
        }
    }
}
