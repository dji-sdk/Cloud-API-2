// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 发起固件升级结果 */
public final class FirmwareUpgradeResult {

    private int deviceCode;
    private String status;
    private List<FirmwareUpgradeResponseDeviceItemResult> devices;
    public FirmwareUpgradeResult() {
    }

/**
     * 构造 FirmwareUpgradeResult。
     *
     * @param deviceCode 设备编码
     * @param status 状态
     * @param devices 设备列表
     */
    public FirmwareUpgradeResult(
            int deviceCode, String status, List<FirmwareUpgradeResponseDeviceItemResult> devices) {
        this.deviceCode = deviceCode;
        this.status = status != null ? status : "";
        this.devices = devices != null ? devices : Collections.emptyList();
    }

    /** 设备编码 */
    public int getDeviceCode() {
        return deviceCode;
    }

    /** 设备编码 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 状态 */
    public String getStatus() {
        return status;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 设备列表 */
    public List<FirmwareUpgradeResponseDeviceItemResult> getDevices() {
        return devices;
    }

    /** 设备列表 */
    public void setDevices(List<FirmwareUpgradeResponseDeviceItemResult> devices) {
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
        FirmwareUpgradeResult that = (FirmwareUpgradeResult) o;
        return deviceCode == that.deviceCode
                && Objects.equals(status, that.status)
                && Objects.equals(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceCode, status, devices);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeResult{deviceCode=" + deviceCode + ", status='" + status + '\'' + ", devices=" + devices + '}';
    }
}
