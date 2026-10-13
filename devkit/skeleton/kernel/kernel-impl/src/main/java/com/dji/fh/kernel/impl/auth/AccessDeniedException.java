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

package com.dji.fh.kernel.impl.auth;

/**
 * 鉴权失败异常。
 *
 * <p>{@link AuthorizationAspect} 检测到当前 {@link com.dji.fh.kernel.spi.auth.SecurityContext}
 * 不满足 {@code @RequireRole} / {@code @RequirePermission} / {@code @RequireAuthenticated} 的要求时抛出。
 *
 * <p>ISV 可在 {@code @ControllerAdvice} 中捕获并映射为 HTTP 403。
 *
 * @author DJI DevKit Team
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message 异常消息，应包含失败原因（如缺少哪个角色 / 权限）
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
