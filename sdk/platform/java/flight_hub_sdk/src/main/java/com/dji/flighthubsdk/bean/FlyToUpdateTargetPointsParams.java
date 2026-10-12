// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;

import java.util.Objects;
/** 更新指点飞行目标点参数 */
public class FlyToUpdateTargetPointsParams {
    /** 当前执行中的指点飞行任务 ID */
    private String flyToId = "";
    /** 起始点坐标（当前飞机位置） */
    private FlyToTargetPoint startPoint;
    /** 目标点坐标数组（至少 1 个） */
    private List<FlyToTargetPoint> points;
    /** 最大飞行速度，单位：m/s */
    private int maxSpeed;

    public FlyToUpdateTargetPointsParams() {
    }


    /** 当前执行中的指点飞行任务 ID */
    public String getFlyToId() { return flyToId; }
    /** @param flyToId 当前执行中的指点飞行任务 ID */
    public void setFlyToId(String flyToId) { this.flyToId = flyToId != null ? flyToId : ""; }
    /** 起始点坐标（当前飞机位置） */
    public FlyToTargetPoint getStartPoint() { return startPoint; }
    /** @param startPoint 起始点坐标（当前飞机位置） */
    public void setStartPoint(FlyToTargetPoint startPoint) { this.startPoint = startPoint; }
    /** 目标点坐标数组（至少 1 个） */
    public List<FlyToTargetPoint> getPoints() { return points; }
    /** @param points 目标点坐标数组（至少 1 个） */
    public void setPoints(List<FlyToTargetPoint> points) { this.points = points; }
    /** 最大飞行速度，单位：m/s */
    public int getMaxSpeed() { return maxSpeed; }
    /** @param maxSpeed 最大飞行速度，单位：m/s */
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlyToUpdateTargetPointsParams that = (FlyToUpdateTargetPointsParams) o;
        return Objects.equals(flyToId, that.flyToId)
                && Objects.equals(startPoint, that.startPoint)
                && Objects.equals(points, that.points)
                && maxSpeed == that.maxSpeed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flyToId, startPoint, points, maxSpeed);
    }

    @Override
    public String toString() {
        return "FlyToUpdateTargetPointsParams{"
                + "flyToId='" + flyToId + '\''
                + ", startPoint=" + startPoint
                + ", points=" + points
                + ", maxSpeed=" + maxSpeed
                + '}';
    }
}
