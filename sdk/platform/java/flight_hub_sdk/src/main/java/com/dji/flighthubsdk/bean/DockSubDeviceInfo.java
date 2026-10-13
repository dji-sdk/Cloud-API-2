// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场子设备条目。 */
public final class DockSubDeviceInfo {

    /** string, "子设备序列号(SN)"; */
    private String deviceSn;
    /** string,"子设备枚举值", {domain-type-subtype}; */
    private String deviceModelKey;
    /** int,"机场停机坪上的飞行器开机状态", { 0: 关机, 1: 开机 }; */
    private int deviceOnlineStatus;
    /** int,"机场停机坪上的飞行器是否与机场对频", { 0: 未对频, 1: 已对频 }; */
    private int devicePaired;
    public DockSubDeviceInfo() {
    }

/**
     * 构造 DockSubDeviceInfo。
     *
     * @param deviceSn 设备序列号
     * @param deviceModelKey 设备型号标识
     * @param deviceOnlineStatus 设备在线状态
     * @param devicePaired 设备配对状态
     */
    public DockSubDeviceInfo(
            String deviceSn,
            String deviceModelKey,
            int deviceOnlineStatus,
            int devicePaired) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.deviceOnlineStatus = deviceOnlineStatus;
        this.devicePaired = devicePaired;
    }

    /** string, "子设备序列号(SN)"; */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn string, "子设备序列号(SN)"; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** string,"子设备枚举值", {domain-type-subtype}; */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** @param deviceModelKey string,"子设备枚举值", {domain-type-subtype}; */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** int,"机场停机坪上的飞行器开机状态", { 0: 关机, 1: 开机 }; */
    public int getDeviceOnlineStatus() {
        return deviceOnlineStatus;
    }

    /** @param deviceOnlineStatus int,"机场停机坪上的飞行器开机状态", { 0: 关机, 1: 开机 }; */
    public void setDeviceOnlineStatus(int deviceOnlineStatus) {
        this.deviceOnlineStatus = deviceOnlineStatus;
    }

    /** int,"机场停机坪上的飞行器是否与机场对频", { 0: 未对频, 1: 已对频 }; */
    public int getDevicePaired() {
        return devicePaired;
    }

    /** @param devicePaired int,"机场停机坪上的飞行器是否与机场对频", { 0: 未对频, 1: 已对频 }; */
    public void setDevicePaired(int devicePaired) {
        this.devicePaired = devicePaired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockSubDeviceInfo that = (DockSubDeviceInfo) o;
        return deviceOnlineStatus == that.deviceOnlineStatus
                && devicePaired == that.devicePaired
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(deviceModelKey, that.deviceModelKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, deviceModelKey, deviceOnlineStatus, devicePaired);
    }

    @Override
    public String toString() {
        return "DockSubDeviceInfo{deviceSn='" + deviceSn + '\''
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", deviceOnlineStatus=" + deviceOnlineStatus
                + ", devicePaired=" + devicePaired + '}';
    }
}
