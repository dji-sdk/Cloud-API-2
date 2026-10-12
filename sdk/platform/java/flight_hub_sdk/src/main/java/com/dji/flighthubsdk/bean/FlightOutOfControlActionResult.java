// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 失控动作回调结果 */
public final class FlightOutOfControlActionResult {
    private String outOfControlAction;
    private FlightSafeSyncResult deviceShadowSync;
    public FlightOutOfControlActionResult() {
    }

/**
     * 构造 FlightOutOfControlActionResult。
     *
     * @param outOfControlAction 失控动作: "Hover" / "ReturnHome" / "Continue"
     * @param deviceShadowSync 设备影子同步结果
     */
    public FlightOutOfControlActionResult(String outOfControlAction, FlightSafeSyncResult deviceShadowSync) {
        this.outOfControlAction = outOfControlAction != null ? outOfControlAction : "";
        this.deviceShadowSync = deviceShadowSync;
    }

    /** 失控动作: "Hover" / "ReturnHome" / "Continue" */
    public String getOutOfControlAction() { return outOfControlAction; }
    /** 设备影子同步结果 */
    public FlightSafeSyncResult getDeviceShadowSync() { return deviceShadowSync; }

    /** 失控动作: "Hover" / "ReturnHome" / "Continue" */
    public void setOutOfControlAction(String outOfControlAction) {
        this.outOfControlAction = outOfControlAction;
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
        FlightOutOfControlActionResult that = (FlightOutOfControlActionResult) o;
        return Objects.equals(outOfControlAction, that.outOfControlAction)
                && Objects.equals(deviceShadowSync, that.deviceShadowSync);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outOfControlAction, deviceShadowSync);
    }

    @Override
    public String toString() {
        return "FlightOutOfControlActionResult{outOfControlAction='" + outOfControlAction + '\'' + ", deviceShadowSync=" + deviceShadowSync + '}';
    }
}
