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

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.terra.dto.FolderDto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * {@link FolderListCache} 单元测试 — 覆盖 TTL 命中/过期/失效/不可变语义.
 *
 * @author DJI DevKit Team
 */
class FolderListCacheTest {

    /** 固定起始时间，便于断言过期行为. */
    private static final Instant NOW = Instant.parse("2026-07-31T00:00:00Z");

    @Test
    void get_shouldReturnEmpty_whenCacheMiss() {
        FolderListCache cache = new FolderListCache(Duration.ofMinutes(10), Clock.fixed(NOW, ZoneOffset.UTC));

        Optional<List<FolderDto>> result = cache.get();

        assertThat(result).isEmpty();
    }

    @Test
    void get_shouldReturnCachedList_whenWithinTtl() {
        FolderListCache cache = new FolderListCache(Duration.ofMinutes(10), Clock.fixed(NOW, ZoneOffset.UTC));
        List<FolderDto> folders = List.of(
                new FolderDto(1, "uuid-1", "folder-A"),
                new FolderDto(2, "uuid-2", "folder-B"));
        cache.put(folders);

        Optional<List<FolderDto>> result = cache.get();

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).extracting(FolderDto::folderUuid)
                .containsExactly("uuid-1", "uuid-2");
    }

    @Test
    void get_shouldReturnEmpty_whenExpired() {
        // TTL 10 分钟，写入后推进 11 分钟 → 应 miss
        FolderListCache cache = new FolderListCache(Duration.ofMinutes(10), Clock.fixed(NOW, ZoneOffset.UTC));
        cache.put(List.of(new FolderDto(1, "uuid-1", "folder-A")));

        // 推进时间需可变 Clock；用 systemUTC 不可控，故改用 mutable
        MutableClock clock = new MutableClock(NOW);
        FolderListCache timedCache = new FolderListCache(Duration.ofMinutes(10), clock);
        timedCache.put(List.of(new FolderDto(1, "uuid-1", "folder-A")));
        assertThat(timedCache.get()).isPresent();

        clock.advance(Duration.ofMinutes(11));
        assertThat(timedCache.get()).isEmpty();
    }

    @Test
    void put_shouldReturnImmutableCopy() {
        FolderListCache cache = new FolderListCache(Duration.ofMinutes(10), Clock.fixed(NOW, ZoneOffset.UTC));
        List<FolderDto> source = new java.util.ArrayList<>(List.of(
                new FolderDto(1, "uuid-1", "folder-A")));
        cache.put(source);

        // 修改源列表不应影响缓存内容
        source.add(new FolderDto(2, "uuid-2", "folder-B"));

        Optional<List<FolderDto>> result = cache.get();
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        // 缓存返回值同样不可变
        assertThatThrownByImmutable(() -> result.get().add(new FolderDto(3, "uuid-3", "folder-C")));
    }

    @Test
    void invalidate_shouldClearCache() {
        FolderListCache cache = new FolderListCache(Duration.ofMinutes(10), Clock.fixed(NOW, ZoneOffset.UTC));
        cache.put(List.of(new FolderDto(1, "uuid-1", "folder-A")));
        assertThat(cache.get()).isPresent();

        cache.invalidate();

        assertThat(cache.get()).isEmpty();
    }

    /** 断言列表不可变 — 调用即应抛 UnsupportedOperationException. */
    private static void assertThatThrownByImmutable(final Runnable action) {
        try {
            action.run();
            throw new AssertionError("期望抛 UnsupportedOperationException，但未抛出");
        } catch (UnsupportedOperationException expected) {
            // 预期行为
        }
    }

    /** 可推进时间的 Clock，用于测试 TTL 过期. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(final Instant start) {
            this.instant = start;
        }

        void advance(final Duration d) {
            this.instant = instant.plus(d);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final java.time.ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
