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

import com.dji.fh.feature.dashboard.dto.CreateFlightTaskDto;
import com.dji.fh.feature.dashboard.dto.FlightTaskListItemDto;
import com.dji.fh.feature.dashboard.dto.FlightTaskPageDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskCommandService;
import com.dji.fh.feature.dashboard.spi.FlightTaskCommandService.CreateResult;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.ConflictingTask;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService.TaskWindow;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflicts;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDetailDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskPage;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskWindow;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 飞行任务 REST API（查询 + 命令）。
 *
 * <p>供飞行任务管理页面使用，暴露分页查询和 CRUD 命令能力。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api/v1/flight-tasks")
@RequireAuthenticated
public class FlightTaskRestController {

    private static final Logger LOG = LoggerFactory.getLogger(FlightTaskRestController.class);

    /**
     * 「待执行」状态标识。
     *
     * <p>删除仅放行该状态；编辑在此基础上还需满足类型与断点续飞条件，
     * 见 {@link #validateTaskEditable(String)}。
     */
    private static final String STATUS_CREATED = "CREATED";

    /** 冲突提示中单个任务的时间展示格式（本地时区，精确到分钟） */
    private static final DateTimeFormatter CONFLICT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /** 冲突提示中最多列出的任务条数，超出部分折叠为「等 N 个任务」 */
    private static final int MAX_LISTED_CONFLICTS = 3;

    /** 立即任务类型标识：平台侧下发即执行，无编辑语义 */
    private static final int TASK_TYPE_IMMEDIATE = 1;

    /** 试飞任务类型标识：平台侧显式禁止编辑 */
    private static final int TASK_TYPE_TEST_FLIGHT = 7;

    /** 任务不可编辑的业务错误码，供前端识别并展示置灰原因 */
    private static final String ERROR_CODE_TASK_NOT_EDITABLE = "TASK_NOT_EDITABLE";

    /** 不可编辑原因文案，需与前端 {@code flightTaskEditability.ts} 保持逐字一致 */
    private static final String REASON_NOT_CREATED = "仅待执行状态的任务允许编辑";
    private static final String REASON_IMMEDIATE_TASK = "即时任务不支持编辑";
    private static final String REASON_TEST_FLIGHT_TASK = "试飞任务不支持编辑";
    private static final String REASON_BREAK_POINT_RESUME = "断点续飞任务不支持编辑";

    /** 多原因拼接分隔符，与前端保持一致 */
    private static final String REASON_SEPARATOR = "；";

    /** 任务名称重复的业务错误码，供前端定位到名称输入框展示 */
    private static final String ERROR_CODE_TASK_NAME_DUPLICATE = "TASK_NAME_DUPLICATE";

    private final FlightTaskQueryService queryService;
    private final FlightTaskCommandService commandService;
    private final FlightTaskConflictService conflictService;

