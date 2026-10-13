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

package com.dji.fh.feature.dashboard.spi;

import java.util.Collection;
import java.util.List;

/**
 * 飞行任务查询 SPI，供看板聚合和飞行记录列表使用。
 *
 * <p>默认提供 mock 实现；ISV 接入真实 SDK 时通过声明自有 bean 覆盖。
 *
 * @author DJI DevKit Team
 */
public interface FlightTaskQueryService {

    /**
     * 查询指定时间范围内的任务摘要列表（看板聚合用）。
     *
     * @param range 时间范围（week / month / recent_month / half_year / year）
     * @return 任务摘要列表
     */
    List<TaskSummary> listTasks(String range);

    /**
     * 分页查询任务列表（飞行记录列表 REST API 使用）。
     *
     * <p>实现层应自动获取当前在线设备 SN 并传入 SDK 查询，无需调用方指定。
     *
     * @param status   状态过滤，null 表示全部
     * @param page     页码（1-indexed）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    default FlightTaskPage listTasksPaged(String status, int page, int pageSize) {
        throw new UnsupportedOperationException("未实现分页查询");
    }

    /**
     * 查询单个任务详情（编辑预填用）。
     *
     * @param taskUuid 任务 UUID
     * @return 任务详情 DTO
     */
    default FlightTaskDetailDto getTaskDetail(String taskUuid) {
        throw new UnsupportedOperationException("未实现任务详情查询");
    }

    /**
     * 查询指定设备上全部任务的时间窗（时间冲突校验用，不分页）。
     *
     * <p>与 {@link #listTasksPaged} 不同：冲突校验需覆盖该设备的完整时间窗集合，
     * 客户端分页结果无法表达「全部已排期任务」。实现层无需过滤状态与任务类型，
     * 由调用方按 {@code FlightTaskConflicts} 的语义判定。
     *
     * @param sn 机场 SN
     * @return 该设备的任务时间窗列表，无数据时返回空列表
     */
    default List<FlightTaskWindow> listTaskWindows(String sn) {
        throw new UnsupportedOperationException("未实现任务时间窗查询");
    }

    /**
     * 批量查询多个设备上全部任务的时间窗（列表冲突标识用，不分页）。
     *
     * <p>语义与 {@link #listTaskWindows(String)} 完全一致，仅把多设备查询合并为一次调用：
     * 列表页可能横跨数十台设备，逐台调用会让同一次分页查询触发数十次 SDK 全量拉取。
     * 实现层可复用一次不分页的 SDK 查询，由调用方按 {@code sn} 分组后再做冲突判定。
     *
     * <p>默认实现退化为逐个调用 {@link #listTaskWindows(String)}，兼容尚未实现批量查询的
     * 存量实现；SDK 实现应覆写以避免重复拉取。空集合直接返回空列表。
     *
     * @param sns 机场 SN 集合
     * @return 全部设备的任务时间窗列表（跨设备混合），无数据时返回空列表
     */
    default List<FlightTaskWindow> listTaskWindows(Collection<String> sns) {
        if (sns == null || sns.isEmpty()) {
            return List.of();
        }
        return sns.stream()
                .flatMap(sn -> listTaskWindows(sn).stream())
                .toList();
    }

    /**
     * 判断当前工作空间内是否已存在同名任务（任务名称唯一性校验用）。
     *
     * <p>唯一性范围为整个工作空间，而非单个机场 SN：任务列表本就跨全部机场聚合展示，
     * 仅按设备维度限制无法解决「列表中分不清」的问题。
     *
     * <p>默认抛出 {@link UnsupportedOperationException}。存量 ISV 实现未覆盖该方法时，
     * 调用方（{@code FlightTaskRestController}）按 fail-open 处理并记录 WARN 日志，
     * 避免接口变更导致存量 ISV 的创建/编辑功能整体不可用。
     *
     * @param name            待校验的任务名称（调用方需先归一化，即去除首尾空白）
     * @param excludeTaskUuid 需排除的任务 UUID（编辑场景排除自身），创建场景传 null
     * @return 存在同名任务返回 true
     */
    default boolean existsTaskWithName(String name, String excludeTaskUuid) {
        throw new UnsupportedOperationException("未实现任务名称唯一性查询");
    }

    /**
     * 任务摘要（看板聚合用）。
     *
     * @param taskId          任务 ID
     * @param status          任务状态（CREATED / PREPARING / QUEUED / EXECUTING / PAUSED / SUCCESS / FAILED / CANCELLED）
     * @param taskType        任务类型（1=立即执行, 2=重复定时）
     * @param createDate      创建日期（yyyy-MM-dd）
     * @param durationSeconds 飞行时长（秒），SDK 取 waylineDuration，无数据时为 0
     * @param distanceMeters  飞行里程（米），SDK 取飞行轨迹的 flightDistance；
     *                        mock 实现生成确定性模拟值，无数据时为 0
     */
    record TaskSummary(String taskId, String status, int taskType,
                       String createDate, double durationSeconds, double distanceMeters) {}

