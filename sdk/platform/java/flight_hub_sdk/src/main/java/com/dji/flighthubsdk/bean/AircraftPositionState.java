// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机搜星 / 定位状态。
 */
public final class AircraftPositionState {

    /** 是否收敛,{0: 非开始, 1: 收敛中, 2: 收敛成功, 3: 收敛失败}; */
    private int isFixed;
    /** 搜星档位,{1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档}; */
    private int quality;
    /** gps搜星数量; */
    private int gpsNumber;
    /** rtk搜星数量; */
    private int rtkNumber;
    public AircraftPositionState() {
    }

/**
     * 构造 AircraftPositionState。
     *
     * @param isFixed 是否收敛等状态
     * @param quality 搜星档位等
     * @param gpsNumber GPS 搜星数量
     * @param rtkNumber RTK 搜星数量
     */
    public AircraftPositionState(int isFixed, int quality, int gpsNumber, int rtkNumber) {
        this.isFixed = isFixed;
        this.quality = quality;
        this.gpsNumber = gpsNumber;
        this.rtkNumber = rtkNumber;
    }

    /** 是否收敛,{0: 非开始, 1: 收敛中, 2: 收敛成功, 3: 收敛失败}; */
    public int getIsFixed() {
        return isFixed;
    }

    /** @param isFixed 是否收敛,{0: 非开始, 1: 收敛中, 2: 收敛成功, 3: 收敛失败}; */
    public void setIsFixed(int isFixed) {
        this.isFixed = isFixed;
    }

    /** 搜星档位,{1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档}; */
    public int getQuality() {
        return quality;
    }

    /** @param quality 搜星档位,{1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档}; */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    /** gps搜星数量; */
    public int getGpsNumber() {
        return gpsNumber;
    }

    /** @param gpsNumber gps搜星数量; */
    public void setGpsNumber(int gpsNumber) {
        this.gpsNumber = gpsNumber;
    }

    /** rtk搜星数量; */
    public int getRtkNumber() {
        return rtkNumber;
    }

    /** @param rtkNumber rtk搜星数量; */
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
        AircraftPositionState that = (AircraftPositionState) o;
        return isFixed == that.isFixed
                && quality == that.quality
                && gpsNumber == that.gpsNumber
                && rtkNumber == that.rtkNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFixed, quality, gpsNumber, rtkNumber);
    }

    @Override
    public String toString() {
        return "AircraftPositionState{isFixed=" + isFixed + ", quality=" + quality
                + ", gpsNumber=" + gpsNumber + ", rtkNumber=" + rtkNumber + '}';
    }
}
