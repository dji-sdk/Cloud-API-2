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

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link ChatSseEvent} 单元测试。
 *
 * @author DJI CloudAPI2 Team
 */
class ChatSseEventTest {

    @Test
    void token_shouldBuildCorrectly() {
        ChatSseEvent event = ChatSseEvent.token("hello");
        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_TOKEN);
        Map<String, Object> data = event.getData();
        assertThat(data.get("content")).isEqualTo("hello");
        assertThat(data.get("type")).isEqualTo("token");
    }

    @Test
    void done_shouldIncludeTokenCounts() {
        ChatSseEvent event = ChatSseEvent.done(100, 200);
        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_DONE);
        Map<String, Object> data = event.getData();
        assertThat(data.get("inputTokens")).isEqualTo(100);
        assertThat(data.get("outputTokens")).isEqualTo(200);
    }

    @Test
    void error_shouldIncludeCodeAndMessage() {
        ChatSseEvent event = ChatSseEvent.error("stream_failed", "服务不可用");
        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_ERROR);
        Map<String, Object> data = event.getData();
        assertThat(data.get("code")).isEqualTo("stream_failed");
        assertThat(data.get("message")).isEqualTo("服务不可用");
    }

    @Test
    void toolCall_shouldIncludeNameAndArgs() {
        ChatSseEvent event = ChatSseEvent.toolCall("fh_device_list", "{\"online\":true}");
        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_TOOL_CALL);
        Map<String, Object> data = event.getData();
        assertThat(data.get("toolName")).isEqualTo("fh_device_list");
        assertThat(data.get("args")).isEqualTo("{\"online\":true}");
    }

    @Test
    void toolResult_shouldIncludeContent() {
        ChatSseEvent event = ChatSseEvent.toolResult("执行成功");
        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_TOOL_RESULT);
        assertThat(event.getData().get("content")).isEqualTo("执行成功");
    }

    @Test
    void navigation_shouldIncludePathAndQuery() {
        Map<String, String> query = Map.of("drone_sn", "DRONE-001");
        ChatSseEvent event = ChatSseEvent.navigation("/cockpit", query);

        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_NAVIGATION);
        Map<String, Object> data = event.getData();
        assertThat(data.get("path")).isEqualTo("/cockpit");
        assertThat(data.get("query")).isEqualTo(query);
    }

    @Test
    void status_shouldBuildCorrectly() {
        ChatSseEvent event = ChatSseEvent.status("正在查询设备列表...");

        assertThat(event.getType()).isEqualTo(ChatSseEvent.TYPE_STATUS);
        Map<String, Object> data = event.getData();
        assertThat(data.get("type")).isEqualTo("status");
        assertThat(data.get("text")).isEqualTo("正在查询设备列表...");
    }
}
