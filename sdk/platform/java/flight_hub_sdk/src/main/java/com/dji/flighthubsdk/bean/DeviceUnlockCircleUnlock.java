// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁 - 圆形区域解锁（半径、经纬度、高度） */
public final class DeviceUnlockCircleUnlock {

    private double radius;
    private double latitude;
    private double longitude;
    private double height;
    public DeviceUnlockCircleUnlock() {
    }

/**
     * 构造 DeviceUnlockCircleUnlock。
     *
     * @param radius 半径
     * @param latitude 纬度
     * @param longitude 经度
     * @param height 高度
     */
    public DeviceUnlockCircleUnlock(double radius, double latitude, double longitude, double height) {
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
    }

    /** 半径 */
    public double getRadius() {
        return radius;
    }

    /** 半径 */
    public void setRadius(double radius) {
        this.radius = radius;
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

    /** 高度 */
    public double getHeight() {
        return height;
    }

    /** 高度 */
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
        DeviceUnlockCircleUnlock that = (DeviceUnlockCircleUnlock) o;
        return Double.compare(that.radius, radius) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(radius, latitude, longitude, height);
    }

    @Override
    public String toString() {
        return "DeviceUnlockCircleUnlock{"
                + "radius=" + radius
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + '}';
    }
}
