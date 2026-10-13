// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场搜星状态。 */
public final class DockPositionState {

    /** int, 是否标定, 0:未标定,1:已标定; 只有基站才有是否标定; */
    private int isCalibration;
    /** int, 是否收敛, 0:未开始, 1:收敛中, 2:收敛成功, 3: 收敛失败; */
    private int isFixed;
    /** int, 搜星档位, 1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档; */
    private int quality;
    /** int, GPS 搜星数量; */
    private int gpsNumber;
    /** int, RTK 搜星数量; */
    private int rtkNumber;
    public DockPositionState() {
    }

/**
     * 构造 DockPositionState。
     *
     * @param isCalibration 是否标定（0:未标定, 1:已标定）
     * @param isFixed 是否收敛（0:未开始, 1:收敛中, 2:成功, 3:失败）
     * @param quality 搜星档位（1~5档）
     * @param gpsNumber GPS 搜星数量
     * @param rtkNumber RTK 搜星数量
     */
    public DockPositionState(
            int isCalibration,
            int isFixed,
            int quality,
            int gpsNumber,
            int rtkNumber) {
        this.isCalibration = isCalibration;
        this.isFixed = isFixed;
        this.quality = quality;
        this.gpsNumber = gpsNumber;
        this.rtkNumber = rtkNumber;
    }

    /** int, 是否标定, 0:未标定,1:已标定; 只有基站才有是否标定; */
    public int getIsCalibration() {
        return isCalibration;
    }

    /** @param isCalibration int, 是否标定, 0:未标定,1:已标定; 只有基站才有是否标定; */
    public void setIsCalibration(int isCalibration) {
        this.isCalibration = isCalibration;
    }

    /** int, 是否收敛, 0:未开始, 1:收敛中, 2:收敛成功, 3: 收敛失败; */
    public int getIsFixed() {
        return isFixed;
    }

    /** @param isFixed int, 是否收敛, 0:未开始, 1:收敛中, 2:收敛成功, 3: 收敛失败; */
    public void setIsFixed(int isFixed) {
        this.isFixed = isFixed;
    }

    /** int, 搜星档位, 1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档; */
    public int getQuality() {
        return quality;
    }

    /** @param quality int, 搜星档位, 1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档; */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    /** int, GPS 搜星数量; */
    public int getGpsNumber() {
        return gpsNumber;
    }

    /** @param gpsNumber int, GPS 搜星数量; */
    public void setGpsNumber(int gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

    /** int, RTK 搜星数量; */
    public int getRtkNumber() {
        return rtkNumber;
    }

    /** @param rtkNumber int, RTK 搜星数量; */
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
        DockPositionState that = (DockPositionState) o;
        return isCalibration == that.isCalibration
                && isFixed == that.isFixed
                && quality == that.quality
                && gpsNumber == that.gpsNumber
                && rtkNumber == that.rtkNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCalibration, isFixed, quality, gpsNumber, rtkNumber);
    }

    @Override
    public String toString() {
        return "DockPositionState{isCalibration=" + isCalibration
                + ", isFixed=" + isFixed
                + ", quality=" + quality
                + ", gpsNumber=" + gpsNumber
                + ", rtkNumber=" + rtkNumber + '}';
    }
}
