// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备日志上报 - 日志项参数 */
public class DeviceLogReportLogItemParam {
    /** 启动索引 */
    private int bootIndex;
    /** 日志开始时间 */
    private long startTime;
    /** 日志结束时间 */
    private long endTime;
    /** 日志大小，单位：字节 */
    private long size;
    /** 文件 MD5 */
    private String md5 = "";

    public DeviceLogReportLogItemParam() {
    }


    /** 启动索引 */
    public int getBootIndex() { return bootIndex; }
    /** @param bootIndex 启动索引 */
    public void setBootIndex(int bootIndex) { this.bootIndex = bootIndex; }
    /** 日志开始时间 */
    public long getStartTime() { return startTime; }
    /** @param startTime 日志开始时间 */
    public void setStartTime(long startTime) { this.startTime = startTime; }
    /** 日志结束时间 */
    public long getEndTime() { return endTime; }
    /** @param endTime 日志结束时间 */
    public void setEndTime(long endTime) { this.endTime = endTime; }
    /** 日志大小，单位：字节 */
    public long getSize() { return size; }
    /** @param size 日志大小，单位：字节 */
    public void setSize(long size) { this.size = size; }
    /** 文件 MD5 */
    public String getMd5() { return md5; }
    /** @param md5 文件 MD5 */
    public void setMd5(String md5) { this.md5 = md5 != null ? md5 : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogReportLogItemParam that = (DeviceLogReportLogItemParam) o;
        return bootIndex == that.bootIndex
                && startTime == that.startTime
                && endTime == that.endTime
                && size == that.size
                && Objects.equals(md5, that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                bootIndex,
                startTime,
                endTime,
                size,
                md5);
    }

    @Override
    public String toString() {
        return "DeviceLogReportLogItemParam{"
                + "bootIndex=" + bootIndex
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", size=" + size
                + ", md5='" + md5 + '\''
                + '}';
    }
}
