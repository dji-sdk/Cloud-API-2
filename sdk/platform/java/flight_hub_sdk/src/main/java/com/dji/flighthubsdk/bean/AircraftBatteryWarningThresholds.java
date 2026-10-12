// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 电量告警阈值（用户设置）。 */
public final class AircraftBatteryWarningThresholds {

    /** int, 用户设置的电池低电量告警百分比, [0, 100]; */
    private int lowBatteryWarningThreshold;
    /** int, 用户设置的电池严重低电量告警百分比, [0,100]; */
    private int seriousLowBatteryWarningThreshold;
    public AircraftBatteryWarningThresholds() {
    }

/**
     * 构造 AircraftBatteryWarningThresholds。
     *
     * @param lowBatteryWarningThreshold 低电量告警阈值百分比
     * @param seriousLowBatteryWarningThreshold 严重低电量告警阈值百分比
     */
    public AircraftBatteryWarningThresholds(int lowBatteryWarningThreshold, int seriousLowBatteryWarningThreshold) {
        this.lowBatteryWarningThreshold = lowBatteryWarningThreshold;
        this.seriousLowBatteryWarningThreshold = seriousLowBatteryWarningThreshold;
    }

    /** int, 用户设置的电池低电量告警百分比, [0, 100]; */
    public int getLowBatteryWarningThreshold() {
        return lowBatteryWarningThreshold;
    }

    /** @param lowBatteryWarningThreshold int, 用户设置的电池低电量告警百分比, [0, 100]; */
    public void setLowBatteryWarningThreshold(int lowBatteryWarningThreshold) {
        this.lowBatteryWarningThreshold = lowBatteryWarningThreshold;
    }

    /** int, 用户设置的电池严重低电量告警百分比, [0,100]; */
    public int getSeriousLowBatteryWarningThreshold() {
        return seriousLowBatteryWarningThreshold;
    }

    /** @param seriousLowBatteryWarningThreshold int, 用户设置的电池严重低电量告警百分比, [0,100]; */
    public void setSeriousLowBatteryWarningThreshold(int seriousLowBatteryWarningThreshold) {
        this.seriousLowBatteryWarningThreshold = seriousLowBatteryWarningThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftBatteryWarningThresholds that = (AircraftBatteryWarningThresholds) o;
        return lowBatteryWarningThreshold == that.lowBatteryWarningThreshold
                && seriousLowBatteryWarningThreshold == that.seriousLowBatteryWarningThreshold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowBatteryWarningThreshold, seriousLowBatteryWarningThreshold);
    }

    @Override
    public String toString() {
        return "AircraftBatteryWarningThresholds{low=" + lowBatteryWarningThreshold
                + ", seriousLow=" + seriousLowBatteryWarningThreshold + '}';
    }
}
