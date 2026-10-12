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

package com.dji.fh.kernel.spi.event;

/**
 * 飞行任务状态变化事件。
 *
 * @author DJI DevKit Team
 */
public final class FlightTaskProgressEvent extends AbstractKernelEvent implements DeviceScopedEvent {

    private final String deviceSn;
    private final String taskId;
    private final FlightTaskStatus status;
    private final int progress;

    /**
     * @param source   触发来源
     * @param deviceSn 设备 SN
     * @param taskId   任务 ID
     * @param status   任务状态
     * @param progress 进度百分比（SDK 原始值，典型范围 0~100；本构造不做范围校验）
     */
    public FlightTaskProgressEvent(String source, String deviceSn, String taskId,
                                   FlightTaskStatus status, int progress) {
        super(source);
        this.deviceSn = deviceSn;
        this.taskId = taskId;
        this.status = status;
        this.progress = progress;
    }

    @Override
    public String deviceSn() {
        return deviceSn;
    }

    /**
     * @return 任务 ID
     */
    public String taskId() {
        return taskId;
    }

    /**
     * @return 任务状态
     */
    public FlightTaskStatus status() {
        return status;
    }

    /**
     * @return 进度百分比（0~100）
     */
    public int progress() {
        return progress;
    }
}
