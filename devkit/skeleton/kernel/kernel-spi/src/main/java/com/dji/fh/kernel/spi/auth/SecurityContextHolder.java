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

package com.dji.fh.kernel.spi.auth;

/**
 * 线程级 SecurityContext 持有者。基于 ThreadLocal。
 *
 * <p>HTTP filter 在请求开始时通过 {@link #set(SecurityContext)} 注入，
 * 请求结束时必须调用 {@link #clear()} 防止线程复用泄漏。
 *
 * <p>Holder 是 {@code final class} <b>不可替换</b>；要替换的是
 * {@link AuthenticationProvider}。
 *
 * @author DJI DevKit Team
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> CONTEXT = new ThreadLocal<>();

    private SecurityContextHolder() {
    }

    /**
     * 获取当前线程的 SecurityContext。未设置时返回 {@link NoOpSecurityContext#INSTANCE}，
     * 调用方零 NPE 风险。
     *
     * @return 当前 SecurityContext（绝不返回 null）
     */
    public static SecurityContext current() {
        SecurityContext ctx = CONTEXT.get();
        return ctx != null ? ctx : NoOpSecurityContext.INSTANCE;
    }

    /**
     * 设置当前线程的 SecurityContext。
     *
     * <p>传入 {@code null} 等价于调用 {@link #clear()} —— 内部走 {@code ThreadLocal.remove()}
     * 而不是 {@code ThreadLocal.set(null)}，避免在线程池场景下残留 null 条目造成内存滞留。
     *
     * @param context 要设置的 context；{@code null} 等价于 clear()
     */
    public static void set(SecurityContext context) {
        if (context == null) {
            clear();
            return;
        }
        CONTEXT.set(context);
    }

    /**
     * 清除当前线程的 SecurityContext。请求结束 / 线程复用前必须调用。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
