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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 飞行任务查询 mock 实现。
 *
 * <p>使用基于日期的确定性种子生成数据，保证同一天内多次查询结果一致。
 *
 * @author DJI DevKit Team
 */
public class MockFlightTaskQueryService implements FlightTaskQueryService {

    private static final String[] STATUSES = {"SUCCESS", "FAILED", "CANCELLED"};
    private static final String[] ALL_STATUSES = {"SUCCESS", "FAILED", "CANCELLED", "EXECUTING", "PAUSED",
            "CREATED", "PREPARING", "QUEUED"};
    /**
     * 看板任务分布（{@link #listTasks}）使用的任务类型。
     *
     * <p>看板只有"计划任务/手飞任务"两个堆叠系列，二元划分由
     * {@code DashboardService#aggregateTasks} 按 {@code taskType == 2} 判定，
     * 与策略细分无关，故保持原有的 1/2 取值以维持看板数值稳定。
     */
    private static final int[] SUMMARY_TASK_TYPES = {1, 2};
    /**
     * 分页列表（{@link #listTasksPaged}）使用的任务类型。
     *
     * <p>覆盖平台 TaskType 枚举的全部业务取值，使 mock 模式下
     * 列表"类型"列能演示立即 / 单次定时 / 重复定时 / 连续执行四类策略：
     * 1=即时, 2=重复定时, 3=单次定时, 4=重复定时子任务, 5=连续执行, 6=连续执行子任务。
     */
    private static final int[] LIST_TASK_TYPES = {1, 2, 3, 4, 5, 6};

    /** 任务类型：即时（平台也会用它表示连续执行，需靠电量阈值区分） */
    private static final int TASK_TYPE_IMMEDIATE = 1;

    /** 任务类型：连续执行 */
    private static final int TASK_TYPE_CONTINUOUS = 5;

    /** 任务类型：连续执行子任务 */
    private static final int TASK_TYPE_CONTINUOUS_SUBTASK = 6;

    /** mock 最低电量阈值（百分比）：连续执行类任务的固定取值，足以让前端识别出该策略 */
    private static final int MIN_BATTERY_CAPACITY = 30;

    private static final String[] TASK_NAMES = {
            "厂区北侧巡检", "光伏板定期巡查", "仓库周边安防巡逻", "河道水质监测",
            "建筑工地进度航拍", "高压线塔通道巡检", "农田植保喷洒", "园区道路巡查",
            "应急响应勘测", "港口船舶监控", "桥梁结构检测", "林区防火巡护"
    };
    private static final String[] MOCK_SNS = {
            "1581F6GKD233400001", "1581F6GKD233400002", "1581F6GKD233400003"
    };

    /** Mock 详情演示用的时间窗口长度（秒） */
    private static final long MOCK_DURATION_SECONDS = 3600L;

    /** Mock 详情执行窗口相对当前时间的延迟（秒）：保证编辑表单"执行时间必须是未来时间"校验可通过 */
    private static final long MOCK_BEGIN_DELAY_SECONDS = 3600L;

    /** mock 设备名模板，与 {@code MockWaylineQueryService} 的航线名模板风格一致 */
    private static final String MOCK_DEVICE_NAME_PREFIX = "演示设备-";

    /** mock 航线名模板，取值需与 {@code MockWaylineQueryService} 保持一致 */
    private static final String MOCK_WAYLINE_NAME_PREFIX = "演示航线-";

