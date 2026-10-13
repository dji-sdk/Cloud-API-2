// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 飞机速度。 */
public final class AircraftSpeed {

    /** 水平速度 m/s */
    private double horizontalSpeed;
    /** 垂直速度 m/s */
    private double verticalSpeed;
    public AircraftSpeed() {
    }

/**
     * 构造 AircraftSpeed。
     *
     * @param horizontalSpeed 水平速度(m/s)
     * @param verticalSpeed 垂直速度(m/s)
     */
    public AircraftSpeed(double horizontalSpeed, double verticalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    /** 水平速度 m/s */
    public double getHorizontalSpeed() {
        return horizontalSpeed;
    }

    /** @param horizontalSpeed 水平速度 m/s */
    public void setHorizontalSpeed(double horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
    }

    /** 垂直速度 m/s */
    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    /** @param verticalSpeed 垂直速度 m/s */
    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftSpeed that = (AircraftSpeed) o;
        return Double.compare(that.horizontalSpeed, horizontalSpeed) == 0
                && Double.compare(that.verticalSpeed, verticalSpeed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(horizontalSpeed, verticalSpeed);
    }

    @Override
    public String toString() {
        return "AircraftSpeed{horizontalSpeed=" + horizontalSpeed + ", verticalSpeed=" + verticalSpeed + '}';
    }
}
