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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * 工具调用上下文 — 以 requestId 为 key 实现请求级隔离，支持并发请求互不干扰。
 *
 * <p>使用 {@link ConcurrentHashMap} 存储每个请求的回调，配合 {@link ThreadLocal}
 * 标识当前线程所属请求。在流式模式下，Spring AI 的 tool 回调可能运行在 Reactor
 * 调度线程上，通过 Reactor Context Propagation 自动传播 requestId ThreadLocal。
 *
 * <p>使用规范：
 * <pre>
 *   // ChatController 中：
 *   String requestId = UUID.randomUUID().toString();
 *   ToolCallContext.set(requestId, event -> sendEvent(emitter, event));
 *   try {
 *       chatClient.prompt()...stream().content().blockLast();
 *   } finally {
 *       ToolCallContext.clear(requestId);
 *   }
 *
 *   // McpProxyChatTool 中（Reactor 线程自动传播了 requestId）：
 *   ToolCallContext.emit(ChatSseEvent.status("正在查询设备列表..."));
 * </pre>
 *
 * @author DJI DevKit Team
 */
public final class ToolCallContext {

    private static final ConcurrentMap<String, Consumer<ChatSseEvent>> HOLDER = new ConcurrentHashMap<>();

    /**
     * 当前线程所属请求的 ID。
     * 通过 Reactor Context Propagation 自动传播到 Reactor 调度线程，
     * 确保 tool 回调线程也能正确获取 requestId。
     */
    static final ThreadLocal<String> CURRENT_REQUEST_ID = new ThreadLocal<>();

    private ToolCallContext() {
    }

    /**
     * 注册指定请求的 SSE 事件回调，并绑定 requestId 到当前线程。
     *
     * @param requestId 请求唯一标识，不能为 null
     * @param callback  事件消费者，不能为 null
     */
    public static void set(String requestId, Consumer<ChatSseEvent> callback) {
        HOLDER.put(requestId, callback);
        CURRENT_REQUEST_ID.set(requestId);
    }

    /**
     * 清除指定请求的回调并解绑 ThreadLocal，必须在 finally 块中调用。
     *
     * @param requestId 请求唯一标识
     */
    public static void clear(String requestId) {
        HOLDER.remove(requestId);
        CURRENT_REQUEST_ID.remove();
    }

    /**
     * 触发事件。根据当前线程绑定的 requestId 查找对应回调。
     * 若当前线程未绑定请求或未注册回调，则静默忽略（不抛异常）。
     *
     * @param event 要发送的 SSE 事件
     */
    public static void emit(ChatSseEvent event) {
        String requestId = CURRENT_REQUEST_ID.get();
        if (requestId == null) {
            return;
        }
        Consumer<ChatSseEvent> cb = HOLDER.get(requestId);
        if (cb != null) {
            cb.accept(event);
        }
    }

    /**
     * 获取当前线程绑定的 requestId。
     *
     * @return 当前请求 ID，可能为 null
     */
    public static String currentRequestId() {
        return CURRENT_REQUEST_ID.get();
    }
}
