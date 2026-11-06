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

import com.dji.fh.feature.dashboard.dto.CreateFlightTaskDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskCommandService;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.CreateTaskReq;
import com.dji.flighthubsdk.bean.CreateTaskResp;
import com.dji.flighthubsdk.bean.EditTaskReq;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.Workspace;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 飞行任务命令真实 SDK 实现。
 *
 * <p>通过 {@code FlightTaskModule} 执行任务创建、状态变更等写操作。
 *
 * <p>协议约束：任务状态接口（{@code UpdateTaskStatusReq}）仅允许下发
 * 0（待开始）和 6（已挂起）两个状态值，"立即执行"/"取消"等操作无法通过
 * 该接口表达（UI 层已对应禁用并提示）。
 *
 * @author DJI DevKit Team
 */
public class SdkFlightTaskCommandService implements FlightTaskCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkFlightTaskCommandService.class);

    /**
     * 协议允许下发的任务状态码（{@code UpdateTaskStatusReq} 注释明确仅允许这两个值）：
     * 0=待开始（平台按计划时间自动调度执行）、6=已挂起（用户主动暂停）。
     */
    private static final int STATUS_PENDING_START = 0;
    private static final int STATUS_SUSPENDED = 6;

    /** 执行中状态码（2）：协议状态接口不支持下发，仅为保持接口兼容保留于 executeTask */
    private static final int STATUS_EXECUTING = 2;

    /** 已终止状态码（4）：协议状态接口不支持下发，仅为保持接口兼容保留于 cancelTask */
    private static final int STATUS_TERMINATED = 4;

    /** repeat_type 的「不重复」取值（kcron RepeatType.NonRepeating），前端无条件下发的默认值 */
    private static final int REPEAT_TYPE_NONE = 0;

    /** plan_type 的「普通计划」取值，前端无条件下发的默认值，属无副作用字段 */
    private static final int PLAN_TYPE_DEFAULT = 0;

    /**
     * 前端 resumable_status=1（开启自动续飞）对应的 SDK 取值。
     *
     * <p>{@code CreateTaskReq#resumable_status} 是字符串枚举而非整数，
     * {@code 1} 必须映射为 {@code "auto"} 下发，不能写进 {@code is_break_point_resume}——
     * 后者表达的是「本任务是否为断点续飞任务」，与续飞策略是两个概念。
     */
    private static final String RESUMABLE_STATUS_AUTO = "auto";

    /** 前端 resumable_status=0（不开启自动续飞）对应的 SDK 取值。 */
    private static final String RESUMABLE_STATUS_MANUAL = "manual";

    @Override
    public CreateResult createTask(CreateFlightTaskDto dto) {
        FlightTaskModule ftm = getFlightTaskModule();

        CreateTaskReq req = buildCreateReq(dto);

        CompletableFuture<CreateTaskResp> future = new CompletableFuture<>();
        ftm.createFlightTask(req, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "createFlightTask 失败: " + error.getServerMessage()));
                return;
            }
            future.complete(result);
        });

        CreateTaskResp resp = SdkBridge.await(future);

        List<Integer> checkCodes = resp.getWaylineValidityCheckCodes();
        if (checkCodes != null && !checkCodes.isEmpty()
                && checkCodes.stream().anyMatch(code -> code != 0)) {
            throw new SdkBridge.SdkQueryException(
                    "航线有效性校验失败，检查码: " + checkCodes);
        }

        String uuid = resp.getUuid();
        if (uuid == null || uuid.isBlank()) {
            throw new SdkBridge.SdkQueryException(
                    "创建飞行任务失败: 服务端返回空任务 UUID");
        }

        LOG.info("SDK 创建飞行任务成功: taskUuid={}", uuid);
        return new CreateResult(uuid);
    }

    /**
     * 执行任务：下发状态 2（执行中）。
     *
     * <p>注意：协议状态接口仅允许 0/6，下发 2 会被平台拒绝，
     * 前端已禁用该操作入口，此方法仅为保持 SPI 接口完整而保留。
     */
    @Override
    public void executeTask(String taskUuid) {
        updateStatus(taskUuid, STATUS_EXECUTING, "执行");
    }

    /**
     * 编辑任务：通过 {@code EditTaskReq} 覆写任务字段。
     *
     * <p>注意：SDK 的 {@code EditTaskReq} 同样没有重复规则字段，
     * 编辑一张重复任务不会改变其周期配置（见 {@link #warnUnsupportedFields}）。
     */
    @Override
    public void updateTask(String taskUuid, CreateFlightTaskDto dto) {
        FlightTaskModule ftm = getFlightTaskModule();

        EditTaskReq req = buildEditReq(taskUuid, dto);

        CompletableFuture<Void> future = new CompletableFuture<>();
        ftm.editFlightTask(taskUuid, req, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "编辑任务失败: " + error.getServerMessage()));
                return;
            }
            future.complete(null);
        });

        SdkBridge.await(future);
        LOG.info("SDK 编辑飞行任务成功: taskUuid={}", taskUuid);
    }

    @Override
    public void deleteTask(String taskUuid) {
        FlightTaskModule ftm = getFlightTaskModule();

        CompletableFuture<Void> future = new CompletableFuture<>();
        ftm.deleteFlightTask(taskUuid, error -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "删除任务失败: " + error.getServerMessage()));
                return;
            }
            future.complete(null);
        });

        SdkBridge.await(future);
        LOG.info("SDK 删除飞行任务成功: taskUuid={}", taskUuid);
    }

    /**
     * 暂停任务：下发状态 6（已挂起），协议允许。
     */
    @Override
    public void pauseTask(String taskUuid) {
        updateStatus(taskUuid, STATUS_SUSPENDED, "暂停");
    }

    /**
     * 恢复任务：下发状态 0（待开始），协议允许。
     *
     * <p>平台将挂起任务改回"待开始"后会按计划时间重新调度执行。
     */
    @Override
    public void resumeTask(String taskUuid) {
        updateStatus(taskUuid, STATUS_PENDING_START, "恢复");
    }

    /**
     * 取消任务：下发状态 4（已终止）。
     *
     * <p>注意：协议状态接口仅允许 0/6，下发 4 会被平台拒绝，
     * 前端已禁用该操作入口，此方法仅为保持 SPI 接口完整而保留。
     */
    @Override
    public void cancelTask(String taskUuid) {
        updateStatus(taskUuid, STATUS_TERMINATED, "取消");
    }

    private void updateStatus(String taskUuid, int status, String actionLabel) {
        FlightTaskModule ftm = getFlightTaskModule();

        CompletableFuture<Void> future = new CompletableFuture<>();
        ftm.updateFlightTaskStatus(taskUuid, status, error -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        actionLabel + "任务失败: " + error.getServerMessage()));
                return;
            }
            future.complete(null);
        });

        SdkBridge.await(future);
        LOG.info("SDK {}飞行任务成功: taskUuid={}", actionLabel, taskUuid);
    }

    /**
     * 获取 FlightTaskModule；SDK 未初始化时抛出异常。
     *
     * <p>package-private 便于单元测试覆写注入 mock（同
     * {@link SdkFlightTaskQueryService#requireFlightTaskModule()} 惯例）。
     *
     * @return FlightTaskModule 实例
     */
    FlightTaskModule getFlightTaskModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new SdkBridge.SdkQueryException("Workspace 不可用，SDK 未初始化");
        }
        FlightTaskModule ftm = ws.getFlightTaskModule();
        if (ftm == null) {
            throw new SdkBridge.SdkQueryException("FlightTaskModule 不可用");
        }
        return ftm;
    }

    /**
     * 把前端的整型 {@code resumable_status} 映射为 SDK 的字符串枚举。
     *
     * <p>{@code CreateTaskReq#setResumableStatus(String)} 内部对 null 的处理是把字段
     * 写成空串 {@code ""} 而非跳过，因此调用方必须先判空，不能把 null 透传进去。
     *
     * @param resumableStatus 前端取值：1=自动续飞，其余=手动续飞
     * @return SDK 字符串枚举："auto" 或 "manual"
     */
    private static String mapResumableStatus(int resumableStatus) {
        return resumableStatus == 1 ? RESUMABLE_STATUS_AUTO : RESUMABLE_STATUS_MANUAL;
    }

    private CreateTaskReq buildCreateReq(CreateFlightTaskDto dto) {
        warnUnsupportedFields(dto, false);

        CreateTaskReq req = new CreateTaskReq();
        req.setName(dto.getName());
        req.setSn(dto.getSn());
        req.setTaskType(dto.getTaskType());
        req.setWaylineUuid(dto.getWaylineUuid());
        req.setRthAltitude(dto.getRthAltitude());
        req.setTimeZone(dto.getTimeZone());
        req.setOutOfControlActionInFlight(dto.getOutOfControlActionInFlight());

        if (dto.getBeginAt() != null) {
            req.setBeginAt(dto.getBeginAt());
        }
        if (dto.getEndAt() != null) {
            req.setEndAt(dto.getEndAt());
        }
        if (dto.getRthMode() != null) {
            req.setRthMode(dto.getRthMode());
        }
        if (dto.getWaylinePrecisionType() != null) {
            req.setWaylinePrecisionType(dto.getWaylinePrecisionType());
        }
        if (dto.getMinBatteryCapacity() != null) {
            req.setMinBatteryCapacity(dto.getMinBatteryCapacity());
        }
        if (dto.getResumableStatus() != null) {
            req.setResumableStatus(mapResumableStatus(dto.getResumableStatus()));
        }
        // 显式下发航线预估时长：平台按 int(wayline_duration) + preflight_prepare_duration 计算任务时间窗，
        // 不传时窗口退化为仅准备时长，会让冲突校验的时间窗与实际执行窗不一致。
        // 仅创建请求支持该字段（EditTaskReq 无对应 setter）。
        if (dto.getWaylineDuration() != null) {
            req.setWaylineDuration(dto.getWaylineDuration());
        }

        return req;
    }

    /**
     * 构建编辑任务请求。
     *
     * <p>编辑请求复用创建 DTO 的字段子集：必填字段（name/sn/taskType 等）无条件 set，
     * 可选字段（beginAt/endAt 等）仅非 null 时 set，与 {@link #buildCreateReq(CreateFlightTaskDto)}
     * 保持相同的条件 set 策略。
     *
     * <p><b>能力边界</b>：{@code EditTaskReq} 没有 {@code resumableStatus} 承载位，
     * 编辑任务无法改变自动续飞策略（创建时下发的策略保持不变）；{@code vlm_enabled}
     * 在创建与编辑两侧均无承载位。两者均由 {@link #warnUnsupportedFields} 显式告警。
     *
     * @param taskUuid 待编辑任务的 UUID
     * @param dto      编辑请求 DTO
     * @return SDK 编辑请求对象
     */
    private EditTaskReq buildEditReq(String taskUuid, CreateFlightTaskDto dto) {
        warnUnsupportedFields(dto, true);

        EditTaskReq req = new EditTaskReq();
        req.setUuid(taskUuid);
        req.setName(dto.getName());
        req.setSn(dto.getSn());
        req.setTaskType(dto.getTaskType());
        req.setWaylineUuid(dto.getWaylineUuid());
        req.setRthAltitude(dto.getRthAltitude());
        req.setTimeZone(dto.getTimeZone());
        req.setOutOfControlActionInFlight(dto.getOutOfControlActionInFlight());

        if (dto.getBeginAt() != null) {
            req.setBeginAt(dto.getBeginAt());
        }
        if (dto.getEndAt() != null) {
            req.setEndAt(dto.getEndAt());
        }
        if (dto.getRthMode() != null) {
            req.setRthMode(dto.getRthMode());
        }
        if (dto.getWaylinePrecisionType() != null) {
            req.setWaylinePrecisionType(dto.getWaylinePrecisionType());
        }
        if (dto.getMinBatteryCapacity() != null) {
            req.setMinBatteryCapacity(dto.getMinBatteryCapacity());
        }
        // resumable_status 在 EditTaskReq 上无承载位（该类未暴露 resumableStatus setter），
        // 故编辑路径不 set 任何续飞字段：既不再错写成 isBreakPointResume，
        // 也无法改变服务端的续飞策略。该限制由 warnUnsupportedFields 显式告警。

        return req;
    }

    /**
     * 对 SDK 无法承载的请求字段显式告警，替代原先的静默丢弃。
     *
     * <p>截至 SDK 3.3.7，{@code CreateTaskReq} / {@code EditTaskReq} 仍没有
     * {@code repeat_type} / {@code interval} / {@code plan_type} / {@code vlm_enabled} /
     * {@code continuous_task_periods} 的 setter，这些字段在 BFF 层就被丢弃。
     * 历史实现对此毫无反馈，导致「界面选了重复定时、提交后无任何提示、任务却按单次执行」
     * 的现场问题。本方法让该丢失在 BFF 日志中可见。
     *
     * <p>{@code resumable_status} 需要分路径看待：创建路径由
     * {@link CreateTaskReq#setResumableStatus(String)} 真正下发，不告警；编辑路径
     * {@code EditTaskReq}（独立类，非 CreateTaskReq 子类）没有该 setter，只能丢弃并告警。
     *
     * <p>仅当字段真正表达了非默认语义时才告警：{@code plan_type=0}（普通计划）、
     * {@code repeat_type=0}（不重复）、{@code vlm_enabled=false} 是前端无条件下发的默认值，
     * 丢弃它们没有副作用，若一并告警会让真正的问题淹没在噪声里。
     *
     * <p><b>根治需要 SDK 侧补齐字段并由 PaaS 服务端支持重复调度，devkit 单侧无法闭环。</b>
     *
     * @param dto    待提交的任务请求
     * @param isEdit 是否为编辑路径
     */
    private void warnUnsupportedFields(CreateFlightTaskDto dto, boolean isEdit) {
        List<String> dropped = new ArrayList<>();
        if (dto.getRepeatType() != null && dto.getRepeatType() != REPEAT_TYPE_NONE) {
            dropped.add("repeat_type=" + dto.getRepeatType());
        }
        if (dto.getInterval() != null) {
            dropped.add("interval=" + dto.getInterval());
        }
        if (dto.getContinuousTaskPeriods() != null && !dto.getContinuousTaskPeriods().isEmpty()) {
            dropped.add("continuous_task_periods=" + dto.getContinuousTaskPeriods());
        }
        if (dto.getPlanType() != null && dto.getPlanType() != PLAN_TYPE_DEFAULT) {
            dropped.add("plan_type=" + dto.getPlanType());
        }
        if (Boolean.TRUE.equals(dto.getVlmEnabled())) {
            dropped.add("vlm_enabled=true");
        }
        if (isEdit && dto.getResumableStatus() != null) {
            dropped.add("resumable_status=" + dto.getResumableStatus());
        }

        if (!dropped.isEmpty()) {
            LOG.warn("请求含 SDK 无法承载的字段，已丢弃，服务端不会按此调度: isEdit={}, taskType={}, 丢弃字段=[{}]",
                    isEdit, dto.getTaskType(), String.join(", ", dropped));
        }
    }
}
