// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 航线障碍物。 */
public final class WaylineObstacle {
    /** 避障信息 ID */
    private int id;
    /** 避障点类型。枚举：0=绕行开始(Start), 1=绕行结束(Return), 2=避障刹停(Stop) */
    private int type;
    /** 时间戳，单位：毫秒 */
    private long timestamp;
    /** 纬度，单位 1/2^32*180度 */
    private double latitude;
    /** 经度，单位 1/2^32*360度 */
    private double longitude;
    /** 避障点高度（米） */
    private double height;
    /** 航线 ID */
    private int waylineId;
    /** 当前航点索引 */
    private int waypointIndex;
    public WaylineObstacle() {
    }

/**
     * 构造 WaylineObstacle。
     *
     * @param id ID
     * @param type 避障点类型。枚举：0=绕行开始(Start), 1=绕行结束(Return), 2=避障刹停(Stop)
     * @param timestamp 时间戳，单位：毫秒
     * @param latitude 纬度，单位 1/2^32*180度
     * @param longitude 经度，单位 1/2^32*360度
     * @param height 高度
     * @param waylineId 航线 ID
     * @param waypointIndex 当前航点索引
     */
    public WaylineObstacle(int id, int type, long timestamp, double latitude,
            double longitude, double height, int waylineId, int waypointIndex) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.waylineId = waylineId;
        this.waypointIndex = waypointIndex;
    }

    /** 避障信息 ID */
    public int getId() { return id; }
    /** 避障点类型。枚举：0=绕行开始(Start), 1=绕行结束(Return), 2=避障刹停(Stop) */
    public int getType() { return type; }
    /** 时间戳，单位：毫秒 */
    public long getTimestamp() { return timestamp; }
    /** 纬度，单位 1/2^32*180度 */
    public double getLatitude() { return latitude; }
    /** 经度，单位 1/2^32*360度 */
    public double getLongitude() { return longitude; }
    /** 避障点高度（米） */
    public double getHeight() { return height; }
    /** 航线 ID */
    public int getWaylineId() { return waylineId; }
    /** 当前航点索引 */
    public int getWaypointIndex() { return waypointIndex; }

    /** @param id 避障信息 ID */
    public void setId(int id) {
        this.id = id;
    }

    /** @param type 避障点类型。枚举：0=绕行开始(Start), 1=绕行结束(Return), 2=避障刹停(Stop) */
    public void setType(int type) {
        this.type = type;
    }

    /** 时间戳，单位：毫秒 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 纬度，单位 1/2^32*180度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** 经度，单位 1/2^32*360度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** @param height 避障点高度（米） */
    public void setHeight(double height) {
        this.height = height;
    }

    /** 航线 ID */
    public void setWaylineId(int waylineId) {
        this.waylineId = waylineId;
    }

    /** 当前航点索引 */
    public void setWaypointIndex(int waypointIndex) {
        this.waypointIndex = waypointIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineObstacle that = (WaylineObstacle) o;
        return id == that.id
                && type == that.type
                && timestamp == that.timestamp
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0
                && waylineId == that.waylineId
                && waypointIndex == that.waypointIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                type,
                timestamp,
                latitude,
                longitude,
                height,
                waylineId,
                waypointIndex);
    }

    @Override
    public String toString() {
        return "WaylineObstacle{"
                + "id=" + id
                + ", type=" + type
                + ", timestamp=" + timestamp
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + ", waylineId=" + waylineId
                + ", waypointIndex=" + waypointIndex
                + '}';
    }
}
