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

import io.micrometer.context.ThreadLocalAccessor;

/**
 * Reactor Context Propagation 适配器 — 自动传播 requestId ThreadLocal 到 Reactor 调度线程。
 *
 * <p>Spring AI 的 tool 回调在流式模式下运行在 Reactor 调度线程（如 boundedElastic），
 * 而 requestId 最初设置在 streamExecutor 线程上。此适配器通过 Reactor 的
 * Context Propagation 机制，在线程切换时自动捕获和恢复 requestId，
 * 使 {@link ToolCallContext#emit} 和 {@link NavigationHolder#offer} 能正确定位所属请求。
 *
 * <p>注册方式：通过 {@link RequestContextPropagationConfig} 在 Spring 启动时注册。
 *
 * @author DJI DevKit Team
 */
public class RequestIdThreadLocalAccessor implements ThreadLocalAccessor<String> {

    /** Context Propagation registry key。 */
    public static final String KEY = "chat.requestId";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public String getValue() {
        return ToolCallContext.CURRENT_REQUEST_ID.get();
    }

    @Override
    public void setValue(String value) {
        ToolCallContext.CURRENT_REQUEST_ID.set(value);
    }

    @Override
    public void setValue() {
        ToolCallContext.CURRENT_REQUEST_ID.remove();
    }
}
