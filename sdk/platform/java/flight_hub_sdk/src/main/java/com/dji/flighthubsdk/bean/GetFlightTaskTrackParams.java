// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 查询飞行任务轨迹参数 */
public class GetFlightTaskTrackParams {
    /** 任务 UUID（必填） */
    private String taskUuid = "";

    public GetFlightTaskTrackParams() {
    }

    /** 任务 UUID（必填） */
    public String getTaskUuid() { return taskUuid; }
    /** @param taskUuid 任务 UUID（必填） */
    public void setTaskUuid(String taskUuid) { this.taskUuid = taskUuid != null ? taskUuid : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetFlightTaskTrackParams that = (GetFlightTaskTrackParams) o;
        return Objects.equals(taskUuid, that.taskUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskUuid);
    }

    @Override
    public String toString() {
        return "GetFlightTaskTrackParams{"
                + "taskUuid='" + taskUuid + '\''
                + '}';
    }
}
