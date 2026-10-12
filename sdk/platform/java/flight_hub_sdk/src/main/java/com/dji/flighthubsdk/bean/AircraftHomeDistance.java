// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机与 Home 点距离，单位见协议（通常为米）。
 */
public final class AircraftHomeDistance {

    /** Home点距离; */
    private double homeDistance;
    public AircraftHomeDistance() {
    }

/**
     * 构造 AircraftHomeDistance。
     *
     * @param homeDistance Home点距离
     */
    public AircraftHomeDistance(double homeDistance) {
        this.homeDistance = homeDistance;
    }

    /** Home点距离; */
    public double getHomeDistance() {
        return homeDistance;
    }

    /** @param homeDistance Home点距离; */
    public void setHomeDistance(double homeDistance) {
        this.homeDistance = homeDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftHomeDistance that = (AircraftHomeDistance) o;
        return Double.compare(that.homeDistance, homeDistance) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(homeDistance);
    }

    @Override
    public String toString() {
        return "AircraftHomeDistance{homeDistance=" + homeDistance + '}';
    }
}
