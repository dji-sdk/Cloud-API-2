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

package com.dji.fh.feature.media;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 媒体预签名 URL 缓存 — 避免 {@link MediaProxyController} 对同一文件反复回源 SDK。
 *
 * <p>必要性：{@code <video>} 拖动进度会连续发出多个 Range 请求，若每个请求都调用一次
 * {@link MediaService#getDownloadUrl(long)}，SDK 侧会产生大量无谓调用甚至触发限流。
 *
 * <p>与 {@code com.dji.fh.feature.terra.MediaUrlCache} 语义一致 —— 但 feature 模块之间
 * 禁止相互 import，且 {@code kernel/} 不可改动，无公共落点，故本模块自建一份。
 *
 * <p>键格式约定：{@code content:{fileId}}。
 *
 * @author DJI DevKit Team
 */
public final class MediaUrlCache {

    /** 默认缓存 TTL（秒）：50 分钟，对应 SDK 预签名 URL 默认 1 小时有效期，留 10 分钟余量. */
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
     * @param ttlSeconds 默认缓存 TTL 秒数
     */
    public MediaUrlCache(final long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 使用默认 TTL 存入缓存.
     *
     * @param key 缓存键（如 {@code content:123}）
     * @param url SDK 返回的预签名 URL
     */
    public void put(final String key, final String url) {
        put(key, url, ttlSeconds);
    }

    /**
     * 使用指定 TTL 存入缓存.
     *
     * <p>{@code ttlSeconds <= 0} 表示该 URL 已临近过期，直接放弃缓存。
     *
     * @param key        缓存键
     * @param url        SDK 返回的预签名 URL
     * @param ttlSeconds 本条目的缓存时长（秒）
     */
    public void put(final String key, final String url, final long ttlSeconds) {
        if (key == null || url == null || url.isBlank() || ttlSeconds <= 0) {
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
     * @return 预签名 URL；缓存 miss 或已过期时返回 null
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
     * @param url       预签名 URL
     * @param expiresAt 过期时间
     */
    private record CacheEntry(String url, Instant expiresAt) { }
}
