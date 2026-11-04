// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 任务异常事件。 */
public final class TaskExceptionEvent {
    private String uuid;
    private String name;
    private String sn;
    private int taskType;
    private int status;
    private long timestamp;
    private List<TaskExceptionItem> exceptions;

    public TaskExceptionEvent() {
        this.exceptions = new ArrayList<TaskExceptionItem>();
    }

    /**
     * 构造 TaskExceptionEvent。
     *
     * @param uuid 任务 UUID
     * @param name 任务名称
     * @param sn 设备序列号
     * @param taskType 任务类型
     * @param status 任务状态
     * @param timestamp 时间戳，单位：毫秒
     * @param exceptions 异常列表
     */
    public TaskExceptionEvent(String uuid, String name, String sn, int taskType, int status,
            long timestamp, List<TaskExceptionItem> exceptions) {
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.sn = sn != null ? sn : "";
        this.taskType = taskType;
        this.status = status;
        this.timestamp = timestamp;
        this.exceptions = exceptions != null ? exceptions : new ArrayList<TaskExceptionItem>();
    }

    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 任务名称 */
    public String getName() { return name; }
    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 任务类型 */
    public int getTaskType() { return taskType; }
    /** 任务状态 */
    public int getStatus() { return status; }
    /** 时间戳，单位：毫秒 */
    public long getTimestamp() { return timestamp; }
    /** 异常列表 */
    public List<TaskExceptionItem> getExceptions() { return exceptions; }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 任务名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 任务类型 */
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    /** 任务状态 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 时间戳，单位：毫秒 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 异常列表 */
    public void setExceptions(List<TaskExceptionItem> exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskExceptionEvent that = (TaskExceptionEvent) o;
        return taskType == that.taskType
                && status == that.status
                && timestamp == that.timestamp
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && Objects.equals(sn, that.sn)
                && Objects.equals(exceptions, that.exceptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, sn, taskType, status, timestamp, exceptions);
    }

    @Override
    public String toString() {
        return "TaskExceptionEvent{"
                + "uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", sn='" + sn + '\''
                + ", taskType=" + taskType
                + ", status=" + status
                + ", timestamp=" + timestamp
                + ", exceptions=" + exceptions
                + '}';
    }
}
