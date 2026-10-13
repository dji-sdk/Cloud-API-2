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

package com.dji.fh.feature.terra;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 媒体 URL 缓存 — 存储 SDK 返回的内网预签名 URL，供媒体代理端点查询。
 *
 * <p>SDK 预签名 URL 通常有效期 1 小时，本缓存默认 50 分钟 TTL（留 10 分钟余量）。
 * 缓存 miss 时代理端点会 fallback 到 {@link TerraService} 重新获取 URL。
 *
 * <p>使用 {@link ConcurrentHashMap} 实现，不引入额外依赖。
 * 键格式约定：{@code thumb:{fileId}}、{@code preview:{fileId}}、
 * {@code image:{taskId}:{imageId}}、{@code report:{taskId}}。
 *
 * @author DJI DevKit Team
 */
public final class MediaUrlCache {

    /** 默认缓存 TTL（秒）：50 分钟. */
    private static final long DEFAULT_TTL_SECONDS = 50L * 60;

    /** 清理阈值 — 缓存条目超过此数量时触发过期条目清理. */
    private static final int CLEANUP_THRESHOLD = 500;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    /**
     * 使用默认 TTL 构造.
     */
    public MediaUrlCache() {
        this(DEFAULT_TTL_SECONDS);
    }

    /**
     * 使用自定义 TTL 构造（测试用）.
     *
     * @param ttlSeconds 缓存 TTL 秒数
     */
    public MediaUrlCache(final long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 存入缓存.
     *
     * @param key 缓存键（如 {@code thumb:123}）
     * @param url 原始 SDK URL
     */
    public void put(final String key, final String url) {
        if (key == null || url == null || url.isBlank()) {
            return;
        }
        cache.put(key, new CacheEntry(url, Instant.now().plusSeconds(ttlSeconds)));
        if (cache.size() > CLEANUP_THRESHOLD) {
            evictExpired();
        }
    }

    /**
     * 获取缓存中的 URL.
     *
     * @param key 缓存键
     * @return 原始 URL；缓存 miss 或已过期时返回 null
     */
    public String get(final String key) {
        if (key == null) {
            return null;
        }
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            cache.remove(key);
            return null;
        }
        return entry.url;
    }

    /**
     * 清理所有过期条目.
     */
    private void evictExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            if (now.isAfter(it.next().getValue().expiresAt)) {
                it.remove();
            }
        }
    }

    /**
     * 返回当前缓存大小（测试用）.
     *
     * @return 缓存条目数量
     */
    int size() {
        return cache.size();
    }

    /**
     * 缓存条目.
     *
     * @param url       原始 URL
     * @param expiresAt 过期时间
     */
    private record CacheEntry(String url, Instant expiresAt) { }
}
