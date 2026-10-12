// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 智能跟踪点。 */
public final class SmartTrackPointInfo {

    /** 跟踪目标海拔高度; 单位：米; */
    private double trackAltitude;
    /** 目标纬度, [-90, 90]度; */
    private double trackLatitude;
    /** 目标经度, [-180, 180]度; */
    private double trackLongitude;
    /** track 模式, {1: 正常跟踪, 2: 正常跟踪，可信度低, 3: 目标是GPS点，或目标丢失但有预测}; */
    private int trackTargetMode;
    public SmartTrackPointInfo() {
    }

/**
     * 构造 SmartTrackPointInfo。
     *
     * @param trackAltitude 跟踪目标海拔高度
     * @param trackLatitude 目标纬度
     * @param trackLongitude 目标经度
     * @param trackTargetMode 跟踪模式，1:正常跟踪 2:正常跟踪可信度低 3:目标是GPS点或目标丢失但有预测
     */
    public SmartTrackPointInfo(double trackAltitude, double trackLatitude, double trackLongitude, int trackTargetMode) {
        this.trackAltitude = trackAltitude;
        this.trackLatitude = trackLatitude;
        this.trackLongitude = trackLongitude;
        this.trackTargetMode = trackTargetMode;
    }

    /** 跟踪目标海拔高度; 单位：米; */
    public double getTrackAltitude() { return trackAltitude; }
    /** 目标纬度, [-90, 90]度; */
    public double getTrackLatitude() { return trackLatitude; }
    /** 目标经度, [-180, 180]度; */
    public double getTrackLongitude() { return trackLongitude; }
    /** track 模式, {1: 正常跟踪, 2: 正常跟踪，可信度低, 3: 目标是GPS点，或目标丢失但有预测}; */
    public int getTrackTargetMode() { return trackTargetMode; }

    /** @param trackAltitude 跟踪目标海拔高度; 单位：米; */
    public void setTrackAltitude(double trackAltitude) {
        this.trackAltitude = trackAltitude;
    }

    /** @param trackLatitude 目标纬度, [-90, 90]度; */
    public void setTrackLatitude(double trackLatitude) {
        this.trackLatitude = trackLatitude;
    }

    /** @param trackLongitude 目标经度, [-180, 180]度; */
    public void setTrackLongitude(double trackLongitude) {
        this.trackLongitude = trackLongitude;
    }

    /** @param trackTargetMode track 模式, {1: 正常跟踪, 2: 正常跟踪，可信度低, 3: 目标是GPS点，或目标丢失但有预测}; */
    public void setTrackTargetMode(int trackTargetMode) {
        this.trackTargetMode = trackTargetMode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SmartTrackPointInfo that = (SmartTrackPointInfo) o;
        return Double.compare(that.trackAltitude, trackAltitude) == 0
                && Double.compare(that.trackLatitude, trackLatitude) == 0
                && Double.compare(that.trackLongitude, trackLongitude) == 0
                && trackTargetMode == that.trackTargetMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackAltitude, trackLatitude, trackLongitude, trackTargetMode);
    }

    @Override
    public String toString() {
        return "SmartTrackPointInfo{"
                + "trackAltitude=" + trackAltitude
                + ", trackLatitude=" + trackLatitude
                + ", trackLongitude=" + trackLongitude
                + ", trackTargetMode=" + trackTargetMode
                + '}';
    }
}
