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

import com.dji.fh.feature.chat.ChatProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;

/**
 * ChatModel provider 工厂。
 *
 * <p>根据 {@link ChatProperties#getProvider()} 配置，从 Spring {@link ApplicationContext}
 * 中按 bean 名称查找对应 provider 的 {@link ChatModel} 实现。
 *
 * <p>各 provider 对应的 bean 名称：
 * <ul>
 *   <li>{@link ProviderType#ANTHROPIC} → {@code anthropicChatModel}</li>
 *   <li>{@link ProviderType#OPENAI}    → {@code openAiChatModel}</li>
 *   <li>{@link ProviderType#DASHSCOPE} → {@code dashscopeChatModel}</li>
 *   <li>{@link ProviderType#DEEPSEEK}  → {@code openAiChatModel}（兼容 OpenAI 协议）</li>
 * </ul>
 *
 * <p>若指定 provider 对应的 bean 不存在，尝试回退到 ApplicationContext 中唯一可用的
 * {@link ChatModel} bean；若仍无法找到，则抛出 {@link IllegalStateException}。
 *
 * @author DJI DevKit Team
 */
public class ChatProviderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ChatProviderFactory.class);

    /** Anthropic ChatModel 的 Spring bean 名称。 */
    private static final String BEAN_ANTHROPIC = "anthropicChatModel";

    /** OpenAI ChatModel 的 Spring bean 名称（同时兼容 DeepSeek）。 */
    private static final String BEAN_OPENAI = "openAiChatModel";

    /** DashScope ChatModel 的 Spring bean 名称。 */
    private static final String BEAN_DASHSCOPE = "dashscopeChatModel";

    /** Chat 配置属性。 */
    private final ChatProperties properties;

    /** Spring 应用上下文，用于按 bean 名称查找 ChatModel。 */
    private final ApplicationContext applicationContext;

    /**
     * 构造函数。
     *
     * @param properties        Chat 配置属性，不能为 null
     * @param applicationContext Spring 应用上下文，不能为 null
     */
    public ChatProviderFactory(ChatProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    /**
     * 根据配置的 provider 类型，解析并返回对应的 {@link ChatModel} 实例。
     *
     * @return 对应 provider 的 ChatModel
     * @throws IllegalStateException 当找不到任何可用的 ChatModel 时
     */
    public ChatModel resolve() {
        ProviderType providerType = properties.getProvider();
        String beanName = resolveBeanName(providerType);

        if (applicationContext.containsBean(beanName)) {
            LOG.info("ChatProviderFactory：使用 provider={}, bean={}", providerType, beanName);
            return applicationContext.getBean(beanName, ChatModel.class);
        }

        LOG.warn("ChatProviderFactory：未找到 bean='{}', 尝试回退到唯一可用的 ChatModel", beanName);
        return fallbackToAnyAvailableChatModel(providerType);
    }

    /**
     * 将 {@link ProviderType} 映射为对应的 Spring bean 名称。
     *
     * @param providerType provider 类型枚举
     * @return bean 名称字符串
     */
    private String resolveBeanName(ProviderType providerType) {
        return switch (providerType) {
            case ANTHROPIC -> BEAN_ANTHROPIC;
            case OPENAI    -> BEAN_OPENAI;
            case DASHSCOPE -> BEAN_DASHSCOPE;
            case DEEPSEEK  -> BEAN_OPENAI;
        };
    }

    /**
     * 尝试从 ApplicationContext 中查找任意可用的 {@link ChatModel} bean 作为回退。
     *
     * @param originalProvider 原始配置的 provider 类型（用于日志）
     * @return 找到的第一个 ChatModel bean
     * @throws IllegalStateException 当 ApplicationContext 中不存在任何 ChatModel bean 时
     */
    private ChatModel fallbackToAnyAvailableChatModel(ProviderType originalProvider) {
        String[] beanNames = applicationContext.getBeanNamesForType(ChatModel.class);
        if (beanNames.length == 0) {
            throw new IllegalStateException(
                    "未找到任何 ChatModel bean。请确保已配置 provider '" + originalProvider
                            + "' 的 Spring AI starter 依赖和相应的 API Key。"
            );
        }
        String fallbackBean = beanNames[0];
        LOG.warn("ChatProviderFactory：回退到 bean='{}' (原始 provider={})", fallbackBean, originalProvider);
        return applicationContext.getBean(fallbackBean, ChatModel.class);
    }
}
