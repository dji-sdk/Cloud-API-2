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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityHeadersWebFilter 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SecurityHeadersWebFilterTest {

    private SecurityHeadersWebFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersWebFilter();
    }

    @Test
    void filter_shouldAddSecurityHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/message").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(headers.getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(headers.getFirst("X-XSS-Protection")).isEqualTo("1; mode=block");
        assertThat(headers.getFirst("Strict-Transport-Security"))
                .isEqualTo("max-age=31536000; includeSubDomains");
        assertThat(headers.getFirst("Cache-Control")).contains("no-store");
        assertThat(headers.getFirst("Content-Security-Policy")).contains("default-src 'self'");
        assertThat(headers.getFirst("Permissions-Policy")).contains("camera=()");
    }

    @Test
    void filter_shouldNotAddCorsHeaders_whenNoOrigin() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/sse").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    void filter_shouldNotAllowUnknownOrigin() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                .header(HttpHeaders.ORIGIN, "https://evil.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    void filter_shouldReturn204ForPreflight() {
        MockServerHttpRequest request = MockServerHttpRequest.options("/sse")
                .header(HttpHeaders.ORIGIN, "https://unknown.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicInteger chainCalled = new AtomicInteger(0);
        WebFilterChain chain = ex -> {
            chainCalled.incrementAndGet();
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(chainCalled.get()).isEqualTo(0);
    }

    @Test
    void filter_shouldCallChainForNonOptionsRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/message")
                .header(HttpHeaders.ORIGIN, "https://unknown.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicInteger chainCalled = new AtomicInteger(0);
        WebFilterChain chain = ex -> {
            chainCalled.incrementAndGet();
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chainCalled.get()).isEqualTo(1);
    }
}
