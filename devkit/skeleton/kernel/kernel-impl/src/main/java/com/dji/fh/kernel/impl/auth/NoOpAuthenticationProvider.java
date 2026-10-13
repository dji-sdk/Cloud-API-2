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

import com.dji.fh.kernel.spi.auth.AuthenticationException;
import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * AuthenticationProvider 的兜底默认实现 —— 无论什么请求都返回 {@link NoOpSecurityContext}。
 *
 * <p>语义：未配置任何业务 Provider 时，Kernel 默认走匿名（{@code isAuthenticated() == false}）；
 * 配合 {@code @RequireAuthenticated} 切面，所有需登录的端点立刻被拒，达到 fail-safe 效果。
 *
 * <p>{@link #order()} = {@link Integer#MAX_VALUE}，永远是 Provider 链的最后兜底。
 *
 * @author DJI DevKit Team
 */
public class NoOpAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Optional<SecurityContext> authenticate(HttpServletRequest request)
            throws AuthenticationException {
        return Optional.of(NoOpSecurityContext.INSTANCE);
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }
}
