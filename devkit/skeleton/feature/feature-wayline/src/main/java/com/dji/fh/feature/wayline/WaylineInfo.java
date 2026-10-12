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

package com.dji.fh.feature.wayline;

/**
 * 单条航线信息 DTO。
 *
 * <p>对齐前端 {@code web/src/types/wayline.ts} 的 {@code WaylineInfo} 接口。
 * 航点数与距离由服务端补拉航线详情后填充，补拉失败时为 null。
 *
 * @param uuid              航线 UUID
 * @param name              航线名称
 * @param droneType         机型
 * @param payloadType       负载类型（多负载取第一个，空负载返回空串）
 * @param waylineType       航线类型中文标签（航点航线 / 面状航线 等，未知返回"未知类型"）
 * @param updatedAt         更新时间（Unix 秒），可能为 null
 * @param waypointCount     航点数量（来自航线详情 kmzData，补拉失败为 null）
 * @param estimatedDistance 预计飞行距离米（来自航线详情 kmzData，补拉失败为 null）
 * @author DJI DevKit Team
 */
public record WaylineInfo(
        String uuid,
        String name,
        String droneType,
        String payloadType,
        String waylineType,
        Long updatedAt,
        Integer waypointCount,
        Long estimatedDistance
) {}
