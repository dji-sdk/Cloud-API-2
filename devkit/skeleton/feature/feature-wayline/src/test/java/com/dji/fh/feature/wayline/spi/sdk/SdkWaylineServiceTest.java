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

package com.dji.fh.feature.wayline.spi.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dji.fh.feature.wayline.WaylineListResponse;
import com.dji.fh.feature.wayline.WaylineExecutionsResponse;
import com.dji.fh.feature.wayline.WaylineProperties;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlightTaskListParams;
import com.dji.flighthubsdk.bean.FlightTaskListResult;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.ImportKmzItem;
import com.dji.flighthubsdk.bean.ImportKmzResult;
import com.dji.flighthubsdk.bean.ImportWaylineEventResult;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.bean.PaginationInfo;
import com.dji.flighthubsdk.bean.SubmitWaylineInfoParam;
import com.dji.flighthubsdk.bean.TaskDetail;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.bean.WaylineListPaginationResult;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.core.WaylineManageModule;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * SdkWaylineService 单元测试。
 *
 * <p>使用 package-private 方法覆写注入 mock Module，避免 Java 24 下 Mockito 无法
 * inline-mock SDK 具体类的问题。覆盖场景：
 * <ul>
 *   <li>SDK 未初始化时的异常行为</li>
 *   <li>listExecutions 的 waylineUuid 过滤逻辑</li>
 *   <li>listExecutions 的 status==5（成功）过滤逻辑</li>
 *   <li>queryPhotoCount 使用 deviceSn + 时间范围（非 flightId）</li>
 *   <li>queryPhotoCount 失败回退为 0</li>
 *   <li>queryPhotoCount 结果为 null 时回退为 0</li>
 *   <li>空列表 break 条件</li>
 *   <li>航线列表补拉详情填充航点数 / 距离，以及缓存与降级行为</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
class SdkWaylineServiceTest {

    /** 飞行任务状态：执行中（SDK 枚举 2）。 */
    private static final int STATUS_EXECUTING = 2;

    /** 飞行任务状态：成功完成（SDK 枚举 5，须与 SdkWaylineService.TASK_STATUS_SUCCESS 一致）。 */
    private static final int STATUS_SUCCESS = 5;

    /** 飞行任务状态：失败（SDK 枚举 6）。 */
    private static final int STATUS_FAILED = 6;

    /** 导入航线用例：上传后平台返回的对象存储 key。 */
    private static final String KMZ_OBJECT_KEY = "kmz/2026/demo.kmz";

    /** 导入航线用例：航线名称。 */
    private static final String IMPORTED_NAME = "测试航线-1";

    /** 导入航线用例：平台登记成功后回推的航线 UUID。 */
    private static final String IMPORTED_UUID = "b2b6041b-8cac-4cf1-96a9-b76516788018";

    /** 导入航线用例：监听注册返回的 listenId。 */
    private static final int LISTEN_ID = 7;

    /** 导入航线用例：平台以异步模式导入时返回的请求 id。 */
    private static final String ASYNC_REQUEST_ID = "async-req-001";

    /** 导入完成事件的失败码。 */
    private static final int IMPORT_EVENT_CODE_FAILED = 100001;

    /** 按名称反查 UUID 的重试次数，与 SdkWaylineService 保持一致。 */
    private static final int UUID_LOOKUP_MAX_ATTEMPTS = 3;

    /** 航线列表用例：航线 UUID。 */
    private static final String LISTED_UUID = "wl-uuid-1";

    /** 航线列表用例：航线更新时间（毫秒），同时参与详情缓存 key。 */
    private static final long LISTED_UPDATED_AT_MILLIS = 1_700_000_000_000L;

    /** 航线详情用例：航点数量。 */
    private static final int DETAIL_POINT_COUNT = 12;

    /** 航线详情用例：飞行距离（米，含小数用于验证四舍五入）。 */
    private static final double DETAIL_DISTANCE_METERS = 1234.6;

