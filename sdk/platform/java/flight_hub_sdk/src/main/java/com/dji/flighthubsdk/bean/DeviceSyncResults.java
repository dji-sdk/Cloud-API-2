// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备同步结果。 */
public final class DeviceSyncResults {
    /** 待同步设备的SN,; */
    private String deviceSn;
    /** 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时; */
    private int code;
    public DeviceSyncResults() {
    }

/**
     * 构造 DeviceSyncResults。
     *
     * @param deviceSn 设备序列号
     * @param code 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时
     */
    public DeviceSyncResults(String deviceSn, int code) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.code = code;
    }

    /** 待同步设备的SN,; */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 待同步设备的SN,; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时; */
    public int getCode() {
        return code;
    }

    /** @param code 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时; */
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceSyncResults that = (DeviceSyncResults) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, code);
    }

    @Override
    public String toString() {
        return "DeviceSyncResults{deviceSn='" + deviceSn + '\'' + ", code=" + code + '}';
    }
}
