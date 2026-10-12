// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 固件升级请求参数，包含待升级设备列表 */
public class FirmwareUpgradeParam {
    private List<FirmwareUpgradeDeviceItem> devices = new ArrayList<>();

    public FirmwareUpgradeParam() {
    }


    /** 设备列表 */
    public List<FirmwareUpgradeDeviceItem> getDevices() {
        return devices;
    }

    /** @param devices 设备列表 */
    public void setDevices(List<FirmwareUpgradeDeviceItem> devices) {
        this.devices = devices != null ? devices : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareUpgradeParam that = (FirmwareUpgradeParam) o;
        return Objects.equals(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devices);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeParam{devices=" + devices + '}';
    }
}
