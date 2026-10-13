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

package com.dji.fh.feature.user.security;

import com.dji.fh.kernel.spi.auth.AuthenticationException;
import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.InvalidTokenException;
import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.PublicPaths;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 认证安全过滤器 — 委托 {@link AuthenticationProvider} 链尝试解析请求身份。
 *
 * <p>认证成功时设置真实 {@link SecurityContext}；
 * 所有 Provider 返回 empty 时回退到 {@link NoOpSecurityContext}（匿名）；
 * Provider 抛 {@link AuthenticationException} 时<b>直接拒绝 401</b>（fail-closed），
 * 与旧版"回退匿名继续放行"相反 — 安全发布专项修复：
 * 携带无效/过期 token 的请求不得降级为匿名继续访问受保护端点。
 *
 * <p>未携带任何凭证的请求（Provider 返回 empty）仍按匿名继续处理 ——
 * 受保护端点由 {@code @RequireAuthenticated} 切面统一拦截返回 401；
 * 公开端点（登录/注册）经 {@link PublicPaths} 路径白名单在鉴权解析前短路放行，
 * 携带失效 Bearer 的登录/注册请求不会被误判为 401（安全发布专项）。
 *
 * <p>替换 {@code NoOpSecurityFilter}：当 feature-user 模块启用时，
 * 通过 {@code UserAutoConfiguration} 注册同名 bean 覆盖默认 NoOp filter。
 *
 * @author DJI DevKit Team
 */
public class AuthenticatingSecurityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticatingSecurityFilter.class);

    /**
     * 无效凭证的固定响应消息 —— 不向客户端回显 JWT 解析细节。
     */
    static final String INVALID_TOKEN_MESSAGE = "无效或已过期的访问凭证，请重新登录";

    private final AuthenticationProvider authenticationProvider;

    /**
     * 构造函数。
     *
     * @param authenticationProvider 认证 Provider（由 UserAutoConfiguration 注入 JwtAuthenticationProvider）
     */
    public AuthenticatingSecurityFilter(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 公开路径（登录/注册）在鉴权解析前短路放行：携带失效 Bearer 的登录/注册请求
        // 仍应可达，不得因旧 token 过期而 401。
        if (PublicPaths.isPublicPath(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        SecurityContext ctx;
        try {
            ctx = resolveContext(httpRequest);
        } catch (InvalidTokenException e) {
            // 过滤器异常发生在 DispatcherServlet 之前，@RestControllerAdvice 不会拦截，
            // 因此在此直接写 401 JSON 终止链路；JWT 细节已由 resolveContext 写入日志。
            writeUnauthorized(httpResponse);
            return;
        }
        SecurityContextHolder.set(ctx);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    /**
     * 通过 AuthenticationProvider 尝试解析请求身份。
     *
     * @param request HTTP 请求
     * @return 解析到的 SecurityContext
     * @throws InvalidTokenException 携带无效/过期凭证时抛出（由 doFilter 捕获并写 401 JSON）
     */
    private SecurityContext resolveContext(HttpServletRequest request) {
        try {
            Optional<SecurityContext> result = authenticationProvider.authenticate(request);
            return result.orElse(NoOpSecurityContext.INSTANCE);
        } catch (AuthenticationException e) {
            // fail-closed：无效/过期 token 立即 401，不再降级匿名。
            // 日志保留 JWT 解析细节供排查，响应体统一为固定文案（不回显 token 内容）。
            LOG.warn("安全发布专项修复：拒绝携带无效凭证的请求, reason={}", e.getMessage());
            throw new InvalidTokenException(e.getMessage());
        }
    }

    /**
     * 直接向响应写出 401 JSON（不依赖 DispatcherServlet 后的异常处理器）。
     *
     * @param response HTTP 响应
     * @throws IOException 写响应失败
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"invalid_token\",\"message\":\""
                + INVALID_TOKEN_MESSAGE + "\"}");
    }
}
