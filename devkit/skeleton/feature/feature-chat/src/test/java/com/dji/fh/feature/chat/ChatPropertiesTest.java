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

import com.dji.fh.feature.chat.provider.ProviderType;
import org.junit.jupiter.api.Test;

/**
 * {@link ChatProperties} 单元测试，验证默认值是否合理。
 *
 * @author DJI DevKit Team
 */
class ChatPropertiesTest {

    @Test
    void defaults_shouldBeReasonable() {
        ChatProperties props = new ChatProperties();
        assertThat(props.isEnabled()).isFalse();
        // 默认走 OpenAI 兼容协议接入 GLM 等模型网关，可通过 fh.chat.provider 覆盖
        assertThat(props.getProvider()).isEqualTo(ProviderType.OPENAI);
        assertThat(props.getMaxHistoryTurns()).isEqualTo(10);
        // GLM-5.2 等 reasoning 模型需较大 max-tokens，避免 content 被 reasoning 耗尽后截断
        assertThat(props.getMaxTokens()).isEqualTo(8192);
        assertThat(props.getSystemPromptPath()).isEqualTo("classpath:chat-system-prompt.txt");
    }

    @Test
    void rag_defaults_shouldBeReasonable() {
        ChatProperties.Rag rag = new ChatProperties().getRag();
        assertThat(rag.isEnabled()).isTrue();
        assertThat(rag.getDocumentsPath()).isEqualTo("classpath:chat-docs/");
        assertThat(rag.getChunkSize()).isEqualTo(1000);
        assertThat(rag.getChunkOverlap()).isEqualTo(200);
        assertThat(rag.getTopK()).isEqualTo(5);
        assertThat(rag.getSimilarityThreshold()).isEqualTo(0.7);
    }
}
