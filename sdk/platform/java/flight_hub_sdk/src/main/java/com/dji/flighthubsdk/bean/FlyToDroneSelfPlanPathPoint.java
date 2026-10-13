// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 飞行器自主规划路径点 */
public final class FlyToDroneSelfPlanPathPoint {

    /** 纬度，单位：度 */
    private double latitude;
    /** 经度，单位：度 */
    private double longitude;
    /** 高度，单位：米 */
    private double height;
    public FlyToDroneSelfPlanPathPoint() {
    }

/**
     * 构造 FlyToDroneSelfPlanPathPoint。
     *
     * @param latitude 纬度，单位：度
     * @param longitude 经度，单位：度
     * @param height 高度，单位：米
     */
    public FlyToDroneSelfPlanPathPoint(double latitude, double longitude, double height) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
    }

    /** 纬度，单位：度 */
    public double getLatitude() {
        return latitude;
    }

    /** 纬度，单位：度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** 经度，单位：度 */
    public double getLongitude() {
        return longitude;
    }

    /** 经度，单位：度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** 高度，单位：米 */
    public double getHeight() {
        return height;
    }

    /** 高度，单位：米 */
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
        FlyToDroneSelfPlanPathPoint that = (FlyToDroneSelfPlanPathPoint) o;
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
        return "FlyToDroneSelfPlanPathPoint{latitude=" + latitude + ", longitude=" + longitude + ", height=" + height + '}';
    }
}
