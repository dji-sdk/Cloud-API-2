// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 手动 RTK 标定参数 */
public class ManulRtkCalibrationParams {
    /** 经度 */
    private double longitude;
    /** 纬度 */
    private double latitude;
    /** 高度，单位：米 */
    private double height;

    public ManulRtkCalibrationParams() {
    }


    /** 经度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 经度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /** 纬度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 纬度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** 高度，单位：米 */
    public double getHeight() { return height; }
    /** @param height 高度，单位：米 */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManulRtkCalibrationParams that = (ManulRtkCalibrationParams) o;
        return Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, height);
    }

    @Override
    public String toString() {
        return "ManulRtkCalibrationParams{longitude=" + longitude + ", latitude=" + latitude + ", height=" + height + '}';
    }
}
