// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 日志报告设备日志分片。 */
public final class LogReportListResultDeviceLogItemList {
    /** 启动序号 */
    private int bootIndex;
    /** 分片开始时间（Unix 秒） */
    private long startTime;
    /** 分片结束时间（Unix 秒） */
    private long endTime;
    /** 分片大小（字节） */
    private long size;
    public LogReportListResultDeviceLogItemList() {
    }

    public LogReportListResultDeviceLogItemList(int bootIndex, long startTime, long endTime, long size) {
        this.bootIndex = bootIndex;
        this.startTime = startTime;
        this.endTime = endTime;
        this.size = size;
    }

    /** 启动序号 */
    public int getBootIndex() {
        return bootIndex;
    }

    /** @param bootIndex 启动序号 */
    public void setBootIndex(int bootIndex) {
        this.bootIndex = bootIndex;
    }

    /** 分片开始时间（Unix 秒） */
    public long getStartTime() {
        return startTime;
    }

    /** @param startTime 分片开始时间（Unix 秒） */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /** 分片结束时间（Unix 秒） */
    public long getEndTime() {
        return endTime;
    }

    /** @param endTime 分片结束时间（Unix 秒） */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /** 分片大小（字节） */
    public long getSize() {
        return size;
    }

    /** @param size 分片大小（字节） */
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultDeviceLogItemList that = (LogReportListResultDeviceLogItemList) o;
        return bootIndex == that.bootIndex
                && startTime == that.startTime
                && endTime == that.endTime
                && size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bootIndex, startTime, endTime, size);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceLogItemList{bootIndex=" + bootIndex + ", startTime=" + startTime + ", endTime=" + endTime + ", size=" + size + '}';
    }
}
