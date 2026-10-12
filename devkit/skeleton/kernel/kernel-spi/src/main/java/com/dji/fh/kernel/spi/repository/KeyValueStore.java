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

package com.dji.fh.kernel.spi.repository;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 键值存储 SPI。序列化由实现层决定（默认 Jackson）。
 * 复杂查询请使用 JPA / MyBatis 逃生口。
 *
 * @author DJI DevKit Team
 */
public interface KeyValueStore {

    /**
     * 按 key 读取并反序列化为指定类型。
     *
     * @param key  键
     * @param type 目标类型
     * @param <T>  类型参数
     * @return 存在则返回值；不存在返回 empty
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * 写入键值对。已存在则覆盖。
     *
     * @param key   键
     * @param value 值
     * @param <T>   类型参数
     */
    <T> void put(String key, T value);

    /**
     * @param key 键
     * @return 键存在则 true
     */
    boolean exists(String key);

    /**
     * @param key 键
     */
    void delete(String key);

    /**
     * 按前缀扫描键名。
     *
     * @param prefix 键前缀
     * @return 匹配的键流
     */
    Stream<String> keys(String prefix);
}
