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

package com.dji.fh.kernel.impl.repository;

import com.dji.fh.kernel.spi.repository.KeyValueStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * KeyValueStore 的内存默认实现。
 *
 * <p>以 {@link ConcurrentHashMap} 持有 key → 序列化后的 byte[]；读写用 Jackson 做对象 ↔ JSON 转换。
 *
 * <p><b>不持久化</b> —— 进程重启数据丢失。生产持久化场景请引官方 starter 或自家 @Bean 覆盖。
 *
 * @author DJI DevKit Team
 */
public class InMemoryKeyValueStore implements KeyValueStore {

    private final ConcurrentMap<String, byte[]> store = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper Jackson ObjectMapper（由 Spring 注入）
     */
    public InMemoryKeyValueStore(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper 不能为空");
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Objects.requireNonNull(key, "key 不能为空");
        Objects.requireNonNull(type, "type 不能为空");
        byte[] raw = store.get(key);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw, type));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "反序列化 key=" + key + " 到 " + type.getName() + " 失败", e);
        }
    }

    @Override
    public <T> void put(String key, T value) {
        Objects.requireNonNull(key, "key 不能为空");
        Objects.requireNonNull(value, "value 不能为空");
        try {
            store.put(key, objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "序列化 key=" + key + " value 失败", e);
        }
    }

    @Override
    public boolean exists(String key) {
        Objects.requireNonNull(key, "key 不能为空");
        return store.containsKey(key);
    }

    @Override
    public void delete(String key) {
        Objects.requireNonNull(key, "key 不能为空");
        store.remove(key);
    }

    @Override
    public Stream<String> keys(String prefix) {
        Objects.requireNonNull(prefix, "prefix 不能为空");
        if (prefix.isEmpty()) {
            return store.keySet().stream();
        }
        return store.keySet().stream().filter(k -> k.startsWith(prefix));
    }
}
