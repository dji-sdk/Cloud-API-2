// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞行作业高度回调结果 */
public final class FlightCommanderHeightResult {
    private int commanderFlightHeight;
    private FlightSafeSyncResult deviceShadowSync;
    public FlightCommanderHeightResult() {
    }

/**
     * 构造 FlightCommanderHeightResult。
     *
     * @param commanderFlightHeight 飞行作业高度（米）
     * @param deviceShadowSync 设备影子同步结果
     */
    public FlightCommanderHeightResult(int commanderFlightHeight, FlightSafeSyncResult deviceShadowSync) {
        this.commanderFlightHeight = commanderFlightHeight;
        this.deviceShadowSync = deviceShadowSync;
    }

    /** 飞行作业高度（米） */
    public int getCommanderFlightHeight() { return commanderFlightHeight; }
    /** 设备影子同步结果 */
    public FlightSafeSyncResult getDeviceShadowSync() { return deviceShadowSync; }

    /** 飞行作业高度（米） */
    public void setCommanderFlightHeight(int commanderFlightHeight) {
        this.commanderFlightHeight = commanderFlightHeight;
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
        FlightCommanderHeightResult that = (FlightCommanderHeightResult) o;
        return commanderFlightHeight == that.commanderFlightHeight
                && Objects.equals(deviceShadowSync, that.deviceShadowSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commanderFlightHeight, deviceShadowSync);
    }

    @Override
    public String toString() {
        return "FlightCommanderHeightResult{commanderFlightHeight=" + commanderFlightHeight + ", deviceShadowSync=" + deviceShadowSync + '}';
    }
}
