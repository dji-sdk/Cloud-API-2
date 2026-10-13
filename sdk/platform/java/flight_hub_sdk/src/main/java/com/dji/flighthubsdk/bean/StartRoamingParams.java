// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 开始漫游请求。sourceTaskType：1=航线任务，2=一键起飞任务。 */
public class StartRoamingParams {
    /** 无人机 SN（必填） */
    private String droneSn = "";
    /** 架次 UUID（必填） */
    private String flightId = "";
    /** 来源任务类型：1=航线 tasks，2=command_tasks（必填） */
    private int sourceTaskType = 1;

    /** 无人机 SN（必填） */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 无人机 SN（必填） */
    public void setDroneSn(String droneSn) { this.droneSn = droneSn != null ? droneSn : ""; }
    /** 架次 UUID（必填） */
    public String getFlightId() { return flightId; }
    /** @param flightId 架次 UUID（必填） */
    public void setFlightId(String flightId) { this.flightId = flightId != null ? flightId : ""; }
    /** 来源任务类型：1=航线 tasks，2=command_tasks（必填） */
    public int getSourceTaskType() { return sourceTaskType; }
    /** @param sourceTaskType 来源任务类型：1=航线 tasks，2=command_tasks（必填） */
    public void setSourceTaskType(int sourceTaskType) { this.sourceTaskType = sourceTaskType; }
}
