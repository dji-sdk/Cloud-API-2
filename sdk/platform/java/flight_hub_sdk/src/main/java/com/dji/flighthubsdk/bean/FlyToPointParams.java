// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 指点飞行参数 */
public class FlyToPointParams {
    /** 目标点纬度，单位：度 */
    private double latitude;
    /** 目标点经度，单位：度 */
    private double longitude;
    /** 目标点高度，单位：米 */
    private double height;
    /** 最大飞行速度，单位：m/s */
    private int maxSpeed;

    public FlyToPointParams() {
    }


    /** 目标点纬度，单位：度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 目标点纬度，单位：度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** 目标点经度，单位：度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 目标点经度，单位：度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /** 目标点高度，单位：米 */
    public double getHeight() { return height; }
    /** @param height 目标点高度，单位：米 */
    public void setHeight(double height) { this.height = height; }
    /** 最大飞行速度，单位：m/s */
    public int getMaxSpeed() { return maxSpeed; }
    /** @param maxSpeed 最大飞行速度，单位：m/s */
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlyToPointParams that = (FlyToPointParams) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0
                && maxSpeed == that.maxSpeed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, height, maxSpeed);
    }

    @Override
    public String toString() {
        return "FlyToPointParams{"
                + "latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + ", maxSpeed=" + maxSpeed
                + '}';
    }
}
