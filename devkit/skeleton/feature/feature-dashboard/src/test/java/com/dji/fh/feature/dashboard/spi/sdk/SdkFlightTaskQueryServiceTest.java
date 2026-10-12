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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskPage;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.TaskSummary;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.FlightTrackInfo;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackParams;
import com.dji.flighthubsdk.bean.GetFlightTaskTrackResults;
import com.dji.flighthubsdk.bean.PaginationInfo;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link SdkFlightTaskQueryService} 单元测试。
 *
 * <p>SDK 模块通过覆写 package-private 的 {@code requireFlightTaskModule()} 注入 mock，
 * 避免依赖 JNI 原生库。
 *
 * @author DJI DevKit Team
 */
class SdkFlightTaskQueryServiceTest {

    private static final String DOCK_SN = "8UUXP4F00A160H";
    private static final String SECOND_DOCK_SN = "8UUXP4F00A160J";
    private static final int STATUS_SUCCESS = 5;

    private DeviceQueryService deviceQueryService;
    private WaylineQueryService waylineQueryService;
    private FlightTaskModule flightTaskModule;
    private SdkFlightTaskQueryService service;

    /** 每次 getFlightTaskList 调用收到的 task_type，按调用顺序记录 */
    private List<Integer> capturedTaskTypes;

    /** 每次 getFlightTaskList 调用收到的 SN 列表，按调用顺序记录 */
    private List<List<String>> capturedSns;

