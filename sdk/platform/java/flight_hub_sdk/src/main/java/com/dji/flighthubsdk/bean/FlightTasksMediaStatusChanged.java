// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞行任务媒体状态变化。 */
public final class FlightTasksMediaStatusChanged {

    private List<TasksMediaStatus> taskMedia;
    public FlightTasksMediaStatusChanged() {
    }

/**
     * 构造 FlightTasksMediaStatusChanged。
     *
     * @param taskMedia 任务媒体状态列表
     */
    public FlightTasksMediaStatusChanged(List<TasksMediaStatus> taskMedia) {
        this.taskMedia = taskMedia != null ? taskMedia : Collections.emptyList();
    }

    /** 任务媒体状态列表 */
    public List<TasksMediaStatus> getTaskMedia() {
        return taskMedia;
    }

    /** 任务媒体状态列表 */
    public void setTaskMedia(List<TasksMediaStatus> taskMedia) {
        this.taskMedia = taskMedia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTasksMediaStatusChanged that = (FlightTasksMediaStatusChanged) o;
        return Objects.equals(taskMedia, that.taskMedia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskMedia);
    }

    @Override
    public String toString() {
        return "FlightTasksMediaStatusChanged{taskMedia=" + taskMedia + '}';
    }
}
