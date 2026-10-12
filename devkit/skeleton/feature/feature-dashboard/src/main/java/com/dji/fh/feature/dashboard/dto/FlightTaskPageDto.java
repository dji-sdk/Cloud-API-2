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
 * 飞行任务分页结果 DTO。
 *
 * <p>字段名与查询 SPI 的 {@code FlightTaskPage} 保持一致，条目类型换成带冲突标识的
 * {@link FlightTaskListItemDto}：字段为查询 SPI 条目的超集，仅额外追加 {@code isConflicted}，
 * 不删除既有字段，故前端可按原结构消费新增标识。
 *
 * @param tasks      当前页任务条目
 * @param totalCount 满足过滤条件的任务总数
 * @param hasMore    是否还有下一页
 * @author DJI DevKit Team
 */
public record FlightTaskPageDto(List<FlightTaskListItemDto> tasks, int totalCount, boolean hasMore) {
}