    @Override
    public List<TaskSummary> listTasks(String range) {
        int days = toDays(range);
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay();
        Random rng = new Random(seed);

        List<TaskSummary> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            int taskCount = 5 + rng.nextInt(20);
            for (int j = 0; j < taskCount; j++) {
                String taskId = "MOCK-" + date + "-" + j;
                String status = STATUSES[rng.nextInt(STATUSES.length)];
                int taskType = SUMMARY_TASK_TYPES[rng.nextInt(SUMMARY_TASK_TYPES.length)];
                // 模拟单架次里程：500m ~ 3km，单位为米
                double distanceMeters = (0.5 + rng.nextDouble() * 2.5) * 1000;
                result.add(new TaskSummary(taskId, status, taskType, date,
                        600 + rng.nextInt(3000), distanceMeters));
            }
        }
        return result;
    }

    @Override
    public FlightTaskPage listTasksPaged(String status, int page, int pageSize) {
        List<FlightTaskDto> allTasks = generateAllMockTasks();

        List<FlightTaskDto> filtered = (status == null)
                ? allTasks
                : allTasks.stream().filter(t -> t.status().equals(status)).toList();

        int totalCount = filtered.size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<FlightTaskDto> paged = (startIndex >= totalCount)
                ? List.of()
                : filtered.subList(startIndex, endIndex);

        boolean hasMore = endIndex < totalCount;
        return new FlightTaskPage(paged, totalCount, hasMore);
    }

    @Override
    public FlightTaskDetailDto getTaskDetail(String taskUuid) {
        List<FlightTaskDto> allTasks = generateAllMockTasks();
        FlightTaskDto matched = allTasks.stream()
                .filter(t -> t.taskUuid().equals(taskUuid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskUuid));

        // Mock 数据无真实执行时间窗口，取当前时间 + 延迟作为未来执行窗口
        // （过去时间会被前端编辑表单"执行时间必须是未来时间"校验拦截，导致 mock 任务无法编辑）
        long beginAt = Instant.now().getEpochSecond() + MOCK_BEGIN_DELAY_SECONDS;
        return new FlightTaskDetailDto(
                matched.taskUuid(), matched.name(), matched.sn(),
                MOCK_DEVICE_NAME_PREFIX + matched.sn(),
                matched.status(), matched.taskType(), matched.waylineUuid(),
                MOCK_WAYLINE_NAME_PREFIX + matched.waylineUuid(),
                beginAt, beginAt + MOCK_DURATION_SECONDS,
                100.0, 0, 1, 1, matched.minBatteryCapacity(), matched.isBreakPointResume());
    }

    /**
     * Mock 模式不提供任务时间窗。
     *
     * <p>Mock 任务无真实执行时间（详情里的窗口是相对当前时间临时构造的），
     * 返回随机时间窗只会给冲突校验制造假数据；配合 {@link MockFlightTaskConflictService}
     * 恒不拦截，Mock 模式下保持"可自由创建任务"的演示体验。
     *
     * @param sn 机场 SN
     * @return 空列表
     */
    @Override
    public List<FlightTaskWindow> listTaskWindows(String sn) {
        return List.of();
    }

    @Override
    public boolean existsTaskWithName(String name, String excludeTaskUuid) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return generateAllMockTasks().stream()
                .anyMatch(t -> t.name().equals(name)
                        && !t.taskUuid().equals(excludeTaskUuid));
    }

    private List<FlightTaskDto> generateAllMockTasks() {
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay();
        Random rng = new Random(seed);

        List<FlightTaskDto> result = new ArrayList<>();
        int immediateOrdinal = 0;
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);
            long createdAt = date.atTime(16, 0)
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();

            String taskUuid = String.format("mock-task-%03d", i + 1);
            String name = TASK_NAMES[rng.nextInt(TASK_NAMES.length)];
            String sn = MOCK_SNS[rng.nextInt(MOCK_SNS.length)];
            String taskStatus = ALL_STATUSES[rng.nextInt(ALL_STATUSES.length)];
            // 任务类型按序循环而非随机：保证任一页都能同时看到四种策略，
            // mock 模式下演示/联调不会因为随机种子而漏掉"连续执行"分支
            int taskType = LIST_TASK_TYPES[i % LIST_TASK_TYPES.length];
            int minBatteryCapacity = resolveMockMinBatteryCapacity(taskType, immediateOrdinal);
            if (taskType == TASK_TYPE_IMMEDIATE) {
                immediateOrdinal++;
            }
            String waylineUuid = "wl-" + String.format("%03d", i + 1);
            boolean isBreakPointResume = rng.nextBoolean();

            result.add(new FlightTaskDto(taskUuid, name, sn, taskStatus, taskType, waylineUuid, createdAt,
                (0.5 + rng.nextDouble() * 2.5) * 1000, minBatteryCapacity, isBreakPointResume));
        }
        return result;
    }

    /**
     * 生成与 taskType 自洽的最低电量阈值（百分比）。
     *
     * <p>平台会把连续执行任务也记为 {@code taskType=1}，只能靠
     * {@code min_battery_capacity > 0} 与"立即"任务区分。mock 需复现这一特征
     * （部分 taskType=1 的任务带阈值），否则列表的"连续执行"分支在 mock 模式下
     * 永远走不到，演示与联调都看不到效果。
     *
     * @param taskType       任务类型
     * @param immediateOrdinal 当前任务在"taskType=1"序列中的序号（从 0 开始），
     *                        按奇偶交替产出"立即"与"连续执行"两类样例
     * @return 最低电量阈值，不适用时为 0
     */
    private int resolveMockMinBatteryCapacity(int taskType, int immediateOrdinal) {
        if (taskType == TASK_TYPE_CONTINUOUS || taskType == TASK_TYPE_CONTINUOUS_SUBTASK) {
            return MIN_BATTERY_CAPACITY;
        }
        if (taskType == TASK_TYPE_IMMEDIATE && immediateOrdinal % 2 == 1) {
            return MIN_BATTERY_CAPACITY;
        }
        return 0;
    }

    private int toDays(String range) {
        return switch (range) {
            case "week", "recent_week" -> 7;
            case "month", "recent_month" -> 30;
            case "half_year" -> 180;
            case "year" -> 365;
            default -> 30;
        };
    }
}
