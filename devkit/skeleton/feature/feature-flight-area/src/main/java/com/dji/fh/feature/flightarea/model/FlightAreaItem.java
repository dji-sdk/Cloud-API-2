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

import java.util.Map;

/**
 * 飞行区域列表项。
 *
 * @param id          区域 ID
 * @param name        区域名称
 * @param type        区域类型
 * @param status      启用状态
 * @param content     GeoJSON Feature 内容
 * @param createdTime 创建时间（Unix 秒）
 * @param createdBy   创建人
 * @param updatedTime 更新时间（Unix 秒）
 * @param updatedBy   更新人
 */
public record FlightAreaItem(
        String id,
        String name,
        FlightAreaType type,
        FlightAreaStatus status,
        Map<String, Object> content,
        long createdTime,
        String createdBy,
        long updatedTime,
        String updatedBy
) {}
