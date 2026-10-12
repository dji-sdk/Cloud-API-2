// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 返航高度回调结果 */
public final class FlightRthAltitudeResult {
    private int rthAltitude;
    private FlightSafeSyncResult deviceShadowSync;
    public FlightRthAltitudeResult() {
    }

/**
     * 构造 FlightRthAltitudeResult。
     *
     * @param rthAltitude 返航高度（米）
     * @param deviceShadowSync 设备影子同步结果
     */
    public FlightRthAltitudeResult(int rthAltitude, FlightSafeSyncResult deviceShadowSync) {
        this.rthAltitude = rthAltitude;
        this.deviceShadowSync = deviceShadowSync;
    }

    /** 返航高度（米） */
    public int getRthAltitude() { return rthAltitude; }
    /** 设备影子同步结果 */
    public FlightSafeSyncResult getDeviceShadowSync() { return deviceShadowSync; }

    /** 返航高度（米） */
    public void setRthAltitude(int rthAltitude) {
        this.rthAltitude = rthAltitude;
    }

    /** 设备影子同步结果 */
    public void setDeviceShadowSync(FlightSafeSyncResult deviceShadowSync) {
        this.deviceShadowSync = deviceShadowSync;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightRthAltitudeResult that = (FlightRthAltitudeResult) o;
        return rthAltitude == that.rthAltitude
                && Objects.equals(deviceShadowSync, that.deviceShadowSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rthAltitude, deviceShadowSync);
    }

    @Override
    public String toString() {
        return "FlightRthAltitudeResult{rthAltitude=" + rthAltitude + ", deviceShadowSync=" + deviceShadowSync + '}';
    }
}
