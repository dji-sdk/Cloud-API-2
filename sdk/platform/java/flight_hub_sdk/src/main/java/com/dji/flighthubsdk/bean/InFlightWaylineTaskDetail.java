// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 空中航线子任务详情 */
public class InFlightWaylineTaskDetail {
    /** 空中航线任务 UUID */
    private String uuid = "";
    /** 创建用户 ID */
    private String userId = "";
    /** 任务状态 */
    private int status;
    /** 航线 ID */
    private String waylineId = "";
    /** 错误码 */
    private int errCode;
    /** 当前航点索引 */
    private int wayPointIndex;
    /** 执行进度百分比 */
    private int progressPercent;
    /** 任务预估时长 */
    private double waylineDuration;
    /** 航线长度 */
    private double waylineLength;
    /** 任务名称 */
    private String taskName = "";

    /** 空中航线任务 UUID */
    public String getUuid() { return uuid; }
    /** 空中航线任务 UUID */
    public void setUuid(String uuid) { this.uuid = uuid != null ? uuid : ""; }

    /** 创建用户 ID */
    public String getUserId() { return userId; }
    /** 创建用户 ID */
    public void setUserId(String userId) { this.userId = userId != null ? userId : ""; }

    /** 任务状态 */
    public int getStatus() { return status; }
    /** 任务状态 */
    public void setStatus(int status) { this.status = status; }

    /** 航线 ID */
    public String getWaylineId() { return waylineId; }
    /** 航线 ID */
    public void setWaylineId(String waylineId) { this.waylineId = waylineId != null ? waylineId : ""; }

    /** 错误码 */
    public int getErrCode() { return errCode; }
    /** 错误码 */
    public void setErrCode(int errCode) { this.errCode = errCode; }

    /** 当前航点索引 */
    public int getWayPointIndex() { return wayPointIndex; }
    /** 当前航点索引 */
    public void setWayPointIndex(int wayPointIndex) { this.wayPointIndex = wayPointIndex; }

    /** 执行进度百分比 */
    public int getProgressPercent() { return progressPercent; }
    /** 执行进度百分比 */
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InFlightWaylineTaskDetail)) return false;
        InFlightWaylineTaskDetail that = (InFlightWaylineTaskDetail) o;
        return status == that.status
                && errCode == that.errCode
                && wayPointIndex == that.wayPointIndex
                && progressPercent == that.progressPercent
                && Double.compare(that.waylineDuration, waylineDuration) == 0
                && Double.compare(that.waylineLength, waylineLength) == 0
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(userId, that.userId)
                && Objects.equals(waylineId, that.waylineId)
                && Objects.equals(taskName, that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, userId, status, waylineId, errCode, wayPointIndex, progressPercent,
                waylineDuration, waylineLength, taskName);
    }
}
