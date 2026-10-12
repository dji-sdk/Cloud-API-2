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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * MCP 传输层认证过滤器。
 * 采用「默认拒绝」安全模型：除显式声明的公开路径外，所有请求均需通过认证。
 *
 * <p>公开路径（无需 token）：/health、/ready（K8s 探针必须无障碍访问）。
 * <p>受保护路径（需 token）：/sse、/message、/status 以及未来新增的所有端点。
 *
 * <p>仅在 sse profile 下激活。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Profile("sse")
@Order(1)
public class AuthenticationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationWebFilter.class);

    /** 公开路径白名单 — 无需认证即可访问（K8s 探针等） */
    private static final Set<String> PUBLIC_PATHS = Set.of("/health", "/ready");

    private final McpAuthenticator authenticator;
    private final ObjectMapper objectMapper;
    /** 对外 base-path 前缀（如 /pf-devkit/ai-plugin/mcp），用于在白名单匹配前剥离 */
    private final String basePath;

    public AuthenticationWebFilter(McpAuthenticator authenticator, ObjectMapper objectMapper,
                                   @Value("${spring.webflux.base-path:}") String basePath) {
        this.authenticator = authenticator;
        this.objectMapper = objectMapper;
        this.basePath = basePath;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 公开路径直接放行（K8s 探针无需 token）
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 认证器未启用时放行（开发模式）
        if (!authenticator.isEnabled()) {
            return chain.filter(exchange);
        }

        // 执行认证
        McpAuthenticator.AuthResult result = authenticator.authenticate(request);

        // null → 认证通过（BearerTokenAuthenticator 兼容路径，无 claims）
        if (result == null) {
            return chain.filter(exchange);
        }

        // success → 认证通过并携带服务端验证过的 claims（JWT 路径）
        if (result.isSuccess()) {
            enrichExchangeFromAuthResult(exchange, result);
            return chain.filter(exchange);
        }

        // 认证失败 — 返回错误响应
        log.warn("Auth rejected [{}]: {} — path={}, remote={}",
                authenticator.scheme(), result.detail(), path, request.getRemoteAddress());
        return writeErrorResponse(exchange, result);
    }

    /**
     * 判断路径是否为公开路径（无需认证）。
     * 安全模型：默认拒绝，仅白名单内路径放行。
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(McpPathNormalizer.stripBasePath(basePath, path));
    }

    /**
     * 认证成功后，将服务端验证过的 userId 存入 exchange 属性。
     * 仅从 AuthResult 读取（服务端解析 JWT 后生成），不信任客户端请求 header，
     * 防止客户端通过伪造 fh.userId header 劫持其他用户的 session。
     */
    private void enrichExchangeFromAuthResult(ServerWebExchange exchange, McpAuthenticator.AuthResult result) {
        if (result.userId() != null && !result.userId().isBlank()) {
            exchange.getAttributes().put(JwtAuthenticator.ATTR_USER_ID, result.userId());
        }
    }

    /**
     * 写入 JSON 错误响应。
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, McpAuthenticator.AuthResult result) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.valueOf(result.httpStatus()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "jsonrpc", "2.0",
                "error", Map.of(
                        "code", -32000 - result.httpStatus(),
                        "message", result.error(),
                        "data", result.detail()
                )
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] fallback = ("{\"error\":\"" + result.error() + "\"}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
