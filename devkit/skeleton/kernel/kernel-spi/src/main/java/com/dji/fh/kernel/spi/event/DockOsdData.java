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

package com.dji.fh.kernel.spi.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 机场 OSD 实时遥测数据。开放属性结构。
 *
 * <p>常见 key（非穷举）：cover_state / power_state / network_state / wind_speed 等。
 *
 * <p>SDK 上报的字段可能稀疏（value 为 null）；构造时容忍 null value。
 *
 * @param attributes 属性 map（构造时做防御性不可变拷贝，外部不可变更；允许 null value）
 * @author DJI DevKit Team
 */
public record DockOsdData(Map<String, Object> attributes) {

    public DockOsdData {
        if (attributes == null) {
            attributes = Map.of();
        } else {
            Map<String, Object> copy = new HashMap<>(attributes);
            attributes = Collections.unmodifiableMap(copy);
        }
    }

    /**
     * 按 key 取属性。
     *
     * @param key 属性 key
     * @return 存在返回值；不存在或为 null 返回 empty
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(attributes.get(key));
    }
}
