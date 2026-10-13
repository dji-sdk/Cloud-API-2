// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 创建空中航线任务结果 */
public class CreateInFlightWaylineTaskResult {
    /** 飞行主任务 ID */
    private String flightTaskId = "";
    /** 空中航线子任务 ID */
    private String inFlightWaylineTaskId = "";

    public CreateInFlightWaylineTaskResult() {
    }

    public CreateInFlightWaylineTaskResult(String flightTaskId, String inFlightWaylineTaskId) {
        this.flightTaskId = flightTaskId != null ? flightTaskId : "";
        this.inFlightWaylineTaskId = inFlightWaylineTaskId != null ? inFlightWaylineTaskId : "";
    }

    /** 飞行主任务 ID */
    public String getFlightTaskId() { return flightTaskId; }
    /** @param flightTaskId 飞行主任务 ID */
    public void setFlightTaskId(String flightTaskId) { this.flightTaskId = flightTaskId != null ? flightTaskId : ""; }

    /** 空中航线子任务 ID */
    public String getInFlightWaylineTaskId() { return inFlightWaylineTaskId; }
    /** @param inFlightWaylineTaskId 空中航线子任务 ID */
    public void setInFlightWaylineTaskId(String inFlightWaylineTaskId) {
        this.inFlightWaylineTaskId = inFlightWaylineTaskId != null ? inFlightWaylineTaskId : "";
    }

    @Override
    public String toString() {
        return "CreateInFlightWaylineTaskResult{flightTaskId='" + flightTaskId
                + "', inFlightWaylineTaskId='" + inFlightWaylineTaskId + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateInFlightWaylineTaskResult)) return false;
        CreateInFlightWaylineTaskResult that = (CreateInFlightWaylineTaskResult) o;
        return Objects.equals(flightTaskId, that.flightTaskId)
                && Objects.equals(inFlightWaylineTaskId, that.inFlightWaylineTaskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightTaskId, inFlightWaylineTaskId);
    }
}
