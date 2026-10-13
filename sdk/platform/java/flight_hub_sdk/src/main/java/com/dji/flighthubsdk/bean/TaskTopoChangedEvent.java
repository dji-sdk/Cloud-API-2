// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 漫游任务拓扑变更 WebSocket 事件。 */
public class TaskTopoChangedEvent {
    private String droneSn = "";
    private String flightId = "";
    private String takeoffSn = "";

    public TaskTopoChangedEvent() {}
    public TaskTopoChangedEvent(String droneSn, String flightId, String takeoffSn) {
        setDroneSn(droneSn);
        setFlightId(flightId);
        setTakeoffSn(takeoffSn);
    }
    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String droneSn) { this.droneSn = droneSn != null ? droneSn : ""; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId != null ? flightId : ""; }
    public String getTakeoffSn() { return takeoffSn; }
    public void setTakeoffSn(String takeoffSn) { this.takeoffSn = takeoffSn != null ? takeoffSn : ""; }
}
