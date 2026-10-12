// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 创建空中航线任务参数（不含 drone_sn / gateway_sn，由 Module 注入） */
public class CreateInFlightWaylineTaskParams {
    /** 航线 UUID（必填） */
    private String waylineUuid = "";
    /** 返航高度(米)（必填，飞控验证要求≥2） */
    private int rthAltitude;
    /** 航线精度类型。-1 表示未设置 */
    private int waylinePrecisionType = -1;
    /** 返航高度模式。-1 表示未设置 */
    private int rthMode = -1;
    /** 飞控失控动作: 0-返航 1-悬停 2-降落。-1 表示未设置 */
    private int outOfControlAction = -1;
    /** 航线失控动作: 0-继续执行 1-执行飞控失控动作。-1 表示未设置 */
    private int exitWaylineWhenRcLost = -1;
    /** 任务预估时长。-1 表示未设置 */
    private double waylineDuration = -1;
    /** 航线长度。-1 表示未设置 */
    private double waylineLength = -1;
    /** 任务名称。空串表示未设置 */
    private String taskName = "";
    /** 快速建模。-1 未设置；0=false；1=true */
    private Boolean fastReconstruction;

    /** 航线 UUID（必填） */
    public String getWaylineUuid() { return waylineUuid; }
    /** @param waylineUuid 航线 UUID（必填） */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid != null ? waylineUuid : ""; }

    /** 返航高度(米)（必填，飞控验证要求≥2） */
    public int getRthAltitude() { return rthAltitude; }
    /** @param rthAltitude 返航高度(米)（必填，飞控验证要求≥2） */
    public void setRthAltitude(int rthAltitude) { this.rthAltitude = rthAltitude; }

    /** 航线精度类型。-1 表示未设置 */
    public int getWaylinePrecisionType() { return waylinePrecisionType; }
    /** @param waylinePrecisionType 航线精度类型。-1 表示未设置 */
    public void setWaylinePrecisionType(int waylinePrecisionType) { this.waylinePrecisionType = waylinePrecisionType; }

    /** 返航高度模式。-1 表示未设置 */
    public int getRthMode() { return rthMode; }
    /** @param rthMode 返航高度模式。-1 表示未设置 */
    public void setRthMode(int rthMode) { this.rthMode = rthMode; }

    /** 飞控失控动作: 0-返航 1-悬停 2-降落。-1 表示未设置 */
    public int getOutOfControlAction() { return outOfControlAction; }
    /** @param outOfControlAction 飞控失控动作: 0-返航 1-悬停 2-降落。-1 表示未设置 */
    public void setOutOfControlAction(int outOfControlAction) { this.outOfControlAction = outOfControlAction; }

    /** 航线失控动作: 0-继续执行 1-执行飞控失控动作。-1 表示未设置 */
    public int getExitWaylineWhenRcLost() { return exitWaylineWhenRcLost; }
    /** @param exitWaylineWhenRcLost 航线失控动作: 0-继续执行 1-执行飞控失控动作。-1 表示未设置 */
    public void setExitWaylineWhenRcLost(int exitWaylineWhenRcLost) { this.exitWaylineWhenRcLost = exitWaylineWhenRcLost; }

    /** 任务预估时长。-1 表示未设置 */
    public double getWaylineDuration() { return waylineDuration; }
    /** @param waylineDuration 任务预估时长。-1 表示未设置 */
    public void setWaylineDuration(double waylineDuration) { this.waylineDuration = waylineDuration; }

    /** 航线长度。-1 表示未设置 */
    public double getWaylineLength() { return waylineLength; }
    /** @param waylineLength 航线长度。-1 表示未设置 */
    public void setWaylineLength(double waylineLength) { this.waylineLength = waylineLength; }

    /** 任务名称。空串表示未设置 */
    public String getTaskName() { return taskName; }
    /** @param taskName 任务名称。空串表示未设置 */
    public void setTaskName(String taskName) { this.taskName = taskName != null ? taskName : ""; }

    /** 快速建模。-1 未设置；0=false；1=true */
    public Boolean getFastReconstruction() { return fastReconstruction; }
    /** @param fastReconstruction 快速建模。-1 未设置；0=false；1=true */
    public void setFastReconstruction(Boolean fastReconstruction) { this.fastReconstruction = fastReconstruction; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateInFlightWaylineTaskParams)) return false;
        CreateInFlightWaylineTaskParams that = (CreateInFlightWaylineTaskParams) o;
        return rthAltitude == that.rthAltitude
                && waylinePrecisionType == that.waylinePrecisionType
                && rthMode == that.rthMode
                && outOfControlAction == that.outOfControlAction
                && exitWaylineWhenRcLost == that.exitWaylineWhenRcLost
                && Double.compare(that.waylineDuration, waylineDuration) == 0
                && Double.compare(that.waylineLength, waylineLength) == 0
                && Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(taskName, that.taskName)
                && Objects.equals(fastReconstruction, that.fastReconstruction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waylineUuid, rthAltitude, waylinePrecisionType, rthMode, outOfControlAction,
                exitWaylineWhenRcLost, waylineDuration, waylineLength, taskName, fastReconstruction);
    }
}
