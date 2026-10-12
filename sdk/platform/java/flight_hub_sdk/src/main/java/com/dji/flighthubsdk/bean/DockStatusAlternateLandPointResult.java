// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 备降点 */
public final class DockStatusAlternateLandPointResult {

    private double height;
    private int configured;
    private double latitude;
    private double longitude;
    private double safeLandHeight;
    public DockStatusAlternateLandPointResult() {
    }

/**
     * 构造 DockStatusAlternateLandPointResult。
     *
     * @param height 高度
     * @param configured 是否已配置
     * @param latitude 纬度
     * @param longitude 经度
     * @param safeLandHeight 安全降落高度
     */
    public DockStatusAlternateLandPointResult(
            double height, int configured, double latitude, double longitude, double safeLandHeight) {
        this.height = height;
        this.configured = configured;
        this.latitude = latitude;
        this.longitude = longitude;
        this.safeLandHeight = safeLandHeight;
    }

    /** 高度 */
    public double getHeight() {
        return height;
    }

    /** 高度 */
    public void setHeight(double height) {
        this.height = height;
    }

        /** 是否已配置 */
        public int getConfigured() {
        return configured;
    }

        /** 是否已配置 */
        public void setConfigured(int configured) {
            this.configured = configured;
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

    /** 安全降落高度 */
    public double getSafeLandHeight() {
        return safeLandHeight;
    }

    /** 安全降落高度 */
    public void setSafeLandHeight(double safeLandHeight) {
        this.safeLandHeight = safeLandHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusAlternateLandPointResult that = (DockStatusAlternateLandPointResult) o;
        return Double.compare(that.height, height) == 0
                && configured == that.configured
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.safeLandHeight, safeLandHeight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                height,
                configured,
                latitude,
                longitude,
                safeLandHeight);
    }

    @Override
    public String toString() {
        return "DockStatusAlternateLandPointResult{"
                + "height=" + height
                + ", configured=" + configured
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", safeLandHeight=" + safeLandHeight
                + '}';
    }
}
