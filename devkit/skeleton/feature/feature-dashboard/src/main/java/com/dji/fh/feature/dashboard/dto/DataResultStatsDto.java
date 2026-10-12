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
import java.util.List;

/**
 * 数据成果统计 DTO。
 *
 * @param media   媒体统计
 * @param models  模型统计
 * @param reports 报告统计（mock）
 * @author DJI DevKit Team
 */
public record DataResultStatsDto(TrendItem media, TrendItem models, MockTrendItem reports) {

    /**
     * 带趋势数据的统计项。
     *
     * @param total 总数
     * @param trend 趋势数据
     */
    public record TrendItem(int total, List<Integer> trend) {}

    /**
     * 带趋势数据和 mock 标记的统计项。
     *
     * @param total 总数
     * @param trend 趋势数据
     * @param mock  是否为模拟数据
     */
    public record MockTrendItem(int total, List<Integer> trend, @JsonProperty("_mock") boolean mock) {}
}
