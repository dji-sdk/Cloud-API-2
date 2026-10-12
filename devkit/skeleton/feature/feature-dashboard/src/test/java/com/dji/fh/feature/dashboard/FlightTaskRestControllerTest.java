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

package com.dji.fh.feature.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dji.fh.feature.dashboard.dto.CreateFlightTaskDto;
import com.dji.fh.feature.dashboard.dto.FlightTaskListItemDto;
import com.dji.fh.feature.dashboard.dto.FlightTaskPageDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskCommandService;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.ConflictingTask;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.TaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDetailDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskPage;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class FlightTaskRestControllerTest {

    private FlightTaskRestController controller;
    private StubFlightTaskQueryService stubQueryService;
    private StubFlightTaskCommandService stubCommandService;
    private StubFlightTaskConflictService stubConflictService;

    @BeforeEach
    void setUp() {
        stubQueryService = new StubFlightTaskQueryService();
        stubCommandService = new StubFlightTaskCommandService();
        stubConflictService = new StubFlightTaskConflictService();
        controller = new FlightTaskRestController(stubQueryService, stubCommandService, stubConflictService);
    }

    @Test
    void list_shouldPassNullStatus_whenStatusIsAll() {
        controller.list("all", 1, 20);
        assertThat(stubQueryService.lastStatus).isNull();
    }

    @Test
    void list_shouldPassStatusFilter_whenSpecified() {
        controller.list("SUCCESS", 1, 20);
        assertThat(stubQueryService.lastStatus).isEqualTo("SUCCESS");
    }

    @Test
    void list_shouldPassPageAndPageSize() {
        controller.list("all", 3, 15);
        assertThat(stubQueryService.lastPage).isEqualTo(3);
        assertThat(stubQueryService.lastPageSize).isEqualTo(15);
    }

    @Test
    void list_shouldReturnResultFromService() {
        FlightTaskPageDto result = controller.list("all", 1, 20);
        assertThat(result.tasks()).hasSize(1);
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.hasMore()).isFalse();
    }

    @Test
    void list_shouldPropagateIsBreakPointResume_fromQueryServiceToListItem() {
        // 回归用例：toListItem() 曾遗漏透传该字段，导致真实链路下列表编辑按钮永远不置灰
        stubQueryService.tasksToReturn = List.of(
                new FlightTaskDto("uuid-a", "断点续飞任务", "SN001", "CREATED", 2,
                        "wl-001", 1700000000L, 1200.0, 0, true));

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks()).singleElement()
                .extracting(FlightTaskListItemDto::isBreakPointResume)
                .isEqualTo(true);
    }

    @Test
    void list_shouldUseDefaultValues() {
        FlightTaskPageDto result = controller.list("all", 1, 20);
        assertThat(result).isNotNull();
        assertThat(stubQueryService.lastPage).isEqualTo(1);
        assertThat(stubQueryService.lastPageSize).isEqualTo(20);
    }

    @Test
    void list_shouldNotMarkConflict_whenSingleScheduledTask() {
        stubQueryService.windowsToReturn = List.of(
                new FlightTaskWindow("test-uuid", "测试任务", "SN001", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0));

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks()).singleElement()
                .extracting(FlightTaskListItemDto::isConflicted)
                .isEqualTo(false);
    }

    @Test
    void list_shouldMarkBothTasks_whenTwoScheduledTasksOverlap() {
        stubQueryService.tasksToReturn = List.of(
                listTask("uuid-a", "yzp单次任务冲突"),
                listTask("uuid-b", "yzp重复定时3"));
        // uuid-a 是单次定时任务（3）；uuid-b 是重复规则展开出的子任务（4）——
        // 平台真正占用设备时间的是展开后的子任务，父任务 2 不参与冲突比对
        stubQueryService.windowsToReturn = List.of(
                new FlightTaskWindow("uuid-a", "yzp单次任务冲突", "SN001", "CREATED", 3,
                        1700000000L, 0L, 600.0),
                new FlightTaskWindow("uuid-b", "yzp重复定时3", "SN001", "CREATED", 4,
                        1700000000L, 1700003600L, 600.0));

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks())
                .extracting(FlightTaskListItemDto::taskUuid, FlightTaskListItemDto::isConflicted)
                .containsExactly(
                        tuple("uuid-a", true),
                        tuple("uuid-b", true));
        assertThat(stubQueryService.lastWindowSns).containsExactly("SN001");
    }

    @Test
    void list_shouldQueryWindowsOnce_whenPageSpansMultipleDevices() {
        stubQueryService.tasksToReturn = List.of(
                listTask("uuid-a", "任务A", "SN001"),
                listTask("uuid-b", "任务B", "SN002"),
                listTask("uuid-c", "任务C", "SN003"));
        stubQueryService.windowsToReturn = List.of(
                new FlightTaskWindow("uuid-a", "任务A", "SN001", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0),
                new FlightTaskWindow("uuid-b", "任务B", "SN002", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0),
                new FlightTaskWindow("uuid-c", "任务C", "SN003", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0));

        controller.list("all", 1, 20);

        // 三台设备合并为一次批量查询，而非逐台各查一次
        assertThat(stubQueryService.lastWindowSns)
                .containsExactlyInAnyOrder("SN001", "SN002", "SN003");
    }

    @Test
    void list_shouldNotMarkConflict_whenTasksOnDifferentDevicesShareSameWindow() {
        stubQueryService.tasksToReturn = List.of(
                listTask("uuid-a", "任务A", "SN001"),
                listTask("uuid-b", "任务B", "SN002"));
        // 两台设备上的任务时间窗完全一致，但属于不同机场，不构成冲突
        stubQueryService.windowsToReturn = List.of(
                new FlightTaskWindow("uuid-a", "任务A", "SN001", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0),
                new FlightTaskWindow("uuid-b", "任务B", "SN002", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0));

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks())
                .extracting(FlightTaskListItemDto::isConflicted)
                .containsExactly(false, false);
    }

    @Test
    void list_shouldFailOpenForWholePage_whenAnyDeviceWindowQueryFails() {
        stubQueryService.tasksToReturn = List.of(
                listTask("uuid-a", "yzp单次任务冲突", "SN001"),
                listTask("uuid-b", "yzp重复定时3", "SN001"),
                listTask("uuid-c", "任务C", "SN002"));
        stubQueryService.windowsBySnToReturn = Map.of(
                "SN001", List.of(
                        new FlightTaskWindow("uuid-a", "yzp单次任务冲突", "SN001", "CREATED", 3,
                                1700000000L, 0L, 600.0),
                        new FlightTaskWindow("uuid-b", "yzp重复定时3", "SN001", "CREATED", 4,
                                1700000000L, 1700003600L, 600.0)));
        stubQueryService.failingSns = Set.of("SN002");

        FlightTaskPageDto result = controller.list("all", 1, 20);

        // 批量查询以整批为单位失败，SN002 失败会连带 SN001 一起降级为无冲突标识，不做逐设备隔离
        assertThat(result.tasks())
                .extracting(FlightTaskListItemDto::taskUuid, FlightTaskListItemDto::isConflicted)
                .containsExactly(
                        tuple("uuid-a", false),
                        tuple("uuid-b", false),
                        tuple("uuid-c", false));
    }

    @Test
    void list_shouldNotMarkConflict_whenWindowsAreAdjacent() {
        stubQueryService.tasksToReturn = List.of(
                listTask("uuid-a", "任务A"),
                listTask("uuid-b", "任务B"));
        stubQueryService.windowsToReturn = List.of(
                new FlightTaskWindow("uuid-a", "任务A", "SN001", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0),
                new FlightTaskWindow("uuid-b", "任务B", "SN001", "CREATED", 3,
                        1700003600L, 1700007200L, 600.0));

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks())
                .extracting(FlightTaskListItemDto::isConflicted)
                .containsExactly(false, false);
    }

    @Test
    void list_shouldIgnoreFinishedAndImmediateTasks_whenScanningConflicts() {
        stubQueryService.tasksToReturn = List.of(listTask("uuid-a", "任务A"));
        stubQueryService.windowsToReturn = List.of(
                new FlightTaskWindow("uuid-a", "任务A", "SN001", "CREATED", 3,
                        1700000000L, 1700003600L, 600.0),
                // 已结束任务与新任务时间重叠，但不参与比对
                new FlightTaskWindow("uuid-b", "已完成任务", "SN001", "SUCCESS", 3,
                        1700001000L, 1700004600L, 600.0),
                // 即时任务无排期窗口，同样不参与比对
                new FlightTaskWindow("uuid-c", "即时任务", "SN001", "CREATED", 1,
                        1700001000L, 1700004600L, 600.0));

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks()).singleElement()
                .extracting(FlightTaskListItemDto::isConflicted)
                .isEqualTo(false);
    }

    @Test
    void list_shouldFailOpen_whenWindowQueryThrows() {
        stubQueryService.windowExceptionToThrow =
                new UnsupportedOperationException("未实现任务时间窗查询");

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks()).singleElement()
                .extracting(FlightTaskListItemDto::taskUuid, FlightTaskListItemDto::isConflicted)
                .containsExactly("test-uuid", false);
    }

    @Test
    void list_shouldKeepPageShape_whenTasksEmpty() {
        stubQueryService.tasksToReturn = List.of();

        FlightTaskPageDto result = controller.list("all", 1, 20);

        assertThat(result.tasks()).isEmpty();
        assertThat(result.totalCount()).isZero();
        assertThat(result.hasMore()).isFalse();
    }

    @Test
    void create_shouldReturnBadRequest_whenNameIsBlank() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");

        ResponseEntity<?> response = controller.create(dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_shouldReturnBadRequest_whenSnIsBlank() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("");
        dto.setWaylineUuid("wl-001");

        ResponseEntity<?> response = controller.create(dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_shouldReturnBadRequest_whenWaylineUuidIsBlank() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("");

        ResponseEntity<?> response = controller.create(dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldReturnTaskUuid_whenValid() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");
        dto.setTaskType(1);
        dto.setRthAltitude(100.0);

        ResponseEntity<?> response = controller.create(dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsKey("taskUuid");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnOk() {
        ResponseEntity<?> response = controller.execute("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("action", "execute");
        assertThat(stubCommandService.lastAction).isEqualTo("execute");
    }

    @Test
    @SuppressWarnings("unchecked")
    void pause_shouldReturnOk() {
        ResponseEntity<?> response = controller.pause("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubCommandService.lastAction).isEqualTo("pause");
    }

    @Test
    @SuppressWarnings("unchecked")
    void cancel_shouldReturnOk() {
        ResponseEntity<?> response = controller.cancel("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubCommandService.lastAction).isEqualTo("cancel");
    }

    @Test
    void detail_shouldReturnDetail_whenUuidValid() {
        stubQueryService.detailToReturn = sampleDetail();

        ResponseEntity<?> response = controller.detail("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        FlightTaskDetailDto body = (FlightTaskDetailDto) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.taskUuid()).isEqualTo("test-uuid");
        assertThat(body.name()).isEqualTo("测试任务");
    }

    @Test
    void detail_shouldReturnNotFound_whenTaskMissing() {
        stubQueryService.exceptionToThrow =
                new IllegalArgumentException("任务不存在: test-uuid");

        ResponseEntity<?> response = controller.detail("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void detail_shouldReturn500_whenServiceThrows() {
        stubQueryService.exceptionToThrow = new RuntimeException("SDK 未初始化");

        ResponseEntity<?> response = controller.detail("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void edit_shouldReturnBadRequest_whenNameIsBlank() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");

        ResponseEntity<?> response = controller.edit("test-uuid", dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnOk_whenValid() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("改名任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");
        dto.setTaskType(1);
        dto.setRthAltitude(120.0);

        ResponseEntity<?> response = controller.edit("test-uuid", dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("action", "edit");
        assertThat(stubCommandService.lastAction).isEqualTo("edit");
        assertThat(stubCommandService.lastUuid).isEqualTo("test-uuid");
        assertThat(stubCommandService.lastEditDto).isSameAs(dto);
    }

    @Test
    void edit_shouldReturn500_whenServiceThrows() {
        stubCommandService.exceptionToThrow = new RuntimeException("编辑任务失败: 超时");

        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("改名任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");

        ResponseEntity<?> response = controller.edit("test-uuid", dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void edit_shouldReturnBadRequest_whenUuidIsBlank() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("改名任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");

        ResponseEntity<?> response = controller.edit("", dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnBadRequest_whenTaskNotCreated() {
        stubQueryService.detailToReturn = detailWithStatus("EXECUTING");

        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("改名任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");

        ResponseEntity<?> response = controller.edit("test-uuid", dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).contains("仅待执行状态");
    }

    @Test
    void edit_shouldReturnNotFound_whenTaskMissing() {
        stubQueryService.exceptionToThrow =
                new IllegalArgumentException("任务不存在: test-uuid");

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── edit 可编辑性门禁（对齐平台 EditTask 的 4 析取谓词）──

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnBadRequestWithCode_whenTaskIsImmediateType() {
        stubQueryService.detailToReturn = detailOf("CREATED", TASK_TYPE_IMMEDIATE, false);

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NOT_EDITABLE");
        assertThat(body.get("message")).isEqualTo("即时任务不支持编辑");
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnBadRequestWithCode_whenTaskIsTestFlightType() {
        stubQueryService.detailToReturn = detailOf("CREATED", TASK_TYPE_TEST_FLIGHT, false);

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NOT_EDITABLE");
        assertThat(body.get("message")).isEqualTo("试飞任务不支持编辑");
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnBadRequestWithCode_whenTaskIsBreakPointResume() {
        stubQueryService.detailToReturn = detailOf("CREATED", TASK_TYPE_REPEATED, true);

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NOT_EDITABLE");
        assertThat(body.get("message")).isEqualTo("断点续飞任务不支持编辑");
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnAllReasons_whenMultipleConditionsFail() {
        // 复现客户现场：起始状态任务 + 断点续飞标记，平台侧 ①④ 两条同时成立
        stubQueryService.detailToReturn = detailOf("FAILED", TASK_TYPE_REPEATED, true);

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NOT_EDITABLE");
        assertThat((List<String>) body.get("reasons"))
                .containsExactly("仅待执行状态的任务允许编辑", "断点续飞任务不支持编辑");
    }

    @Test
    void edit_shouldAllow_whenTaskIsRepeatedTypeAndNotBreakPointResume() {
        stubQueryService.detailToReturn = detailOf("CREATED", TASK_TYPE_REPEATED, false);

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void edit_shouldAllow_whenTaskIsSingleTimedType() {
        // 现场任务类型：单次定时（3），本身不应被编辑门禁拒绝
        stubQueryService.detailToReturn = detailOf("CREATED", 3, false);

        ResponseEntity<?> response = controller.edit("test-uuid", editRequestDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_shouldStillAllow_whenTaskIsImmediateType() {
        // 删除门禁比编辑宽松（平台只校验状态），不能因编辑收紧而误伤删除
        stubQueryService.detailToReturn = detailOf("CREATED", TASK_TYPE_IMMEDIATE, false);

        ResponseEntity<?> response = controller.delete("test-uuid");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_shouldStillAllow_whenTaskIsBreakPointResume() {
        stubQueryService.detailToReturn = detailOf("CREATED", TASK_TYPE_REPEATED, true);

        ResponseEntity<?> response = controller.delete("test-uuid");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * 构造满足编辑前置参数校验的请求体（用于编辑门禁测试）。
     *
     * @return 编辑请求体
     */
    private static CreateFlightTaskDto editRequestDto() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("改名任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");
        return dto;
    }

    @Test
    @SuppressWarnings("unchecked")
    void delete_shouldReturnOk() {
        ResponseEntity<?> response = controller.delete("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("action", "delete");
        assertThat(stubCommandService.lastAction).isEqualTo("delete");
        assertThat(stubCommandService.lastUuid).isEqualTo("test-uuid");
    }

    @Test
    void delete_shouldReturn500_whenServiceThrows() {
        stubCommandService.exceptionToThrow = new RuntimeException("删除任务失败: 无权限");

        ResponseEntity<?> response = controller.delete("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void delete_shouldReturnBadRequest_whenUuidIsBlank() {
        ResponseEntity<?> response = controller.delete("");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @SuppressWarnings("unchecked")
    void delete_shouldReturnBadRequest_whenTaskNotCreated() {
        stubQueryService.detailToReturn = detailWithStatus("EXECUTING");

        ResponseEntity<?> response = controller.delete("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).contains("仅待执行状态");
    }

    @Test
    void delete_shouldReturnNotFound_whenTaskMissing() {
        stubQueryService.exceptionToThrow =
                new IllegalArgumentException("任务不存在: test-uuid");

        ResponseEntity<?> response = controller.delete("test-uuid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldReturnBadRequestWithCode_whenNameDuplicated() {
        stubQueryService.conflictingName = "重复任务";

        ResponseEntity<?> response = controller.create(dtoWithName("重复任务"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NAME_DUPLICATE");
        assertThat(body.get("message")).contains("重复任务").contains("已存在");
        // 重名应被前置拦截，不应下发到命令服务
        assertThat(stubCommandService.lastAction).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldPassThrough_whenNameNotDuplicated() {
        stubQueryService.conflictingName = "另一个任务";

        ResponseEntity<?> response = controller.create(dtoWithName("全新任务"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsKey("taskUuid");
        assertThat(stubCommandService.lastAction).isEqualTo("create");
    }

    @Test
    void create_shouldPassNullExcludeUuid() {
        controller.create(dtoWithName("全新任务"));
        assertThat(stubQueryService.lastExcludeTaskUuid).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldTrimNameBeforeDuplicateCheck() {
        // 未 trim 时 "巡检 " 能绕过 "巡检" 的唯一性校验，直接调 API 的 ISV 即可复现重名
        stubQueryService.conflictingName = "巡检";

        ResponseEntity<?> response = controller.create(dtoWithName("  巡检  "));
        assertThat(stubQueryService.lastCheckedName).isEqualTo("巡检");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NAME_DUPLICATE");
    }

    @Test
    void create_shouldPassTrimmedNameToCommandService() {
        controller.create(dtoWithName("  全新任务  "));
        assertThat(stubCommandService.lastCreateDto).isNotNull();
        // 判重用的值与实际入库的值必须一致，否则带空格的值仍会入库
        assertThat(stubCommandService.lastCreateDto.getName()).isEqualTo("全新任务");
    }

    @Test
    void create_shouldRejectName_whenNameIsOnlySpaces() {
        ResponseEntity<?> response = controller.create(dtoWithName("   "));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(stubCommandService.lastAction).isNull();
    }

    @Test
    void edit_shouldTrimNameBeforeDuplicateCheck() {
        stubQueryService.detailToReturn = sampleDetail();
        stubQueryService.conflictingName = "巡检";

        ResponseEntity<?> response = controller.edit("test-uuid", dtoWithName("  巡检  "));
        assertThat(stubQueryService.lastCheckedName).isEqualTo("巡检");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void edit_shouldPassTrimmedNameToCommandService() {
        stubQueryService.detailToReturn = sampleDetail();

        controller.edit("test-uuid", dtoWithName("  测试任务  "));
        assertThat(stubCommandService.lastEditDto).isNotNull();
        assertThat(stubCommandService.lastEditDto.getName()).isEqualTo("测试任务");
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldAllow_whenNameQueryUnsupported() {
        // 存量 ISV 未实现 existsTaskWithName 时走 default 抛 UnsupportedOperationException，
        // 必须 fail-open，否则存量 ISV 的创建功能整体不可用
        stubQueryService.nameQueryExceptionToThrow =
                new UnsupportedOperationException("未实现任务名称唯一性查询");

        ResponseEntity<?> response = controller.create(dtoWithName("任意任务"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubCommandService.lastAction).isEqualTo("create");
    }

    @Test
    void create_shouldAllow_whenNameQueryThrows() {
        stubQueryService.nameQueryExceptionToThrow = new RuntimeException("SDK 未初始化");

        ResponseEntity<?> response = controller.create(dtoWithName("任意任务"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubCommandService.lastAction).isEqualTo("create");
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnBadRequestWithCode_whenNameDuplicated() {
        stubQueryService.detailToReturn = sampleDetail();
        stubQueryService.conflictingName = "已占用名称";

        ResponseEntity<?> response = controller.edit("test-uuid", dtoWithName("已占用名称"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo("TASK_NAME_DUPLICATE");
        assertThat(stubCommandService.lastAction).isNull();
    }

    @Test
    void edit_shouldPassCurrentUuidAsExcludeUuid() {
        // 编辑场景必须排除任务自身，否则「不改名直接保存」会被判定为重名
        stubQueryService.detailToReturn = sampleDetail();

        controller.edit("test-uuid", dtoWithName("测试任务"));
        assertThat(stubQueryService.lastCheckedName).isEqualTo("测试任务");
        assertThat(stubQueryService.lastExcludeTaskUuid).isEqualTo("test-uuid");
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldAllow_whenNameQueryUnsupported() {
        stubQueryService.detailToReturn = sampleDetail();
        stubQueryService.nameQueryExceptionToThrow =
                new UnsupportedOperationException("未实现任务名称唯一性查询");

        ResponseEntity<?> response = controller.edit("test-uuid", dtoWithName("任意任务"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubCommandService.lastAction).isEqualTo("edit");
    }

    /**
     * 构造指定名称的合法创建/编辑请求体。
     */
    private static CreateFlightTaskDto dtoWithName(String name) {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName(name);
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");
        dto.setTaskType(1);
        dto.setRthAltitude(100.0);
        return dto;
    }

    /**
     * 构造样例任务详情（默认 CREATED 状态、重复定时类型、非断点续飞，命中编辑/删除前置校验）。
     */
    private static FlightTaskDetailDto sampleDetail() {
        return detailWithStatus("CREATED");
    }

    /**
     * 构造指定状态的任务详情（用于校验非 CREATED 状态的拒绝场景）。
     */
    private static FlightTaskDetailDto detailWithStatus(String status) {
        return detailOf(status, TASK_TYPE_REPEATED, false);
    }

    /**
     * 构造指定状态 / 类型 / 断点续飞标记的任务详情。
     *
     * <p>编辑门禁是「状态 + 类型 + 断点续飞」的析取条件，逐项覆盖需要能单独控制每个维度。
     *
     * @param status             任务状态字符串
     * @param taskType           任务类型（平台 TaskType）
     * @param isBreakPointResume 是否断点续飞
     * @return 任务详情 DTO
     */
    private static FlightTaskDetailDto detailOf(String status, int taskType, boolean isBreakPointResume) {
        return new FlightTaskDetailDto(
                "test-uuid", "测试任务", "SN001", "测试机场",
                status, taskType, "wl-001", "测试航线",
                1700000000L, 1700003600L,
                100.0, 0, 1, 1, 0, isBreakPointResume);
    }

    /** 平台 TaskType：重复定时任务，可编辑类型 */
    private static final int TASK_TYPE_REPEATED = 2;

    /** 平台 TaskType：立即任务，平台禁止编辑 */
    private static final int TASK_TYPE_IMMEDIATE = 1;

    /** 平台 TaskType：试飞任务，平台禁止编辑 */
    private static final int TASK_TYPE_TEST_FLIGHT = 7;

    /**
     * 构造样例列表任务（定时任务，CREATED 状态）。
     *
     * @param taskUuid 任务 UUID
     * @param name     任务名称
     * @return 查询 SPI 的任务条目
     */
    private static FlightTaskDto listTask(String taskUuid, String name) {
        return listTask(taskUuid, name, "SN001");
    }

    private static FlightTaskDto listTask(String taskUuid, String name, String sn) {
        return new FlightTaskDto(
                taskUuid, name, sn, "CREATED", 2, "wl-001", 1700000000L, 1200.0, 0, false);
    }

    /**
     * 查询测试替身：记录调用参数并返回固定结果。
     */
    private static class StubFlightTaskQueryService implements FlightTaskQueryService {

        String lastStatus;
        int lastPage;
        int lastPageSize;
        List<String> lastWindowSns;
        List<FlightTaskWindow> windowsToReturn = List.of();
        /** 按 SN 分组的时间窗；非空时批量查询改由该映射驱动 */
        Map<String, List<FlightTaskWindow>> windowsBySnToReturn = Map.of();
        /** 批量查询入参命中该集合中的任一 SN 时，整批查询抛出异常（与 SDK 语义一致） */
        Set<String> failingSns = Set.of();
        List<FlightTaskDto> tasksToReturn = List.of(sampleListTask());
        RuntimeException windowExceptionToThrow;
        FlightTaskDetailDto detailToReturn;
        RuntimeException exceptionToThrow;

        /** 模拟已存在的任务名称，用于构造重名场景；null 表示无重名 */
        String conflictingName;
        /** 名称唯一性查询是否抛异常（非 UnsupportedOperationException），用于验证 fail-open */
        RuntimeException nameQueryExceptionToThrow;
        /** 最近一次名称唯一性查询的入参 */
        String lastCheckedName;
        String lastExcludeTaskUuid;

        private static FlightTaskDto sampleListTask() {
            return new FlightTaskDto(
                    "test-uuid", "测试任务", "SN001",
                    "SUCCESS", 1, "wl-001", 1700000000L, 1200.0, 0, false);
        }

        @Override
        public List<TaskSummary> listTasks(String range) {
            return List.of();
        }

        @Override
        public boolean existsTaskWithName(String name, String excludeTaskUuid) {
            this.lastCheckedName = name;
            this.lastExcludeTaskUuid = excludeTaskUuid;
            if (nameQueryExceptionToThrow != null) {
                throw nameQueryExceptionToThrow;
            }
            return name != null && name.equals(conflictingName);
        }

        @Override
        public FlightTaskPage listTasksPaged(String status, int page, int pageSize) {
            this.lastStatus = status;
            this.lastPage = page;
            this.lastPageSize = pageSize;

            return new FlightTaskPage(tasksToReturn, tasksToReturn.size(), false);
        }

        @Override
        public FlightTaskDetailDto getTaskDetail(String taskUuid) {
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return detailToReturn != null ? detailToReturn : sampleDetail();
        }

        @Override
        public List<FlightTaskWindow> listTaskWindows(Collection<String> sns) {
            this.lastWindowSns = List.copyOf(sns);
            if (windowExceptionToThrow != null) {
                throw windowExceptionToThrow;
            }
            if (windowsBySnToReturn.isEmpty()) {
                return windowsToReturn;
            }
            // 与 SDK 语义一致：多设备合并为一次查询，任一设备失败则整批失败
            if (sns.stream().anyMatch(failingSns::contains)) {
                throw new IllegalStateException("批量任务时间窗查询失败");
            }
            return sns.stream()
                    .flatMap(sn -> windowsBySnToReturn.getOrDefault(sn, List.of()).stream())
                    .toList();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldReturn500_whenServiceThrows() {
        stubCommandService.exceptionToThrow =
                new RuntimeException("航线有效性校验失败，检查码: [-4]");

        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");
        dto.setTaskType(1);
        dto.setRthAltitude(100.0);

        ResponseEntity<?> response = controller.create(dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).contains("检查码");
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldReturnBadRequestWithMessage_whenTimeConflict() {
        stubConflictService.conflictsToReturn = List.of(
                new ConflictingTask("other-uuid", "已排期巡检", 1700000000L, 1700003600L));

        CreateFlightTaskDto dto = validCreateDto();
        ResponseEntity<?> response = controller.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message"))
                .contains("SN001")
                .contains("已排期巡检")
                .contains("调整执行时间");
        assertThat(stubCommandService.lastAction).isNull();
    }

    @Test
    void create_shouldPassEmptySelfUuid_whenNewTask() {
        controller.create(validCreateDto());
        assertThat(stubConflictService.lastCandidate.taskUuid()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldReturnOk_whenConflictServiceThrows() {
        stubConflictService.exceptionToThrow = new RuntimeException("SDK 未初始化");

        ResponseEntity<?> response = controller.create(validCreateDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsKey("taskUuid");
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldFoldExtraConflicts_whenMoreThanMaxListed() {
        stubConflictService.conflictsToReturn = List.of(
                new ConflictingTask("u1", "任务A", 1700000000L, 1700003600L),
                new ConflictingTask("u2", "任务B", 1700000000L, 1700003600L),
                new ConflictingTask("u3", "任务C", 1700000000L, 1700003600L),
                new ConflictingTask("u4", "任务D", 1700000000L, 1700003600L));

        ResponseEntity<?> response = controller.create(validCreateDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message"))
                .contains("任务C")
                .doesNotContain("任务D")
                .contains("另有 1 个冲突任务");
    }

    @Test
    @SuppressWarnings("unchecked")
    void edit_shouldReturnBadRequest_whenTimeConflict() {
        stubConflictService.conflictsToReturn = List.of(
                new ConflictingTask("other-uuid", "已排期巡检", 1700000000L, 1700003600L));

        ResponseEntity<?> response = controller.edit("test-uuid", validCreateDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("message")).contains("已排期巡检");
        assertThat(stubCommandService.lastAction).isNull();
    }

    @Test
    void edit_shouldPassSelfUuid_whenExcludingSelf() {
        controller.edit("test-uuid", validCreateDto());
        assertThat(stubConflictService.lastCandidate.taskUuid()).isEqualTo("test-uuid");
    }

    @Test
    void edit_shouldSkipConflictCheck_whenTaskNotCreated() {
        stubQueryService.detailToReturn = detailWithStatus("EXECUTING");

        ResponseEntity<?> response = controller.edit("test-uuid", validCreateDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(stubConflictService.lastCandidate).isNull();
    }

    /**
     * 构造可通过参数校验的创建请求体。
     */
    private static CreateFlightTaskDto validCreateDto() {
        CreateFlightTaskDto dto = new CreateFlightTaskDto();
        dto.setName("测试任务");
        dto.setSn("SN001");
        dto.setWaylineUuid("wl-001");
        dto.setTaskType(2);
        dto.setRthAltitude(100.0);
        return dto;
    }

    /**
     * 命令测试替身：记录最后一次操作，支持模拟异常。
     */
    private static class StubFlightTaskCommandService implements FlightTaskCommandService {

        String lastAction;
        String lastUuid;
        CreateFlightTaskDto lastCreateDto;
        CreateFlightTaskDto lastEditDto;
        RuntimeException exceptionToThrow;

        @Override
        public CreateResult createTask(CreateFlightTaskDto dto) {
            lastAction = "create";
            lastCreateDto = dto;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return new CreateResult("task_stub_12345");
        }

        @Override
        public void updateTask(String taskUuid, CreateFlightTaskDto dto) {
            lastAction = "edit";
            lastUuid = taskUuid;
            lastEditDto = dto;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
        }

        @Override
        public void deleteTask(String taskUuid) {
            lastAction = "delete";
            lastUuid = taskUuid;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
        }

        @Override
        public void executeTask(String taskUuid) {
            lastAction = "execute";
            lastUuid = taskUuid;
        }

        @Override
        public void pauseTask(String taskUuid) {
            lastAction = "pause";
            lastUuid = taskUuid;
        }

        @Override
        public void resumeTask(String taskUuid) {
            lastAction = "resume";
            lastUuid = taskUuid;
        }

        @Override
        public void cancelTask(String taskUuid) {
            lastAction = "cancel";
            lastUuid = taskUuid;
        }
    }

    /**
     * 冲突校验测试替身：默认返回无冲突，可注入冲突列表或异常。
     */
    private static class StubFlightTaskConflictService implements FlightTaskConflictService {

        List<ConflictingTask> conflictsToReturn = List.of();
        RuntimeException exceptionToThrow;
        TaskWindow lastCandidate;

        @Override
        public List<ConflictingTask> findConflicts(TaskWindow candidate) {
            this.lastCandidate = candidate;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return conflictsToReturn;
        }
    }
}
