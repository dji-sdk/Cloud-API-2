// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 空中航线任务进度推送 */
public class InFlightWaylineProgress {
    /** 空中航线任务 ID */
    private String inFlightWaylineId = "";
    /** 执行进度百分比(0-100) */
    private int percent;
    /** 任务状态 */
    private int status;
    /** 执行结果 */
    private int result;
    /** 网关 SN */
    private String gatewaySn = "";
    /** 当前航点索引 */
    private int wayPointIndex;
    /** 任务预估时长 */
    private double waylineDuration;
    /** 航线长度 */
    private double waylineLength;
    /** 任务名称 */
    private String taskName = "";
    /** 时间戳(秒)。勿用 int64_t（SWIG 无法映射到 Java） */
    private long timestamp;

    /** 空中航线任务 ID */
    public String getInFlightWaylineId() { return inFlightWaylineId; }
    /** 空中航线任务 ID */
    public void setInFlightWaylineId(String inFlightWaylineId) {
        this.inFlightWaylineId = inFlightWaylineId != null ? inFlightWaylineId : "";
    }

    /** 执行进度百分比(0-100) */
    public int getPercent() { return percent; }
    /** 执行进度百分比(0-100) */
    public void setPercent(int percent) { this.percent = percent; }

    /** 任务状态 */
    public int getStatus() { return status; }
    /** 任务状态 */
    public void setStatus(int status) { this.status = status; }

    /** 执行结果 */
    public int getResult() { return result; }
    /** 执行结果 */
    public void setResult(int result) { this.result = result; }

    /** 网关 SN */
    public String getGatewaySn() { return gatewaySn; }
    /** 网关 SN */
    public void setGatewaySn(String gatewaySn) { this.gatewaySn = gatewaySn != null ? gatewaySn : ""; }

    /** 当前航点索引 */
    public int getWayPointIndex() { return wayPointIndex; }
    /** 当前航点索引 */
    public void setWayPointIndex(int wayPointIndex) { this.wayPointIndex = wayPointIndex; }

    /** 任务预估时长 */
    public double getWaylineDuration() { return waylineDuration; }
    /** 任务预估时长 */
    public void setWaylineDuration(double waylineDuration) { this.waylineDuration = waylineDuration; }

    /** 航线长度 */
    public double getWaylineLength() { return waylineLength; }
    /** 航线长度 */
    public void setWaylineLength(double waylineLength) { this.waylineLength = waylineLength; }

    /** 任务名称 */
    public String getTaskName() { return taskName; }
    /** 任务名称 */
    public void setTaskName(String taskName) { this.taskName = taskName != null ? taskName : ""; }

    /** 时间戳(秒)。勿用 int64_t（SWIG 无法映射到 Java） */
    public long getTimestamp() { return timestamp; }
    /** @param timestamp 时间戳(秒)。勿用 int64_t（SWIG 无法映射到 Java） */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InFlightWaylineProgress)) return false;
        InFlightWaylineProgress that = (InFlightWaylineProgress) o;
        return percent == that.percent
                && status == that.status
                && result == that.result
                && wayPointIndex == that.wayPointIndex
                && Double.compare(that.waylineDuration, waylineDuration) == 0
                && Double.compare(that.waylineLength, waylineLength) == 0
                && timestamp == that.timestamp
                && Objects.equals(inFlightWaylineId, that.inFlightWaylineId)
                && Objects.equals(gatewaySn, that.gatewaySn)
                && Objects.equals(taskName, that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inFlightWaylineId, percent, status, result, gatewaySn, wayPointIndex,
                waylineDuration, waylineLength, taskName, timestamp);
    }
}
