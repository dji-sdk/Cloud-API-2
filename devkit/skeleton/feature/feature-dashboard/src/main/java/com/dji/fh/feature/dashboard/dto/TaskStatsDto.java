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

import java.util.List;

/**
 * 任务分布统计 DTO。
 *
 * @param daily 日维度任务分布
 * @author DJI DevKit Team
 */
public record TaskStatsDto(List<DailyEntry> daily) {

    /**
     * 日维度任务条目。
     *
     * @param date    日期
     * @param planned 计划任务数
     * @param manual  手飞任务数
     */
    public record DailyEntry(String date, int planned, int manual) {}
}
