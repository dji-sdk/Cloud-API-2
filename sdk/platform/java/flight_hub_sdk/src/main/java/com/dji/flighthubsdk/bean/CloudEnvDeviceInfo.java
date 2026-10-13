// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设备信息（呼号/型号/SN）。 */
public class CloudEnvDeviceInfo {
    private String callsign = "";
    private String deviceModelKey = "";
    private String deviceSn = "";

    public CloudEnvDeviceInfo() {
    }

    /**
     * @param callsign 设备呼号
     * @param deviceModelKey 设备型号 key
     * @param deviceSn 设备经纬 SN
     */
    public CloudEnvDeviceInfo(String callsign, String deviceModelKey, String deviceSn) {
        this.callsign = callsign != null ? callsign : "";
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 设备呼号 */
    public String getCallsign() {
        return callsign;
    }

    /** 设备呼号 */
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    /** 设备型号 key */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** 设备型号 key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 设备经纬 SN */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备经纬 SN */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvDeviceInfo that = (CloudEnvDeviceInfo) o;
        return Objects.equals(callsign, that.callsign)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(deviceSn, that.deviceSn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callsign, deviceModelKey, deviceSn);
    }

    @Override
    public String toString() {
        return "CloudEnvDeviceInfo{callsign='" + callsign + '\'' + ", deviceModelKey='" + deviceModelKey + '\'' + ", deviceSn='" + deviceSn + '\'' + '}';
    }
}
