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

package com.dji.flighthub.mcp.security;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Bearer Token 认证器 — 默认认证实现。
 * 从 Authorization: Bearer <token> 头中提取 token 并与配置值比对。
 *
 * <p>配置方式：
 * <ul>
 *   <li>环境变量 FH_SSE_AUTH_TOKEN 设置期望的 token 值</li>
 *   <li>token 为空时自动禁用认证（开发模式）</li>
 * </ul>
 *
 * <p>扩展点：如需 OAuth2/mTLS 等认证方式，实现 {@link McpAuthenticator} 接口
 * 并通过 @Primary 或 @ConditionalOnProperty 切换即可。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class BearerTokenAuthenticator implements McpAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(BearerTokenAuthenticator.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SCHEME_NAME = "bearer-token";

    private final FlightHubProperties properties;

    public BearerTokenAuthenticator(FlightHubProperties properties) {
        this.properties = properties;
    }

    @Override
    public AuthResult authenticate(ServerHttpRequest request) {
        String configuredToken = properties.getTransport().getSseAuthToken();

        // token 未配置时跳过认证（开发模式）
        if (!isEnabled()) {
            return null;
        }

        // 提取 Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank()) {
            return AuthResult.unauthorized(
                    "Missing Authorization header. Expected: Authorization: Bearer <token>");
        }

        // 校验 Bearer 前缀
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            return AuthResult.unauthorized(
                    "Invalid Authorization scheme. Expected: Bearer <token>");
        }

        // 提取并比对 token
        String providedToken = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (providedToken.isEmpty()) {
            return AuthResult.unauthorized("Empty bearer token.");
        }

        // 常量时间比较，防止时序攻击
        if (!constantTimeEquals(configuredToken, providedToken)) {
            log.warn("Authentication failed: invalid token from {}",
                    request.getRemoteAddress());
            return AuthResult.unauthorized("Invalid bearer token.");
        }

        return null; // 认证通过
    }

    @Override
    public String scheme() {
        return SCHEME_NAME;
    }

    @Override
    public boolean isEnabled() {
        String token = properties.getTransport().getSseAuthToken();
        return token != null && !token.isBlank();
    }

    /**
     * 常量时间字符串比较，防止时序攻击。
     * 使用 JDK 标准库 MessageDigest.isEqual() 确保无 JIT 优化风险。
     */
    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
