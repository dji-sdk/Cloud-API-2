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

package com.dji.fh.feature.chat.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dji.fh.feature.chat.ChatProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;

/**
 * {@link ChatProviderFactory} 单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>Anthropic provider 按 bean 名称解析成功</li>
 *   <li>OpenAI provider 按 bean 名称解析成功</li>
 *   <li>DeepSeek provider 回退到 openAiChatModel bean</li>
 *   <li>bean 不存在时回退到任意可用 ChatModel</li>
 *   <li>完全无 ChatModel bean 时抛出 IllegalStateException</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
@ExtendWith(MockitoExtension.class)
class ChatProviderFactoryTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ChatModel chatModel;

    /** Anthropic bean 名称常量（与 ChatProviderFactory 保持一致）。 */
    private static final String BEAN_ANTHROPIC = "anthropicChatModel";

    /** OpenAI bean 名称常量。 */
    private static final String BEAN_OPENAI = "openAiChatModel";

    // -----------------------------------------------------------------------
    // resolve — Anthropic
    // -----------------------------------------------------------------------

    @Test
    void resolve_shouldReturnAnthropicModel_whenAnthropicConfigured() {
        // given
        ChatProperties props = new ChatProperties();
        props.setProvider(ProviderType.ANTHROPIC);
        when(applicationContext.containsBean(BEAN_ANTHROPIC)).thenReturn(true);
        when(applicationContext.getBean(BEAN_ANTHROPIC, ChatModel.class)).thenReturn(chatModel);

        // when
        ChatProviderFactory factory = new ChatProviderFactory(props, applicationContext);
        ChatModel resolved = factory.resolve();

        // then
        assertThat(resolved).isSameAs(chatModel);
    }

    // -----------------------------------------------------------------------
    // resolve — OpenAI
    // -----------------------------------------------------------------------

    @Test
    void resolve_shouldReturnOpenAiModel_whenOpenAiConfigured() {
        // given
        ChatProperties props = new ChatProperties();
        props.setProvider(ProviderType.OPENAI);
        when(applicationContext.containsBean(BEAN_OPENAI)).thenReturn(true);
        when(applicationContext.getBean(BEAN_OPENAI, ChatModel.class)).thenReturn(chatModel);

        // when
        ChatProviderFactory factory = new ChatProviderFactory(props, applicationContext);
        ChatModel resolved = factory.resolve();

        // then
        assertThat(resolved).isSameAs(chatModel);
    }

    // -----------------------------------------------------------------------
    // resolve — DeepSeek（兼容 OpenAI 协议）
    // -----------------------------------------------------------------------

    @Test
    void resolve_shouldUseOpenAiBean_whenDeepSeekConfigured() {
        // given
        ChatProperties props = new ChatProperties();
        props.setProvider(ProviderType.DEEPSEEK);
        when(applicationContext.containsBean(BEAN_OPENAI)).thenReturn(true);
        when(applicationContext.getBean(BEAN_OPENAI, ChatModel.class)).thenReturn(chatModel);

        // when
        ChatProviderFactory factory = new ChatProviderFactory(props, applicationContext);
        ChatModel resolved = factory.resolve();

        // then
        assertThat(resolved).isSameAs(chatModel);
    }

    // -----------------------------------------------------------------------
    // resolve — 回退逻辑
    // -----------------------------------------------------------------------

    @Test
    void resolve_shouldFallbackToAnyAvailableModel_whenConfiguredBeanNotFound() {
        // given
        ChatProperties props = new ChatProperties();
        props.setProvider(ProviderType.ANTHROPIC);
        when(applicationContext.containsBean(BEAN_ANTHROPIC)).thenReturn(false);
        when(applicationContext.getBeanNamesForType(ChatModel.class))
                .thenReturn(new String[]{"someFallbackChatModel"});
        when(applicationContext.getBean("someFallbackChatModel", ChatModel.class))
                .thenReturn(chatModel);

        // when
        ChatProviderFactory factory = new ChatProviderFactory(props, applicationContext);
        ChatModel resolved = factory.resolve();

        // then
        assertThat(resolved).isSameAs(chatModel);
    }

    @Test
    void resolve_shouldThrowIllegalStateException_whenNoChatModelBeanExists() {
        // given
        ChatProperties props = new ChatProperties();
        props.setProvider(ProviderType.ANTHROPIC);
        when(applicationContext.containsBean(BEAN_ANTHROPIC)).thenReturn(false);
        when(applicationContext.getBeanNamesForType(ChatModel.class))
                .thenReturn(new String[]{});

        // when / then
        ChatProviderFactory factory = new ChatProviderFactory(props, applicationContext);
        assertThatThrownBy(factory::resolve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ANTHROPIC");
    }

    // -----------------------------------------------------------------------
    // constructor
    // -----------------------------------------------------------------------

    @Test
    void constructor_shouldCreateInstance_withValidArguments() {
        // given
        ChatProperties props = new ChatProperties();

        // when
        ChatProviderFactory factory = new ChatProviderFactory(props, applicationContext);

        // then
        assertThat(factory).isNotNull();
    }
}