    public FlightTaskRestController(FlightTaskQueryService queryService,
                                     FlightTaskCommandService commandService,
                                     FlightTaskConflictService conflictService) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.conflictService = conflictService;
    }

    /**
     * 分页查询飞行任务列表。
     *
     * <p>返回前对当前页任务追加「执行时间冲突」标识：同设备（sn 相同）已有多个参与调度的
     * 定时任务且时间窗互相重叠时，这些任务均被标记。冲突判定复用建单闸门的
     * {@link FlightTaskConflicts} 语义，前端不做任何计算。
     *
     * @param status   状态过滤（CREATED / PREPARING / QUEUED / EXECUTING / PAUSED / SUCCESS / FAILED / CANCELLED），
     *                 "all" 或不传表示全部
     * @param page     页码（1-indexed），默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，条目携带 isConflicted 标识
     */
    @GetMapping
    public FlightTaskPageDto list(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String statusFilter = "all".equals(status) ? null : status;
        FlightTaskPage pageResult = queryService.listTasksPaged(statusFilter, page, pageSize);
        Set<String> conflictedUuids = listConflictedTaskUuids(pageResult.tasks());
        List<FlightTaskListItemDto> items = pageResult.tasks().stream()
                .map(task -> toListItem(task, conflictedUuids.contains(task.taskUuid())))
                .toList();
        return new FlightTaskPageDto(items, pageResult.totalCount(), pageResult.hasMore());
    }

    /**
     * 找出当前页中已存在执行时间冲突的任务 UUID。
     *
     * <p>整页设备的时间窗一次性批量拉取（{@link FlightTaskQueryService} 的批量重载），
     * 把「逐台设备各拉一次全量任务」收敛为一次 SDK 查询。
     *
     * <p>冲突判定必须按设备分组：{@link FlightTaskConflicts#findConflictedTaskUuids} 的语义是
     * 「同一设备集合内部两两比对」，跨设备比对会把不同机场的任务误判为互相冲突。
     * 查询失败时采用 fail-open：记录告警并返回空集合，不影响正常建单/编辑，
     * 与建单闸门 {@link #validateNoTimeConflict} 的容错策略一致。
     *
     * @param tasks 当前页任务条目
     * @return 存在冲突的任务 UUID 集合，查询失败时为空集合
     */
    private Set<String> listConflictedTaskUuids(List<FlightTaskDto> tasks) {
        Set<String> sns = tasks.stream()
                .map(FlightTaskDto::sn)
                .filter(sn -> sn != null && !sn.isBlank())
                .collect(Collectors.toSet());
        if (sns.isEmpty()) {
            return new HashSet<>();
        }

        Map<String, List<FlightTaskWindow>> windowsBySn = loadWindowsBySn(sns);

        Set<String> conflictedUuids = new HashSet<>();
        for (List<FlightTaskWindow> windows : windowsBySn.values()) {
            conflictedUuids.addAll(FlightTaskConflicts.findConflictedTaskUuids(windows));
        }
        return conflictedUuids;
    }

    /**
     * 按设备装载当前页涉及的全部任务时间窗。
     *
     * <p>{@link FlightTaskQueryService#listTaskWindows(Collection)} 的实现（Mock 与 SDK）
     * 均不对外抛出单台设备查询异常——SDK 实现按 taskType 内部捕获并降级为部分结果，
     * 因此这里不再重复一层「按设备回退查询」：该分支在当前实现下不可达，保留只会
     * 掩盖调用方对真实异常语义的判断。若接入的 {@link FlightTaskQueryService} 实现
     * 允许异常外抛，应在该实现内部处理单设备降级，而不是在此处兜底。
     *
     * @param sns 当前页涉及的机场 SN 集合
     * @return SN 到该设备任务时间窗的映射，查询失败时返回空映射
     */
    private Map<String, List<FlightTaskWindow>> loadWindowsBySn(Set<String> sns) {
        try {
            return queryService.listTaskWindows(sns).stream()
                    .filter(window -> window.sn() != null && !window.sn().isBlank())
                    .collect(Collectors.groupingBy(FlightTaskWindow::sn));
        } catch (Exception e) {
            LOG.warn("任务时间窗批量查询失败，本次跳过冲突标识: sns={}, error={}", sns, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 将查询 SPI 的任务条目转换为带冲突标识的列表条目。
     *
     * @param task         查询 SPI 返回的任务条目
     * @param isConflicted 是否已存在执行时间冲突
     * @return 列表条目 DTO
     */
    private FlightTaskListItemDto toListItem(FlightTaskDto task, boolean isConflicted) {
        return new FlightTaskListItemDto(
                task.taskUuid(), task.name(), task.sn(), task.status(), task.taskType(),
                task.waylineUuid(), task.createdAt(), task.distanceMeters(),
                task.minBatteryCapacity(), isConflicted, task.isBreakPointResume());
    }

    /**
     * 创建飞行任务。
     *
     * @param dto 创建请求体
     * @return 包含 taskUuid 的响应
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateFlightTaskDto dto) {
        String name = normalizeTaskName(dto);
        if (name == null || name.isEmpty()) {
            return badRequest("任务名称不能为空");
        }
        if (dto.getSn() == null || dto.getSn().isBlank()) {
            return badRequest("设备 SN 不能为空");
        }
        if (dto.getWaylineUuid() == null || dto.getWaylineUuid().isBlank()) {
            return badRequest("航线 UUID 不能为空");
        }

        ResponseEntity<?> conflictError = validateNoTimeConflict(dto, "");
        if (conflictError != null) {
            return conflictError;
        }

        ResponseEntity<?> duplicateError = validateTaskNameUnique(name, null);
        if (duplicateError != null) {
            return duplicateError;
        }

        // 回写归一化后的名称，避免判重用的值与实际入库的值不一致
        dto.setName(name);

        try {
            CreateResult result = commandService.createTask(dto);
            return ResponseEntity.ok(Map.of("taskUuid", result.taskUuid()));
        } catch (Exception e) {
            LOG.error("创建飞行任务失败: {}", e.getMessage(), e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 查询任务详情（编辑预填用）。
     *
     * @param uuid 任务 UUID
     * @return 任务详情
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<?> detail(@PathVariable String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return badRequest("任务 UUID 不能为空");
        }
        try {
            FlightTaskDetailDto detail = queryService.getTaskDetail(uuid);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            LOG.info("查询任务详情未命中: uuid={}, error={}", uuid, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            LOG.error("查询任务详情失败: uuid={}, error={}", uuid, e.getMessage(), e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 编辑任务（更新名称、执行时间、返航高度等参数）。
     *
     * <p>可编辑条件与平台门禁对齐，见 {@link #validateTaskEditable(String)}；
     * 不满足时返回 {@code TASK_NOT_EDITABLE} 错误码与全部原因，便于前端与列表页保持一致。
     *
     * @param uuid 任务 UUID
     * @param dto  编辑请求体
     * @return 操作结果
     */
    @PostMapping("/{uuid}/edit")
    public ResponseEntity<?> edit(@PathVariable String uuid, @RequestBody CreateFlightTaskDto dto) {
        if (uuid == null || uuid.isBlank()) {
            return badRequest("任务 UUID 不能为空");
        }
        String name = normalizeTaskName(dto);
        if (name == null || name.isEmpty()) {
            return badRequest("任务名称不能为空");
        }
        if (dto.getSn() == null || dto.getSn().isBlank()) {
            return badRequest("设备 SN 不能为空");
        }
        if (dto.getWaylineUuid() == null || dto.getWaylineUuid().isBlank()) {
            return badRequest("航线 UUID 不能为空");
        }

        ResponseEntity<?> editableError = validateTaskEditable(uuid);
        if (editableError != null) {
            return editableError;
        }

        ResponseEntity<?> conflictError = validateNoTimeConflict(dto, uuid);
        if (conflictError != null) {
            return conflictError;
        }

        // 编辑场景需排除任务自身，否则「不改名直接保存」会被判定为重名
        ResponseEntity<?> duplicateError = validateTaskNameUnique(name, uuid);
        if (duplicateError != null) {
            return duplicateError;
        }

        dto.setName(name);

        try {
            commandService.updateTask(uuid, dto);
            return ResponseEntity.ok(Map.of("taskUuid", uuid, "action", "edit", "status", "ok"));
        } catch (Exception e) {
            LOG.error("编辑飞行任务失败: uuid={}, error={}", uuid, e.getMessage(), e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 删除任务（仅未下发的任务允许删除）。
     *
     * @param uuid 任务 UUID
     * @return 操作结果
     */
    @PostMapping("/{uuid}/delete")
    public ResponseEntity<?> delete(@PathVariable String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return badRequest("任务 UUID 不能为空");
        }
        ResponseEntity<?> stateError = validateTaskInCreatedState(uuid, "删除");
        if (stateError != null) {
            return stateError;
        }
        try {
            commandService.deleteTask(uuid);
            return ResponseEntity.ok(Map.of("taskUuid", uuid, "action", "delete", "status", "ok"));
        } catch (Exception e) {
            LOG.error("删除飞行任务失败: uuid={}, error={}", uuid, e.getMessage(), e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 执行已创建的任务。
     *
     * @param uuid 任务 UUID
     * @return 操作结果
     */
    @PostMapping("/{uuid}/execute")
    public ResponseEntity<?> execute(@PathVariable String uuid) {
        return doAction(uuid, "execute");
    }

    /**
     * 暂停执行中的任务。
     *
     * @param uuid 任务 UUID
     * @return 操作结果
     */
    @PostMapping("/{uuid}/pause")
    public ResponseEntity<?> pause(@PathVariable String uuid) {
        return doAction(uuid, "pause");
    }

    /**
     * 恢复已暂停的任务。
     *
     * @param uuid 任务 UUID
     * @return 操作结果
     */
    @PostMapping("/{uuid}/resume")
    public ResponseEntity<?> resume(@PathVariable String uuid) {
        return doAction(uuid, "resume");
    }

    /**
     * 取消任务。
     *
     * @param uuid 任务 UUID
     * @return 操作结果
     */
    @PostMapping("/{uuid}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String uuid) {
        return doAction(uuid, "cancel");
    }

    private ResponseEntity<?> doAction(String uuid, String action) {
        try {
            switch (action) {
                case "execute" -> commandService.executeTask(uuid);
                case "pause" -> commandService.pauseTask(uuid);
                case "resume" -> commandService.resumeTask(uuid);
                case "cancel" -> commandService.cancelTask(uuid);
                default -> {
                    return badRequest("不支持的操作: " + action);
                }
            }
            return ResponseEntity.ok(Map.of("taskUuid", uuid, "action", action, "status", "ok"));
        } catch (Exception e) {
            LOG.error("{}飞行任务失败: uuid={}, error={}", action, uuid, e.getMessage(), e);
            return serverError(e.getMessage());
        }
    }

    /**
     * 校验任务当前是否可编辑，条件与平台门禁（{@code es-task-management/app/service/task.go} 的
     * {@code EditTask} 前置判断）逐条对齐：
     *
     * <ol>
     *   <li>状态必须为「待执行」(CREATED)</li>
     *   <li>任务类型不能是立即任务（平台 {@code ImmediateTask}=1）</li>
     *   <li>任务类型不能是试飞任务（平台 {@code TestFlightTask}=7）</li>
     *   <li>不能是断点续飞任务</li>
     * </ol>
     *
     * <p>任一条件不满足即拒绝，并在响应的 {@code reasons} 中返回**全部**未满足项，
     * 供列表页展示与「编辑」按钮置灰原因保持一致（前端
     * {@code utils/flightTaskEditability.ts} 使用同一套判断与文案）。
     *
     * <p>注意：删除的门禁比编辑宽松（平台只校验状态，且放行 {@code QueueForTakeoff}），
     * 因此本方法只用于编辑，删除请用 {@link #validateTaskInCreatedState(String, String)}。
     *
     * @param uuid 任务 UUID
     * @return 不可编辑时返回带 {@code code} 字段的 400 响应；可编辑时返回 null
     */
    private ResponseEntity<?> validateTaskEditable(String uuid) {
        DetailLookup lookup = lookupDetail(uuid);
        if (lookup.error() != null) {
            return lookup.error();
        }
        FlightTaskDetailDto detail = lookup.detail();

        List<String> reasons = new ArrayList<>();
        if (!STATUS_CREATED.equals(detail.status())) {
            reasons.add(REASON_NOT_CREATED);
        }
        if (detail.taskType() == TASK_TYPE_IMMEDIATE) {
            reasons.add(REASON_IMMEDIATE_TASK);
        }
        if (detail.taskType() == TASK_TYPE_TEST_FLIGHT) {
            reasons.add(REASON_TEST_FLIGHT_TASK);
        }
        if (detail.isBreakPointResume()) {
            reasons.add(REASON_BREAK_POINT_RESUME);
        }
        if (reasons.isEmpty()) {
            return null;
        }

        String message = String.join(REASON_SEPARATOR, reasons);
        LOG.info("任务不可编辑，已拦截: uuid={}, reasons={}", uuid, message);
        return ResponseEntity.badRequest().body(Map.of(
                "code", ERROR_CODE_TASK_NOT_EDITABLE,
                "message", message,
                "reasons", reasons));
    }

    /**
     * 查询任务详情，把「任务不存在」和「查询异常」统一转换为错误响应。
     *
     * <p>详情查询是编辑/删除门禁的共同前置步骤，抽出以免两处重复 try-catch。
     *
     * @param uuid 任务 UUID
     * @return 查询结果；失败时 {@code error} 非空，成功时 {@code detail} 非空
     */
    private DetailLookup lookupDetail(String uuid) {
        try {
            return new DetailLookup(queryService.getTaskDetail(uuid), null);
        } catch (IllegalArgumentException e) {
            LOG.info("任务不存在: uuid={}, error={}", uuid, e.getMessage());
            return new DetailLookup(null, ResponseEntity.notFound().build());
        } catch (Exception e) {
            LOG.error("查询任务详情失败: uuid={}, error={}", uuid, e.getMessage(), e);
            return new DetailLookup(null, serverError(e.getMessage()));
        }
    }

    /**
     * 任务详情查询结果：成功时 {@code detail} 非空，失败时 {@code error} 非空。
     *
     * @param detail 任务详情
     * @param error  错误响应
     */
    private record DetailLookup(FlightTaskDetailDto detail, ResponseEntity<?> error) {}

    /**
     * 校验任务存在且处于「待执行」(CREATED) 状态（删除的前置条件）。
     *
     * @param uuid        任务 UUID
     * @param actionLabel 动作名称，用于错误提示（"编辑" / "删除"）
     * @return 校验不通过时返回对应错误响应；通过时返回 null
     */
    private ResponseEntity<?> validateTaskInCreatedState(String uuid, String actionLabel) {
        DetailLookup lookup = lookupDetail(uuid);
        if (lookup.error() != null) {
            return lookup.error();
        }
        if (!STATUS_CREATED.equals(lookup.detail().status())) {
            return badRequest("仅待执行状态的任务允许" + actionLabel);
        }
        return null;
    }

    /**
     * 校验候选任务是否与同设备已有任务存在执行时间冲突。
     *
     * <p>冲突判定语义见 {@link FlightTaskConflicts}（同设备 + 半开区间重叠 + 状态/类型过滤）。
     * 查询失败时采用 fail-open：记录告警并放行，避免查询侧故障反而阻断正常建单。
     *
     * @param dto            创建/编辑请求体
     * @param selfTaskUuid   编辑场景传当前任务 UUID（排除自身）；新建场景传空字符串
     * @return 存在冲突时返回 400 响应；无冲突或查询失败时返回 null
     */
    private ResponseEntity<?> validateNoTimeConflict(CreateFlightTaskDto dto, String selfTaskUuid) {
        long beginAt = dto.getBeginAt() != null ? dto.getBeginAt() : 0L;
        long endAt = dto.getEndAt() != null ? dto.getEndAt() : 0L;
        double waylineDuration = dto.getWaylineDuration() != null ? dto.getWaylineDuration() : 0.0;
        warnIfConflictWindowDegraded(dto, beginAt, endAt, waylineDuration);

        TaskWindow candidate = new TaskWindow(
                selfTaskUuid, dto.getName(), dto.getSn(), dto.getTaskType(), beginAt, endAt, waylineDuration);

        List<ConflictingTask> conflicts;
        try {
            conflicts = conflictService.findConflicts(candidate);
        } catch (Exception e) {
            LOG.warn("任务时间冲突校验失败，放行本次请求: sn={}, error={}", dto.getSn(), e.getMessage(), e);
            return null;
        }

        if (conflicts.isEmpty()) {
            return null;
        }
        LOG.info("任务时间冲突拦截: sn={}, name={}, 冲突数={}", dto.getSn(), dto.getName(), conflicts.size());
        return badRequest(buildConflictMessage(dto, conflicts));
    }

    /**
     * 候选任务未下发航线时长且结束时间未知时，冲突窗口会退化为
     * {@value FlightTaskConflicts#PREFLIGHT_PREPARE_DURATION_SECONDS} 秒下限——
     * 航线实际执行时长明显长于该下限时会漏判冲突，记录 WARN 便于观测该已知局限的实际触发频率。
     *
     * @param dto      创建/编辑请求体
     * @param beginAt  归一化后的开始时间
     * @param endAt    归一化后的结束时间
     * @param waylineDuration 归一化后的航线时长
     */
    private void warnIfConflictWindowDegraded(CreateFlightTaskDto dto, long beginAt, long endAt,
                                               double waylineDuration) {
        boolean windowUnknown = endAt <= beginAt && waylineDuration <= 0.0;
        if (windowUnknown && FlightTaskConflicts.CANDIDATE_TASK_TYPES.contains(dto.getTaskType())) {
            LOG.warn("候选任务未提供航线时长，冲突校验窗口退化为 {} 秒下限，可能漏判: sn={}, name={}",
                    FlightTaskConflicts.PREFLIGHT_PREPARE_DURATION_SECONDS, dto.getSn(), dto.getName());
        }
    }

    /**
     * 组装用户可见的冲突提示。
     *
     * @param dto       被拦截的候选任务
     * @param conflicts 冲突任务列表
     * @return 冲突提示文案
     */
    private String buildConflictMessage(CreateFlightTaskDto dto, List<ConflictingTask> conflicts) {
        String details = conflicts.stream()
                .limit(MAX_LISTED_CONFLICTS)
                .map(this::formatConflictTask)
                .collect(Collectors.joining("；"));

        String omitted = conflicts.size() > MAX_LISTED_CONFLICTS
                ? "，另有 " + (conflicts.size() - MAX_LISTED_CONFLICTS) + " 个冲突任务"
                : "";
        String deviceLabel = dto.getSn() != null ? dto.getSn() : "";
        return "该设备（" + deviceLabel + "）在所选执行时间已有任务：" + details + omitted
                + "。同一机场同一时间只能执行一个架次，请调整执行时间后重试。";
    }

    /**
     * 格式化单个冲突任务为「名称（开始 - 结束）」。
     *
     * @param task 冲突任务
     * @return 格式化文案
     */
    private String formatConflictTask(ConflictingTask task) {
        String name = task.name() != null && !task.name().isBlank() ? task.name() : "未命名任务";
        return name + "（" + formatEpochSecond(task.beginAt()) + " - " + formatEpochSecond(task.endAt()) + "）";
    }

    /**
     * 将 Unix 秒格式化为本地时区的「yyyy-MM-dd HH:mm」。
     *
     * @param epochSeconds Unix 秒，非正数返回「未知」
     * @return 格式化时间
     */
    private String formatEpochSecond(long epochSeconds) {
        if (epochSeconds <= 0) {
            return "未知";
        }
        return CONFLICT_TIME_FORMATTER.format(Instant.ofEpochSecond(epochSeconds));
    }

    /**
     * 归一化任务名称：去除首尾空白。
     *
     * <p>判重与入库共用归一化后的值。若只在前端 trim，直接调 API 的调用方
     * 可用 {@code "巡检 "} 与 {@code "巡检"} 绕过唯一性校验，并让带尾空格的值入库。
     *
     * @param dto 创建/编辑请求体
     * @return 去除首尾空白后的名称；入参为 null 时返回 null
     */
    private static String normalizeTaskName(CreateFlightTaskDto dto) {
        return dto.getName() == null ? null : dto.getName().trim();
    }

    /**
     * 校验任务名称在工作空间内唯一（创建/编辑的前置条件）。
     *
     * <p>唯一性范围为整个工作空间，与任务列表的展示范围一致。
     * 查询能力缺失或异常时按 fail-open 处理并记录 WARN：
     * 存量 ISV 未实现 {@code existsTaskWithName} 时会抛 {@link UnsupportedOperationException}，
     * 若此时直接报错会让存量 ISV 的创建/编辑功能整体不可用，回归风险大于漏拦一个重名。
     *
     * @param name            任务名称（调用方需先归一化）
     * @param excludeTaskUuid 需排除的任务 UUID（编辑场景传当前任务 UUID），创建场景传 null
     * @return 重名时返回带 {@code code} 字段的 400 响应；不冲突时返回 null
     */
    private ResponseEntity<?> validateTaskNameUnique(String name, String excludeTaskUuid) {
        try {
            if (queryService.existsTaskWithName(name, excludeTaskUuid)) {
                LOG.info("任务名称重复，已拦截: name={}, excludeTaskUuid={}", name, excludeTaskUuid);
                return duplicateNameError(name);
            }
            return null;
        } catch (UnsupportedOperationException e) {
            LOG.warn("当前 FlightTaskQueryService 实现不支持任务名称唯一性校验，已放行: name={}", name);
            return null;
        } catch (Exception e) {
            // 有意吞掉异常以保证 fail-open，但需保留堆栈，便于定位 SPI 实现中的真实缺陷
            LOG.warn("任务名称唯一性校验失败，已放行: name={}", name, e);
            return null;
        }
    }

    /**
     * 构造任务重名的 400 响应。
     *
     * <p>额外返回 {@code code} 字段，便于前端将错误定位到名称输入框而非通用错误横幅。
     *
     * @param name 冲突的任务名称
     * @return 400 响应
     */
    private ResponseEntity<Map<String, String>> duplicateNameError(String name) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", ERROR_CODE_TASK_NAME_DUPLICATE,
                "message", "任务名称「" + name + "」已存在，请更换名称"));
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private ResponseEntity<Map<String, String>> serverError(String message) {
        String safeMessage = message != null ? message : "未知错误";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", safeMessage));
    }
}
