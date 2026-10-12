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

package com.dji.fh.feature.terra.spi.sdk;

import com.dji.fh.feature.terra.dto.FolderDto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 媒体文件夹列表 TTL 缓存。
 *
 * <p>{@link SdkTerraService#listFolders()} 需遍历已有模型的重建设置（N+1 次 SDK 调用），
 * 单次聚合开销大。文件夹列表变更频率低，用短 TTL 缓存把 N 次调用摊薄到首次请求。
 *
 * <p>使用 {@link ConcurrentHashMap} 实现，不引入额外依赖；写入时惰性清理过期条目。
 * 单 key 设计：平台无分页文件夹概念，整个列表作为一个条目缓存。
 *
 * @author DJI DevKit Team
 */
public final class FolderListCache {

    /** 默认 TTL：10 分钟，兼顾新鲜度与 SDK 调用开销。 */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /** 唯一缓存 key（单列表缓存）。 */
    private static final String CACHE_KEY = "folders";

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    /**
     * 使用默认 TTL 构造。
     */
    public FolderListCache() {
        this(DEFAULT_TTL, Clock.systemUTC());
    }

    /**
     * 使用自定义 TTL 构造（测试用）。
     *
     * @param ttl    缓存 TTL
     * @param clock  时间源（测试可注入）
     */
    public FolderListCache(final Duration ttl, final Clock clock) {
        this.ttl = java.util.Objects.requireNonNull(ttl, "ttl 不能为空");
        this.clock = java.util.Objects.requireNonNull(clock, "clock 不能为空");
    }

    /**
     * 读取缓存的文件夹列表。
     *
     * @return 命中返回列表；miss 或已过期返回 {@link Optional#empty()}
     */
    public Optional<List<FolderDto>> get() {
        CacheEntry entry = cache.get(CACHE_KEY);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now(clock).isAfter(entry.expiresAt)) {
            cache.remove(CACHE_KEY);
            return Optional.empty();
        }
        return Optional.of(List.copyOf(entry.folders));
    }

    /**
     * 写入文件夹列表。
     *
     * @param folders 文件夹列表（不可为 null）
     */
    public void put(final List<FolderDto> folders) {
        java.util.Objects.requireNonNull(folders, "folders 不能为空");
        cache.put(CACHE_KEY, new CacheEntry(
                List.copyOf(folders),
                Instant.now(clock).plus(ttl)));
    }

    /**
     * 主动失效缓存（重建任务创建后文件夹归属可能变化，供调用方按需刷新）。
     */
    public void invalidate() {
        cache.remove(CACHE_KEY);
    }

    /**
     * 缓存条目。
     *
     * @param folders   文件夹列表（不可变副本）
     * @param expiresAt 过期时间
     */
    private record CacheEntry(List<FolderDto> folders, Instant expiresAt) { }
}
