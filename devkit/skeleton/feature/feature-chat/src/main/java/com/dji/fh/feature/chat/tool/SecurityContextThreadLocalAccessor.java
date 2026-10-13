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

import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import io.micrometer.context.ThreadLocalAccessor;

/**
 * Reactor Context Propagation 适配器 — 自动传播 SecurityContext 到 Reactor 调度线程。
 *
 * <p>Chat 流式请求中，{@link SecurityContextHolder} 最初在 servlet 线程设置，
 * 经 streamExecutor 传递后，tool 回调运行在 Reactor boundedElastic 线程。
 * 此适配器通过 Reactor 的 Context Propagation 机制，在线程切换时自动
 * 捕获和恢复 SecurityContext，使 {@code SecurityGatedCallback} 等安全门控
 * 能在回调线程中读取到真实的用户身份和角色。
 *
 * <p>注册方式：通过 {@link RequestContextPropagationConfig} 在 Spring 启动时注册。
 *
 * @author DJI DevKit Team
 */
public class SecurityContextThreadLocalAccessor implements ThreadLocalAccessor<SecurityContext> {

    /** Context Propagation registry key。 */
    public static final String KEY = "chat.securityContext";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public SecurityContext getValue() {
        SecurityContext ctx = SecurityContextHolder.current();
        // NoOp（未认证）不需要传播 — 返回 null 表示"无值"
        return ctx.isAuthenticated() ? ctx : null;
    }

    @Override
    public void setValue(SecurityContext value) {
        SecurityContextHolder.set(value);
    }

    @Override
    public void setValue() {
        SecurityContextHolder.clear();
    }
}
