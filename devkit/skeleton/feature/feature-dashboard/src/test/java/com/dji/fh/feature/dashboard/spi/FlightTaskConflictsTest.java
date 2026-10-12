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

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.TaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link FlightTaskConflicts} 单元测试。
 *
 * <p>覆盖重叠谓词的边界语义（半开区间）、任务参与比对的前置条件，
 * 以及结束时间未知时的保守最小窗口兜底。
 *
 * @author DJI DevKit Team
 */
class FlightTaskConflictsTest {

    private static final long BEGIN = 1700000000L;
    private static final long END = 1700003600L;

    @Test
    void isComparableCandidate_shouldReturnFalse_whenWindowIsNull() {
        assertThat(FlightTaskConflicts.isComparableCandidate(null)).isFalse();
    }

    @Test
    void isComparableCandidate_shouldReturnFalse_whenTaskTypeIsImmediate() {
        assertThat(FlightTaskConflicts.isComparableCandidate(window(1, BEGIN, END))).isFalse();
    }

    @Test
    void isComparableCandidate_shouldReturnFalse_whenBeginAtMissing() {
        assertThat(FlightTaskConflicts.isComparableCandidate(window(2, 0L, END))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void isComparableCandidate_shouldReturnFalse_whenSnIsBlank(String sn) {
        TaskWindow candidate = new TaskWindow("", "任务", sn, 2, BEGIN, END, 0.0);
        assertThat(FlightTaskConflicts.isComparableCandidate(candidate)).isFalse();
    }

    @Test
    void isComparableCandidate_shouldReturnFalse_whenSnIsNull() {
        TaskWindow candidate = new TaskWindow("", "任务", null, 2, BEGIN, END, 0.0);
        assertThat(FlightTaskConflicts.isComparableCandidate(candidate)).isFalse();
    }

    @Test
    void isComparableCandidate_shouldReturnTrue_whenScheduledTaskWithTimeWindow() {
        assertThat(FlightTaskConflicts.isComparableCandidate(window(2, BEGIN, END))).isTrue();
    }

    /**
     * BFF 三种定时策略（repeat/once/continuous）各自映射到的 task_type 都要参与候选侧比对。
     */
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 5})
    void isComparableCandidate_shouldReturnTrue_whenCandidateTypeIsProducibleByBff(int taskType) {
        assertThat(FlightTaskConflicts.isComparableCandidate(window(taskType, BEGIN, END))).isTrue();
    }

