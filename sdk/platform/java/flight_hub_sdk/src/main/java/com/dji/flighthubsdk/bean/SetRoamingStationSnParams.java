// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 设置飞机级漫游基站白名单请求。 */
public class SetRoamingStationSnParams {
    /** 无人机 SN */
    private String droneSn = "";
    /** 基站 SN 列表（0–50） */
    private List<String> stationSns = new ArrayList<>();

    /** 无人机 SN */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 无人机 SN */
    public void setDroneSn(String droneSn) { this.droneSn = droneSn != null ? droneSn : ""; }
    /** 基站 SN 列表（0–50） */
    public List<String> getStationSns() { return stationSns; }
    /** @param stationSns 基站 SN 列表（0–50） */
    public void setStationSns(List<String> stationSns) {
        this.stationSns = stationSns != null ? stationSns : new ArrayList<String>();
    }
}
