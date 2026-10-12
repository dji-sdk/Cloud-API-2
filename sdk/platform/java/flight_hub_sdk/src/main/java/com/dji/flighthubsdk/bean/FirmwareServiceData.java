// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 固件服务数据，包含参与升级的设备列表 */
public final class FirmwareServiceData {

    private List<FirmwareServiceDevice> devices;
    public FirmwareServiceData() {
    }

/**
     * 构造 FirmwareServiceData。
     *
     * @param devices 设备列表
     */
    public FirmwareServiceData(List<FirmwareServiceDevice> devices) {
        this.devices = devices != null ? devices : Collections.emptyList();
    }

    /** 设备列表 */
    public List<FirmwareServiceDevice> getDevices() {
        return devices;
    }

    /** 设备列表 */
    public void setDevices(List<FirmwareServiceDevice> devices) {
        this.devices = devices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareServiceData that = (FirmwareServiceData) o;
        return Objects.equals(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devices);
    }

    @Override
    public String toString() {
        return "FirmwareServiceData{devices=" + devices + '}';
    }
}