    /** 航线详情用例：四舍五入后的飞行距离（米）。 */
    private static final long DETAIL_DISTANCE_ROUNDED = 1235L;

    private FlightTaskModule flightTaskModule;
    private MediaServiceModule mediaServiceModule;
    private WaylineManageModule waylineManageModule;
    private DeviceQueryService deviceQueryService;
    private SdkWaylineService service;

    @BeforeEach
    void setUp() {
        WaylineProperties props = new WaylineProperties(20, 100, true);
        flightTaskModule = mock(FlightTaskModule.class);
        mediaServiceModule = mock(MediaServiceModule.class);
        waylineManageModule = mock(WaylineManageModule.class);
        deviceQueryService = mock(DeviceQueryService.class);

        // 默认返回一个设备 SN
        org.mockito.Mockito.when(deviceQueryService.listDevices()).thenReturn(
                List.of(new DeviceSnapshot("drone-1", "DOCK", null, null, null, true)));

        service = new SdkWaylineService(props, deviceQueryService) {
            @Override
            FlightTaskModule requireFlightTaskModule() {
                return flightTaskModule;
            }

            @Override
            MediaServiceModule requireMediaServiceModule() {
                return mediaServiceModule;
            }

            @Override
            WaylineManageModule requireWaylineManageModule() {
                return waylineManageModule;
            }
        };
    }

    @Test
    void listExecutions_shouldThrow_whenSdkNotInitialized() {
        DeviceQueryService dqs = mock(DeviceQueryService.class);
        org.mockito.Mockito.when(dqs.listDevices()).thenReturn(
                List.of(new DeviceSnapshot("dock-1", "DOCK", null, null, null, true)));
        SdkWaylineService realService = new SdkWaylineService(
                new WaylineProperties(20, 100, true), dqs);

        assertThatThrownBy(() -> realService.listExecutions("any-uuid"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class,
                        NullPointerException.class, ExceptionInInitializerError.class);
    }

    @Test
    void listExecutions_shouldFilterByWaylineUuid() {
        TaskDetail matched = createTask("task-1", "target-wl", STATUS_SUCCESS, "任务A",
                "drone-sn-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");
        TaskDetail unmatched = createTask("task-2", "other-wl", STATUS_SUCCESS, "任务B",
                "drone-sn-2", "2026-04-01T11:00:00Z", "2026-04-01T11:30:00Z");
        stubFlightTaskList(List.of(matched, unmatched), 2);
        stubMediaFileCount(42);

        WaylineExecutionsResponse resp = service.listExecutions("target-wl");

        assertThat(resp.executions()).hasSize(1);
        // id 保持任务 UUID（唯一）；folderId 为媒体文件夹 id
        assertThat(resp.executions().get(0).id()).isEqualTo("task-1");
        assertThat(resp.executions().get(0).folderId()).isEqualTo(265);
        assertThat(resp.executions().get(0).photoCount()).isEqualTo(42);
    }

    @Test
    void listExecutions_shouldFilterByStatusSuccess() {
        TaskDetail success = createTask("task-ok", "wl-1", STATUS_SUCCESS, "成功任务",
                "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");
        TaskDetail executing = createTask("task-run", "wl-1", STATUS_EXECUTING, "执行中",
                "drone-1", "2026-04-01T11:00:00Z", null);
        TaskDetail failed = createTask("task-fail", "wl-1", STATUS_FAILED, "失败任务",
                "drone-1", "2026-04-01T12:00:00Z", "2026-04-01T12:05:00Z");
        stubFlightTaskList(List.of(success, executing, failed), 3);
        stubMediaFileCount(100);

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).hasSize(1);
        assertThat(resp.executions().get(0).id()).isEqualTo("task-ok");
        assertThat(resp.executions().get(0).folderId()).isEqualTo(265);
    }