    @BeforeEach
    void setUp() throws Exception {
        deviceQueryService = mock(DeviceQueryService.class);
        waylineQueryService = mock(WaylineQueryService.class);
        flightTaskModule = mock(FlightTaskModule.class);
        capturedTaskTypes = new ArrayList<>();
        capturedSns = new ArrayList<>();

        when(deviceQueryService.listDevices()).thenReturn(List.of(
                new DeviceSnapshot(DOCK_SN, "DOCK", "0-89-0", "机场", "机场", true)));
        when(waylineQueryService.getWaylineNames(any())).thenReturn(Map.of());

        // getFlightTaskTrack 默认返回带轨迹的结果，成功回调
        doAnswer(invocation -> {
            GetFlightTaskTrackParams params = invocation.getArgument(0);
            FlightHubCallback.GetFlightTaskTrackCallback cb = invocation.getArgument(1);
            GetFlightTaskTrackResults results = new GetFlightTaskTrackResults();
            results.setUuid(params.getTaskUuid());
            FlightTrackInfo track = new FlightTrackInfo();
            track.setFlightDistance(1200);
            results.setTrack(track);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), results);
            return null;
        }).when(flightTaskModule).getFlightTaskTrack(
                any(GetFlightTaskTrackParams.class),
                any(FlightHubCallback.GetFlightTaskTrackCallback.class));

        service = new SdkFlightTaskQueryService(deviceQueryService, waylineQueryService) {
            @Override
            FlightTaskModule requireFlightTaskModule() {
                return flightTaskModule;
            }
        };
    }

    @Test
    void listTasksPaged_shouldQueryEveryTaskType_whenListing() {
        stubByTaskType(taskType -> List.of(task("uuid-" + taskType, "1700000000000")));

        service.listTasksPaged(null, 1, 10);

        assertThat(capturedTaskTypes).containsExactly(0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    void listTasksPaged_shouldIncludeScheduledTasks_whenOnlyReturnedByNonZeroTaskType() {
        stubByTaskType(taskType -> taskType == 2
                ? List.of(task("scheduled-uuid", "1700000000000"))
                : List.of());

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.totalCount()).isEqualTo(1);
        assertThat(page.tasks()).extracting(FlightTaskDto::taskUuid)
                .containsExactly("scheduled-uuid");
    }

    @Test
    void listTasksPaged_shouldCarryMinBatteryCapacity_whenTaskIsContinuousExecution() {
        stubByTaskType(taskType -> taskType == 1
                ? List.of(task("continuous-uuid", "1700000000000", STATUS_SUCCESS, 30))
                : List.of());

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.tasks()).extracting(FlightTaskDto::minBatteryCapacity).containsExactly(30);
    }

    @Test
    void listTasksPaged_shouldDeduplicate_whenSameTaskReturnedByMultipleTaskTypes() {
        stubByTaskType(taskType -> List.of(task("shared-uuid", "1700000000000")));

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.totalCount()).isEqualTo(1);
    }

    @Test
    void listTasksPaged_shouldSortByCreatedAtDesc_whenMergingAcrossTaskTypes() {
        stubByTaskType(taskType -> switch (taskType) {
            case 0 -> List.of(task("old", "1700000000000"));
            case 1 -> List.of(task("newest", "1900000000000"));
            default -> List.of(task("middle", "1800000000000"));
        });

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.tasks()).extracting(FlightTaskDto::taskUuid)
                .containsExactly("newest", "middle", "old");
    }

    @Test
    void listTasksPaged_shouldKeepOtherTypes_whenOneTaskTypeFails() {
        doAnswer(invocation -> {
            FlightTaskListParams params = invocation.getArgument(0);
            FlightHubCallback.GetFlightTaskListCallback cb = invocation.getArgument(1);
            capturedTaskTypes.add(params.getTaskType());
            capturedSns.add(params.getSn() == null ? List.of() : List.copyOf(params.getSn()));
            if (params.getTaskType() == 1) {
                cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_INVALID_PARAM), null);
                return null;
            }
            cb.onResult(success(), page(List.of(task("uuid-" + params.getTaskType(),
                    "1700000000000"))));
            return null;
        }).when(flightTaskModule).getFlightTaskList(
                any(FlightTaskListParams.class),
                any(FlightHubCallback.GetFlightTaskListCallback.class));

        FlightTaskPage result = service.listTasksPaged(null, 1, 10);

        assertThat(result.tasks()).extracting(FlightTaskDto::taskUuid)
                .containsExactlyInAnyOrder("uuid-0", "uuid-2", "uuid-3", "uuid-4", "uuid-5", "uuid-6");
    }

    @Test
    void listTasksPaged_shouldIncludeOfflineDocks_whenQueryingSns() {
        when(deviceQueryService.listDevices()).thenReturn(List.of(
                new DeviceSnapshot("OFFLINE-DOCK", "DOCK", "0-89-0", "离线机场", "离线机场", false)));
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("uuid-offline", "1700000000000"))
                : List.of());

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.totalCount()).isEqualTo(1);
    }

    @Test
    void listTasksPaged_shouldReturnEmpty_whenNoAirportDevice() {
        when(deviceQueryService.listDevices()).thenReturn(List.of(
                new DeviceSnapshot("DRONE-SN", "DRONE", "0-89-0", "飞机", "飞机", true)));

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.tasks()).isEmpty();
        assertThat(page.totalCount()).isZero();
        assertThat(capturedTaskTypes).isEmpty();
    }

    @Test
    void listTasksPaged_shouldFilterByStatus_whenStatusGiven() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("done", "1700000000000", STATUS_SUCCESS),
                          task("running", "1700000000001", 2))
                : List.of());

        FlightTaskPage page = service.listTasksPaged("SUCCESS", 1, 10);

        assertThat(page.totalCount()).isEqualTo(1);
        assertThat(page.tasks()).extracting(FlightTaskDto::taskUuid).containsExactly("done");
    }

    @Test
    void listTasksPaged_shouldReportHasMore_whenResultsExceedPageSize() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("a", "1700000000002"), task("b", "1700000000001"),
                          task("c", "1700000000000"))
                : List.of());

        FlightTaskPage first = service.listTasksPaged(null, 1, 2);

        assertThat(first.tasks()).extracting(FlightTaskDto::taskUuid).containsExactly("a", "b");
        assertThat(first.totalCount()).isEqualTo(3);
        assertThat(first.hasMore()).isTrue();
    }

    @Test
    void listTasksPaged_shouldReturnEmptyPage_whenPageBeyondRange() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("only", "1700000000000"))
                : List.of());

        FlightTaskPage page = service.listTasksPaged(null, 5, 10);

        assertThat(page.tasks()).isEmpty();
        assertThat(page.totalCount()).isEqualTo(1);
        assertThat(page.hasMore()).isFalse();
    }

    @Test
    void listTasks_shouldQueryEveryTaskType_whenAggregating() throws Exception {
        stubByTaskType(taskType -> List.of(task("uuid-" + taskType, nowMillis())));

        service.listTasks("month");

        assertThat(capturedTaskTypes).containsExactly(0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    void listTasks_shouldAggregateDistanceFromTrack_whenTrackAvailable() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("uuid-dist", nowMillis(), STATUS_SUCCESS))
                : List.of());
        // createdAt 用当前时间，确保在「month」范围内

        List<TaskSummary> summaries = service.listTasks("month");

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).distanceMeters()).isEqualTo(1200.0);
    }

    @Test
    void listTasksPaged_shouldUseConcurrentBatchDistance_whenFillingDtos() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("done", nowMillis(), STATUS_SUCCESS),
                          task("failed", nowMillis(), 4))
                : List.of());

        FlightTaskPage page = service.listTasksPaged("SUCCESS", 1, 10);

        assertThat(page.tasks()).hasSize(1);
        assertThat(page.tasks()).extracting(FlightTaskDto::taskUuid).containsExactly("done");
        assertThat(page.tasks().get(0).distanceMeters()).isEqualTo(1200.0);
    }

    @Test
    void listTasksPaged_shouldZeroDistance_whenNonSuccessTask() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("running", nowMillis(), 2))
                : List.of());

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.tasks()).hasSize(1);
        assertThat(page.tasks().get(0).distanceMeters()).isZero();
    }

    @Test
    void listTasksPaged_shouldMapStatus1ToFailed_whenStartingFailure() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("start-fail", nowMillis(), 1))
                : List.of());

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.tasks()).hasSize(1);
        assertThat(page.tasks().get(0).status()).isEqualTo("FAILED");
    }

    @Test
    void listTasksPaged_shouldMapExtendedStatus_whenStatus7To10Returned() {
        stubByTaskType(taskType -> switch (taskType) {
            case 0 -> List.of(
                    task("timeout", nowMillis(), 7),
                    task("partial", nowMillis(), 8));
            case 1 -> List.of(
                    task("preparing", nowMillis(), 9),
                    task("queuing", nowMillis(), 10));
            default -> List.of();
        });

        FlightTaskPage page = service.listTasksPaged(null, 1, 10);

        assertThat(page.tasks()).extracting(FlightTaskDto::taskUuid, FlightTaskDto::status)
                .containsExactlyInAnyOrder(
                        tuple("timeout", "FAILED"),
                        tuple("partial", "FAILED"),
                        tuple("preparing", "PREPARING"),
                        tuple("queuing", "QUEUED"));
    }

    // ── existsTaskWithName 名称唯一性校验测试 ──

    @Test
    void existsTaskWithName_shouldReturnTrue_whenNameExists() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(namedTask("uuid-a", "巡检任务A"))
                : List.of());

        assertThat(service.existsTaskWithName("巡检任务A", null)).isTrue();
    }

    @Test
    void existsTaskWithName_shouldReturnFalse_whenNameAbsent() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(namedTask("uuid-a", "巡检任务A"))
                : List.of());

        assertThat(service.existsTaskWithName("巡检任务B", null)).isFalse();
    }

    @Test
    void existsTaskWithName_shouldReturnFalse_whenNameIsNullOrBlank() {
        stubByTaskType(taskType -> List.of(namedTask("uuid-a", "巡检任务A")));

        assertThat(service.existsTaskWithName(null, null)).isFalse();
        assertThat(service.existsTaskWithName("   ", null)).isFalse();
        // 空名称不应触发 SDK 查询
        assertThat(capturedTaskTypes).isEmpty();
    }

    @Test
    void existsTaskWithName_shouldReturnFalse_whenOnlyMatchIsExcludedTask() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(namedTask("uuid-self", "唯一任务"))
                : List.of());

        assertThat(service.existsTaskWithName("唯一任务", "uuid-self")).isFalse();
        assertThat(service.existsTaskWithName("唯一任务", null)).isTrue();
    }

    @Test
    void existsTaskWithName_shouldReturnTrue_whenOtherTaskSharesName() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(namedTask("uuid-1", "重名任务"), namedTask("uuid-2", "重名任务"))
                : List.of());

        assertThat(service.existsTaskWithName("重名任务", "uuid-1")).isTrue();
    }

    @Test
    void existsTaskWithName_shouldSearchAcrossEveryTaskType() {
        // 名称唯一性校验复用全量拉取，需覆盖所有 task_type 而非仅即时任务
        stubByTaskType(taskType -> taskType == 2
                ? List.of(namedTask("uuid-scheduled", "定时巡检"))
                : List.of());

        assertThat(service.existsTaskWithName("定时巡检", null)).isTrue();
        // 断言需与 QUERY_TASK_TYPES 保持一致：4（重复定时子任务）/ 6（连续执行子任务）
        // 同样是列表要展示的任务，名称唯一性校验必须一并覆盖
        assertThat(capturedTaskTypes).containsExactly(0, 1, 2, 3, 4, 5, 6);
    }

    @Test
    void existsTaskWithName_shouldReturnFalse_whenNoAirportDevice() {
        when(deviceQueryService.listDevices()).thenReturn(List.of(
                new DeviceSnapshot("DRONE-SN", "DRONE", "0-89-0", "飞机", "飞机", true)));

        assertThat(service.existsTaskWithName("任意任务", null)).isFalse();
        assertThat(capturedTaskTypes).isEmpty();
    }

    @Test
    void listTaskWindows_shouldQueryAllDevicesInOnePass_whenBatchRequested() {
        when(deviceQueryService.listDevices()).thenReturn(List.of(
                new DeviceSnapshot(DOCK_SN, "DOCK", "0-89-0", "机场A", "机场A", true),
                new DeviceSnapshot(SECOND_DOCK_SN, "DOCK", "0-89-1", "机场B", "机场B", true)));
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("uuid-a", DOCK_SN, "1700000000000"),
                          task("uuid-b", SECOND_DOCK_SN, "1700000000001"))
                : List.of());

        List<FlightTaskWindow> windows = service.listTaskWindows(List.of(DOCK_SN, SECOND_DOCK_SN));

        // 多设备合并为一次扇出：QUERY_TASK_TYPES 各查一次，而非逐台各查一次
        assertThat(capturedTaskTypes).containsExactly(0, 1, 2, 3, 4, 5, 6);
        assertThat(capturedSns).allSatisfy(sns ->
                assertThat(sns).containsExactlyInAnyOrder(DOCK_SN, SECOND_DOCK_SN));
        assertThat(windows).extracting(FlightTaskWindow::taskUuid, FlightTaskWindow::sn)
                .containsExactlyInAnyOrder(
                        tuple("uuid-a", DOCK_SN),
                        tuple("uuid-b", SECOND_DOCK_SN));
    }

    @Test
    void listTaskWindows_shouldSkipSdkQuery_whenAllSnsBlank() {
        List<FlightTaskWindow> windows = service.listTaskWindows(Arrays.asList(null, " "));

        assertThat(windows).isEmpty();
        assertThat(capturedTaskTypes).isEmpty();
    }

    @Test
    void listTaskWindows_shouldDelegateToBatch_whenSingleSnGiven() {
        stubByTaskType(taskType -> taskType == 0
                ? List.of(task("uuid-a", DOCK_SN, "1700000000000"))
                : List.of());

        List<FlightTaskWindow> windows = service.listTaskWindows(DOCK_SN);

        assertThat(windows).extracting(FlightTaskWindow::taskUuid).containsExactly("uuid-a");
        assertThat(capturedSns).allSatisfy(sns -> assertThat(sns).containsExactly(DOCK_SN));
    }

    /**
     * 按 task_type 返回不同任务列表；同时记录每次调用的 task_type。
     */
    private void stubByTaskType(IntFunction<List<TaskDetail>> tasksOfType) {
        doAnswer(invocation -> {
            FlightTaskListParams params = invocation.getArgument(0);
            FlightHubCallback.GetFlightTaskListCallback cb = invocation.getArgument(1);
            capturedTaskTypes.add(params.getTaskType());
            capturedSns.add(params.getSn() == null ? List.of() : List.copyOf(params.getSn()));
            cb.onResult(success(), page(tasksOfType.apply(params.getTaskType())));
            return null;
        }).when(flightTaskModule).getFlightTaskList(
                any(FlightTaskListParams.class),
                any(FlightHubCallback.GetFlightTaskListCallback.class));
    }

    private static FlightHubError success() {
        return new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
    }

    private static FlightTaskListResult page(List<TaskDetail> tasks) {
        return new FlightTaskListResult(tasks, new PaginationInfo(1, 100, tasks.size()));
    }

    private static TaskDetail task(String uuid, String createdAtMillis) {
        return task(uuid, createdAtMillis, STATUS_SUCCESS);
    }

    private static TaskDetail task(String uuid, String createdAtMillis, int status) {
        return task(uuid, "任务-" + uuid, DOCK_SN, createdAtMillis, status);
    }

    private static TaskDetail task(String uuid, String sn, String createdAtMillis) {
        return task(uuid, "任务-" + uuid, sn, createdAtMillis, STATUS_SUCCESS);
    }

    private static TaskDetail task(String uuid, String sn, String createdAtMillis, int status) {
        return task(uuid, "任务-" + uuid, sn, createdAtMillis, status);
    }

    /** 构造携带最低电量阈值的任务，用于区分「连续执行」与「立即」策略。 */
    private static TaskDetail task(String uuid, String createdAtMillis, int status, int minBatteryCapacity) {
        return namedTask(uuid, "任务-" + uuid, createdAtMillis, status, minBatteryCapacity);
    }

    /**
     * 构造指定名称的任务（创建时间取当前时间，确保落在查询时间窗口内）。
     */
    private static TaskDetail namedTask(String uuid, String name) {
        return namedTask(uuid, name, nowMillis(), STATUS_SUCCESS);
    }

    private static TaskDetail namedTask(String uuid, String name, String createdAtMillis, int status) {
        return namedTask(uuid, name, createdAtMillis, status, 0);
    }

    /**
     * 构造指定名称与最低电量阈值的任务。
     *
     * @param uuid                任务 UUID
     * @param name                任务名称
     * @param createdAtMillis     创建时间（毫秒字符串）
     * @param status              任务状态码
     * @param minBatteryCapacity  最低电量阈值，0 表示未设置
     * @return SDK 任务详情
     */
    private static TaskDetail namedTask(String uuid, String name, String createdAtMillis, int status,
                                        int minBatteryCapacity) {
        return task(uuid, name, DOCK_SN, createdAtMillis, status, minBatteryCapacity);
    }

    /**
     * 构造任务详情（最低电量阈值取 0）。
     *
     * @param uuid             任务 UUID
     * @param name             任务名称
     * @param sn               机场 SN
     * @param createdAtMillis  创建时间（毫秒字符串）
     * @param status           任务状态码
     * @return SDK 任务详情
     */
    private static TaskDetail task(String uuid, String name, String sn, String createdAtMillis, int status) {
        return task(uuid, name, sn, createdAtMillis, status, 0);
    }

    /**
     * 构造任务详情的唯一入口：名称、SN、创建时间、状态、最低电量阈值均可指定。
     *
     * @param uuid                任务 UUID
     * @param name                任务名称
     * @param sn                  机场 SN
     * @param createdAtMillis     创建时间（毫秒字符串）
     * @param status              任务状态码
     * @param minBatteryCapacity  最低电量阈值，0 表示未设置
     * @return SDK 任务详情
     */
    private static TaskDetail task(String uuid, String name, String sn, String createdAtMillis, int status,
                                   int minBatteryCapacity) {
        TaskDetail detail = new TaskDetail();
        detail.setUuid(uuid);
        detail.setName(name);
        detail.setSn(sn);
        detail.setStatus(status);
        detail.setTaskType(2);
        detail.setWaylineUuid("wayline-" + uuid);
        detail.setCreatedAt(createdAtMillis);
        detail.setMinBatteryCapacity(minBatteryCapacity);
        return detail;
    }

    private static String nowMillis() {
        return String.valueOf(System.currentTimeMillis());
    }
}
