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

import com.dji.fh.feature.chat.controller.ChatStatusController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * AI 助手可用性探测的 AutoConfiguration。
 *
 * <p><b>本配置类刻意不标注 {@code @ConditionalOnProperty(fh.chat.enabled=true)}。</b>
 * 关闭 AI 助手时 {@code GET /api/v1/chat/status} 仍必须返回 200，否则前端无法区分
 * 「功能未启用」（应隐藏入口）与「服务异常」（应报错）。
 *
 * <p>它只注册一个轻量的探测接口，不会拉起任何 Chat / MCP / RAG 相关 bean，
 * 因此关闭状态下零额外开销。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@EnableConfigurationProperties(ChatProperties.class)
public class ChatStatusAutoConfiguration {

    /**
     * 注册 AI 助手可用性探测接口。集成商 可通过声明同类型 bean 覆盖默认实现。
     *
     * @param properties Chat 配置属性
     * @return ChatStatusController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatStatusController chatStatusController(ChatProperties properties) {
        return new ChatStatusController(properties);
    }
}
