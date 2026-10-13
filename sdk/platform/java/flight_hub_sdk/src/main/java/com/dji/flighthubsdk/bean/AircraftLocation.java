// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机位置。
 */
public final class AircraftLocation {

    /** 椭球高度 */
    private double height;
    /** 相对起飞点高度 */
    private double elevation;
    private double longitude;
    private double latitude;
    public AircraftLocation() {
    }

/**
     * 构造 AircraftLocation。
     *
     * @param height 椭球高度
     * @param elevation 相对起飞点高度
     * @param longitude 经度
     * @param latitude 纬度
     */
    public AircraftLocation(double height, double elevation, double longitude, double latitude) {
        this.height = height;
        this.elevation = elevation;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /** 椭球高度 */
    public double getHeight() {
        return height;
    }

    /** 椭球高度 */
    public void setHeight(double height) {
        this.height = height;
    }

    /** 相对起飞点高度 */
    public double getElevation() {
        return elevation;
    }

    /** 相对起飞点高度 */
    public void setElevation(double elevation) {
        this.elevation = elevation;
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
        AircraftLocation that = (AircraftLocation) o;
        return Double.compare(that.height, height) == 0
                && Double.compare(that.elevation, elevation) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, elevation, longitude, latitude);
    }

    @Override
    public String toString() {
        return "AircraftLocation{height=" + height + ", elevation=" + elevation
                + ", longitude=" + longitude + ", latitude=" + latitude + '}';
    }
}
