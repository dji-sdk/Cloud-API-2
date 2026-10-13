// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 定位状态 */
public final class DockStatusPositionStateResult {

    private int gpsNumber;
    private int calibration;
    private int fixed;
    private int quality;
    private int rtkNumber;
    public DockStatusPositionStateResult() {
    }

/**
     * 构造 DockStatusPositionStateResult。
     *
     * @param gpsNumber GPS 卫星数
     * @param calibration 标定
     * @param fixed 已修复
     * @param quality 质量
     * @param rtkNumber RTK 卫星数
     */
    public DockStatusPositionStateResult(
            int gpsNumber, int calibration, int fixed, int quality, int rtkNumber) {
        this.gpsNumber = gpsNumber;
        this.calibration = calibration;
        this.fixed = fixed;
        this.quality = quality;
        this.rtkNumber = rtkNumber;
    }

    /** GPS 卫星数 */
    public int getGpsNumber() {
        return gpsNumber;
    }

    /** GPS 卫星数 */
    public void setGpsNumber(int gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

        /** 标定 */
        public int getCalibration() {
        return calibration;
    }

        /** 标定 */
        public void setCalibration(int calibration) {
            this.calibration = calibration;
        }

        /** 已修复 */
        public int getFixed() {
        return fixed;
    }

        /** 已修复 */
        public void setFixed(int fixed) {
            this.fixed = fixed;
        }

    /** 质量 */
    public int getQuality() {
        return quality;
    }

    /** 质量 */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    /** RTK 卫星数 */
    public int getRtkNumber() {
        return rtkNumber;
    }

    /** RTK 卫星数 */
    public void setRtkNumber(int rtkNumber) {
        this.rtkNumber = rtkNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusPositionStateResult that = (DockStatusPositionStateResult) o;
        return gpsNumber == that.gpsNumber
                && calibration == that.calibration
                && fixed == that.fixed
                && quality == that.quality
                && rtkNumber == that.rtkNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                gpsNumber,
                calibration,
                fixed,
                quality,
                rtkNumber);
    }

    @Override
    public String toString() {
        return "DockStatusPositionStateResult{"
                + "gpsNumber=" + gpsNumber
                + ", calibration=" + calibration
                + ", fixed=" + fixed
                + ", quality=" + quality
                + ", rtkNumber=" + rtkNumber
                + '}';
    }
}
