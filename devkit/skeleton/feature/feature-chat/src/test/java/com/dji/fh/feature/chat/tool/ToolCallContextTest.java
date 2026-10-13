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

package com.dji.fh.feature.chat.tool;

import com.dji.fh.feature.chat.controller.ChatSseEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ToolCallContext} 单元测试。
 *
 * @author DJI DevKit Team
 */
class ToolCallContextTest {

    private static final String REQUEST_ID = "test-req-001";

    @AfterEach
    void cleanup() {
        ToolCallContext.clear(REQUEST_ID);
    }

    @Test
    void emit_shouldInvokeCallback_whenCallbackRegistered() {
        List<ChatSseEvent> received = new ArrayList<>();
        ToolCallContext.set(REQUEST_ID, received::add);

        ToolCallContext.emit(ChatSseEvent.status("正在查询..."));

        assertThat(received).hasSize(1);
        assertThat(received.get(0).getType()).isEqualTo(ChatSseEvent.TYPE_STATUS);
    }

    @Test
    void emit_shouldSilentlyIgnore_whenNoCallbackRegistered() {
        ToolCallContext.emit(ChatSseEvent.status("无回调"));
    }

    @Test
    void emit_shouldSilentlyIgnore_whenNoRequestIdBound() {
        List<ChatSseEvent> received = new ArrayList<>();
        ToolCallContext.set(REQUEST_ID, received::add);
        ToolCallContext.CURRENT_REQUEST_ID.remove();

        ToolCallContext.emit(ChatSseEvent.status("无 requestId"));

        assertThat(received).isEmpty();
    }

    @Test
    void clear_shouldRemoveCallback() {
        List<ChatSseEvent> received = new ArrayList<>();
        ToolCallContext.set(REQUEST_ID, received::add);
        ToolCallContext.clear(REQUEST_ID);

        ToolCallContext.CURRENT_REQUEST_ID.set(REQUEST_ID);
        ToolCallContext.emit(ChatSseEvent.status("清除后"));

        assertThat(received).isEmpty();
        ToolCallContext.CURRENT_REQUEST_ID.remove();
    }

    @Test
    void concurrentRequests_shouldIsolateCallbacks() throws InterruptedException {
        String reqA = "req-A";
        String reqB = "req-B";
        List<ChatSseEvent> receivedA = new ArrayList<>();
        List<ChatSseEvent> receivedB = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        Thread threadA = new Thread(() -> {
            ToolCallContext.set(reqA, receivedA::add);
            ToolCallContext.emit(ChatSseEvent.status("A"));
            ToolCallContext.clear(reqA);
            latch.countDown();
        });

        Thread threadB = new Thread(() -> {
            ToolCallContext.set(reqB, receivedB::add);
            ToolCallContext.emit(ChatSseEvent.status("B"));
            ToolCallContext.clear(reqB);
            latch.countDown();
        });

        threadA.start();
        threadB.start();
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();

        assertThat(receivedA).hasSize(1);
        assertThat(receivedB).hasSize(1);
        assertThat(receivedA.get(0).getData().toString()).contains("A");
        assertThat(receivedB.get(0).getData().toString()).contains("B");
    }

    @Test
    void currentRequestId_shouldReturnBoundId() {
        ToolCallContext.set(REQUEST_ID, event -> { });
        assertThat(ToolCallContext.currentRequestId()).isEqualTo(REQUEST_ID);
    }
}
