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

/**
 * 地形障碍物（离线地图）模型信息。
 *
 * @param disabled        是否禁用
 * @param version         数据版本
 * @param updateTime      最后更新时间（Unix 秒）
 * @param dataDescription 数据描述
 */
public record OfflineMapModelInfo(
        boolean disabled,
        String version,
        long updateTime,
        String dataDescription
) {}
