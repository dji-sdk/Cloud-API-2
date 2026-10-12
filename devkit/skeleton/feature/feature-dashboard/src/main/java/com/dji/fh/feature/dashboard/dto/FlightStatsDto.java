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
 * 飞行数据统计 DTO。
 *
 * <p>次数与时长来自 SDK 真实数据；里程由每任务轨迹距离累加得出，SDK 查询成本待
 * 联调核实，当前以 {@link MockableDouble} 标记为 mock，由前端渲染为占位符。
 * 各指标独立标记，避免单个 mock 指标把同组的真实指标一并打成不可见。
 *
 * @param count           飞行次数（真实）
 * @param durationMinutes 飞行时长（分钟，真实；由秒累加后统一换算，保留 2 位小数）
 * @param distanceKm      飞行里程（公里；由各任务轨迹距离累加后换算，保留 2 位小数）
 * @param daily           日维度飞行数据
 * @author DJI DevKit Team
 */
public record FlightStatsDto(
        int count,
        double durationMinutes,
        MockableDouble distanceKm,
        List<DailyEntry> daily
) {

    /**
     * 可标记为 mock 的浮点指标。
     *
     * @param value 数值；mock 为 true 时无实际意义，前端不应展示
     * @param mock  是否为模拟/随机生成的数据
     */
    public record MockableDouble(double value, @JsonProperty("_mock") boolean mock) {}

    /**
     * 日维度飞行数据条目。
     *
     * @param date    日期
     * @param count   飞行次数
     * @param minutes 飞行时长（分钟，保留 2 位小数）
     * @param km      飞行里程（公里，保留 2 位小数）
     */
    public record DailyEntry(String date, int count, double minutes, double km) {}
}
