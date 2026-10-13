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

package com.dji.fh.feature.chat.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link ChatRequest} 单元测试。
 *
 * @author DJI CloudAPI2 Team
 */
class ChatRequestTest {

    @Test
    void gettersAndSetters_shouldWorkCorrectly() {
        ChatRequest request = new ChatRequest();
        request.setMessage("你好");
        request.setConversationId("conv-001");

        assertThat(request.getMessage()).isEqualTo("你好");
        assertThat(request.getConversationId()).isEqualTo("conv-001");
    }

    @Test
    void history_shouldBeSettableAndRetrievable() {
        ChatRequest request = new ChatRequest();
        ChatRequest.HistoryMessage msg = new ChatRequest.HistoryMessage();
        msg.setRole("user");
        msg.setContent("之前的问题");

        request.setHistory(List.of(msg));

        assertThat(request.getHistory()).hasSize(1);
        assertThat(request.getHistory().get(0).getRole()).isEqualTo("user");
        assertThat(request.getHistory().get(0).getContent()).isEqualTo("之前的问题");
    }

    @Test
    void defaults_shouldBeNull() {
        ChatRequest request = new ChatRequest();
        assertThat(request.getMessage()).isNull();
        assertThat(request.getConversationId()).isNull();
        assertThat(request.getHistory()).isNull();
    }
}
