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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dji.fh.feature.dashboard.dto.CreateFlightTaskDto;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.EditTaskReq;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * {@link SdkFlightTaskCommandService} 单元测试。
 *
 * <p>SDK 模块通过覆写 package-private 的 {@code getFlightTaskModule()} 注入 mock，
 * 避免依赖 JNI 原生库（同 {@link SdkFlightTaskQueryServiceTest} 惯例）。
 * 用回调捕获 {@code updateFlightTaskStatus} 实际下发的状态码，锁定协议约束：
 * 暂停=6（已挂起）、恢复=0（待开始）。
 *
 * @author DJI DevKit Team
 */
class SdkFlightTaskCommandServiceTest {

    private static final String TASK_UUID = "task-uuid-001";

    /** 协议允许下发的状态码：0=待开始，6=已挂起（UpdateTaskStatusReq） */
    private static final int STATUS_PENDING_START = 0;
    private static final int STATUS_SUSPENDED = 6;

    /** 协议不支持下发、仅为保持 SPI 接口完整保留的状态码：2=执行中，4=已终止 */
    private static final int STATUS_EXECUTING = 2;
    private static final int STATUS_TERMINATED = 4;

    private static final String DOCK_SN = "8UUXN5F00A08FT";
    private static final String WAYLINE_UUID = "wl-001";

    private FlightTaskModule flightTaskModule;
    private SdkFlightTaskCommandService service;

    /** 每次状态更新调用收到的 taskUuid，按调用记录 */
    private String capturedUuid;

    /** 每次状态更新调用收到的 status，按调用记录 */
    private Integer capturedStatus;

    /** 每次创建调用收到的请求，验证 SDK 可承载字段是否被正确映射 */
    private CreateTaskReq capturedCreateReq;

    /** 每次编辑调用收到的请求 */
    private EditTaskReq capturedEditReq;

    /** 捕获 {@link SdkFlightTaskCommandService} 日志，用于断言「丢弃字段」告警 */
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        flightTaskModule = mock(FlightTaskModule.class);
        capturedUuid = null;
        capturedStatus = null;
        capturedCreateReq = null;

