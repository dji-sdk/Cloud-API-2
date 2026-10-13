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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthenticationWebFilter 单元测试。
 * 验证路径匹配、鉴权委托、公开路径放行、默认拒绝模型、JSON 错误序列化等逻辑。
 *
 * @author DJI FlightHub SDK Team
 */
class AuthenticationWebFilterTest {

    private static final String VALID_TOKEN = "secret-token-xyz";

    private AuthenticationWebFilter filter;
    private McpAuthenticator authenticator;
    private WebFilterChain chain;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authenticator = mock(McpAuthenticator.class);
        objectMapper = new ObjectMapper();
        filter = new AuthenticationWebFilter(authenticator, objectMapper, "/pf-devkit/ai-plugin/mcp");

        // chain 默认放行
        chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    // ==================== 公开路径放行 ====================

    @Nested
    @DisplayName("公开路径 — 无需认证直接放行")
    class PublicPathsTest {

        @Test
        @DisplayName("/health 不经过认证")
        void filter_shouldPassThrough_whenHealthPath() {
            when(authenticator.isEnabled()).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/health").build());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
            verify(authenticator, never()).authenticate(any());
        }

        @Test
        @DisplayName("/ready 不经过认证")
        void filter_shouldPassThrough_whenReadyPath() {
            when(authenticator.isEnabled()).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/ready").build());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
            verify(authenticator, never()).authenticate(any());
        }

        @Test
        @DisplayName("/pf-devkit/ai-plugin/mcp/health 带前缀时仍无需认证（base-path 剥离生效）")
        void filter_shouldPassThrough_whenHealthPathHasBasePathPrefix() {
            when(authenticator.isEnabled()).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/pf-devkit/ai-plugin/mcp/health").build());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
            verify(authenticator, never()).authenticate(any());
        }

        @Test
        @DisplayName("/pf-devkit/ai-plugin/mcp/ready 带前缀时仍无需认证")
        void filter_shouldPassThrough_whenReadyPathHasBasePathPrefix() {
            when(authenticator.isEnabled()).thenReturn(true);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/pf-devkit/ai-plugin/mcp/ready").build());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }
    }

    // ==================== 默认拒绝模型 ====================

    @Nested
    @DisplayName("默认拒绝 — 非公开路径需要认证")
    class DefaultDenyTest {

        @Test
        @DisplayName("/sse 需要认证")
        void filter_shouldRequireAuth_whenSsePath() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(
                    McpAuthenticator.AuthResult.unauthorized("Missing token"));
            when(authenticator.scheme()).thenReturn("bearer-token");

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/sse").build());

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verify(chain, never()).filter(exchange);
        }

        @Test
        @DisplayName("/message 需要认证")
        void filter_shouldRequireAuth_whenMessagePath() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(
                    McpAuthenticator.AuthResult.unauthorized("Missing token"));
            when(authenticator.scheme()).thenReturn("bearer-token");

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("/message").build());

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verify(chain, never()).filter(exchange);
        }

        @Test
        @DisplayName("/status 需要认证（运维敏感信息）")
        void filter_shouldRequireAuth_whenStatusPath() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(
                    McpAuthenticator.AuthResult.unauthorized("Missing token"));
            when(authenticator.scheme()).thenReturn("bearer-token");

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/status").build());

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verify(chain, never()).filter(exchange);
        }

        @Test
        @DisplayName("未知新路径默认需要认证（安全默认）")
        void filter_shouldRequireAuth_whenUnknownPath() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(
                    McpAuthenticator.AuthResult.unauthorized("No token"));
            when(authenticator.scheme()).thenReturn("bearer-token");

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/events").build());

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verify(chain, never()).filter(exchange);
        }
    }

    // ==================== 认证器禁用（开发模式） ====================

    @Nested
    @DisplayName("开发模式 — 认证器未启用时所有路径放行")
    class DevModeTest {

        @Test
        @DisplayName("认证器禁用时 /sse 直接放行")
        void filter_shouldPassThrough_whenAuthenticatorDisabled() {
            when(authenticator.isEnabled()).thenReturn(false);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/sse").build());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
            verify(authenticator, never()).authenticate(any());
        }
    }

    // ==================== 认证通过 ====================

    @Nested
    @DisplayName("认证通过 — 放行请求")
    class AuthPassTest {

        @Test
        @DisplayName("有效 token 放行 /sse")
        void filter_shouldPassThrough_whenAuthPasses() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(null);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/sse")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                            .build());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }
    }

    // ==================== 错误响应格式 ====================

    @Nested
    @DisplayName("错误响应 — JSON-RPC 格式")
    class ErrorResponseTest {

        @Test
        @DisplayName("401 响应包含 JSON-RPC error 结构 + application/json Content-Type")
        void filter_shouldReturnJsonRpcError_whenUnauthorized() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(
                    McpAuthenticator.AuthResult.unauthorized("Token expired"));
            when(authenticator.scheme()).thenReturn("bearer-token");

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/sse").build());

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            assertEquals("application/json",
                    exchange.getResponse().getHeaders().getContentType().toString());
        }

        @Test
        @DisplayName("403 响应正确设置 HTTP 状态码")
        void filter_shouldReturn403_whenForbidden() {
            when(authenticator.isEnabled()).thenReturn(true);
            when(authenticator.authenticate(any())).thenReturn(
                    McpAuthenticator.AuthResult.forbidden("Insufficient permissions"));
            when(authenticator.scheme()).thenReturn("bearer-token");

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/message").build());

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        }
    }
}
