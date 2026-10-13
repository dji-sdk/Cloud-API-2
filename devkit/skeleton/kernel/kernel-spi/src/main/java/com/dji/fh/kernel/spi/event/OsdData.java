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
 * 飞行器 OSD 实时遥测数据。开放属性结构 —— 字段名由 SDK 映射层决定。
 *
 * <p>常见 key（非穷举）：battery_percent / altitude / longitude / latitude / speed / mode 等。
 *
 * <p>SDK OSD 帧是稀疏的，部分字段可能缺失（value 为 null）；
 * record 构造时对 null map 用空 map 兜底，对含 null value 的 map 也能正常拷贝。
 *
 * @param attributes 属性 map（构造时做防御性不可变拷贝，外部不可变更；允许 null value）
 * @author DJI DevKit Team
 */
public record OsdData(Map<String, Object> attributes) {

    public OsdData {
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
