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

import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.TaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 飞行任务时间窗冲突判定工具。
 *
 * <p>判定语义对齐平台 es-task-management 的冲突引擎（重叠谓词取自
 * {@code CheckConflictsWithTimeSlop}，参与比对的任务范围对齐
 * {@code WaitingTasks} × {@code CheckConflictStatuses}）：
 *
 * <ul>
 *   <li>重叠：{@code beginAt < 已有任务.endAt && endAt > 已有任务.beginAt}（半开区间，边界相接不算冲突）</li>
 *   <li>同设备：{@code sn} 相同</li>
 *   <li>状态 ∈ {CREATED, EXECUTING, PAUSED}</li>
 *   <li>类型：候选侧与已有侧取值范围不同，见 {@link #CANDIDATE_TASK_TYPES} / {@link #EXISTING_TASK_TYPES}</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public final class FlightTaskConflicts {

    /**
     * 任务执行准备时长（秒），对齐平台 es-task-management 的 {@code preflight_prepare_duration}。
     *
     * <p>平台侧 {@code estimateDuration(d) = int(d) + PreFlightPrepareDuration}，
     * 客户端未提供航线时长时窗口为 {@code [beginAt, beginAt + 100]}。
     */
    public static final long PREFLIGHT_PREPARE_DURATION_SECONDS = 100L;

    /**
     * 参与冲突比对的<b>候选任务</b>类型：BFF 可产出范围内的一切非立即任务。
     *
     * <p>对应 es-task-management {@code TaskType} 的 2=RepeatedTask（重复规则父任务）、
     * 3=SingleTimedTask（单次定时）、5=ContinuousTask（连续执行）。BFF 的三种定时策略
     * （repeat / once / continuous）各占其一，故三者都要参与候选侧比对。
     *
     * <p>注意与 {@link #EXISTING_TASK_TYPES} 的差异：平台在创建时把父任务展开为子任务，
     * 展开后落库的是 4=SubTask / 6=ContinuousSubTask，父任务类型 2 不再出现于调度视野。
     * 两侧范围不同是平台语义本身决定的，不是口径漂移。
     */
    public static final Set<Integer> CANDIDATE_TASK_TYPES = Set.of(2, 3, 5);

    /**
     * 参与冲突比对的<b>已有任务</b>类型，对齐 es-task-management
     * {@code ComputeConflictTaskTypes = {3, 4, 5, 6}}。
     *
     * <p>即 {@code {SingleTimedTask, SubTask, ContinuousTask, ContinuousSubTask}}。
     * 父任务类型 2 被平台有意排除在冲突比对之外——重复规则本身不占用设备时间，
     * 真正参与调度的是它展开出的子任务（4/6）。
     *
     * <p>已确认平台的列表链路不会漏掉子任务：{@code listFilter} 的默认分支为
     * {@code task_type <> RepeatedTask}，4/6 均在返回范围内。
     */
    public static final Set<Integer> EXISTING_TASK_TYPES = Set.of(3, 4, 5, 6);

    /** 参与冲突比对的任务状态（对齐平台 {@code CheckConflictStatuses} 在本链路可表达的取值） */
    private static final Set<String> CONFLICT_STATUSES = Set.of("CREATED", "EXECUTING", "PAUSED");

    private FlightTaskConflicts() {
        // 工具类，禁止实例化
    }

    /**
     * 判断候选任务是否需要参与冲突比对。
     *
     * <p>即时任务（task_type=1）随下发立即执行、无排期时间窗，不参与比对；
     * 缺少开始时间的定时任务无法定位窗口，同样跳过。
     *
     * @param window 候选任务时间窗
     * @return 需要比对返回 true
     */
    public static boolean isComparableCandidate(TaskWindow window) {
        return window != null
                && CANDIDATE_TASK_TYPES.contains(window.taskType())
                && window.beginAt() > 0
                && window.sn() != null
                && !window.sn().isBlank();
    }

    /**
     * 判断任务状态是否参与冲突比对。
     *
     * @param status 任务状态字符串
     * @return 参与比对返回 true
     */
    public static boolean isParticipatingStatus(String status) {
        return status != null && CONFLICT_STATUSES.contains(status);
    }

    /**
     * 判断两个时间窗是否重叠（半开区间）。
     *
     * <p>谓词与平台 {@code CheckConflictsWithTimeSlop} 一致：
     * 前一任务 {@code endAt} 恰好等于后一任务 {@code beginAt} 时视为不冲突。
     *
     * @param beginAt      候选任务开始时间（Unix 秒）
     * @param endAt        候选任务结束时间（Unix 秒）
     * @param otherBeginAt 已有任务开始时间（Unix 秒）
     * @param otherEndAt   已有任务结束时间（Unix 秒）
     * @return 重叠返回 true
     */
    public static boolean overlaps(long beginAt, long endAt, long otherBeginAt, long otherEndAt) {
        return beginAt < otherEndAt && endAt > otherBeginAt;
    }

    /**
     * 解析任务的实际结束时间，航线时长未知时退化为 100 秒下限窗口。
     *
     * <p>窗口长度与平台一致取 {@code int(waylineDuration) + 100}；
     * 航线时长未知时退化为 {@value #PREFLIGHT_PREPARE_DURATION_SECONDS} 秒。
     * 该退化值是「下限」而非「保守上限」：当航线实际执行时长明显长于 100 秒时，
     * 冲突校验会因窗口过短而漏判与后续任务的重叠，方向是漏判而非误判。
     * 之所以仍不采用闭区间判定，是为了避免窗口长度为 0 时连显而易见的
     * 完全重叠都判不出来——即在无法获知真实时长的前提下取一个尽量不坏的下限。
     *
     * <p><b>与平台的 1 秒偏差（编辑路径，已知且有意保留）</b>：平台的
     * {@code estimateDuration(d) = int(d) + preflight_prepare_duration(100)}，其中
     * {@code int()} 是截断而非四舍五入；SDK 用 {@code -1.0} 作为「未设置」哨兵，
     * 因此编辑路径（{@code EditTaskReq} 无 waylineDuration setter，BFF 只能按未知处理）
     * 在平台侧实际算出 {@code int(-1.0) + 100 = 99} 秒窗口，而此处按
     * {@code 0 + 100 = 100} 秒计算。差值为 1 秒且方向保守（BFF 窗口略长），
     * 只会让边界相邻的任务被多算入冲突、不会漏判，故不做对齐。
     *
     * @param beginAt         开始时间（Unix 秒）
     * @param endAt           结束时间（Unix 秒），未知时为 0
     * @param waylineDuration 航线预估时长（秒），未知时为 0
     * @return 实际用于比对的结束时间（Unix 秒）
     */
    public static long resolveEndAt(long beginAt, long endAt, double waylineDuration) {
        if (endAt > beginAt) {
            return endAt;
        }
        return beginAt + (long) waylineDuration + PREFLIGHT_PREPARE_DURATION_SECONDS;
    }

    /**
     * 解析候选任务的实际结束时间。
     *
     * @param window 候选任务时间窗
     * @return 实际用于比对的结束时间（Unix 秒）
     */
    public static long resolveEndAt(TaskWindow window) {
        return resolveEndAt(window.beginAt(), window.endAt(), window.waylineDuration());
    }

    /**
     * 在单个设备的任务时间窗集合中找出已存在执行时间冲突的任务 UUID。
     *
     * <p>供任务列表标识使用：先按 {@link #isParticipatingStatus} 与
     * {@link #EXISTING_TASK_TYPES} 收窄参与比对的范围，再两两比对。
     * 与建单闸门不同，此处没有「候选任务」，冲突只发生在集合内部，
     * 因此互相重叠的一组任务会被全部标记。
     *
     * @param windows 同一设备的全部任务时间窗
     * @return 存在冲突的任务 UUID 集合（可变集合，无冲突时为空）
     */
    public static Set<String> findConflictedTaskUuids(List<FlightTaskWindow> windows) {
        Set<String> conflictedUuids = new HashSet<>();
        if (windows == null || windows.isEmpty()) {
            return conflictedUuids;
        }

        List<FlightTaskWindow> participants = windows.stream()
                .filter(window -> window.taskUuid() != null)
                .filter(window -> isParticipatingStatus(window.status()))
                .filter(window -> EXISTING_TASK_TYPES.contains(window.taskType()))
                .filter(window -> window.beginAt() > 0)
                .toList();

        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                FlightTaskWindow first = participants.get(i);
                FlightTaskWindow second = participants.get(j);
                if (overlaps(first.beginAt(), resolveEndAt(first),
                        second.beginAt(), resolveEndAt(second))) {
                    conflictedUuids.add(first.taskUuid());
                    conflictedUuids.add(second.taskUuid());
                }
            }
        }
        return conflictedUuids;
    }

    /**
     * 解析已有任务的实际结束时间。
     *
     * @param window 已有任务时间窗
     * @return 实际用于比对的结束时间（Unix 秒）
     */
    public static long resolveEndAt(FlightTaskWindow window) {
        return resolveEndAt(window.beginAt(), window.endAt(), window.waylineDuration());
    }
}
