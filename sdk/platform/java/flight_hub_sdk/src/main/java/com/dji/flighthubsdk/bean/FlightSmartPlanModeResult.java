// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 智能规划模式回调结果 */
public final class FlightSmartPlanModeResult {
    private int smartPlanningMode;
    /** 设备影子同步结果 */
    private FlightSafeSyncResult deviceShadowSync;
    public FlightSmartPlanModeResult() {
    }

/**
     * 构造 FlightSmartPlanModeResult。
     *
     * @param smartPlanningMode 智能规划模式: 0=开启, 1=关闭
     * @param deviceShadowSync 设备影子同步结果
     */
    public FlightSmartPlanModeResult(int smartPlanningMode, FlightSafeSyncResult deviceShadowSync) {
        this.smartPlanningMode = smartPlanningMode;
        this.deviceShadowSync = deviceShadowSync;
    }

    /** 智能规划模式: 0=开启, 1=关闭 */
    public int getSmartPlanningMode() { return smartPlanningMode; }
    /** 设备影子同步结果 */
    public FlightSafeSyncResult getDeviceShadowSync() { return deviceShadowSync; }

    /** 智能规划模式: 0=开启, 1=关闭 */
    public void setSmartPlanningMode(int smartPlanningMode) {
        this.smartPlanningMode = smartPlanningMode;
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
        FlightSmartPlanModeResult that = (FlightSmartPlanModeResult) o;
        return smartPlanningMode == that.smartPlanningMode
                && Objects.equals(deviceShadowSync, that.deviceShadowSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(smartPlanningMode, deviceShadowSync);
    }

    @Override
    public String toString() {
        return "FlightSmartPlanModeResult{smartPlanningMode=" + smartPlanningMode + ", deviceShadowSync=" + deviceShadowSync + '}';
    }
}
