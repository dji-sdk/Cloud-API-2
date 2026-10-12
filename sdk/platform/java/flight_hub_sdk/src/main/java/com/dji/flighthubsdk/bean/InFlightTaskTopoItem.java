// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 飞行中任务拓扑项。 */
public class InFlightTaskTopoItem {
    private String droneSn = "";
    private String flightId = "";
    /** 起飞基站 SN */
    private String takeoffSn = "";

    public InFlightTaskTopoItem() {}
    public InFlightTaskTopoItem(String droneSn, String flightId, String takeoffSn) {
        setDroneSn(droneSn);
        setFlightId(flightId);
        setTakeoffSn(takeoffSn);
    }
    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String droneSn) { this.droneSn = droneSn != null ? droneSn : ""; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId != null ? flightId : ""; }
    /** 起飞基站 SN */
    public String getTakeoffSn() { return takeoffSn; }
    /** @param takeoffSn 起飞基站 SN */
    public void setTakeoffSn(String takeoffSn) { this.takeoffSn = takeoffSn != null ? takeoffSn : ""; }
}
