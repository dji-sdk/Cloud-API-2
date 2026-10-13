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

import com.dji.fh.feature.user.entity.UserEntity;
import com.dji.fh.feature.user.repository.UserRepository;
import com.dji.fh.kernel.spi.auth.AuthenticationException;
import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * JWT 认证 Provider — 实现 kernel-spi 的 {@link AuthenticationProvider} 接口。
 *
 * <p>从 {@code Authorization: Bearer <token>} 头提取 JWT，验证后返回 {@link JwtSecurityContext}。
 * 非 Bearer 请求返回 {@code Optional.empty()} 交给下游 Provider 处理。
 *
 * <p>安全机制：验证 token 中的 {@code pwd_ver} claim 与 DB 当前密码版本是否一致，
 * 改密后旧 token 立即失效（无需等待过期）。
 *
 * @author DJI DevKit Team
 */
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_QUERY_PARAM = "token";
    private static final int PROVIDER_ORDER = 100;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 构造函数。
     *
     * @param jwtTokenProvider JWT token 签发/验证器
     * @param userRepository   用户数据访问（用于密码版本校验）
     */
    public JwtAuthenticationProvider(JwtTokenProvider jwtTokenProvider,
                                     UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<SecurityContext> authenticate(HttpServletRequest request) throws AuthenticationException {
        String token = extractToken(request);
        if (token == null) {
            return Optional.empty();
        }

        try {
            Long userId = jwtTokenProvider.getUserId(token);
            String username = jwtTokenProvider.getUsername(token);
            String role = jwtTokenProvider.getRole(token);
            int tokenPwdVer = jwtTokenProvider.getPasswordVersion(token);

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || userOpt.get().getPasswordVersion() != tokenPwdVer) {
                throw new AuthenticationException("token 已失效（密码已变更），请重新登录");
            }

            return Optional.of(new JwtSecurityContext(userId, username, role));
        } catch (JwtException | NumberFormatException e) {
            throw new AuthenticationException("JWT token 无效: " + e.getMessage());
        }
    }

    /**
     * 提取 JWT token：优先 Authorization Bearer 头，其次 query 参数 {@code token}.
     *
     * <p>query 参数回退用于 EventSource 等 API 无法自定义请求头的场景（如 SSE 订阅）。
     *
     * @param request HTTP 请求
     * @return token 字符串，无则 null
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        String queryToken = request.getParameter(TOKEN_QUERY_PARAM);
        return (queryToken != null && !queryToken.isBlank()) ? queryToken : null;
    }

    @Override
    public int order() {
        return PROVIDER_ORDER;
    }
}