    @Test
    void listExecutions_shouldQueryMediaByDeviceSnAndTimeRange() {
        TaskDetail task = createTask("task-1", "wl-1", STATUS_SUCCESS, "任务",
                "4TADL12345678", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");
        stubFlightTaskList(List.of(task), 1);
        stubMediaFileCount(10);

        service.listExecutions("wl-1");

        ArgumentCaptor<GetMediaFilesListParams> captor =
                ArgumentCaptor.forClass(GetMediaFilesListParams.class);
        verify(mediaServiceModule).getMediaFilesList(captor.capture(), any());

        GetMediaFilesListParams captured = captor.getValue();
        assertThat(captured.getDeviceSn()).isEqualTo("4TADL12345678");
        assertThat(captured.getStartCreateTime()).isEqualTo(1775037600L);
        assertThat(captured.getEndCreateTime()).isEqualTo(1775039400L);
        assertThat(captured.getFlightId()).isEmpty();
    }

    @Test
    void listExecutions_shouldReturnZeroPhotoCount_whenMediaQueryFails() {
        TaskDetail task = createTask("task-1", "wl-1", STATUS_SUCCESS, "任务",
                "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");
        stubFlightTaskList(List.of(task), 1);
        stubMediaFileCountFailure();

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).hasSize(1);
        assertThat(resp.executions().get(0).photoCount()).isZero();
    }

