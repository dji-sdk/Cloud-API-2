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

package com.dji.fh.feature.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 中继站统计 DTO（mock 数据）。
 *
 * @param total   总数
 * @param online  在线
 * @param offline 离线
 * @param mock    是否为模拟数据
 * @author DJI DevKit Team
 */
public record RelayStatsDto(
        int total,
        int online,
        int offline,
        @JsonProperty("_mock") boolean mock
) {}
