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
 * 执行结果统计 DTO。
 *
 * @param daily 日维度执行结果
 * @author DJI DevKit Team
 */
public record ExecutionStatsDto(List<DailyEntry> daily) {

    /**
     * 日维度执行结果条目。
     *
     * @param date      日期
     * @param success   成功数
     * @param suspended 挂起数
     * @param failed    失败数
     */
    public record DailyEntry(String date, int success, int suspended, int failed) {}
}
