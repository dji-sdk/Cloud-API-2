// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备日志上报 - 设备拓扑项参数 */
public class DeviceLogReportDeviceTopoItemParam {
    /** 设备型号 key */
    private String deviceModelKey = "";
    /** 设备 SN */
    private String sn = "";

    public DeviceLogReportDeviceTopoItemParam() {
    }


    /** 设备型号 key */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** @param deviceModelKey 设备型号 key */
    public void setDeviceModelKey(String deviceModelKey) { this.deviceModelKey = deviceModelKey != null ? deviceModelKey : ""; }
    /** 设备 SN */
    public String getSn() { return sn; }
    /** @param sn 设备 SN */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogReportDeviceTopoItemParam that = (DeviceLogReportDeviceTopoItemParam) o;
        return Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceModelKey, sn);
    }

    @Override
    public String toString() {
        return "DeviceLogReportDeviceTopoItemParam{deviceModelKey='" + deviceModelKey + '\'' + ", sn='" + sn + '\'' + '}';
    }
}
