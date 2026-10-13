// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞行任务异常。 */
public final class FlightTaskException {

    private String uuid;
    private String name;
    private String sn;
    private int taskType;
    /** 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中 */
    private int status;
    private int progress;
    private List<FlightTaskExceptionDetail> exceptions;
    private long timestamp;
    public FlightTaskException() {
    }

/**
     * 构造 FlightTaskException。
     *
     * @param uuid 任务 UUID
     * @param name 任务名称
     * @param sn 设备序列号
     * @param taskType 任务类型
     * @param status 任务状态。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中
     * @param progress 任务进度
     * @param exceptions 异常详情列表
     * @param timestamp 时间戳
     */
    public FlightTaskException(
            String uuid,
            String name,
            String sn,
            int taskType,
            int status,
            int progress,
            List<FlightTaskExceptionDetail> exceptions,
            long timestamp) {
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.sn = sn != null ? sn : "";
        this.taskType = taskType;
        this.status = status;
        this.progress = progress;
        this.exceptions = exceptions != null ? exceptions : Collections.emptyList();
        this.timestamp = timestamp;
    }

    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 任务名称 */
    public String getName() { return name; }
    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 任务类型 */
    public int getTaskType() { return taskType; }
    /** 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中 */
    public int getStatus() { return status; }
    /** 任务进度 */
    public int getProgress() { return progress; }
    /** 异常详情列表 */
    public List<FlightTaskExceptionDetail> getExceptions() { return exceptions; }
    /** 时间戳 */
    public long getTimestamp() { return timestamp; }

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

    /** @param status 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 任务进度 */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /** 异常详情列表 */
    public void setExceptions(List<FlightTaskExceptionDetail> exceptions) {
        this.exceptions = exceptions;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTaskException that = (FlightTaskException) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && Objects.equals(sn, that.sn)
                && taskType == that.taskType
                && status == that.status
                && progress == that.progress
                && Objects.equals(exceptions, that.exceptions)
                && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid,
                name,
                sn,
                taskType,
                status,
                progress,
                exceptions,
                timestamp);
    }

    @Override
    public String toString() {
        return "FlightTaskException{"
                + "uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", sn='" + sn + '\''
                + ", taskType=" + taskType
                + ", status=" + status
                + ", progress=" + progress
                + ", exceptions=" + exceptions
                + ", timestamp=" + timestamp
                + '}';
    }
}
