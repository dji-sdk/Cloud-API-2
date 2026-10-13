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
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RateLimitWebFilter 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class RateLimitWebFilterTest {

    private RateLimitWebFilter filter;

    @BeforeEach
    void setUp() {
        // 默认：trustProxy=false，100 req/60s，maxKeys=10000，base-path=/pf-devkit/ai-plugin/mcp
        filter = new RateLimitWebFilter(new ObjectMapper(), "/pf-devkit/ai-plugin/mcp");
    }

    @Test
    @DisplayName("未超限时请求正常放行")
    void filter_shouldPassThrough_whenUnderLimit() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                .remoteAddress(new InetSocketAddress("192.168.1.1", 12345))
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

    @Test
    @DisplayName("健康探针路径不限流")
    void filter_shouldSkipHealthEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/health")
                .remoteAddress(new InetSocketAddress("192.168.1.1", 12345))
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

    @Test
    @DisplayName("超过限额后返回 429")
    void filter_shouldReturn429_whenLimitExceeded() {
        WebFilterChain chain = ex -> Mono.empty();

        for (int i = 0; i < 100; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                    .remoteAddress(new InetSocketAddress("10.0.0.1", 12345))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            filter.filter(exchange, chain).block();
        }

        MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exchange.getResponse().getHeaders().getFirst("Retry-After")).isNotNull();
    }

    @Test
    @DisplayName("不同 IP 独立计数")
    void filter_shouldTrackIpsIndependently() {
        WebFilterChain chain = ex -> Mono.empty();

        for (int i = 0; i < 100; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                    .remoteAddress(new InetSocketAddress("10.0.0.2", 12345))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            filter.filter(exchange, chain).block();
        }

        MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                .remoteAddress(new InetSocketAddress("10.0.0.3", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicInteger chainCalled = new AtomicInteger(0);
        WebFilterChain chain2 = ex -> {
            chainCalled.incrementAndGet();
            return Mono.empty();
        };

        filter.filter(exchange, chain2).block();

        assertThat(chainCalled.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("trustProxy=true 时使用 X-Forwarded-For 提取客户端 IP")
    void filter_shouldUseXForwardedFor_whenTrustProxyEnabled() {
        // 显式启用 trustProxy，限额设为 5 以加速测试
        RateLimitWebFilter proxyFilter = new RateLimitWebFilter(
                new ObjectMapper(), 5, 60, true, 10000, "/pf-devkit/ai-plugin/mcp");
        WebFilterChain chain = ex -> Mono.empty();

        // XFF=203.0.113.50 发 5 次（用完配额），remoteAddress 为不同的代理 IP
        for (int i = 0; i < 5; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                    .header("X-Forwarded-For", "203.0.113.50, 70.41.3.18")
                    .remoteAddress(new InetSocketAddress("10.0.0." + i, 12345))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            proxyFilter.filter(exchange, chain).block();
        }

        // 第 6 次相同 XFF 应被限流（即使 remoteAddress 不同）
        MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                .header("X-Forwarded-For", "203.0.113.50, 70.41.3.18")
                .remoteAddress(new InetSocketAddress("10.0.0.99", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        proxyFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("trustProxy=false 时忽略 X-Forwarded-For，按 remoteAddress 计数")
    void filter_shouldIgnoreXForwardedFor_whenTrustProxyDisabled() {
        // trustProxy=false（默认），限额设为 5
        RateLimitWebFilter noProxyFilter = new RateLimitWebFilter(
                new ObjectMapper(), 5, 60, false, 10000, "/pf-devkit/ai-plugin/mcp");
        WebFilterChain chain = ex -> Mono.empty();

        // 相同 XFF 但不同 remoteAddress → 不应共享计数
        for (int i = 0; i < 5; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                    .header("X-Forwarded-For", "203.0.113.50")
                    .remoteAddress(new InetSocketAddress("10.0.0." + i, 12345))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            noProxyFilter.filter(exchange, chain).block();
        }

        // 第 6 次，不同 remoteAddress → XFF 被忽略 → 新 IP → 应放行
        MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                .header("X-Forwarded-For", "203.0.113.50")
                .remoteAddress(new InetSocketAddress("10.0.0.99", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicInteger chainCalled = new AtomicInteger(0);
        WebFilterChain chain2 = ex -> {
            chainCalled.incrementAndGet();
            return Mono.empty();
        };

        noProxyFilter.filter(exchange, chain2).block();

        assertThat(chainCalled.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("超过 maxKeys 时拒绝新 IP（防 OOM）")
    void filter_shouldReject_whenMaxKeysExhausted() {
        // maxKeys=3，限额 100
        RateLimitWebFilter smallFilter = new RateLimitWebFilter(
                new ObjectMapper(), 100, 60, false, 3, "/pf-devkit/ai-plugin/mcp");
        WebFilterChain chain = ex -> Mono.empty();

        // 注册 3 个不同 IP
        for (int i = 1; i <= 3; i++) {
            MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                    .remoteAddress(new InetSocketAddress("10.0.0." + i, 12345))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            smallFilter.filter(exchange, chain).block();
        }

        // 第 4 个新 IP → 超过 maxKeys → 应返回 429
        MockServerHttpRequest request = MockServerHttpRequest.get("/message")
                .remoteAddress(new InetSocketAddress("10.0.0.4", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        smallFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("带 base-path 前缀的 /pf-devkit/ai-plugin/mcp/health 仍免限流")
    void filter_shouldExemptHealth_whenPathHasBasePathPrefix() {
        RateLimitWebFilter prefixedFilter = new RateLimitWebFilter(
                new ObjectMapper(), 1, 60, false, 100, "/pf-devkit/ai-plugin/mcp");
        WebFilterChain chain = ex -> Mono.empty();

        MockServerHttpRequest request = MockServerHttpRequest.get("/pf-devkit/ai-plugin/mcp/health")
                .remoteAddress(new InetSocketAddress("192.168.1.1", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        prefixedFilter.filter(exchange, chain).block();

        // 探针路径不限流，直接放行到 chain
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }
}
