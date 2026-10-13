// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 飞行器自主规划路径信息 */
public final class FlyToDroneSelfPlanPath {

    /** 指点飞行任务 ID */
    private String flyToId;
    /** 飞行器 SN */
    private String sn;
    /** 时间戳 */
    private long timestamp;
    /** 规划路径点列表 */
    private List<FlyToDroneSelfPlanPathPoint> plannedPathPoints;
    /** 剩余飞行距离，单位：米 */
    private double remainingDistance;
    /** 剩余飞行时间，单位：秒 */
    private double remainingTime;
    public FlyToDroneSelfPlanPath() {
    }

/**
     * 构造 FlyToDroneSelfPlanPath。
     *
     * @param flyToId 指点飞行任务 ID
     * @param sn 飞行器 SN
     * @param timestamp 时间戳
     * @param plannedPathPoints 规划路径点列表
     * @param remainingDistance 剩余飞行距离，单位：米
     * @param remainingTime 剩余飞行时间，单位：秒
     */
    public FlyToDroneSelfPlanPath(
            String flyToId,
            String sn,
            long timestamp,
            List<FlyToDroneSelfPlanPathPoint> plannedPathPoints,
            double remainingDistance,
            double remainingTime) {
        this.flyToId = flyToId != null ? flyToId : "";
        this.sn = sn != null ? sn : "";
        this.timestamp = timestamp;
        this.plannedPathPoints = plannedPathPoints != null ? plannedPathPoints : Collections.emptyList();
        this.remainingDistance = remainingDistance;
        this.remainingTime = remainingTime;
    }

    /** 指点飞行任务 ID */
    public String getFlyToId() {
        return flyToId;
    }

    /** 指点飞行任务 ID */
    public void setFlyToId(String flyToId) {
        this.flyToId = flyToId;
    }

    /** 飞行器 SN */
    public String getSn() {
        return sn;
    }

    /** 飞行器 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 时间戳 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 规划路径点列表 */
    public List<FlyToDroneSelfPlanPathPoint> getPlannedPathPoints() {
        return plannedPathPoints;
    }

    /** 规划路径点列表 */
    public void setPlannedPathPoints(List<FlyToDroneSelfPlanPathPoint> plannedPathPoints) {
        this.plannedPathPoints = plannedPathPoints;
    }

    /** 剩余飞行距离，单位：米 */
    public double getRemainingDistance() {
        return remainingDistance;
    }

    /** 剩余飞行距离，单位：米 */
    public void setRemainingDistance(double remainingDistance) {
        this.remainingDistance = remainingDistance;
    }

    /** 剩余飞行时间，单位：秒 */
    public double getRemainingTime() {
        return remainingTime;
    }

    /** 剩余飞行时间，单位：秒 */
    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlyToDroneSelfPlanPath that = (FlyToDroneSelfPlanPath) o;
        return timestamp == that.timestamp
                && Double.compare(that.remainingDistance, remainingDistance) == 0
                && Double.compare(that.remainingTime, remainingTime) == 0
                && Objects.equals(flyToId, that.flyToId)
                && Objects.equals(sn, that.sn)
                && Objects.equals(plannedPathPoints, that.plannedPathPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flyToId, sn, timestamp, plannedPathPoints, remainingDistance, remainingTime);
    }

    @Override
    public String toString() {
        return "FlyToDroneSelfPlanPath{flyToId='" + flyToId + '\''
                + ", sn='" + sn + '\''
                + ", timestamp=" + timestamp
                + ", plannedPathPoints=" + plannedPathPoints
                + ", remainingDistance=" + remainingDistance
                + ", remainingTime=" + remainingTime + '}';
    }
}
