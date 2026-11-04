// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 航线碰撞事件 */
public final class WaylineCollisionEvent {
    private String waylineUuid;
    private String flightTaskUuid;
    private long timestamp;
    private double latitude;
    private double longitude;
    private double altitude;

    public WaylineCollisionEvent() {
    }

    public WaylineCollisionEvent(String waylineUuid, String flightTaskUuid, long timestamp,
            double latitude, double longitude, double altitude) {
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.flightTaskUuid = flightTaskUuid != null ? flightTaskUuid : "";
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid; }

    /** 飞行任务 UUID */
    public String getFlightTaskUuid() { return flightTaskUuid; }

    /** 飞行任务 UUID */
    public void setFlightTaskUuid(String flightTaskUuid) { this.flightTaskUuid = flightTaskUuid; }

    /** 时间戳 */
    public long getTimestamp() { return timestamp; }

    /** 时间戳 */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /** 纬度 */
    public double getLatitude() { return latitude; }

    /** 纬度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }

    /** 经度 */
    public double getLongitude() { return longitude; }

    /** 经度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }

    /** 高度 */
    public double getAltitude() { return altitude; }

    /** 高度 */
    public void setAltitude(double altitude) { this.altitude = altitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaylineCollisionEvent that = (WaylineCollisionEvent) o;
        return timestamp == that.timestamp
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.altitude, altitude) == 0
                && Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(flightTaskUuid, that.flightTaskUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waylineUuid, flightTaskUuid, timestamp, latitude, longitude, altitude);
    }

    @Override
    public String toString() {
        return "WaylineCollisionEvent{waylineUuid='" + waylineUuid + '\''
                + ", flightTaskUuid='" + flightTaskUuid + '\''
                + ", timestamp=" + timestamp
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", altitude=" + altitude + '}';
    }
}
