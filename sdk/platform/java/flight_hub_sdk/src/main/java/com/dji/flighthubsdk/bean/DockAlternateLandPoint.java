// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场备降点。 */
public final class DockAlternateLandPoint {

    /** 备降点是否设置, {0:未设置, 1:已设置}; */
    private int isConfigured;
    /** 经度; */
    private double longitude;
    /** 纬度; */
    private double latitude;
    /** 安全高度(备降转移高); */
    private double safeLandHeight;
    /** 高度; */
    private double height;
    public DockAlternateLandPoint() {
    }

/**
     * 构造 DockAlternateLandPoint。
     *
     * @param isConfigured 备降点是否已设置（0:未设置, 1:已设置）
     * @param longitude 经度
     * @param latitude 纬度
     * @param safeLandHeight 安全高度（备降转移高）
     * @param height 高度
     */
    public DockAlternateLandPoint(
            int isConfigured,
            double longitude,
            double latitude,
            double safeLandHeight,
            double height) {
        this.isConfigured = isConfigured;
        this.longitude = longitude;
        this.latitude = latitude;
        this.safeLandHeight = safeLandHeight;
        this.height = height;
    }

    /** 备降点是否设置, {0:未设置, 1:已设置}; */
    public int getIsConfigured() {
        return isConfigured;
    }

    /** @param isConfigured 备降点是否设置, {0:未设置, 1:已设置}; */
    public void setIsConfigured(int isConfigured) {
        this.isConfigured = isConfigured;
    }

    /** 经度; */
    public double getLongitude() {
        return longitude;
    }

    /** @param longitude 经度; */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** 纬度; */
    public double getLatitude() {
        return latitude;
    }

    /** @param latitude 纬度; */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** 安全高度(备降转移高); */
    public double getSafeLandHeight() {
        return safeLandHeight;
    }

    /** @param safeLandHeight 安全高度(备降转移高); */
    public void setSafeLandHeight(double safeLandHeight) {
        this.safeLandHeight = safeLandHeight;
    }

    /** 高度; */
    public double getHeight() {
        return height;
    }

    /** @param height 高度; */
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
        DockAlternateLandPoint that = (DockAlternateLandPoint) o;
        return isConfigured == that.isConfigured
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.safeLandHeight, safeLandHeight) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isConfigured, longitude, latitude, safeLandHeight, height);
    }

    @Override
    public String toString() {
        return "DockAlternateLandPoint{isConfigured=" + isConfigured
                + ", longitude=" + longitude
                + ", latitude=" + latitude
                + ", safeLandHeight=" + safeLandHeight
                + ", height=" + height + '}';
    }
}
