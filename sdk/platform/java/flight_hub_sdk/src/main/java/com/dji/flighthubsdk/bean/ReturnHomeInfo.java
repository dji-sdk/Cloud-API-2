// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 返航信息(规划路径/末端点可达性/多机场返航信息)。 */
public final class ReturnHomeInfo {
    private long timestamp;
    private List<Waypoint> plannedPathPoints;
    private int lastPointType;
    private String homeDockSn;
    private List<MultiDockHomeInfo> multiDockHomeInfo;

    public ReturnHomeInfo() {
        this.plannedPathPoints = new ArrayList<Waypoint>();
        this.multiDockHomeInfo = new ArrayList<MultiDockHomeInfo>();
    }

    /**
     * 构造 ReturnHomeInfo。
     *
     * @param timestamp 时间戳，单位：毫秒
     * @param plannedPathPoints 规划路径点
     * @param lastPointType 末端点类型：0-可达，1-不可达
     * @param homeDockSn 返航机场序列号
     * @param multiDockHomeInfo 多机场返航信息
     */
    public ReturnHomeInfo(long timestamp, List<Waypoint> plannedPathPoints, int lastPointType,
            String homeDockSn, List<MultiDockHomeInfo> multiDockHomeInfo) {
        this.timestamp = timestamp;
        this.plannedPathPoints = plannedPathPoints != null ? plannedPathPoints : new ArrayList<Waypoint>();
        this.lastPointType = lastPointType;
        this.homeDockSn = homeDockSn != null ? homeDockSn : "";
        this.multiDockHomeInfo = multiDockHomeInfo != null ? multiDockHomeInfo : new ArrayList<MultiDockHomeInfo>();
    }

    /** 时间戳，单位：毫秒 */
    public long getTimestamp() { return timestamp; }
    /** 规划路径点 */
    public List<Waypoint> getPlannedPathPoints() { return plannedPathPoints; }
    /** 末端点类型：0-可达，1-不可达 */
    public int getLastPointType() { return lastPointType; }
    /** 返航机场序列号 */
    public String getHomeDockSn() { return homeDockSn; }
    /** 多机场返航信息 */
    public List<MultiDockHomeInfo> getMultiDockHomeInfo() { return multiDockHomeInfo; }

    /** 时间戳，单位：毫秒 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 规划路径点 */
    public void setPlannedPathPoints(List<Waypoint> plannedPathPoints) {
        this.plannedPathPoints = plannedPathPoints;
    }

    /** 末端点类型：0-可达，1-不可达 */
    public void setLastPointType(int lastPointType) {
        this.lastPointType = lastPointType;
    }

    /** 返航机场序列号 */
    public void setHomeDockSn(String homeDockSn) {
        this.homeDockSn = homeDockSn;
    }

    /** 多机场返航信息 */
    public void setMultiDockHomeInfo(List<MultiDockHomeInfo> multiDockHomeInfo) {
        this.multiDockHomeInfo = multiDockHomeInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReturnHomeInfo that = (ReturnHomeInfo) o;
        return timestamp == that.timestamp
                && lastPointType == that.lastPointType
                && Objects.equals(plannedPathPoints, that.plannedPathPoints)
                && Objects.equals(homeDockSn, that.homeDockSn)
                && Objects.equals(multiDockHomeInfo, that.multiDockHomeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, plannedPathPoints, lastPointType, homeDockSn, multiDockHomeInfo);
    }

    @Override
    public String toString() {
        return "ReturnHomeInfo{"
                + "timestamp=" + timestamp
                + ", plannedPathPoints=" + plannedPathPoints
                + ", lastPointType=" + lastPointType
                + ", homeDockSn='" + homeDockSn + '\''
                + ", multiDockHomeInfo=" + multiDockHomeInfo
                + '}';
    }
}
