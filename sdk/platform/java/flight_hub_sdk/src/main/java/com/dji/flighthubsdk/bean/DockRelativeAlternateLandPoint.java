// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 相对备降点。 */
public final class DockRelativeAlternateLandPoint {

    /** double; */
    private double longitude;
    /** double; */
    private double latitude;
    /** double,安全高度(备降转移高); */
    private double safeLandHeight;
    /** int, { 0: 正常, 1: 机场未设置相对备降点, 2: 机场未进入基站模式, 3: 机场朝向未收敛 }; */
    private int status;
    public DockRelativeAlternateLandPoint() {
    }

/**
     * 构造 DockRelativeAlternateLandPoint。
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @param safeLandHeight 安全降落高度
     * @param status 状态
     */
    public DockRelativeAlternateLandPoint(
            double longitude,
            double latitude,
            double safeLandHeight,
            int status) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.safeLandHeight = safeLandHeight;
        this.status = status;
    }

    /** double; */
    public double getLongitude() {
        return longitude;
    }

    /** @param longitude double; */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** double; */
    public double getLatitude() {
        return latitude;
    }

    /** @param latitude double; */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** double,安全高度(备降转移高); */
    public double getSafeLandHeight() {
        return safeLandHeight;
    }

    /** @param safeLandHeight double,安全高度(备降转移高); */
    public void setSafeLandHeight(double safeLandHeight) {
        this.safeLandHeight = safeLandHeight;
    }

    /** int, { 0: 正常, 1: 机场未设置相对备降点, 2: 机场未进入基站模式, 3: 机场朝向未收敛 }; */
    public int getStatus() {
        return status;
    }

    /** @param status int, { 0: 正常, 1: 机场未设置相对备降点, 2: 机场未进入基站模式, 3: 机场朝向未收敛 }; */
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockRelativeAlternateLandPoint that = (DockRelativeAlternateLandPoint) o;
        return Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.safeLandHeight, safeLandHeight) == 0
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, safeLandHeight, status);
    }

    @Override
    public String toString() {
        return "DockRelativeAlternateLandPoint{longitude=" + longitude
                + ", latitude=" + latitude
                + ", safeLandHeight=" + safeLandHeight
                + ", status=" + status + '}';
    }
}
