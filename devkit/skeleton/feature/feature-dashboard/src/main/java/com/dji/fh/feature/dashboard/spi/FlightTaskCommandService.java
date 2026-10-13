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

import com.dji.fh.feature.dashboard.dto.CreateFlightTaskDto;

/**
 * 飞行任务命令 SPI（创建、编辑、删除、执行、暂停、恢复、取消）。
 *
 * <p>协议约束：平台任务状态接口（{@code UpdateTaskStatusReq}）仅允许下发
 * 0（待开始）和 6（已挂起）两个状态值。"立即执行"/"取消"无法通过该接口
 * 表达，前端 UI 已对应禁用并提示；本 SPI 保留相关方法仅为接口完整性。
 *
 * @author DJI DevKit Team
 */
public interface FlightTaskCommandService {

    /**
     * 创建飞行任务。
     *
     * @param dto 创建请求 DTO
     * @return 创建结果
     */
    CreateResult createTask(CreateFlightTaskDto dto);

    /**
     * 编辑任务（更新名称、执行时间、返航高度等参数）。
     *
     * <p>走独立的 {@code editFlightTask} 接口，不受上述状态协议约束，
     * 仅「待执行」(CREATED) 状态的任务允许编辑。
     *
     * @param taskUuid 任务 UUID
     * @param dto      编辑请求 DTO（复用创建 DTO，字段为创建请求的子集）
     */
    void updateTask(String taskUuid, CreateFlightTaskDto dto);

    /**
     * 删除任务（走独立的 {@code deleteFlightTask} 接口，仅未下发的任务允许删除）。
     *
     * @param taskUuid 任务 UUID
     */
    void deleteTask(String taskUuid);

    /**
     * 执行已创建的任务。
     *
     * <p>注意：协议状态接口不支持下发"执行中"状态，该操作会被平台拒绝，
     * 前端 UI 已禁用该入口；此方法仅为保持接口完整而保留。
     *
     * @param taskUuid 任务 UUID
     */
    void executeTask(String taskUuid);

    /**
     * 暂停任务：下发状态 6（已挂起），协议允许。
     *
     * @param taskUuid 任务 UUID
     */
    void pauseTask(String taskUuid);

    /**
     * 恢复任务：下发状态 0（待开始），协议允许。
     *
     * <p>平台将挂起任务改回"待开始"后按计划时间重新调度执行。
     *
     * @param taskUuid 任务 UUID
     */
    void resumeTask(String taskUuid);

    /**
     * 取消任务。
     *
     * <p>注意：协议状态接口不支持下发"已终止"状态，该操作会被平台拒绝，
     * 前端 UI 已禁用该入口；此方法仅为保持接口完整而保留。
     *
     * @param taskUuid 任务 UUID
     */
    void cancelTask(String taskUuid);

    /**
     * 任务创建结果。
     *
     * @param taskUuid 新建任务的 UUID
     */
    record CreateResult(String taskUuid) {}
}
