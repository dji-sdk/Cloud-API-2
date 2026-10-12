// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 夜航灯状态。 */
public final class AircraftNightLightsState {

    /** 飞行器夜航灯状态, {0:关闭, 1:打开}; */
    private int nightLightsState;
    public AircraftNightLightsState() {
    }

/**
     * 构造 AircraftNightLightsState。
     *
     * @param nightLightsState 夜航灯状态
     */
    public AircraftNightLightsState(int nightLightsState) {
        this.nightLightsState = nightLightsState;
    }

    /** 飞行器夜航灯状态, {0:关闭, 1:打开}; */
    public int getNightLightsState() {
        return nightLightsState;
    }

    /** @param nightLightsState 飞行器夜航灯状态, {0:关闭, 1:打开}; */
    public void setNightLightsState(int nightLightsState) {
        this.nightLightsState = nightLightsState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftNightLightsState that = (AircraftNightLightsState) o;
        return nightLightsState == that.nightLightsState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nightLightsState);
    }

    @Override
    public String toString() {
        return "AircraftNightLightsState{nightLightsState=" + nightLightsState + '}';
    }
}
