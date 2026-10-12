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

import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskPage;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.TaskSummary;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MockFlightTaskQueryServiceTest {

    private MockFlightTaskQueryService service;

    @BeforeEach
    void setUp() {
        service = new MockFlightTaskQueryService();
    }

    @Test
    void listTasks_shouldReturnNonEmptyList_whenRangeIsWeek() {
        List<TaskSummary> tasks = service.listTasks("week");
        assertThat(tasks).isNotEmpty();
    }

    @Test
    void listTasks_shouldReturnDeterministicData() {
        List<TaskSummary> first = service.listTasks("week");
        List<TaskSummary> second = service.listTasks("week");
        assertThat(first).isEqualTo(second);
    }

    @Test
    void listTasks_shouldOnlyContainValidStatuses() {
        Set<String> validStatuses = Set.of("SUCCESS", "FAILED", "CANCELLED");
        List<TaskSummary> tasks = service.listTasks("month");
        assertThat(tasks).allMatch(t -> validStatuses.contains(t.status()));
    }

    @Test
    void listTasks_shouldOnlyContainValidTaskTypes() {
        Set<Integer> validTypes = Set.of(1, 2);
        List<TaskSummary> tasks = service.listTasks("week");
        assertThat(tasks).allMatch(t -> validTypes.contains(t.taskType()));
    }

    @Test
    void listTasks_shouldCoverCorrectDayCount() {
        List<TaskSummary> weekTasks = service.listTasks("week");
        long distinctDates = weekTasks.stream().map(TaskSummary::createDate).distinct().count();
        assertThat(distinctDates).isEqualTo(7);
    }

    @Test
    void listTasks_shouldCoverCorrectDayCount_forMonth() {
        List<TaskSummary> monthTasks = service.listTasks("month");
        long distinctDates = monthTasks.stream().map(TaskSummary::createDate).distinct().count();
        assertThat(distinctDates).isEqualTo(30);
    }

    @Test
    void listTasks_shouldGenerateFiveToTwentyFourTasksPerDay() {
        List<TaskSummary> tasks = service.listTasks("week");
        tasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(TaskSummary::createDate))
                .values()
                .forEach(dailyTasks -> assertThat(dailyTasks.size()).isBetween(5, 24));
    }

    @Test
    void listTasks_shouldHandleDefaultRange() {
        List<TaskSummary> tasks = service.listTasks("unknown_range");
        long distinctDates = tasks.stream().map(TaskSummary::createDate).distinct().count();
        assertThat(distinctDates).isEqualTo(30);
    }

    @Test
    void listTasks_shouldHaveNonNullTaskIds() {
        List<TaskSummary> tasks = service.listTasks("week");
        assertThat(tasks).allMatch(t -> t.taskId() != null && t.taskId().startsWith("MOCK-"));
    }

    // ── listTasksPaged 分页查询测试 ──

    @Test
    void listTasksPaged_shouldReturnFirstPage() {
        FlightTaskPage page = service.listTasksPaged(null, 1, 10);
        assertThat(page.tasks()).hasSize(10);
        assertThat(page.totalCount()).isEqualTo(30);
        assertThat(page.hasMore()).isTrue();
    }

    @Test
    void listTasksPaged_shouldReturnLastPage() {
        FlightTaskPage page = service.listTasksPaged(null, 3, 10);
        assertThat(page.tasks()).hasSize(10);
        assertThat(page.hasMore()).isFalse();
    }

    @Test
    void listTasksPaged_shouldReturnEmptyForOutOfRange() {
        FlightTaskPage page = service.listTasksPaged(null, 10, 10);
        assertThat(page.tasks()).isEmpty();
    }

    @Test
    void listTasksPaged_shouldFilterByStatus() {
        FlightTaskPage page = service.listTasksPaged("SUCCESS", 1, 100);
        assertThat(page.tasks()).allMatch(t -> "SUCCESS".equals(t.status()));
    }

    @Test
    void listTasksPaged_shouldContainAllRequiredFields() {
        FlightTaskPage page = service.listTasksPaged(null, 1, 5);
        for (FlightTaskDto dto : page.tasks()) {
            assertThat(dto.taskUuid()).isNotBlank();
            assertThat(dto.name()).isNotBlank();
            assertThat(dto.sn()).isNotBlank();
            assertThat(dto.status()).isIn("SUCCESS", "FAILED", "CANCELLED", "EXECUTING", "PAUSED",
                    "CREATED", "PREPARING", "QUEUED");
            assertThat(dto.taskType()).isIn(1, 2, 3, 4, 5, 6);
            assertThat(dto.waylineUuid()).isNotBlank();
            assertThat(dto.createdAt()).isGreaterThan(0);
        }
    }

    @Test
    void listTasksPaged_shouldCarryMinBatteryCapacity_whenTaskIsContinuousStrategy() {
        FlightTaskPage page = service.listTasksPaged(null, 1, 100);
        assertThat(page.tasks())
                .filteredOn(t -> t.taskType() == 5 || t.taskType() == 6)
                .allMatch(t -> t.minBatteryCapacity() > 0);
    }

    @Test
    void listTasksPaged_shouldLeaveMinBatteryCapacityZero_whenTaskIsScheduled() {
        FlightTaskPage page = service.listTasksPaged(null, 1, 100);
        assertThat(page.tasks())
                .filteredOn(t -> t.taskType() == 2 || t.taskType() == 3)
                .allMatch(t -> t.minBatteryCapacity() == 0);
    }

    /**
     * 列表需要能演示四种策略，其中"连续执行被平台记为 taskType=1"这一分支
     * 只有存在 minBatteryCapacity > 0 的 taskType=1 任务时才可见。
     */
    @Test
    void listTasksPaged_shouldCoverEveryStrategy_whenAggregating() {
        List<FlightTaskDto> tasks = service.listTasksPaged(null, 1, 100).tasks();

        assertThat(tasks).anyMatch(t -> t.taskType() == 1 && t.minBatteryCapacity() == 0);
        assertThat(tasks).anyMatch(t -> t.taskType() == 1 && t.minBatteryCapacity() > 0);
        assertThat(tasks).anyMatch(t -> t.taskType() == 2 || t.taskType() == 3 || t.taskType() == 4);
        assertThat(tasks).anyMatch(t -> t.taskType() == 5 || t.taskType() == 6);
    }

    @Test
    void getTaskDetail_shouldReuseListMinBatteryCapacity() {
        FlightTaskDto listed = service.listTasksPaged(null, 1, 100).tasks().stream()
                .filter(t -> t.taskType() == 5)
                .findFirst()
                .orElseThrow();

        assertThat(service.getTaskDetail(listed.taskUuid()).minBatteryCapacity())
                .isEqualTo(listed.minBatteryCapacity());
    }

    @Test
    void listTasksPaged_shouldReturnDeterministicData() {
        FlightTaskPage first = service.listTasksPaged(null, 1, 20);
        FlightTaskPage second = service.listTasksPaged(null, 1, 20);
        assertThat(first.tasks()).isEqualTo(second.tasks());
    }

    // ── existsTaskWithName 名称唯一性校验测试 ──

    @Test
    void existsTaskWithName_shouldReturnFalse_whenOnlyMatchIsExcludedTask() {
        // mock 数据由 12 个名称生成 30 条任务，天然存在重名：
        // 排除自身后是否仍有冲突，只能与「按名称分组计数」的结果比对
        FlightTaskDto target = firstMockTask();
        boolean expected = countByName(target.name()) > 1;

        assertThat(service.existsTaskWithName(target.name(), target.taskUuid()))
                .isEqualTo(expected);
    }

    @Test
    void existsTaskWithName_shouldReturnTrue_whenNameExists() {
        FlightTaskDto target = firstMockTask();

        assertThat(service.existsTaskWithName(target.name(), null)).isTrue();
        assertThat(service.existsTaskWithName(target.name(), "不存在的-UUID")).isTrue();
    }

    @Test
    void existsTaskWithName_shouldReturnFalse_whenNameAbsent() {
        assertThat(service.existsTaskWithName("绝不存在的任务名称", null)).isFalse();
    }

    @Test
    void existsTaskWithName_shouldReturnFalse_whenNameIsNullOrBlank() {
        assertThat(service.existsTaskWithName(null, null)).isFalse();
        assertThat(service.existsTaskWithName("   ", null)).isFalse();
    }

    private FlightTaskDto firstMockTask() {
        return mockTasks().get(0);
    }

    private List<FlightTaskDto> mockTasks() {
        return service.listTasksPaged(null, 1, MOCK_TASK_FULL_PAGE_SIZE).tasks();
    }

    private long countByName(String name) {
        return mockTasks().stream().filter(t -> t.name().equals(name)).count();
    }

    /** mock 实现固定生成 30 条任务，用大于总数的页大小一次取全量做名称分组统计 */
    private static final int MOCK_TASK_FULL_PAGE_SIZE = 100;
}
