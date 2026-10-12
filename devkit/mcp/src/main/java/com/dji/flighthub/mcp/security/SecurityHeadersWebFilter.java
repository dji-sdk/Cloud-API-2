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

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 安全响应头 + CORS 策略过滤器。
 *
 * <p>对所有响应注入安全头，防止 XSS、点击劫持、MIME 嗅探等常见攻击。
 * 同时处理跨域（CORS）请求，仅允许配置的来源访问。
 *
 * <p>配置项：
 * <ul>
 *   <li>FH_CORS_ALLOWED_ORIGINS — 允许的来源，逗号分隔（默认空，即不允许跨域）</li>
 * </ul>
 *
 * <p>仅在 SSE profile 下激活（stdio 模式无 HTTP）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Profile("sse")
@Order(-1)
public class SecurityHeadersWebFilter implements WebFilter {

    private static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String HEADER_STRICT_TRANSPORT = "Strict-Transport-Security";
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    private static final String HEADER_PERMISSIONS_POLICY = "Permissions-Policy";

    /** 允许的 CORS 来源 */
    private final Set<String> allowedOrigins;

    public SecurityHeadersWebFilter() {
        String origins = System.getenv("FH_CORS_ALLOWED_ORIGINS");
        if (origins != null && !origins.isBlank()) {
            this.allowedOrigins = Set.of(origins.split(","));
        } else {
            this.allowedOrigins = Set.of();
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders responseHeaders = response.getHeaders();

        // 注入安全响应头
        responseHeaders.set(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        responseHeaders.set(HEADER_X_FRAME_OPTIONS, "DENY");
        responseHeaders.set(HEADER_X_XSS_PROTECTION, "1; mode=block");
        responseHeaders.set(HEADER_STRICT_TRANSPORT, "max-age=31536000; includeSubDomains");
        responseHeaders.set(HEADER_CACHE_CONTROL, "no-store, no-cache, must-revalidate");
        responseHeaders.set(HEADER_CONTENT_SECURITY_POLICY, "default-src 'self'; frame-ancestors 'none'");
        responseHeaders.set(HEADER_PERMISSIONS_POLICY, "camera=(), microphone=(), geolocation=()");

        // CORS 处理
        String origin = request.getHeaders().getOrigin();
        if (origin != null && !origin.isBlank()) {
            if (allowedOrigins.contains(origin.trim())) {
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        "Authorization, Content-Type, X-Request-ID");
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            }

            // CORS preflight 直接返回
            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                response.setStatusCode(HttpStatus.NO_CONTENT);
                return Mono.empty();
            }
        }

        return chain.filter(exchange);
    }
}