    /**
     * 4/6 是平台展开出的子任务类型，BFF 的创建/编辑表单不会产出，故不做候选。
     */
    @ParameterizedTest
    @ValueSource(ints = {4, 6, 7})
    void isComparableCandidate_shouldReturnFalse_whenCandidateTypeIsNotProducibleByBff(int taskType) {
        assertThat(FlightTaskConflicts.isComparableCandidate(window(taskType, BEGIN, END))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATED", "EXECUTING", "PAUSED"})
    void isParticipatingStatus_shouldReturnTrue_whenStatusIsActive(String status) {
        assertThat(FlightTaskConflicts.isParticipatingStatus(status)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUCCESS", "FAILED", "CANCELLED"})
    void isParticipatingStatus_shouldReturnFalse_whenStatusIsTerminal(String status) {
        assertThat(FlightTaskConflicts.isParticipatingStatus(status)).isFalse();
    }

    @Test
    void isParticipatingStatus_shouldReturnFalse_whenStatusIsNull() {
        assertThat(FlightTaskConflicts.isParticipatingStatus(null)).isFalse();
    }

    @Test
    void overlaps_shouldReturnTrue_whenWindowsPartiallyOverlap() {
        assertThat(FlightTaskConflicts.overlaps(BEGIN, END, BEGIN + 600, END + 600)).isTrue();
    }

    @Test
    void overlaps_shouldReturnTrue_whenCandidateContainsExisting() {
        assertThat(FlightTaskConflicts.overlaps(BEGIN, END, BEGIN + 60, END - 60)).isTrue();
    }

    @Test
    void overlaps_shouldReturnTrue_whenWindowsAreIdentical() {
        assertThat(FlightTaskConflicts.overlaps(BEGIN, END, BEGIN, END)).isTrue();
    }

    @Test
    void overlaps_shouldReturnFalse_whenCandidateEndTouchesExistingBegin() {
        assertThat(FlightTaskConflicts.overlaps(BEGIN, END, END, END + 600)).isFalse();
    }

    @Test
    void overlaps_shouldReturnFalse_whenCandidateBeginTouchesExistingEnd() {
        assertThat(FlightTaskConflicts.overlaps(END, END + 600, BEGIN, END)).isFalse();
    }

    @Test
    void overlaps_shouldReturnFalse_whenWindowsAreDisjoint() {
        assertThat(FlightTaskConflicts.overlaps(BEGIN, END, END + 600, END + 1200)).isFalse();
    }

    @Test
    void resolveEndAt_shouldPreferExplicitEndAt_whenItIsAfterBeginAt() {
        assertThat(FlightTaskConflicts.resolveEndAt(BEGIN, END, 300.0)).isEqualTo(END);
    }

    @Test
    void resolveEndAt_shouldFallbackToWaylineDuration_whenEndAtIsMissing() {
        long expected = BEGIN + 300L + FlightTaskConflicts.PREFLIGHT_PREPARE_DURATION_SECONDS;

        assertThat(FlightTaskConflicts.resolveEndAt(BEGIN, 0L, 300.0)).isEqualTo(expected);
    }

    @Test
    void resolveEndAt_shouldFallback_whenEndAtIsNotAfterBeginAt() {
        long expected = BEGIN + 300L + FlightTaskConflicts.PREFLIGHT_PREPARE_DURATION_SECONDS;

        assertThat(FlightTaskConflicts.resolveEndAt(BEGIN, BEGIN, 300.0)).isEqualTo(expected);
    }

    @Test
    void resolveEndAt_shouldUseMinimalWindow_whenWaylineDurationUnknown() {
        long expected = BEGIN + FlightTaskConflicts.PREFLIGHT_PREPARE_DURATION_SECONDS;

        assertThat(FlightTaskConflicts.resolveEndAt(BEGIN, 0L, 0.0)).isEqualTo(expected);
    }

    @Test
    void resolveEndAt_shouldReturnMinimalWindow_whenWindowGiven() {
        long expected = BEGIN + FlightTaskConflicts.PREFLIGHT_PREPARE_DURATION_SECONDS;

        assertThat(FlightTaskConflicts.resolveEndAt(window(2, BEGIN, 0L))).isEqualTo(expected);
    }

    @Test
    void findConflictedTaskUuids_shouldReturnEmpty_whenWindowsIsNull() {
        assertThat(FlightTaskConflicts.findConflictedTaskUuids(null)).isEmpty();
    }

    @Test
    void findConflictedTaskUuids_shouldReturnEmpty_whenSingleWindow() {
        List<FlightTaskWindow> windows = List.of(listWindow("u1", "CREATED", 3, BEGIN, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows)).isEmpty();
    }

    @Test
    void findConflictedTaskUuids_shouldMarkBoth_whenWindowsOverlap() {
        List<FlightTaskWindow> windows = List.of(
                listWindow("u1", "CREATED", 3, BEGIN, 0L),
                listWindow("u2", "CREATED", 3, BEGIN, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows))
                .containsExactlyInAnyOrder("u1", "u2");
    }

    @Test
    void findConflictedTaskUuids_shouldMarkAll_whenThreeWindowsShareSameSlot() {
        List<FlightTaskWindow> windows = List.of(
                listWindow("u1", "CREATED", 3, BEGIN, END),
                listWindow("u2", "EXECUTING", 3, BEGIN, END),
                listWindow("u3", "CREATED", 3, BEGIN, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows))
                .containsExactlyInAnyOrder("u1", "u2", "u3");
    }

    @Test
    void findConflictedTaskUuids_shouldReturnEmpty_whenWindowsAreAdjacent() {
        List<FlightTaskWindow> windows = List.of(
                listWindow("u1", "CREATED", 3, BEGIN, END),
                listWindow("u2", "CREATED", 3, END, END + 600L));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows)).isEmpty();
    }

    @Test
    void findConflictedTaskUuids_shouldIgnoreNonParticipatingWindows() {
        List<FlightTaskWindow> windows = List.of(
                listWindow("u1", "CREATED", 3, BEGIN, END),
                // 已结束任务不参与比对
                listWindow("u2", "SUCCESS", 3, BEGIN, END),
                // 即时任务无排期窗口，不参与比对
                listWindow("u3", "CREATED", 1, BEGIN, END),
                // 缺少开始时间，无法定位窗口
                listWindow("u4", "CREATED", 3, 0L, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows)).isEmpty();
    }

    /**
     * 平台展开出的子任务类型（4=SubTask / 6=ContinuousSubTask）参与已有侧比对。
     *
     * <p>平台在创建父任务（2/5）时把它展开为子任务落库，真正占用设备时间的是 4/6，
     * 因此列表链路返回的冲突任务几乎都是这两个类型。
     */
    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 6})
    void findConflictedTaskUuids_shouldMarkBoth_whenExistingTypeParticipates(int taskType) {
        List<FlightTaskWindow> windows = List.of(
                listWindow("u1", "CREATED", taskType, BEGIN, END),
                listWindow("u2", "CREATED", taskType, BEGIN, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows))
                .containsExactlyInAnyOrder("u1", "u2");
    }

    /**
     * 已有侧的父任务类型 2（RepeatedTask）被平台有意排除在冲突比对之外 ——
     * 重复规则本身不占用设备时间，真正参与调度的是它展开出的子任务。
     */
    @Test
    void findConflictedTaskUuids_shouldIgnoreParentTask_whenExistingTypeIsRepeatedTask() {
        List<FlightTaskWindow> windows = List.of(
                listWindow("u1", "CREATED", 2, BEGIN, END),
                listWindow("u2", "CREATED", 2, BEGIN, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows)).isEmpty();
    }

    @Test
    void findConflictedTaskUuids_shouldIgnoreWindow_whenTaskUuidMissing() {
        List<FlightTaskWindow> windows = List.of(
                listWindow(null, "CREATED", 3, BEGIN, END),
                listWindow("u2", "CREATED", 3, BEGIN, END),
                listWindow("u3", "CREATED", 3, BEGIN, END));

        assertThat(FlightTaskConflicts.findConflictedTaskUuids(windows))
                .containsExactlyInAnyOrder("u2", "u3");
    }

    /**
     * 构造定时任务（task_type=3，单次定时）时间窗。
     */
    private static TaskWindow window(int taskType, long beginAt, long endAt) {
        return new TaskWindow("task-uuid", "测试任务", "SN001", taskType, beginAt, endAt, 0.0);
    }

    /**
     * 构造列表用的任务时间窗。
     *
     * @param taskUuid 任务 UUID
     * @param status   任务状态
     * @param taskType 任务类型
     * @param beginAt  开始时间
     * @param endAt    结束时间
     * @return 任务时间窗
     */
    private static FlightTaskWindow listWindow(String taskUuid, String status, int taskType,
                                               long beginAt, long endAt) {
        return new FlightTaskWindow(taskUuid, "测试任务", "SN001", status, taskType, beginAt, endAt, 0.0);
    }
}
