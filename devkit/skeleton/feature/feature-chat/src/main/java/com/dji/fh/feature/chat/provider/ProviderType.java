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

/**
 * 支持的 LLM provider 类型。
 *
 * <p>每个枚举值对应一个具体的大语言模型服务商，用于在 {@code fh.chat.provider} 配置项中指定。
 *
 * @author DJI DevKit Team
 */
public enum ProviderType {
    /** Anthropic Claude 系列模型 */
    ANTHROPIC,
    /** OpenAI GPT 系列模型 */
    OPENAI,
    /** 阿里云百炼（DashScope）模型 */
    DASHSCOPE,
    /** DeepSeek 系列模型 */
    DEEPSEEK
}
