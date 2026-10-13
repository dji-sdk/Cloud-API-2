// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 修改设备名称项（设备 SN + 设备呼号） */
public class ModifyDeviceNameItem {
    private String deviceSn = "";
    private String deviceCallsign = "";

    public ModifyDeviceNameItem() {
    }


    /** 设备序列号 */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 设备呼号 */
    public String getDeviceCallsign() {
        return deviceCallsign;
    }

    /** @param deviceCallsign 设备呼号 */
    public void setDeviceCallsign(String deviceCallsign) {
        this.deviceCallsign = deviceCallsign != null ? deviceCallsign : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModifyDeviceNameItem that = (ModifyDeviceNameItem) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(deviceCallsign, that.deviceCallsign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, deviceCallsign);
    }

    @Override
    public String toString() {
        return "ModifyDeviceNameItem{deviceSn='" + deviceSn + '\'' + ", deviceCallsign='" + deviceCallsign + '\'' + '}';
    }
}
