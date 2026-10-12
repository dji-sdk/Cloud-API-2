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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MediaUrlCache} 单元测试。
 *
 * @author DJI DevKit Team
 */
class MediaUrlCacheTest {

    private static final String KEY = "content:1";
    private static final String URL = "http://storage.internal:30802/a/b.mp4?sig=x";

    @Test
    @DisplayName("存入后应能取回")
    void put_shouldBeRetrievable_whenValid() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put(KEY, URL);

        assertEquals(URL, cache.get(KEY));
    }

    @Test
    @DisplayName("缓存 miss 时应返回 null")
    void get_shouldReturnNull_whenMiss() {
        MediaUrlCache cache = new MediaUrlCache();

        assertNull(cache.get("content:404"));
    }

    @Test
    @DisplayName("key 为 null 时应返回 null 而非抛异常")
    void get_shouldReturnNull_whenKeyNull() {
        MediaUrlCache cache = new MediaUrlCache();

        assertNull(cache.get(null));
    }

    @Test
    @DisplayName("TTL 为 0 或负数时不应写入缓存")
    void put_shouldSkip_whenTtlNonPositive() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put(KEY, URL, 0);
        cache.put(KEY, URL, -5);

        assertEquals(0, cache.size(), "URL 已临近过期，不应缓存");
    }

    @Test
    @DisplayName("URL 为空或空白时不应写入缓存")
    void put_shouldSkip_whenUrlBlank() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put(KEY, null);
        cache.put(KEY, "");

        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("过期条目应返回 null 并被移除")
    void get_shouldReturnNull_whenExpired() throws Exception {
        MediaUrlCache cache = new MediaUrlCache(1);
        cache.put(KEY, URL);

        Thread.sleep(1100L);

        assertNull(cache.get(KEY));
        assertEquals(0, cache.size(), "取到过期条目后应顺手清除");
    }

    @Test
    @DisplayName("同一 key 重复写入应以最后一次为准")
    void put_shouldOverwrite_whenSameKey() {
        MediaUrlCache cache = new MediaUrlCache();

        cache.put(KEY, URL);
        cache.put(KEY, URL + "-v2");

        assertEquals(URL + "-v2", cache.get(KEY));
        assertEquals(1, cache.size());
    }
}
