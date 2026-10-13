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

package com.dji.fh.feature.flightarea;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 空域安全管理模块配置。
 *
 * @param enabled 是否启用模块（默认 true）
 */
@ConfigurationProperties("fh.skeleton.feature.flight-area")
public record FlightAreaProperties(
        boolean enabled
) {
    public FlightAreaProperties {
        // record 紧凑构造器：无需额外默认值处理
    }
}
