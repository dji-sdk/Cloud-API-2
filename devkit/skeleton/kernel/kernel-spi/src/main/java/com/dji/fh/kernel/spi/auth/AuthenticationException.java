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
 * 认证失败异常。
 *
 * <p>注意区分：
 * <ul>
 *   <li>「token 无效 / 已过期 / 签名错误」→ 抛本异常</li>
 *   <li>「本 Provider 不处理此请求」→ {@link AuthenticationProvider#authenticate}
 *       返回 {@code Optional.empty()}，不抛异常</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message 异常消息
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * @param message 异常消息
     * @param cause   原因
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
