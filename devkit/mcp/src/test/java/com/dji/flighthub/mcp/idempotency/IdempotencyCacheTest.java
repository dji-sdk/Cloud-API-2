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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdempotencyCache 单元测试 — 验证缓存读写、TTL 过期、disabled 模式、sentinel 模式、max-size 限制。
 */
class IdempotencyCacheTest {

    private IdempotencyCache createCache(boolean enabled, int ttlSeconds) {
        return createCache(enabled, ttlSeconds, 10000);
    }

    private IdempotencyCache createCache(boolean enabled, int ttlSeconds, int maxSize) {
        FlightHubProperties props = new FlightHubProperties();
        FlightHubProperties.Idempotency idempotency = new FlightHubProperties.Idempotency();
        idempotency.setEnabled(enabled);
        idempotency.setTtlSeconds(ttlSeconds);
        idempotency.setMaxSize(maxSize);
        props.setIdempotency(idempotency);
        return new IdempotencyCache(props);
    }

    @Test
    void get_shouldReturnNull_whenKeyNotPresent() {
        IdempotencyCache cache = createCache(true, 300);
        assertNull(cache.get("nonexistent:key"));
    }

    @Test
    void put_thenGet_shouldReturnCachedResult() {
        IdempotencyCache cache = createCache(true, 300);
        Map<String, Object> result = Map.of("success", true, "data", "test");

        cache.put("client1:tool1:req1", result);

        Map<String, Object> cached = cache.get("client1:tool1:req1");
        assertNotNull(cached);
        assertEquals(true, cached.get("success"));
        assertEquals("test", cached.get("data"));
    }

    @Test
    void get_shouldReturnNull_whenDisabled() {
        IdempotencyCache cache = createCache(false, 300);
        cache.put("client1:tool1:req1", Map.of("success", true));

        assertNull(cache.get("client1:tool1:req1"));
    }

    @Test
    void put_shouldDoNothing_whenDisabled() {
        IdempotencyCache cache = createCache(false, 300);
        cache.put("client1:tool1:req1", Map.of("success", true));

        assertEquals(0, cache.size());
    }

    @Test
    void get_shouldReturnNull_whenNullKey() {
        IdempotencyCache cache = createCache(true, 300);
        assertNull(cache.get(null));
    }

    @Test
    void put_shouldDoNothing_whenNullKey() {
        IdempotencyCache cache = createCache(true, 300);
        cache.put(null, Map.of("success", true));
        assertEquals(0, cache.size());
    }

    @Test
    void get_shouldReturnNull_whenExpired() throws InterruptedException {
        // TTL = 1 秒，等待 1.1 秒后应过期
        IdempotencyCache cache = createCache(true, 1);
        cache.put("client1:tool1:req1", Map.of("success", true));

        assertEquals(1, cache.size());

        Thread.sleep(1100);

        assertNull(cache.get("client1:tool1:req1"));
    }

    @Test
    void size_shouldReflectCacheEntries() {
        IdempotencyCache cache = createCache(true, 300);

        assertEquals(0, cache.size());
        cache.put("k1", Map.of("a", 1));
        assertEquals(1, cache.size());
        cache.put("k2", Map.of("b", 2));
        assertEquals(2, cache.size());
    }

    @Test
    void isEnabled_shouldReturnCorrectState() {
        assertTrue(createCache(true, 300).isEnabled());
        assertFalse(createCache(false, 300).isEnabled());
    }

    @Test
    void multiplePuts_sameShouldOverwrite() {
        IdempotencyCache cache = createCache(true, 300);

        cache.put("key", Map.of("version", 1));
        cache.put("key", Map.of("version", 2));

        assertEquals(1, cache.size());
        assertEquals(2, cache.get("key").get("version"));
    }

    // ==================== Sentinel 模式测试 ====================

    @Test
    void getOrMarkPending_shouldReturnNull_whenKeyNotPresent() {
        IdempotencyCache cache = createCache(true, 300);

        Map<String, Object> result = cache.getOrMarkPending("client:tool:1");

        assertNull(result);
        assertEquals(1, cache.size()); // PENDING 占位已插入
    }

    @Test
    void getOrMarkPending_shouldReturnCachedResult_whenExists() {
        IdempotencyCache cache = createCache(true, 300);
        cache.put("client:tool:1", Map.of("success", true, "data", "cached"));

        Map<String, Object> result = cache.getOrMarkPending("client:tool:1");

        assertNotNull(result);
        assertEquals("cached", result.get("data"));
    }

    @Test
    void getOrMarkPending_shouldReturnNull_whenDisabled() {
        IdempotencyCache cache = createCache(false, 300);
        assertNull(cache.getOrMarkPending("key"));
        assertEquals(0, cache.size()); // 未插入占位
    }

    @Test
    void removePending_shouldOnlyRemovePendingEntries() {
        IdempotencyCache cache = createCache(true, 300);

        // 插入一个 pending 占位
        cache.getOrMarkPending("client:tool:pending");
        assertEquals(1, cache.size());

        // removePending 应移除 PENDING 占位
        cache.removePending("client:tool:pending");
        assertEquals(0, cache.size());
    }

    @Test
    void removePending_shouldNotRemoveRealResult() {
        IdempotencyCache cache = createCache(true, 300);

        // 先插入真实结果
        cache.put("client:tool:real", Map.of("success", true));

        // removePending 不应移除真实结果
        cache.removePending("client:tool:real");
        assertEquals(1, cache.size());
        assertNotNull(cache.get("client:tool:real"));
    }

    @Test
    void put_afterPending_shouldReplaceWithRealResult() {
        IdempotencyCache cache = createCache(true, 300);

        // 模拟正常流程：getOrMarkPending → execute → put
        cache.getOrMarkPending("client:tool:1");
        assertNull(cache.get("client:tool:1")); // PENDING 不应被 get() 返回

        cache.put("client:tool:1", Map.of("success", true, "data", "real"));

        Map<String, Object> result = cache.get("client:tool:1");
        assertNotNull(result);
        assertEquals("real", result.get("data"));
    }

    // ==================== Max-size 限制测试 ====================

    @Test
    void put_shouldRejectWhenMaxSizeReached() {
        IdempotencyCache cache = createCache(true, 300, 3);

        cache.put("k1", Map.of("a", 1));
        cache.put("k2", Map.of("b", 2));
        cache.put("k3", Map.of("c", 3));
        assertEquals(3, cache.size());

        // 第 4 个应被拒绝
        cache.put("k4", Map.of("d", 4));
        assertEquals(3, cache.size());
        assertNull(cache.get("k4"));
    }

    @Test
    void getOrMarkPending_shouldNotInsertWhenMaxSizeReached() {
        IdempotencyCache cache = createCache(true, 300, 2);

        cache.put("k1", Map.of("a", 1));
        cache.put("k2", Map.of("b", 2));
        assertEquals(2, cache.size());

        // 超出容量，getOrMarkPending 不应插入占位
        Map<String, Object> result = cache.getOrMarkPending("k3");
        assertNull(result);
        assertEquals(2, cache.size()); // 未增加
    }
}
