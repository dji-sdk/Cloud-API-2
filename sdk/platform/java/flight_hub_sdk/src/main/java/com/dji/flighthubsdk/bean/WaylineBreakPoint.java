// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 航线断点。 */
public final class WaylineBreakPoint {
    /** 断点索引 */
    private int index;
    /** 断点状态 */
    private int state;
    /** 进度 */
    private double progress;
    /** 航线 ID */
    private int waylineId;
    /** 断点 Reason */
    private int breakReason;
    /** 断点纬度 */
    private double latitude;
    /** 断点经度 */
    private double longitude;
    /** 断点高度（米） */
    private double height;
    /** 机头朝向角度 */
    private double attitudeHead;
    public WaylineBreakPoint() {
    }

/**
     * 构造 WaylineBreakPoint。
     *
     * @param index 索引
     * @param state 状态
     * @param progress 进度
     * @param waylineId 航线 ID
     * @param breakReason 断点Reason
     * @param latitude 纬度，单位 1/2^32*180度
     * @param longitude 经度，单位 1/2^32*360度
     * @param height 高度
     * @param attitudeHead 航向角（度）
     */
    public WaylineBreakPoint(int index, int state, double progress, int waylineId,
            int breakReason, double latitude, double longitude, double height,
            double attitudeHead) {
        this.index = index;
        this.state = state;
        this.progress = progress;
        this.waylineId = waylineId;
        this.breakReason = breakReason;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.attitudeHead = attitudeHead;
    }

    /** 断点索引 */
    public int getIndex() { return index; }
    /** 断点状态 */
    public int getState() { return state; }
    /** 进度 */
    public double getProgress() { return progress; }
    /** 航线 ID */
    public int getWaylineId() { return waylineId; }
    /** 断点 Reason */
    public int getBreakReason() { return breakReason; }
    /** 断点纬度 */
    public double getLatitude() { return latitude; }
    /** 断点经度 */
    public double getLongitude() { return longitude; }
    /** 断点高度（米） */
    public double getHeight() { return height; }
    /** 机头朝向角度 */
    public double getAttitudeHead() { return attitudeHead; }

    /** @param index 断点索引 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** @param state 断点状态 */
    public void setState(int state) {
        this.state = state;
    }

    /** 进度 */
    public void setProgress(double progress) {
        this.progress = progress;
    }

    /** 航线 ID */
    public void setWaylineId(int waylineId) {
        this.waylineId = waylineId;
    }

    /** @param breakReason 断点 Reason */
    public void setBreakReason(int breakReason) {
        this.breakReason = breakReason;
    }

    /** @param latitude 断点纬度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** @param longitude 断点经度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** @param height 断点高度（米） */
    public void setHeight(double height) {
        this.height = height;
    }

    /** @param attitudeHead 机头朝向角度 */
    public void setAttitudeHead(double attitudeHead) {
        this.attitudeHead = attitudeHead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineBreakPoint that = (WaylineBreakPoint) o;
        return index == that.index
                && state == that.state
                && Double.compare(that.progress, progress) == 0
                && waylineId == that.waylineId
                && breakReason == that.breakReason
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0
                && Double.compare(that.attitudeHead, attitudeHead) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                index,
                state,
                progress,
                waylineId,
                breakReason,
                latitude,
                longitude,
                height,
                attitudeHead);
    }

    @Override
    public String toString() {
        return "WaylineBreakPoint{"
                + "index=" + index
                + ", state=" + state
                + ", progress=" + progress
                + ", waylineId=" + waylineId
                + ", breakReason=" + breakReason
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + ", attitudeHead=" + attitudeHead
                + '}';
    }
}
