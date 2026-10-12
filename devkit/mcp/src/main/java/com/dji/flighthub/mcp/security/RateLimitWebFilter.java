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
import org.springframework.beans.factory.annotation.Autowired;
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 滑动窗口限流过滤器 — 防止接口被刷。
 *
 * <p>基于客户端 IP 进行限流。每个 IP 在固定时间窗口内的请求数不得超过阈值。
 * 超出限制时返回 HTTP 429 Too Many Requests。
 *
 * <p>配置项通过环境变量注入：
 * <ul>
 *   <li>FH_RATE_LIMIT_MAX — 单 IP 每窗口最大请求数（默认 100）</li>
 *   <li>FH_RATE_LIMIT_WINDOW_SECONDS — 窗口时长秒数（默认 60）</li>
 *   <li>FH_RATE_LIMIT_TRUST_PROXY — 是否信任代理头（X-Forwarded-For/X-Real-IP），默认 false</li>
 *   <li>FH_RATE_LIMIT_MAX_KEYS — 最大跟踪 IP 数量，防止 OOM（默认 10000）</li>
 * </ul>
 *
 * <p>仅在 SSE profile 下激活（stdio 模式为本地单用户，无需限流）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Profile("sse")
@Order(0)
public class RateLimitWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitWebFilter.class);

    /** 单 IP 每窗口允许最大请求数 */
    private final int maxRequests;

    /** 滑动窗口时长（毫秒） */
    private final long windowMs;

    /** 是否信任代理头提取客户端 IP（仅在受信反向代理后部署时启用） */
    private final boolean trustProxy;

    /** 最大跟踪 IP 数量，防止伪造 XFF 导致 OOM */
    private final int maxKeys;

    /** 对外 base-path 前缀，用于探针豁免匹配前剥离 */
    private final String basePath;

    /** IP → 窗口计数器 */
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    /** 清理周期（每 256 次请求触发一次过期 entry 清理） */
    private static final int CLEANUP_INTERVAL = 256;
    private final AtomicLong requestCount = new AtomicLong(0);

    private final ObjectMapper objectMapper;

    @Autowired
    public RateLimitWebFilter(ObjectMapper objectMapper,
                              @Value("${spring.webflux.base-path:}") String basePath) {
        this.objectMapper = objectMapper;
        this.maxRequests = getEnvInt("FH_RATE_LIMIT_MAX", 100);
        this.windowMs = getEnvInt("FH_RATE_LIMIT_WINDOW_SECONDS", 60) * 1000L;
        this.trustProxy = "true".equalsIgnoreCase(System.getenv("FH_RATE_LIMIT_TRUST_PROXY"));
        this.maxKeys = getEnvInt("FH_RATE_LIMIT_MAX_KEYS", 10000);
        this.basePath = basePath;
    }

    /**
     * 测试专用构造函数 — 允许显式指定 trustProxy 等参数，避免依赖环境变量。
     */
    RateLimitWebFilter(ObjectMapper objectMapper, int maxRequests, long windowSeconds,
                       boolean trustProxy, int maxKeys, String basePath) {
        this.objectMapper = objectMapper;
        this.maxRequests = maxRequests;
        this.windowMs = windowSeconds * 1000L;
        this.trustProxy = trustProxy;
        this.maxKeys = maxKeys;
        this.basePath = basePath;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 健康探针不限流（剥离 base-path 前缀后匹配，兼容 filter 内路径含/不含前缀两种情况）
        String normalizedPath = McpPathNormalizer.stripBasePath(basePath, path);
        if ("/health".equals(normalizedPath) || "/ready".equals(normalizedPath)) {
            return chain.filter(exchange);
        }

        String clientIp = extractClientIp(request);
        if (clientIp == null) {
            return chain.filter(exchange);
        }

        // 周期性清理过期 entry
        if (requestCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            evictExpired();
        }

        // 容量保护：超过 maxKeys 时拒绝新 IP 注册（防止伪造 XFF 导致 OOM）
        WindowCounter counter = counters.get(clientIp);
        if (counter == null) {
            if (counters.size() >= maxKeys) {
                log.warn("Rate limit key space exhausted ({} keys), rejecting new IP: {}", maxKeys, clientIp);
                return writeRateLimitResponse(exchange);
            }
            counter = counters.computeIfAbsent(clientIp, k -> new WindowCounter());
        }

        if (!counter.tryAcquire(maxRequests, windowMs)) {
            log.warn("Rate limit exceeded: ip={}, path={}, limit={}/{} s",
                    clientIp, path, maxRequests, windowMs / 1000);
            return writeRateLimitResponse(exchange);
        }

        return chain.filter(exchange);
    }

    /**
     * 提取客户端 IP。
     *
     * <p>安全策略：默认仅使用 TCP 连接层 remoteAddress（不可伪造）。
     * 仅当 FH_RATE_LIMIT_TRUST_PROXY=true 时才读取代理头（部署在受信反向代理后方时启用）。
     */
    private String extractClientIp(ServerHttpRequest request) {
        if (trustProxy) {
            String xff = request.getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            String realIp = request.getHeaders().getFirst("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            InetAddress address = remoteAddress.getAddress();
            return address != null ? address.getHostAddress() : null;
        }
        return null;
    }

    private Mono<Void> writeRateLimitResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", String.valueOf(windowMs / 1000));

        Map<String, Object> body = Map.of(
                "jsonrpc", "2.0",
                "error", Map.of(
                        "code", -32429,
                        "message", "Rate limit exceeded",
                        "data", "Too many requests. Please retry after " + (windowMs / 1000) + " seconds."
                )
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] fallback = "{\"error\":\"rate_limit_exceeded\"}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(fallback);
            return response.writeWith(Mono.just(buffer));
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(entry -> entry.getValue().isExpired(now, windowMs));
    }

    private static int getEnvInt(String key, int defaultVal) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 固定窗口计数器。窗口过期后自动重置。
     */
    static class WindowCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean tryAcquire(int max, long windowMs) {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMs) {
                synchronized (this) {
                    if (now - windowStart > windowMs) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            return count.incrementAndGet() <= max;
        }

        boolean isExpired(long now, long windowMs) {
            return now - windowStart > windowMs * 2;
        }
    }
}
