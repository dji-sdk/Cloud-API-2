// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 风速风向。
 */
public final class AircraftWindSpeed {

    /** 当前风向 "enum_desc": "1:正北  2：东北  3:东  4:东南  5:南  6:西南  7:西   8:西北" */
    private int windDirection;
    /** 风速估计，该风速是通过飞机姿态推测出的，有一定的误差，仅供参考，不能作为气象数据使用 */
    private double windSpeed;
    public AircraftWindSpeed() {
    }

/**
     * 构造 AircraftWindSpeed。
     *
     * @param windDirection 当前风向（1:正北, 2:东北, 3:东, 4:东南, 5:南, 6:西南, 7:西, 8:西北）
     * @param windSpeed 风速估计值（通过飞机姿态推测，仅供参考）
     */
    public AircraftWindSpeed(int windDirection, double windSpeed) {
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
    }

    /** 当前风向 "enum_desc": "1:正北  2：东北  3:东  4:东南  5:南  6:西南  7:西   8:西北" */
    public int getWindDirection() {
        return windDirection;
    }

    /** @param windDirection 当前风向 "enum_desc": "1:正北  2：东北  3:东  4:东南  5:南  6:西南  7:西   8:西北" */
    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    /** 风速估计(参考上云 API, 单位: 0.1 米/秒, 0.1 m/s), 该风速是通过飞机姿态推测出的, 有一定的误差, 仅供参考, 不能作为气象数据使用 */
    public double getWindSpeed() {
        return windSpeed;
    }

    /** @param windSpeed 风速估计，该风速是通过飞机姿态推测出的，有一定的误差，仅供参考，不能作为气象数据使用 */
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftWindSpeed that = (AircraftWindSpeed) o;
        return windDirection == that.windDirection
                && Double.compare(that.windSpeed, windSpeed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(windDirection, windSpeed);
    }

    @Override
    public String toString() {
        return "AircraftWindSpeed{windDirection=" + windDirection + ", windSpeed=" + windSpeed + '}';
    }
}
