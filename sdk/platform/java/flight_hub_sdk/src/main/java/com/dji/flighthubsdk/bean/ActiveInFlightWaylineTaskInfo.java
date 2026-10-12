// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 活跃空中航线任务查询结果 */
public class ActiveInFlightWaylineTaskInfo {
    /** 飞行主任务 ID */
    private String flightId = "";
    /** 飞行任务类型 */
    private int flightType;
    /** 设备 SN */
    private String sn = "";
    /** 空中航线子任务详情 */
    private InFlightWaylineTaskDetail inFlightTask = new InFlightWaylineTaskDetail();
    /** 飞行任务扩展数据（JSON） */
    private String flightTaskData = "";

    /** 飞行主任务 ID */
    public String getFlightId() { return flightId; }
    /** 飞行主任务 ID */
    public void setFlightId(String flightId) { this.flightId = flightId != null ? flightId : ""; }

    /** 飞行任务类型 */
    public int getFlightType() { return flightType; }
    /** 飞行任务类型 */
    public void setFlightType(int flightType) { this.flightType = flightType; }

    /** 设备 SN */
    public String getSn() { return sn; }
    /** 设备 SN */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }

    /** 空中航线子任务详情 */
    public InFlightWaylineTaskDetail getInFlightTask() { return inFlightTask; }
    /** 空中航线子任务详情 */
    public void setInFlightTask(InFlightWaylineTaskDetail inFlightTask) {
        this.inFlightTask = inFlightTask != null ? inFlightTask : new InFlightWaylineTaskDetail();
    }

    /** 飞行任务扩展数据（JSON） */
    public String getFlightTaskData() { return flightTaskData; }
    /** 飞行任务扩展数据（JSON） */
    public void setFlightTaskData(String flightTaskData) {
        this.flightTaskData = flightTaskData != null ? flightTaskData : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveInFlightWaylineTaskInfo)) return false;
        ActiveInFlightWaylineTaskInfo that = (ActiveInFlightWaylineTaskInfo) o;
        return flightType == that.flightType
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(sn, that.sn)
                && Objects.equals(inFlightTask, that.inFlightTask)
                && Objects.equals(flightTaskData, that.flightTaskData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, flightType, sn, inFlightTask, flightTaskData);
    }
}
