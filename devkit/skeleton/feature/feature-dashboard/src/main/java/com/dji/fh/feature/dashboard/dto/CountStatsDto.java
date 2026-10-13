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
 * 计数指标 DTO。
 *
 * @param waylines  航线总数
 * @param personnel 人员总数（mock 模式下返回 0，ISV 可通过 PersonnelCountService 覆盖）
 * @author DJI DevKit Team
 */
public record CountStatsDto(int waylines, MockableInt personnel) {

    /**
     * 带 mock 标记的整数值。
     *
     * @param value 数值
     * @param mock  是否为模拟数据
     */
    public record MockableInt(int value, @JsonProperty("_mock") boolean mock) {}
}
