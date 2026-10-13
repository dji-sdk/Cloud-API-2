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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.ConflictingTask;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.TaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflicts;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDetailDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskPage;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.TaskSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link SdkFlightTaskConflictService} 单元测试。
 *
 * <p>通过注入 {@link FlightTaskQueryService} 测试替身驱动筛选逻辑，不依赖 SDK 原生库。
 *
 * @author DJI DevKit Team
 */
class SdkFlightTaskConflictServiceTest {

    private static final String SN = "SN001";
    private static final long BEGIN = 1700000000L;
    private static final long END = 1700003600L;

    private StubQueryService queryService;
    private SdkFlightTaskConflictService service;

    @BeforeEach
    void setUp() {
        queryService = new StubQueryService();
        service = new SdkFlightTaskConflictService(queryService);
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenCandidateIsImmediateTask() {
        queryService.windows = List.of(existing("u1", "CREATED", 3, BEGIN, END));

        assertThat(service.findConflicts(candidate("", 1, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenCandidateHasNoBeginAt() {
        queryService.windows = List.of(existing("u1", "CREATED", 3, BEGIN, END));

        assertThat(service.findConflicts(candidate("", 2, 0L, 0L))).isEmpty();
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenNoExistingTask() {
        assertThat(service.findConflicts(candidate("", 2, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldReturnOverlappingTask_whenWindowsOverlap() {
        queryService.windows = List.of(existing("u1", "已排期巡检", "CREATED", 3, BEGIN + 600, END + 600));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END)))
                .extracting(ConflictingTask::taskUuid, ConflictingTask::name)
                .containsExactly(tuple("u1", "已排期巡检"));
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenWindowsAreBoundaryAdjacent() {
        queryService.windows = List.of(existing("u1", "CREATED", 3, END, END + 600));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenExistingTaskIsInDifferentDevice() {
        queryService.windows = List.of(
                new FlightTaskWindow("u1", "其他设备任务", "SN999", "CREATED", 3, BEGIN, END, 0.0));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenExistingTaskIsTerminal() {
        queryService.windows = List.of(existing("u1", "SUCCESS", 3, BEGIN, END));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldReturnEmpty_whenExistingTaskIsImmediateType() {
        queryService.windows = List.of(existing("u1", "CREATED", 1, BEGIN, END));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldExcludeSelf_whenEditingSameTask() {
        queryService.windows = List.of(existing("self-uuid", "CREATED", 3, BEGIN, END));

        assertThat(service.findConflicts(candidate("self-uuid", 2, BEGIN, END))).isEmpty();
    }

    @Test
    void findConflicts_shouldExcludeSelfButKeepOthers_whenEditing() {
        queryService.windows = List.of(
                existing("self-uuid", "CREATED", 3, BEGIN, END),
                existing("other-uuid", "CREATED", 3, BEGIN, END));

        assertThat(service.findConflicts(candidate("self-uuid", 2, BEGIN, END)))
                .extracting(ConflictingTask::taskUuid)
                .containsExactly("other-uuid");
    }

    @Test
    void findConflicts_shouldUseMinimalWindow_whenExistingTaskEndAtMissing() {
        queryService.windows = List.of(existing("u1", "CREATED", 3, BEGIN + 30, 0L));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END)))
                .extracting(ConflictingTask::taskUuid)
                .containsExactly("u1");
    }

    @Test
    void findConflicts_shouldResolveReportedEndAt_whenExistingTaskEndAtMissing() {
        long expectedEnd = BEGIN + 30 + 300L + 100L;
        queryService.windows = List.of(
                new FlightTaskWindow("u1", "任务", SN, "CREATED", 3, BEGIN + 30, 0L, 300.0));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END)))
                .extracting(ConflictingTask::beginAt, ConflictingTask::endAt)
                .containsExactly(tuple(BEGIN + 30, expectedEnd));
    }

    @Test
    void findConflicts_shouldSortByBeginAt_whenMultipleConflicts() {
        queryService.windows = List.of(
                existing("late", "CREATED", 3, BEGIN + 600, END + 600),
                existing("early", "CREATED", 3, BEGIN, END));

        assertThat(service.findConflicts(candidate("", 2, BEGIN, END)))
                .extracting(ConflictingTask::taskUuid)
                .containsExactly("early", "late");
    }

    @Test
    void findConflicts_shouldQueryWindowsByCandidateSn() {
        service.findConflicts(candidate("", 2, BEGIN, END));

        assertThat(queryService.lastSn).isEqualTo(SN);
    }

    /**
     * 构造候任务时间窗（航线时长未知）。
     */
    private static TaskWindow candidate(String taskUuid, int taskType, long beginAt, long endAt) {
        return new TaskWindow(taskUuid, "候选任务", SN, taskType, beginAt, endAt, 0.0);
    }

    /**
     * 构造同设备已有任务时间窗。
     *
     * <p>已有侧的默认 taskType 取 3（单次定时）：父任务类型 2（RepeatedTask）被平台
     * 有意排除在冲突比对之外（见 {@link FlightTaskConflicts#EXISTING_TASK_TYPES}），
     * 若沿用 2 作为默认值，这些用例会因任务被过滤而全部落空。
     */
    private static FlightTaskWindow existing(String taskUuid, String status, int taskType,
                                             long beginAt, long endAt) {
        return existing(taskUuid, "已有任务", status, taskType, beginAt, endAt);
    }

    /**
     * 构造同设备已有任务时间窗（指定名称）。
     */
    private static FlightTaskWindow existing(String taskUuid, String name, String status, int taskType,
                                             long beginAt, long endAt) {
        return new FlightTaskWindow(taskUuid, name, SN, status, taskType, beginAt, endAt, 0.0);
    }

    /**
     * 查询测试替身：返回预置任务时间窗并记录查询参数。
     */
    private static class StubQueryService implements FlightTaskQueryService {

        List<FlightTaskWindow> windows = List.of();
        String lastSn;

        @Override
        public List<TaskSummary> listTasks(String range) {
            return List.of();
        }

        @Override
        public FlightTaskPage listTasksPaged(String status, int page, int pageSize) {
            return new FlightTaskPage(List.of(), 0, false);
        }

        @Override
        public FlightTaskDetailDto getTaskDetail(String taskUuid) {
            throw new UnsupportedOperationException("测试替身不提供任务详情");
        }

        @Override
        public List<FlightTaskWindow> listTaskWindows(String sn) {
            this.lastSn = sn;
            return windows;
        }
    }
}
