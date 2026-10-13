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

package com.dji.fh.feature.flightarea.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 飞行区域类型。
 */
public enum FlightAreaType {

    /** 限飞区 */
    NFZ("nfz"),
    /** 作业区（电子围栏） */
    DFENCE("dfence"),
    /** 禁降区 */
    NOLAND("noland");

    private final String value;

    FlightAreaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 从小写值反序列化（JSON body 及 @RequestParam 均适用）。
     *
     * @param value 小写值，如 "nfz"、"dfence"、"noland"
     * @return 对应枚举实例
     */
    @JsonCreator
    public static FlightAreaType fromValue(String value) {
        for (FlightAreaType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知的 FlightAreaType: " + value);
    }
}
