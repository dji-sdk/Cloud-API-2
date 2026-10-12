// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 日志报告列表结果设备拓扑条目。 */
public final class LogReportListResultDeviceTopoItem {
    /** 设备 SN */
    private String sn;
    /** 设备型号 key */
    private String deviceModelKey;
    /** 设备组织呼号 */
    private String deviceOrganizationCallsign;
    public LogReportListResultDeviceTopoItem() {
    }

/**
     * 构造 LogReportListResultDeviceTopoItem。
     *
     * @param sn 设备序列号
     * @param deviceModelKey 设备型号标识
     * @param deviceOrganizationCallsign 设备组织呼号
     */
    public LogReportListResultDeviceTopoItem(String sn, String deviceModelKey, String deviceOrganizationCallsign) {
        this.sn = sn != null ? sn : "";
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.deviceOrganizationCallsign = deviceOrganizationCallsign != null ? deviceOrganizationCallsign : "";
    }

    /** 设备 SN */
    public String getSn() {
        return sn;
    }

    /** @param sn 设备 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 设备型号 key */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** @param deviceModelKey 设备型号 key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 设备组织呼号 */
    public String getDeviceOrganizationCallsign() {
        return deviceOrganizationCallsign;
    }

    /** 设备组织呼号 */
    public void setDeviceOrganizationCallsign(String deviceOrganizationCallsign) {
        this.deviceOrganizationCallsign = deviceOrganizationCallsign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultDeviceTopoItem that = (LogReportListResultDeviceTopoItem) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(deviceOrganizationCallsign, that.deviceOrganizationCallsign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, deviceModelKey, deviceOrganizationCallsign);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceTopoItem{sn='" + sn + '\'' + ", deviceModelKey='" + deviceModelKey + '\'' + ", deviceOrganizationCallsign='" + deviceOrganizationCallsign + '\'' + '}';
    }
}
