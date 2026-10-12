// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 返航模式与高度。 */
public final class AircraftRthInfo {

    /** int, 返航高度模式设置值, {0: "智能高度", 1: "设定高度"}; */
    private int rthMode;
    /** int, 返航高度(单位: m), 参考值: [20, 1500]; */
    private int rthAltitude;
    public AircraftRthInfo() {
    }

/**
     * 构造 AircraftRthInfo。
     *
     * @param rthMode 返航模式
     * @param rthAltitude 返航高度
     */
    public AircraftRthInfo(int rthMode, int rthAltitude) {
        this.rthMode = rthMode;
        this.rthAltitude = rthAltitude;
    }

    /** int, 返航高度模式设置值, {0: "智能高度", 1: "设定高度"}; */
    public int getRthMode() {
        return rthMode;
    }

    /** @param rthMode int, 返航高度模式设置值, {0: "智能高度", 1: "设定高度"}; */
    public void setRthMode(int rthMode) {
        this.rthMode = rthMode;
    }

    /** int, 返航高度(单位: m), 参考值: [20, 1500]; */
    public int getRthAltitude() {
        return rthAltitude;
    }

    /** @param rthAltitude int, 返航高度(单位: m), 参考值: [20, 1500]; */
    public void setRthAltitude(int rthAltitude) {
        this.rthAltitude = rthAltitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftRthInfo that = (AircraftRthInfo) o;
        return rthMode == that.rthMode && rthAltitude == that.rthAltitude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rthMode, rthAltitude);
    }

    @Override
    public String toString() {
        return "AircraftRthInfo{rthMode=" + rthMode + ", rthAltitude=" + rthAltitude + '}';
    }
}
