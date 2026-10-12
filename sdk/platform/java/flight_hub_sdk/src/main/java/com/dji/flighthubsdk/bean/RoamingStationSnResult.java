// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 单个漫游基站白名单写入结果。failReason：0=成功，1=非 EA610，2=非漫游模式，3=设备不可达。 */
public class RoamingStationSnResult {
    private String stationSn = "";
    private boolean success;
    private int failReason;

    public RoamingStationSnResult() {}
    public RoamingStationSnResult(String stationSn, boolean success, int failReason) {
        this.stationSn = stationSn != null ? stationSn : "";
        this.success = success;
        this.failReason = failReason;
    }
    public String getStationSn() { return stationSn; }
    public void setStationSn(String stationSn) { this.stationSn = stationSn != null ? stationSn : ""; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getFailReason() { return failReason; }
    public void setFailReason(int failReason) { this.failReason = failReason; }
}
