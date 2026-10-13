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

/**
 * 飞行任务列表条目 DTO。
 *
 * <p>在查询 SPI 返回的 {@code FlightTaskDto} 基础上追加冲突标识，
 * 由接口层按同设备已排期任务的时间窗计算后下发，前端不做任何冲突判定。
 *
 * @param taskUuid       任务 UUID
 * @param name           任务名称
 * @param sn             机场 SN
 * @param status         任务状态
 * @param taskType       任务类型，取值对齐平台 TaskType 枚举：
 *                       0=未知, 1=即时, 2=重复定时（父任务）, 3=单次定时,
 *                       4=重复定时子任务, 5=连续执行, 6=连续执行子任务
 * @param waylineUuid    航线 UUID
 * @param createdAt      创建时间（Unix 秒）
 * @param distanceMeters 飞行距离（米），无数据时为 0
 * @param minBatteryCapacity 最低电量阈值（百分比），无数据时 0。
 *                       平台会把连续执行任务也记为 {@code taskType=1}，
 *                       前端据此字段与"立即"任务区分；缺失该字段会让列表把连续执行
 *                       任务显示为"立即"，故必须与查询 SPI 的
 *                       {@code FlightTaskDto.minBatteryCapacity} 保持同源下发
 * @param isConflicted   是否与同设备其他已排期任务的执行时间窗冲突
 * @param isBreakPointResume 是否断点续飞。平台规则：断点续飞任务不可编辑，
 *                       列表页需据此置灰「编辑」按钮，与后端门禁保持一致
 * @author DJI DevKit Team
 */
public record FlightTaskListItemDto(String taskUuid, String name, String sn,
                                    String status, int taskType, String waylineUuid,
                                    long createdAt, double distanceMeters,
                                    int minBatteryCapacity,
                                    boolean isConflicted,
                                    boolean isBreakPointResume) {
}
