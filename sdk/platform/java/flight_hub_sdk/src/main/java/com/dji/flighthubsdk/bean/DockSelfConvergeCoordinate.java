// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 自收敛坐标。 */
public final class DockSelfConvergeCoordinate {

    private double height;
    private double longitude;
    private double latitude;
    public DockSelfConvergeCoordinate() {
    }

/**
     * 构造 DockSelfConvergeCoordinate。
     *
     * @param height 高度
     * @param longitude 经度
     * @param latitude 纬度
     */
    public DockSelfConvergeCoordinate(double height, double longitude, double latitude) {
        this.height = height;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /** 高度 */
    public double getHeight() {
        return height;
    }

    /** 高度 */
    public void setHeight(double height) {
        this.height = height;
    }

    /** 经度 */
    public double getLongitude() {
        return longitude;
    }

    /** 经度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** 纬度 */
    public double getLatitude() {
        return latitude;
    }

    /** 纬度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockSelfConvergeCoordinate that = (DockSelfConvergeCoordinate) o;
        return Double.compare(that.height, height) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, longitude, latitude);
    }

    @Override
    public String toString() {
        return "DockSelfConvergeCoordinate{height=" + height
                + ", longitude=" + longitude
                + ", latitude=" + latitude + '}';
    }
}
