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

import java.util.List;

/**
 * 飞行任务执行时间冲突校验 SPI。
 *
 * <p>平台的冲突接口未在网关 {@code openapi} 路由表中放行、SDK 3.3.5 也未提供冲突查询能力，
 * 因此该校验只能在 BFF 侧自建：拉取同设备已排期任务的时间窗，与候选任务做区间重叠比对。
 * 默认提供 mock 实现（不做拦截），ISV 接入真实 SDK 时通过声明自有 bean 覆盖。
 *
 * <p>已知局限：本校验是「先查后写」，并发创建同一设备同一时间窗的任务时仍可能双双通过。
 *
 * @author DJI DevKit Team
 */
public interface FlightTaskConflictService {

    /**
     * 检测候选任务与同设备已有任务的时间窗冲突。
     *
     * <p>实现层应自动按 {@link TaskWindow#sn()} 过滤同设备任务，无需调用方指定。
     *
     * @param candidate 待校验的任务时间窗（新建任务时 {@code taskUuid} 为空字符串）
     * @return 与之冲突的已有任务列表（按开始时间升序），无冲突时返回空列表
     */
    List<ConflictingTask> findConflicts(TaskWindow candidate);

    /**
     * 待校验的任务时间窗。
     *
     * <p>{@code endAt} 未知（不大于 {@code beginAt}）时，按
     * {@link FlightTaskConflicts#resolveEndAt(TaskWindow)} 退化为 100 秒下限窗口——
     * 该下限可能小于航线实际执行时长，此时判定结果偏向漏判而非误判。
     *
     * @param taskUuid        任务 UUID，新建任务传空字符串
     * @param name            任务名称
     * @param sn              机场 SN
     * @param taskType        任务类型，取值对齐 es-task-management {@code TaskType} 枚举：
     *                        1=即时、2=重复规则、3=单次定时、4=重复定时子任务、
     *                        5=连续执行、6=连续执行子任务
     * @param beginAt         开始时间（Unix 秒）
     * @param endAt           结束时间（Unix 秒），未知时为 0
     * @param waylineDuration 航线预估时长（秒），未知时为 0
     */
    record TaskWindow(String taskUuid, String name, String sn, int taskType,
                      long beginAt, long endAt, double waylineDuration) {}

    /**
     * 冲突任务摘要，用于组装用户可见的冲突提示。
     *
     * @param taskUuid 冲突任务 UUID
     * @param name     冲突任务名称
     * @param beginAt  冲突任务开始时间（Unix 秒）
     * @param endAt    冲突任务结束时间（Unix 秒）
     */
    record ConflictingTask(String taskUuid, String name, long beginAt, long endAt) {}
}
