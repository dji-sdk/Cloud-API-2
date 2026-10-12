// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁 - 多边形顶点坐标（经纬度） */
public final class DeviceUnlockPolygonPoint {

    private double latitude;
    private double longitude;
    public DeviceUnlockPolygonPoint() {
    }

/**
     * 构造 DeviceUnlockPolygonPoint。
     *
     * @param latitude 纬度
     * @param longitude 经度
     */
    public DeviceUnlockPolygonPoint(double latitude, double longitude) {
        this.latitude = latitude;
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

    /** 经度 */
    public double getLongitude() {
        return longitude;
    }

    /** 经度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockPolygonPoint that = (DeviceUnlockPolygonPoint) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "DeviceUnlockPolygonPoint{latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