        service = new SdkFlightTaskCommandService() {
            @Override
            FlightTaskModule getFlightTaskModule() {
                return flightTaskModule;
            }
        };

        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(SdkFlightTaskCommandService.class)).addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(SdkFlightTaskCommandService.class)).detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void pauseTask_shouldUpdateStatusToSuspended_whenTaskUuidProvided() {
        stubStatusCallbackSuccess();

        service.pauseTask(TASK_UUID);

        assertThat(capturedUuid).isEqualTo(TASK_UUID);
        assertThat(capturedStatus).isEqualTo(STATUS_SUSPENDED);
    }

    @Test
    void resumeTask_shouldUpdateStatusToPendingStart_whenTaskUuidProvided() {
        stubStatusCallbackSuccess();

        service.resumeTask(TASK_UUID);

        assertThat(capturedUuid).isEqualTo(TASK_UUID);
        assertThat(capturedStatus).isEqualTo(STATUS_PENDING_START);
    }

    @Test
    void executeTask_shouldUpdateStatusToExecuting_whenTaskUuidProvided() {
        stubStatusCallbackSuccess();

        service.executeTask(TASK_UUID);

        assertThat(capturedStatus).isEqualTo(STATUS_EXECUTING);
    }

    @Test
    void cancelTask_shouldUpdateStatusToTerminated_whenTaskUuidProvided() {
        stubStatusCallbackSuccess();

        service.cancelTask(TASK_UUID);

        assertThat(capturedStatus).isEqualTo(STATUS_TERMINATED);
    }

    @Test
    void pauseTask_shouldThrowSdkQueryException_whenCallbackReturnsFailure() {
        stubStatusCallbackFailure();

        assertThatThrownBy(() -> service.pauseTask(TASK_UUID))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("暂停");
    }

    @Test
    void createTask_shouldSetWaylineDuration_whenDtoCarriesDuration() {
        stubCreateCallbackSuccess("created-uuid");

        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("SN001");
        dto.setTaskType(2);
        dto.setWaylineDuration(180.0);

        service.createTask(dto);

        assertThat(capturedCreateReq.getWaylineDuration()).isEqualTo(180.0);
    }

    @Test
    void createTask_shouldLeaveWaylineDurationUnset_whenDtoDurationIsNull() {
        stubCreateCallbackSuccess("created-uuid");

        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("SN001");
        dto.setTaskType(2);

        service.createTask(dto);

        // CreateTaskReq 的默认值为 SDK 约定的「未设置」哨兵 -1.0，不被本服务覆盖
        assertThat(capturedCreateReq.getWaylineDuration()).isEqualTo(-1.0);
    }

    @Test
    void createTask_shouldReturnUuidAndMapSupportedFields_whenSdkSucceeds() {
        stubCreateCallbackSuccess("created-uuid");
        CreateFlightTaskDto dto = baseDto();
        dto.setBeginAt(1800000000L);
        dto.setEndAt(1800172800L);
        dto.setRthMode(1);
        dto.setWaylinePrecisionType(1);
        dto.setMinBatteryCapacity(30);
        dto.setResumableStatus(1);

        SdkFlightTaskCommandService.CreateResult result = service.createTask(dto);

        assertThat(result.taskUuid()).isEqualTo("created-uuid");
        assertThat(capturedCreateReq.getName()).isEqualTo("XH重复任务1");
        assertThat(capturedCreateReq.getSn()).isEqualTo(DOCK_SN);
        assertThat(capturedCreateReq.getTaskType()).isEqualTo(2);
        assertThat(capturedCreateReq.getWaylineUuid()).isEqualTo(WAYLINE_UUID);
        assertThat(capturedCreateReq.getBeginAt()).isEqualTo(1800000000L);
        assertThat(capturedCreateReq.getEndAt()).isEqualTo(1800172800L);
        assertThat(capturedCreateReq.getRthMode()).isEqualTo(1);
        assertThat(capturedCreateReq.getWaylinePrecisionType()).isEqualTo(1);
        assertThat(capturedCreateReq.getMinBatteryCapacity()).isEqualTo(30);
    }

    @Test
    void createTask_shouldThrowSdkQueryException_whenWaylineValidityCheckFails() {
        stubCreateCallbackSuccess("created-uuid", List.of(0, 1001));

        assertThatThrownBy(() -> service.createTask(baseDto()))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("航线有效性校验失败");
    }

    @Test
    void createTask_shouldThrowSdkQueryException_whenUuidIsBlank() {
        stubCreateCallbackSuccess("  ");

        assertThatThrownBy(() -> service.createTask(baseDto()))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("空任务 UUID");
    }

    /**
     * 核心约束：截至 SDK 3.3.5，{@code CreateTaskReq} 没有 repeat_type / interval
     * 等 setter，重复计划的字段在 BFF 层必然丢失。测试锁定「丢失必须可见」——
     * 提交非默认值时必须产生告警，否则现场无从发现。
     */
    @Test
    void createTask_shouldWarn_whenRepeatFieldsCannotBeCarried() {
        stubCreateCallbackSuccess("created-uuid");
        CreateFlightTaskDto dto = baseDto();
        dto.setRepeatType(4);
        dto.setInterval(1);
        dto.setContinuousTaskPeriods(List.of(List.of(0, 3600)));
        dto.setPlanType(1);
        dto.setVlmEnabled(true);

        service.createTask(dto);

        String warnings = collectedWarnings();
        assertThat(warnings).contains("repeat_type=4");
        assertThat(warnings).contains("interval=1");
        assertThat(warnings).contains("continuous_task_periods=[[0, 3600]]");
        assertThat(warnings).contains("plan_type=1");
        assertThat(warnings).contains("vlm_enabled=true");
        assertThat(warnings).contains("taskType=2");
    }

    /**
     * 前端对 plan_type / repeat_type / vlm_enabled 是无条件下发默认值，
     * 丢弃它们没有副作用；若一并告警，真正的问题会被噪声淹没。
     */
    @Test
    void createTask_shouldNotWarn_whenOnlyDefaultValuedFieldsAreDropped() {
        stubCreateCallbackSuccess("created-uuid");
        CreateFlightTaskDto dto = baseDto();
        dto.setRepeatType(0);
        dto.setPlanType(0);
        dto.setVlmEnabled(false);

        service.createTask(dto);

        assertThat(collectedWarnings()).isEmpty();
    }

    @Test
    void updateTask_shouldMapFieldsAndWarn_whenRepeatFieldsCannotBeCarried() {
        stubEditCallbackSuccess();
        CreateFlightTaskDto dto = baseDto();
        dto.setRepeatType(4);
        dto.setInterval(1);

        service.updateTask(TASK_UUID, dto);

        assertThat(capturedEditReq.getUuid()).isEqualTo(TASK_UUID);
        assertThat(capturedEditReq.getName()).isEqualTo("XH重复任务1");
        assertThat(capturedEditReq.getTaskType()).isEqualTo(2);
        assertThat(collectedWarnings()).contains("repeat_type=4", "interval=1");
    }

    @Test
    void updateTask_shouldThrowSdkQueryException_whenCallbackReturnsFailure() {
        doAnswer(invocation -> {
            FlightHubCallback.CreateFlightTaskCallback cb = invocation.getArgument(2);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_INVALID_PARAM), null);
            return null;
        }).when(flightTaskModule).editFlightTask(
                anyString(), any(EditTaskReq.class), any(FlightHubCallback.CreateFlightTaskCallback.class));

        assertThatThrownBy(() -> service.updateTask(TASK_UUID, baseDto()))
                .isInstanceOf(SdkBridge.SdkQueryException.class)
                .hasMessageContaining("编辑任务失败");
    }

    /**
     * 构造一份字段齐全的创建请求 DTO（不依赖 SDK，纯 POJO）。
     */
    private CreateFlightTaskDto baseDto() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("XH重复任务1");
        dto.setSn(DOCK_SN);
        dto.setTaskType(2);
        dto.setWaylineUuid(WAYLINE_UUID);
        return dto;
    }

    /** 汇总 {@link SdkFlightTaskCommandService} 产生的 WARN 及以上级别日志文本。 */
    private String collectedWarnings() {
        StringBuilder sb = new StringBuilder();
        for (ILoggingEvent event : logAppender.list) {
            if (!event.getLevel().isGreaterOrEqual(Level.WARN)) {
                continue;
            }
            sb.append(event.getFormattedMessage()).append('\n');
        }
        return sb.toString();
    }

    /**
     * 桩定状态更新回调：捕获下发的 taskUuid 与 status，并以成功 error 完成回调。
     */
    private void stubStatusCallbackSuccess() {
        doAnswer(invocation -> {
            capturedUuid = invocation.getArgument(0);
            capturedStatus = invocation.getArgument(1);
            FlightHubCallback.ResultCallbackWithError cb = invocation.getArgument(2);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(flightTaskModule).updateFlightTaskStatus(
                anyString(), anyInt(), any(FlightHubCallback.ResultCallbackWithError.class));
    }

    /**
     * 桩定状态更新回调：以失败 error 完成回调，验证异常桥接路径。
     */
    private void stubStatusCallbackFailure() {
        doAnswer(invocation -> {
            FlightHubCallback.ResultCallbackWithError cb = invocation.getArgument(2);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_INVALID_PARAM));
            return null;
        }).when(flightTaskModule).updateFlightTaskStatus(
                anyString(), anyInt(), any(FlightHubCallback.ResultCallbackWithError.class));
    }

    /**
     * 桩定创建回调：捕获下发的 {@link CreateTaskReq}，并以无检查码的成功响应完成回调。
     *
     * @param uuid 服务端返回的任务 UUID
     */
    private void stubCreateCallbackSuccess(String uuid) {
        stubCreateCallbackSuccess(uuid, Collections.emptyList());
    }

    /**
     * 桩定创建回调：捕获下发的 {@link CreateTaskReq}，并以指定航线检查码完成回调。
     *
     * @param uuid       服务端返回的任务 UUID
     * @param checkCodes 航线有效性检查码，非 0 表示校验失败
     */
    private void stubCreateCallbackSuccess(String uuid, List<Integer> checkCodes) {
        doAnswer(invocation -> {
            capturedCreateReq = invocation.getArgument(0);
            FlightHubCallback.CreateFlightTaskCallback cb = invocation.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), new CreateTaskResp(uuid, checkCodes));
            return null;
        }).when(flightTaskModule).createFlightTask(
                any(CreateTaskReq.class), any(FlightHubCallback.CreateFlightTaskCallback.class));
    }

    /**
     * 桩定编辑回调：捕获下发的 {@link EditTaskReq}，并以成功响应完成回调。
     */
    private void stubEditCallbackSuccess() {
        doAnswer(invocation -> {
            capturedEditReq = invocation.getArgument(1);
            FlightHubCallback.CreateFlightTaskCallback cb = invocation.getArgument(2);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS), null);
            return null;
        }).when(flightTaskModule).editFlightTask(
                anyString(), any(EditTaskReq.class), any(FlightHubCallback.CreateFlightTaskCallback.class));
    }
}