    /**
     * 飞行任务详情 DTO（REST API 响应用）。
     *
     * @param taskUuid      任务 UUID
     * @param name          任务名称
     * @param sn            机场 SN
     * @param status        任务状态字符串
     * @param taskType      任务类型，取值对齐平台 TaskType 枚举：
     *                      0=未知, 1=即时, 2=重复定时（父任务，仅存重复规则）,
     *                      3=单次定时, 4=重复定时子任务, 5=连续执行, 6=连续执行子任务
     * @param waylineUuid   航线 UUID
     * @param createdAt     创建时间（Unix 秒）
     * @param distanceMeters 飞行距离（米），来自任务轨迹，SDK 可得，无数据时 0
     * @param minBatteryCapacity 最低电量阈值（百分比），无数据时 0。
     *                       平台会把连续执行任务也记为 {@code taskType=1}，
     *                       前端据此字段与"立即"任务区分（与
     *                       {@code FlightTaskDetailDto.minBatteryCapacity} 判定规则同源）
     * @param isBreakPointResume 是否断点续飞。平台规则：断点续飞任务不可编辑，
     *                       列表页需据此置灰「编辑」按钮，与后端门禁保持一致
     */
    record FlightTaskDto(String taskUuid, String name, String sn,
                         String status, int taskType, String waylineUuid,
                         long createdAt, double distanceMeters, int minBatteryCapacity,
                         boolean isBreakPointResume) {}

    /**
     * 飞行任务完整详情（编辑预填用）。
     *
     * <p>时间字段统一为 Unix 秒（由实现层从 SDK 的字符串格式解析）；
     * 无数据时为 0。
     *
     * <p>回显能力边界：{@code waylineName} / {@code deviceName} 由实现层反查补齐
     * （SDK 任务详情只返回 UUID 和 SN）；{@code planType} / {@code repeatType} /
     * {@code vlmEnabled} SDK 详情接口不返回，编辑预填时前端保持默认值。
     *
     * @param taskUuid       任务 UUID
     * @param name           任务名称
     * @param sn             机场 SN
     * @param deviceName     设备名称，由 SN 反查得到；查不到时为 null
     * @param status         任务状态字符串
     * @param taskType       任务类型，取值对齐平台 TaskType 枚举：
     *                       0=未知, 1=即时, 2=重复定时（父任务）, 3=单次定时,
     *                       4=重复定时子任务, 5=连续执行, 6=连续执行子任务
     * @param waylineUuid    航线 UUID
     * @param waylineName    航线名称，由 UUID 反查得到；查不到时为 null
     * @param beginAt        开始时间（Unix 秒，无数据为 0）
     * @param endAt          结束时间（Unix 秒，无数据为 0）
     * @param rthAltitude    返航高度（米）
     * @param rthMode        返航模式
     * @param outOfControlActionInFlight 失联动作（1=返航, 2=继续）
     * @param waylinePrecisionType 航线精度类型
     * @param minBatteryCapacity 最低电量阈值
     * @param isBreakPointResume 是否断点续飞
     */
    record FlightTaskDetailDto(String taskUuid, String name, String sn, String deviceName,
                               String status, int taskType, String waylineUuid, String waylineName,
                               long beginAt, long endAt,
                               double rthAltitude, int rthMode,
                               int outOfControlActionInFlight,
                               int waylinePrecisionType, int minBatteryCapacity,
                               boolean isBreakPointResume) {}

    /**
     * 飞行任务分页结果。
     *
     * @param tasks      当前页任务列表
     * @param totalCount 总条数
     * @param hasMore    是否还有更多
     */
    record FlightTaskPage(List<FlightTaskDto> tasks, int totalCount, boolean hasMore) {}

    /**
     * 飞行任务时间窗（时间冲突校验用）。
     *
     * <p>与 {@link FlightTaskDto} 的差异：携带冲突判定所需的 {@code status} 与时间字段。
     * 列表接口统一为客户端分页，无法表达「该设备全部已排期任务」，故单独建模。
     *
     * @param taskUuid        任务 UUID
     * @param name            任务名称
     * @param sn              机场 SN
     * @param status          任务状态字符串（CREATED / EXECUTING / PAUSED / SUCCESS / FAILED / CANCELLED）
     * @param taskType        任务类型，取值对齐平台 TaskType 枚举：
     *                        0=未知, 1=即时, 2=重复定时（父任务）, 3=单次定时,
     *                        4=重复定时子任务, 5=连续执行, 6=连续执行子任务
     * @param beginAt         开始时间（Unix 秒，无数据为 0）
     * @param endAt           结束时间（Unix 秒，无数据为 0）
     * @param waylineDuration 航线预估时长（秒，无数据为 0）
     */
    record FlightTaskWindow(String taskUuid, String name, String sn, String status, int taskType,
                            long beginAt, long endAt, double waylineDuration) {}
}
