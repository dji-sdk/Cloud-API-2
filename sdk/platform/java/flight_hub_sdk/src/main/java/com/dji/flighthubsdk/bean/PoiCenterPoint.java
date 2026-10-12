// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** POI中心点。 */
public final class PoiCenterPoint {
    /** 纬度 */
    private double latitude;
    /** 经度 */
    private double longitude;
    /** 高度（椭球高，米） */
    private double height;
    public PoiCenterPoint() {
    }

/**
     * 构造 PoiCenterPoint。
     *
     * @param latitude 纬度，单位 1/2^32*180度
     * @param longitude 经度，单位 1/2^32*360度
     * @param height 高度
     */
    public PoiCenterPoint(double latitude, double longitude, double height) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
    }

    /** 纬度 */
    public double getLatitude() { return latitude; }
    /** 经度 */
    public double getLongitude() { return longitude; }
    /** 高度（椭球高，米） */
    public double getHeight() { return height; }

    /** @param latitude 纬度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** @param longitude 经度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** @param height 高度（椭球高，米） */
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PoiCenterPoint that = (PoiCenterPoint) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, height);
    }

    @Override
    public String toString() {
        return "PoiCenterPoint{latitude=" + latitude + ", longitude=" + longitude + ", height=" + height + '}';
    }
}
