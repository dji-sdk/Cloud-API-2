// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 轨迹点 */
public final class FlightTrackPoint {
    /** 时间戳（秒级） */
    private long timestamp;
    /** 纬度 */
    private double latitude;
    /** 经度 */
    private double longitude;
    /** 高度（米） */
    private double height;

    public FlightTrackPoint() {
    }

    /**
     * 构造 FlightTrackPoint。
     *
     * @param timestamp 时间戳（秒级）
     * @param latitude 纬度
     * @param longitude 经度
     * @param height 高度（米）
     */
    public FlightTrackPoint(long timestamp, double latitude, double longitude, double height) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
    }

    /** 时间戳（秒级） */
    public long getTimestamp() { return timestamp; }
    /** @param timestamp 时间戳（秒级） */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    /** 纬度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 纬度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** 经度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 经度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /** 高度（米） */
    public double getHeight() { return height; }
    /** @param height 高度（米） */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTrackPoint that = (FlightTrackPoint) o;
        return timestamp == that.timestamp
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, latitude, longitude, height);
    }

    @Override
    public String toString() {
        return "FlightTrackPoint{"
                + "timestamp=" + timestamp
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + '}';
    }
}
