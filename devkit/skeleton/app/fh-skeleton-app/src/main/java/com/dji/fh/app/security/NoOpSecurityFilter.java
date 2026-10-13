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

package com.dji.fh.app.security;

import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.PublicPaths;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * v0.1 默认 HTTP 安全过滤器 —— 每个请求 set {@link NoOpSecurityContext}，
 * finally 块 clear，让 ThreadLocal 不在线程池场景泄漏。
 *
 * <p>NoOp 默认让 {@code @RequireAuthenticated} 立即拒绝（fail-safe）；
 * {@code @RequireRole} 和 {@code @RequirePermission} 也会因匿名用户被拒。
 * 需要真鉴权时 ISV 覆盖 {@link SecurityFilterAutoConfiguration} 中名为 {@code "securityFilter"} 的 bean。
 *
 * <p>安全发布专项修复：公开路径白名单统一收敛到
 * {@link PublicPaths}（kernel-spi），此处委托其精确匹配（先剥离 context-path 再 equals），
 * 杜绝 {@code endsWith} 宽松匹配导致的越权风险。
 * 未配置 feature-user 鉴权时，公开端点（登录/注册）应保持匿名可达，
 * 其余路径一律按匿名处理（fail-closed 由 {@code @RequireAuthenticated} 切面统一兜底）。
 *
 * @author DJI DevKit Team
 */
public class NoOpSecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest
                && PublicPaths.isPublicPath(httpRequest)) {
            // 公开路径放行（不注入 SecurityContext，避免切面误判）
            chain.doFilter(request, response);
            return;
        }
        SecurityContextHolder.set(NoOpSecurityContext.INSTANCE);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
        }
    }
}
