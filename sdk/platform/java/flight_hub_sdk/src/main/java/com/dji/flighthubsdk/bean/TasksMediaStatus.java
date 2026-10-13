// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 任务媒体状态条目。 */
public final class TasksMediaStatus {

    private String taskUuid;
    /** 0-待上传，1-上传中，2-上传结束 */
    private int mediaStatus;
    public TasksMediaStatus() {
    }

/**
     * 构造 TasksMediaStatus。
     *
     * @param taskUuid 任务UUID
     * @param mediaStatus 媒体状态，0-待上传 1-上传中 2-上传结束
     */
    public TasksMediaStatus(String taskUuid, int mediaStatus) {
        this.taskUuid = taskUuid != null ? taskUuid : "";
        this.mediaStatus = mediaStatus;
    }

    /** 任务UUID */
    public String getTaskUuid() { return taskUuid; }
    /** 0-待上传，1-上传中，2-上传结束 */
    public int getMediaStatus() { return mediaStatus; }

    /** 任务UUID */
    public void setTaskUuid(String taskUuid) {
        this.taskUuid = taskUuid;
    }

    /** @param mediaStatus 0-待上传，1-上传中，2-上传结束 */
    public void setMediaStatus(int mediaStatus) {
        this.mediaStatus = mediaStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TasksMediaStatus that = (TasksMediaStatus) o;
        return Objects.equals(taskUuid, that.taskUuid)
                && mediaStatus == that.mediaStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskUuid, mediaStatus);
    }

    @Override
    public String toString() {
        return "TasksMediaStatus{taskUuid='" + taskUuid + '\'' + ", mediaStatus=" + mediaStatus + '}';
    }
}
