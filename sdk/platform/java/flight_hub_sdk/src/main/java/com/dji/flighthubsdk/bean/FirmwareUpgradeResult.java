// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 发起固件升级结果 */
public final class FirmwareUpgradeResult {

    /** 业务 ID */
    private String bid;
    /** 历史字段：后端不返回，恒为 0 */
    private int deviceCode;
    /** 升级状态 */
    private String status;
    /** 历史字段：后端不返回，恒为空 */
    private List<FirmwareUpgradeResponseDeviceItemResult> devices;
    public FirmwareUpgradeResult() {
    }

/**
     * 构造 FirmwareUpgradeResult（兼容旧调用方）。
     *
     * @param deviceCode 历史字段：设备编码
     * @param status 升级状态
     * @param devices 历史字段：设备列表
     */
    public FirmwareUpgradeResult(
            int deviceCode, String status, List<FirmwareUpgradeResponseDeviceItemResult> devices) {
        this("", deviceCode, status, devices);
    }

/**
     * 构造 FirmwareUpgradeResult。
     *
     * @param bid 业务 ID
     * @param deviceCode 历史字段：设备编码
     * @param status 升级状态
     * @param devices 历史字段：设备列表
     */
    public FirmwareUpgradeResult(
            String bid, int deviceCode, String status, List<FirmwareUpgradeResponseDeviceItemResult> devices) {
        this.bid = bid != null ? bid : "";
        this.deviceCode = deviceCode;
        this.status = status != null ? status : "";
        this.devices = devices != null ? devices : Collections.emptyList();
    }

    /** 业务 ID */
    public String getBid() {
        return bid;
    }

    /** 业务 ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 历史字段：后端不返回，恒为 0 */
    public int getDeviceCode() {
        return deviceCode;
    }

    /** 历史字段：后端不返回，恒为 0 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 升级状态 */
    public String getStatus() {
        return status;
    }

    /** 升级状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 历史字段：后端不返回，恒为空 */
    public List<FirmwareUpgradeResponseDeviceItemResult> getDevices() {
        return devices;
    }

    /** 历史字段：后端不返回，恒为空 */
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
        return Objects.equals(bid, that.bid)
                && deviceCode == that.deviceCode
                && Objects.equals(status, that.status)
                && Objects.equals(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, deviceCode, status, devices);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeResult{bid='" + bid + '\''
                + ", deviceCode=" + deviceCode
                + ", status='" + status + '\''
                + ", devices=" + devices + '}';
    }
}
