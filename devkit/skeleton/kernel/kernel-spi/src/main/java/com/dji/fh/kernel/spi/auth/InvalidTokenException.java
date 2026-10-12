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
 * 无效凭证异常 — fail-closed 认证失败信号（安全发布专项）。
 *
 * <p>携带无效/过期 token 的请求不应降级为匿名继续访问受保护端点。
 * 认证过滤器捕获到 {@link AuthenticationException} 后抛出本异常，
 * 由容器级异常处理映射为 HTTP 401 Unauthorized。
 *
 * @author DJI DevKit Team
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * 构造函数。
     *
     * @param message 失败原因（如 "JWT token 无效: ..."）
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * 构造函数。
     *
     * @param message 失败原因
     * @param cause   根因
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
