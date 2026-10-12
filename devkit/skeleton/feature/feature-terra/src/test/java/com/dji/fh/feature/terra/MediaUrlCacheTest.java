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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MediaUrlCache 单元测试。
 *
 * @author DJI DevKit Team
 */
class MediaUrlCacheTest {

    @Test
    void put_thenGet_shouldReturnUrl() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put("thumb:100", "http://minio/thumb.webp");

        assertThat(cache.get("thumb:100")).isEqualTo("http://minio/thumb.webp");
    }

    @Test
    void get_shouldReturnNull_whenKeyNotPresent() {
        MediaUrlCache cache = new MediaUrlCache();

        assertThat(cache.get("thumb:999")).isNull();
    }

    @Test
    void get_shouldReturnNull_whenKeyIsNull() {
        MediaUrlCache cache = new MediaUrlCache();

        assertThat(cache.get(null)).isNull();
    }

    @Test
    void put_shouldIgnoreNullKey() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put(null, "http://minio/thumb.webp");

        assertThat(cache.size()).isZero();
    }

    @Test
    void put_shouldIgnoreNullUrl() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put("thumb:100", null);

        assertThat(cache.size()).isZero();
    }

    @Test
    void put_shouldIgnoreBlankUrl() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put("thumb:100", "   ");

        assertThat(cache.size()).isZero();
    }

    @Test
    void get_shouldReturnNull_whenExpired() {
        // TTL = 0 秒，立即过期
        MediaUrlCache cache = new MediaUrlCache(0);

        cache.put("thumb:100", "http://minio/thumb.webp");

        assertThat(cache.get("thumb:100")).isNull();
    }

    @Test
    void put_shouldOverwriteExistingEntry() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put("thumb:100", "http://old-url");
        cache.put("thumb:100", "http://new-url");

        assertThat(cache.get("thumb:100")).isEqualTo("http://new-url");
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void size_shouldReflectEntryCount() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put("thumb:1", "http://a");
        cache.put("thumb:2", "http://b");
        cache.put("preview:1", "http://c");

        assertThat(cache.size()).isEqualTo(3);
    }
}
