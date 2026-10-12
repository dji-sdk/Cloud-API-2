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

package com.dji.flighthub.mcp.idempotency;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 幂等缓存 — 防止同一 JSON-RPC 请求被重复执行。
 *
 * <p>缓存 key 格式：{clientId}:{toolName}:{requestId}
 * <p>仅缓存成功结果（失败允许重试）。
 * <p>TTL 由 {@code flighthub.idempotency.ttl-seconds} 控制（默认 300s）。
 * <p>容量上限由 {@code flighthub.idempotency.max-size} 控制（默认 10000），
 * 超出时拒绝写入（降级为无幂等保护），防止 OOM。
 *
 * <p>并发安全：使用 sentinel 模式（PENDING_MARKER）防止 TOCTOU 竞态：
 * <ol>
 *   <li>{@link #getOrMarkPending(String)}：原子性地查缓存或抢占槽位</li>
 *   <li>执行 tool</li>
 *   <li>{@link #put(String, Map)}：成功则替换 PENDING → 真实结果</li>
 *   <li>{@link #removePending(String)}：失败则移除占位</li>
 * </ol>
 *
 * <p>适用场景：
 * <ul>
 *   <li>网络重传：client 超时后重发相同 id 的请求</li>
 *   <li>恶意重放：攻击者截获请求后重放</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class IdempotencyCache {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCache.class);
    private static final long CLEANUP_INTERVAL_SECONDS = 60;

    /** 占位标记：表示该 key 正在执行中，尚未产生真实结果 */
    private static final Map<String, Object> PENDING_MARKER = Collections.emptyMap();

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner;
    private final int ttlSeconds;
    private final int maxSize;
    private final boolean enabled;

    public IdempotencyCache(FlightHubProperties properties) {
        this.ttlSeconds = properties.getIdempotency().getTtlSeconds();
        this.maxSize = properties.getIdempotency().getMaxSize();
        this.enabled = properties.getIdempotency().isEnabled();

        if (enabled) {
            this.cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "idempotency-cleaner");
                t.setDaemon(true);
                return t;
            });
            this.cleaner.scheduleAtFixedRate(this::evictExpired,
                    CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
            log.info("IdempotencyCache 启动: ttl={}s, maxSize={}, cleanup_interval={}s",
                    ttlSeconds, maxSize, CLEANUP_INTERVAL_SECONDS);
        } else {
            this.cleaner = null;
            log.info("IdempotencyCache 已禁用");
        }
    }

    /**
     * 原子性查缓存或抢占槽位（sentinel 模式）。
     *
     * <p>如果 key 已有真实结果（非 PENDING），返回缓存结果。
     * <p>如果 key 不存在，插入 PENDING_MARKER 占位并返回 null（表示需要执行 tool）。
     * <p>如果 key 为 PENDING（另一线程正在执行），返回 null（允许并行执行，
     * 第二个线程的结果会覆盖占位——语义上可接受，因为请求内容相同）。
     *
     * @param key 幂等 key（clientId:toolName:requestId）
     * @return 缓存的真实结果，未命中时返回 null
     */
    public Map<String, Object> getOrMarkPending(String key) {
        if (!enabled || key == null) {
            return null;
        }

        CacheEntry existing = cache.get(key);
        if (existing != null) {
            if (existing.isExpired(ttlSeconds)) {
                cache.remove(key);
            } else if (existing.result() != PENDING_MARKER) {
                log.debug("幂等缓存命中: key={}", key);
                return existing.result();
            }
            // PENDING 状态：另一线程正在执行，放行当前线程（worst case 执行两次，但不会丢结果）
        }

        // 容量检查：超出上限不插入占位，降级为无幂等保护
        if (cache.size() >= maxSize) {
            log.warn("幂等缓存已满(size={}), 放弃占位: key={}", cache.size(), key);
            return null;
        }

        // 抢占槽位
        cache.putIfAbsent(key, new CacheEntry(PENDING_MARKER, System.currentTimeMillis()));
        return null;
    }

    /**
     * 查询缓存（向后兼容，简单查询不抢占）。
     *
     * @param key 幂等 key（clientId:toolName:requestId）
     * @return 缓存的工具执行结果，未命中或已过期返回 null
     */
    public Map<String, Object> get(String key) {
        if (!enabled || key == null) {
            return null;
        }
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired(ttlSeconds)) {
            cache.remove(key);
            return null;
        }
        if (entry.result() == PENDING_MARKER) {
            return null;
        }
        log.debug("幂等缓存命中: key={}", key);
        return entry.result();
    }

    /**
     * 写入缓存（仅缓存成功结果）。
     * 替换 PENDING_MARKER 为真实结果，或直接写入。
     *
     * @param key    幂等 key
     * @param result 工具执行结果
     */
    public void put(String key, Map<String, Object> result) {
        if (!enabled || key == null) {
            return;
        }
        // 容量检查：超出上限拒绝写入，避免 OOM
        if (cache.size() >= maxSize) {
            log.warn("幂等缓存已满(size={}), 拒绝写入: key={}", cache.size(), key);
            return;
        }
        cache.put(key, new CacheEntry(result, System.currentTimeMillis()));
        log.debug("幂等缓存写入: key={}", key);
    }

    /**
     * 移除 PENDING 占位（tool 执行失败时调用，允许后续重试）。
     *
     * @param key 幂等 key
     */
    public void removePending(String key) {
        if (!enabled || key == null) {
            return;
        }
        cache.computeIfPresent(key, (k, entry) ->
                entry.result() == PENDING_MARKER ? null : entry);
    }

    /**
     * 获取当前缓存大小（用于监控/测试）。
     */
    public int size() {
        return cache.size();
    }

    /**
     * 是否启用。
     */
    public boolean isEnabled() {
        return enabled;
    }

    @PreDestroy
    void shutdown() {
        if (cleaner != null) {
            cleaner.shutdownNow();
        }
    }

    /**
     * 清理过期条目（包括过期的 PENDING 占位）。
     */
    private void evictExpired() {
        int before = cache.size();
        cache.entrySet().removeIf(e -> e.getValue().isExpired(ttlSeconds));
        int evicted = before - cache.size();
        if (evicted > 0) {
            log.debug("幂等缓存清理: evicted={}, remaining={}", evicted, cache.size());
        }
    }

    /**
     * 缓存条目。
     */
    private record CacheEntry(Map<String, Object> result, long createdAtMs) {
        boolean isExpired(int ttlSeconds) {
            return System.currentTimeMillis() - createdAtMs > ttlSeconds * 1000L;
        }
    }
}
