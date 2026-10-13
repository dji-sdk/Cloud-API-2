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

package com.dji.fh.feature.dashboard.spi.sdk;

import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflicts;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import java.util.Comparator;
import java.util.List;

/**
 * 飞行任务时间冲突校验的 SDK 实现。
 *
 * <p>平台的冲突接口（{@code POST /task/api/v1/workspaces/:workspace_id/flight-tasks/conflicts}）
 * 未在网关的 {@code openapi} 路由表中放行，且 SDK 3.3.5 未提供任何冲突查询能力
 * （{@code javap} 全量核对 {@code FlightTaskModule} 的 17 个方法均无 conflict/schedule 检查），
 * 因此本实现自行完成两件事：通过 {@link FlightTaskQueryService#listTaskWindows(String)}
 * 拉取同设备全部任务时间窗，再用 {@link FlightTaskConflicts} 的语义
 * （同设备 + 半开区间重叠 + 状态/类型过滤）逐个比对。
 *
 * <p><b>已知局限</b>：本校验是「先查后写」（check-then-act），两个并发创建同一设备同一时间窗的
 * 请求仍可能双双通过。要彻底消除需平台侧在落库事务内做唯一性/冲突约束。
 *
 * @author DJI DevKit Team
 */
public class SdkFlightTaskConflictService implements FlightTaskConflictService {

    private final FlightTaskQueryService queryService;

    public SdkFlightTaskConflictService(FlightTaskQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * 检测候选任务与同设备已有任务的时间窗冲突。
     *
     * <p>候选任务本身不参与比对时（即时任务、缺少开始时间等）直接返回无冲突；
     * 已有任务先按设备 SN 收窄（不依赖查询层是否严格按 SN 过滤，避免误伤其他机场的任务），
     * 再按状态过滤，最后排除编辑场景下的自身 UUID。
     *
     * @param candidate 待校验的任务时间窗
     * @return 冲突任务列表（按开始时间升序），无冲突时返回空列表
     */
    @Override
    public List<ConflictingTask> findConflicts(TaskWindow candidate) {
        if (!FlightTaskConflicts.isComparableCandidate(candidate)) {
            return List.of();
        }

        long candidateBeginAt = candidate.beginAt();
        long candidateEndAt = FlightTaskConflicts.resolveEndAt(candidate);
        String candidateSn = candidate.sn();

        return queryService.listTaskWindows(candidateSn).stream()
                .filter(window -> candidateSn.equals(window.sn()))
                .filter(window -> !isSelf(window, candidate.taskUuid()))
                .filter(window -> FlightTaskConflicts.isParticipatingStatus(window.status()))
                .filter(window -> FlightTaskConflicts.EXISTING_TASK_TYPES.contains(window.taskType()))
                .filter(window -> window.beginAt() > 0)
                .filter(window -> FlightTaskConflicts.overlaps(
                        candidateBeginAt, candidateEndAt, window.beginAt(),
                        FlightTaskConflicts.resolveEndAt(
                                window.beginAt(), window.endAt(), window.waylineDuration())))
                .map(SdkFlightTaskConflictService::toConflictingTask)
                .sorted(Comparator.comparingLong(ConflictingTask::beginAt))
                .toList();
    }

    /**
     * 判断已有任务是否为编辑场景下的候选任务自身。
     */
    private static boolean isSelf(FlightTaskWindow window, String candidateTaskUuid) {
        return candidateTaskUuid != null
                && !candidateTaskUuid.isBlank()
                && candidateTaskUuid.equals(window.taskUuid());
    }

    private static ConflictingTask toConflictingTask(FlightTaskWindow window) {
        return new ConflictingTask(
                window.taskUuid(),
                window.name(),
                window.beginAt(),
                FlightTaskConflicts.resolveEndAt(
                        window.beginAt(), window.endAt(), window.waylineDuration()));
    }
}