    @Test
    void listExecutions_shouldReturnZeroPhotoCount_whenResultsNull() {
        TaskDetail task = createTask("task-1", "wl-1", STATUS_SUCCESS, "任务",
                "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");
        stubFlightTaskList(List.of(task), 1);
        stubMediaFileCountNull();

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).hasSize(1);
        assertThat(resp.executions().get(0).photoCount()).isZero();
    }

    @Test
    void listExecutions_shouldReturnEmptyWaylineName() {
        stubFlightTaskList(List.of(), 0);

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.waylineName()).isEmpty();
        assertThat(resp.executions()).isEmpty();
    }

    @Test
    void listExecutions_shouldBreak_whenTaskListEmpty() {
        stubFlightTaskList(List.of(), 0);

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).isEmpty();
    }

    @Test
    void listExecutions_shouldReturnMultipleExecutions_whenMultipleMatchedTasks() {
        TaskDetail t1 = createTask("task-1", "wl-1", STATUS_SUCCESS, "任务1",
                "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");
        TaskDetail t2 = createTask("task-2", "wl-1", STATUS_SUCCESS, "任务2",
                "drone-1", "2026-04-01T11:00:00Z", "2026-04-01T11:30:00Z");
        TaskDetail t3 = createTask("task-3", "wl-1", STATUS_SUCCESS, "任务3",
                "drone-1", "2026-04-01T12:00:00Z", "2026-04-01T12:30:00Z");
        stubFlightTaskList(List.of(t1, t2, t3), 3);
        stubMediaFileCount(10);

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).hasSize(3);
        assertThat(resp.executions().get(0).name()).isEqualTo("任务1");
        assertThat(resp.executions().get(1).name()).isEqualTo("任务2");
        assertThat(resp.executions().get(2).name()).isEqualTo("任务3");
    }

    @Test
    void listExecutions_shouldNotReject_whenMoreThanMaxThreadsTasks() {
        List<TaskDetail> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            tasks.add(createTask("task-" + i, "wl-1", STATUS_SUCCESS, "任务" + i,
                    "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z"));
        }
        stubFlightTaskList(tasks, 15);
        stubMediaFileCount(3);

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).hasSize(15);
    }

    @Test
    void listExecutions_shouldReturnZeroPhotoCount_whenTimeRangeNull() {
        TaskDetail task = createTask("task-1", "wl-1", STATUS_SUCCESS, "任务",
                "drone-1", null, null);
        stubFlightTaskList(List.of(task), 1);

        WaylineExecutionsResponse resp = service.listExecutions("wl-1");

        assertThat(resp.executions()).hasSize(1);
        assertThat(resp.executions().get(0).photoCount()).isZero();
    }

    @Test
    void listExecutions_shouldPaginate_whenPaginationNull() {
        // 模拟 page 1 返回 100 条（满页），pagination 为 null
        // page 2 返回匹配的任务（非满页，结束）
        TaskDetail target = createTask("task-target", "wl-x", STATUS_SUCCESS, "目标任务",
                "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z");

        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(invocation -> {
            FlightHubCallback.GetFlightTaskListCallback cb = invocation.getArgument(1);
            FlightHubError error = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
            int call = callCount.incrementAndGet();
            if (call == 1) {
                // page 1: 满页，无匹配，pagination 为 null
                List<TaskDetail> page1 = new java.util.ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    page1.add(createTask("filler-" + i, "other-wl", STATUS_SUCCESS, "填充" + i,
                            "drone-1", "2026-04-01T10:00:00Z", "2026-04-01T10:30:00Z"));
                }
                FlightTaskListResult result = new FlightTaskListResult(page1, null);
                cb.onResult(error, result);
            } else {
                // page 2: 有匹配，非满页
                FlightTaskListResult result = new FlightTaskListResult(List.of(target), null);
                cb.onResult(error, result);
            }
            return null;
        }).when(flightTaskModule).getFlightTaskList(
                any(FlightTaskListParams.class),
                any(FlightHubCallback.GetFlightTaskListCallback.class));
        stubMediaFileCount(5);

        WaylineExecutionsResponse resp = service.listExecutions("wl-x");

        assertThat(callCount.get()).isEqualTo(2);
        assertThat(resp.executions()).hasSize(1);
        assertThat(resp.executions().get(0).id()).isEqualTo("task-target");
        assertThat(resp.executions().get(0).folderId()).isEqualTo(265);
    }

    // ── Helper 方法 ──

    private TaskDetail createTask(String uuid, String waylineUuid, int status, String name,
                                  String sn, String runAt, String completedAt) {
        TaskDetail task = new TaskDetail();
        task.setUuid(uuid);
        task.setWaylineUuid(waylineUuid);
        task.setStatus(status);
        task.setName(name);
        task.setSn(sn);
        task.setRunAt(runAt);
        task.setCompletedAt(completedAt);
        task.setCreatedAt("2026-04-01T10:00:00Z");
        return task;
    }

    @Test
    void importWayline_shouldSubmitWaylineInfo_afterUpload() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME);
        stubSubmitSuccess();
        stubImportEvent(KMZ_OBJECT_KEY, IMPORTED_UUID, 0);

        service.importWayline(kmzFile(), IMPORTED_NAME);

        ArgumentCaptor<SubmitWaylineInfoParam> captor =
                ArgumentCaptor.forClass(SubmitWaylineInfoParam.class);
        verify(waylineManageModule).submitWaylineInfo(captor.capture(),
                any(FlightHubCallback.SubmitWaylineInfoCallback.class));
        assertThat(captor.getValue().getKmzObjectKey()).isEqualTo(KMZ_OBJECT_KEY);
        assertThat(captor.getValue().getWaylineName()).isEqualTo(IMPORTED_NAME);
        assertThat(captor.getValue().getAsyncRequestId()).isEqualTo(ASYNC_REQUEST_ID);
        assertThat(captor.getValue().getTimestamp()).isPositive();
    }

    @Test
    void importWayline_shouldSkipSubmitAndLookupUuidByName_whenAsyncRequestIdBlank() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME, "");
        WaylineListItemResult imported = listItem(IMPORTED_UUID);
        imported.setWaylineName(IMPORTED_NAME);
        stubWaylineList(imported);

        assertThat(service.importWayline(kmzFile(), IMPORTED_NAME).waylineUuid())
                .isEqualTo(IMPORTED_UUID);

        // 同步模式下平台已完成入库，再调 submitWaylineInfo 会被网关以 AsyncRequestId 必填拒绝
        verify(waylineManageModule, org.mockito.Mockito.never()).submitWaylineInfo(
                any(SubmitWaylineInfoParam.class),
                any(FlightHubCallback.SubmitWaylineInfoCallback.class));
        verify(waylineManageModule, org.mockito.Mockito.never()).listenImportWaylineEvent(
                any(FlightHubCallback.ImportWaylineEventCallback.class));
    }

    @Test
    void importWayline_shouldThrow_whenSyncLookupMissesAfterRetries() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME, "");
        // 列表始终不含刚导入的航线，模拟平台未落库
        stubWaylineList(listItem("other-uuid"));

        assertThatThrownBy(() -> service.importWayline(kmzFile(), IMPORTED_NAME))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("未在航线库查到该航线");

        verify(waylineManageModule, org.mockito.Mockito.times(UUID_LOOKUP_MAX_ATTEMPTS))
                .getWaylineList(any(GetWaylineListParam.class),
                        any(FlightHubCallback.GetWaylineListCallback.class));
    }

    @Test
    void importWayline_shouldReturnUuid_whenImportEventArrives() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME);
        stubSubmitSuccess();
        stubImportEvent(KMZ_OBJECT_KEY, IMPORTED_UUID, 0);

        assertThat(service.importWayline(kmzFile(), IMPORTED_NAME).waylineUuid())
                .isEqualTo(IMPORTED_UUID);
    }

    @Test
    void importWayline_shouldThrow_whenImportEventReportsFailure() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME);
        stubSubmitSuccess();
        stubImportEvent(KMZ_OBJECT_KEY, null, IMPORT_EVENT_CODE_FAILED);

        assertThatThrownBy(() -> service.importWayline(kmzFile(), IMPORTED_NAME))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("平台导入航线失败");
    }

    @Test
    void importWayline_shouldThrow_whenSubmitFails() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME);
        doAnswer(invocation -> {
            FlightHubCallback.SubmitWaylineInfoCallback cb = invocation.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            return null;
        }).when(waylineManageModule).submitWaylineInfo(
                any(SubmitWaylineInfoParam.class),
                any(FlightHubCallback.SubmitWaylineInfoCallback.class));

        assertThatThrownBy(() -> service.importWayline(kmzFile(), IMPORTED_NAME))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("submitWaylineInfo 失败");
    }

    @Test
    void importWayline_shouldCancelListener_whenSubmitFails() {
        stubImportKmz(KMZ_OBJECT_KEY, IMPORTED_NAME);
        org.mockito.Mockito.when(waylineManageModule.listenImportWaylineEvent(
                any(FlightHubCallback.ImportWaylineEventCallback.class))).thenReturn(LISTEN_ID);
        doAnswer(invocation -> {
            FlightHubCallback.SubmitWaylineInfoCallback cb = invocation.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            return null;
        }).when(waylineManageModule).submitWaylineInfo(
                any(SubmitWaylineInfoParam.class),
                any(FlightHubCallback.SubmitWaylineInfoCallback.class));

        assertThatThrownBy(() -> service.importWayline(kmzFile(), IMPORTED_NAME))
                .isInstanceOf(SdkBridge.SdkQueryException.class);
        verify(waylineManageModule).cancelListenImportWaylineEvent(LISTEN_ID);
    }

    @Test
    void importWayline_shouldThrow_whenUploadReturnsEmptyList() {
        doAnswer(invocation -> {
            FlightHubCallback.ImportKmzCallback cb = invocation.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), new ImportKmzResult());
            return null;
        }).when(waylineManageModule).importKmz(
                org.mockito.ArgumentMatchers.anyList(),
                any(FlightHubCallback.ImportKmzCallback.class));

        assertThatThrownBy(() -> service.importWayline(kmzFile(), IMPORTED_NAME))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("importKmz 返回结果为空");
        verify(waylineManageModule, org.mockito.Mockito.never()).submitWaylineInfo(
                any(SubmitWaylineInfoParam.class),
                any(FlightHubCallback.SubmitWaylineInfoCallback.class));
    }

    private MultipartFile kmzFile() {
        return new MockMultipartFile("file", "demo.kmz",
                "application/octet-stream", new byte[] {1, 2, 3});
    }

    private void stubImportKmz(String kmzObjectKey, String waylineName) {
        stubImportKmz(kmzObjectKey, waylineName, ASYNC_REQUEST_ID);
    }

    private void stubImportKmz(String kmzObjectKey, String waylineName, String asyncRequestId) {
        doAnswer(invocation -> {
            FlightHubCallback.ImportKmzCallback cb = invocation.getArgument(1);
            ImportKmzItem item = new ImportKmzItem();
            item.setKmzObjectKey(kmzObjectKey);
            item.setWaylineName(waylineName);
            item.setAsyncRequestId(asyncRequestId);
            ImportKmzResult result = new ImportKmzResult();
            result.setList(List.of(item));
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), result);
            return null;
        }).when(waylineManageModule).importKmz(
                org.mockito.ArgumentMatchers.anyList(),
                any(FlightHubCallback.ImportKmzCallback.class));
    }

    private void stubSubmitSuccess() {
        doAnswer(invocation -> {
            FlightHubCallback.SubmitWaylineInfoCallback cb = invocation.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(waylineManageModule).submitWaylineInfo(
                any(SubmitWaylineInfoParam.class),
                any(FlightHubCallback.SubmitWaylineInfoCallback.class));
    }

    /**
     * 注册监听时立刻回推一条导入完成事件，模拟平台异步推送。
     */
    private void stubImportEvent(String kmzObjectKey, String waylineUuid, int code) {
        doAnswer(invocation -> {
            FlightHubCallback.ImportWaylineEventCallback cb = invocation.getArgument(0);
            ImportWaylineEventResult event = new ImportWaylineEventResult();
            event.setKmzObjectKey(kmzObjectKey);
            event.setWaylineUuid(waylineUuid);
            event.setCode(code);
            event.setMessage(code == 0 ? "ok" : "import failed");
            cb.onResult(event);
            return LISTEN_ID;
        }).when(waylineManageModule).listenImportWaylineEvent(
                any(FlightHubCallback.ImportWaylineEventCallback.class));
    }

    private void stubFlightTaskList(List<TaskDetail> tasks, int total) {
        doAnswer(invocation -> {
            FlightHubCallback.GetFlightTaskListCallback cb = invocation.getArgument(1);
            FlightHubError error = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
            FlightTaskListResult result = new FlightTaskListResult(
                    tasks, new PaginationInfo(1, 100, total));
            cb.onResult(error, result);
            return null;
        }).when(flightTaskModule).getFlightTaskList(
                any(FlightTaskListParams.class),
                any(FlightHubCallback.GetFlightTaskListCallback.class));
    }

    private void stubMediaFileCount(int total) {
        doAnswer(invocation -> {
            FlightHubCallback.GetMediaFilesListCallback cb = invocation.getArgument(1);
            FlightHubError error = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
            // 返回一条带 filePath（文件夹 id）的媒体项，供 queryMediaFolder 解析
            MediaFilesListItemInfoData item = new MediaFilesListItemInfoData();
            item.setFilePath("265");
            GetMediaFilesListResults results = new GetMediaFilesListResults(
                    List.of(item), 1, 0, total);
            cb.onResult(error, results);
            return null;
        }).when(mediaServiceModule).getMediaFilesList(
                any(GetMediaFilesListParams.class),
                any(FlightHubCallback.GetMediaFilesListCallback.class));
    }

    @Test
    void listWaylines_shouldFillWaypointCountAndDistance_whenDetailAvailable() {
        stubWaylineList(listItem(LISTED_UUID));
        stubWaylineDetail(true);

        WaylineListResponse resp = service.listWaylines(1, 10, null, null, null);

        assertThat(resp.waylines()).hasSize(1);
        assertThat(resp.waylines().get(0).waypointCount()).isEqualTo(DETAIL_POINT_COUNT);
        assertThat(resp.waylines().get(0).estimatedDistance()).isEqualTo(DETAIL_DISTANCE_ROUNDED);
    }

    @Test
    void listWaylines_shouldDegradeToNullStats_whenDetailQueryFails() {
        stubWaylineList(listItem(LISTED_UUID));
        stubWaylineDetail(false);

        WaylineListResponse resp = service.listWaylines(1, 10, null, null, null);

        // 详情补拉失败只影响这两列，整页仍需正常返回
        assertThat(resp.waylines()).hasSize(1);
        assertThat(resp.waylines().get(0).name()).isEqualTo("航线-" + LISTED_UUID);
        assertThat(resp.waylines().get(0).waypointCount()).isNull();
        assertThat(resp.waylines().get(0).estimatedDistance()).isNull();
    }

    @Test
    void listWaylines_shouldReuseCachedStats_whenSameUuidAndUpdatedAt() {
        stubWaylineList(listItem(LISTED_UUID));
        stubWaylineDetail(true);

        service.listWaylines(1, 10, null, null, null);
        WaylineListResponse second = service.listWaylines(1, 10, null, null, null);

        assertThat(second.waylines().get(0).waypointCount()).isEqualTo(DETAIL_POINT_COUNT);
        // uuid@updatedAt 未变，第二次查询不应再次补拉详情
        verify(waylineManageModule, times(1)).getWaylineDetail(
                any(String.class),
                any(FlightHubCallback.GetWaylineDetailCallback.class));
    }

    private static WaylineListItemResult listItem(String uuid) {
        WaylineListItemResult item = new WaylineListItemResult();
        item.setWaylineUuid(uuid);
        item.setWaylineName("航线-" + uuid);
        item.setDroneType("0-89-0");
        item.setPayloadTypes(List.of("81-0-0"));
        item.setWaylineTypes(List.of(0));
        item.setUpdatedAt(LISTED_UPDATED_AT_MILLIS);
        return item;
    }

    private void stubWaylineList(WaylineListItemResult... items) {
        doAnswer(invocation -> {
            FlightHubCallback.GetWaylineListCallback cb = invocation.getArgument(1);
            GetWaylineListResult result = new GetWaylineListResult(
                    new WaylineListPaginationResult(1, 10, items.length), List.of(items));
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), result);
            return null;
        }).when(waylineManageModule).getWaylineList(
                any(GetWaylineListParam.class),
                any(FlightHubCallback.GetWaylineListCallback.class));
    }

    private void stubWaylineDetail(boolean success) {
        doAnswer(invocation -> {
            FlightHubCallback.GetWaylineDetailCallback cb = invocation.getArgument(1);
            if (!success) {
                cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                return null;
            }
            WaylineDetailKmzData kmz = new WaylineDetailKmzData();
            kmz.setWaylinePointCount(DETAIL_POINT_COUNT);
            kmz.setDistance(DETAIL_DISTANCE_METERS);
            GetWaylineDetailResult detail = new GetWaylineDetailResult();
            detail.setWaylineUuid(invocation.getArgument(0));
            detail.setKmzData(kmz);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), detail);
            return null;
        }).when(waylineManageModule).getWaylineDetail(
                any(String.class),
                any(FlightHubCallback.GetWaylineDetailCallback.class));
    }

    private void stubMediaFileCountFailure() {
        doAnswer(invocation -> {
            FlightHubCallback.GetMediaFilesListCallback cb = invocation.getArgument(1);
            FlightHubError error = new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED);
            cb.onResult(error, null);
            return null;
        }).when(mediaServiceModule).getMediaFilesList(
                any(GetMediaFilesListParams.class),
                any(FlightHubCallback.GetMediaFilesListCallback.class));
    }

    private void stubMediaFileCountNull() {
        doAnswer(invocation -> {
            FlightHubCallback.GetMediaFilesListCallback cb = invocation.getArgument(1);
            FlightHubError error = new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
            cb.onResult(error, null);
            return null;
        }).when(mediaServiceModule).getMediaFilesList(
                any(GetMediaFilesListParams.class),
                any(FlightHubCallback.GetMediaFilesListCallback.class));
    }
}
