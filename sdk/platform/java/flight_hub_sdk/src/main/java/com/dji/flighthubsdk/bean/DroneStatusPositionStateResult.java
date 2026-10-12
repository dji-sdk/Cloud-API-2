// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞机设备状态 — 定位状态 */
public final class DroneStatusPositionStateResult {

    private int gpsNumber;
    private int fixed;
    private int quality;
    private int rtkNumber;
    public DroneStatusPositionStateResult() {
    }

/**
     * 构造 DroneStatusPositionStateResult。
     *
     * @param gpsNumber GPS 卫星数
     * @param fixed 已修复
     * @param quality 质量
     * @param rtkNumber RTK 卫星数
     */
    public DroneStatusPositionStateResult(int gpsNumber, int fixed, int quality, int rtkNumber) {
        this.gpsNumber = gpsNumber;
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
        DroneStatusPositionStateResult that = (DroneStatusPositionStateResult) o;
        return gpsNumber == that.gpsNumber
                && fixed == that.fixed
                && quality == that.quality
                && rtkNumber == that.rtkNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gpsNumber, fixed, quality, rtkNumber);
    }

    @Override
    public String toString() {
        return "DroneStatusPositionStateResult{"
                + "gpsNumber=" + gpsNumber
                + ", fixed=" + fixed
                + ", quality=" + quality
                + ", rtkNumber=" + rtkNumber
                + '}';
    }
}
