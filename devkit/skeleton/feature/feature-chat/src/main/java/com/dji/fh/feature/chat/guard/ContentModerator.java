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

package com.dji.fh.feature.chat.guard;

/**
 * 内容审核 SPI 接口。
 *
 * <p>默认实现为关键词正则匹配（{@link DefaultContentModerator}）。
 * ISV 可通过 Spring Bean 覆盖（{@code @ConditionalOnMissingBean}）接入企业内容安全 API
 * （如阿里绿网、自建 guardrails 等）。
 *
 * @author DJI DevKit Team
 */
public interface ContentModerator {

    /**
     * 审核输入文本（用户消息发送到 LLM 之前）。
     *
     * @param text 待审核的用户输入
     * @return 审核结果
     */
    ModerationResult moderateInput(String text);

    /**
     * 审核输出文本（LLM 回复完成后的完整内容）。
     *
     * @param text 待审核的 LLM 输出
     * @return 审核结果
     */
    ModerationResult moderateOutput(String text);
}
