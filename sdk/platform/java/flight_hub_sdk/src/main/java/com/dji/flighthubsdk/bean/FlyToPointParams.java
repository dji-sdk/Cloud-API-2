// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.SpeedMode;

import java.util.List;

import java.util.Objects;
/**
 * 指点飞行参数。
 *
 * <p><b>{@code startPoint} 与 {@code points} 是两个不同的坐标</b>：前者是飞机当前位置（航段起点），
 * 后者是要飞往的目标点。二者不可混用，填成同一个点会让服务端把航段起点误判为目标点。
 */
public class FlyToPointParams {
    /** 起始点坐标（当前飞机位置，必填） */
    private FlyToTargetPoint startPoint;
    /** 目标点坐标数组（至少 1 个） */
    private List<FlyToTargetPoint> points;
    /** 最大飞行速度，单位：m/s */
    private int maxSpeed;
    /**
     * 速度模式（速度档位）。{@link SpeedMode#UNKNOWN} 表示未设置，此时不下发该字段、设备保持当前模式。
     */
    private SpeedMode speedMode = SpeedMode.UNKNOWN;
    /** 高度上限开关。-1 未设置（设备保持当前设置）；0=false；1=true */
    private int realHeightCapEnabled = -1;
    /** 高度上限值（米），范围 20-1500。-1 表示未设置 */
    private int realHeightCapLimit = -1;

    public FlyToPointParams() {
    }


    /** 起始点坐标（当前飞机位置，必填） */
    public FlyToTargetPoint getStartPoint() { return startPoint; }
    /** @param startPoint 起始点坐标（当前飞机位置，必填） */
    public void setStartPoint(FlyToTargetPoint startPoint) { this.startPoint = startPoint; }
    /** 目标点坐标数组（至少 1 个） */
    public List<FlyToTargetPoint> getPoints() { return points; }
    /** @param points 目标点坐标数组（至少 1 个） */
    public void setPoints(List<FlyToTargetPoint> points) { this.points = points; }
    /** 最大飞行速度，单位：m/s */
    public int getMaxSpeed() { return maxSpeed; }

    /** @param maxSpeed 最大飞行速度，单位：m/s */
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }

    /** 速度模式（速度档位）。UNKNOWN 表示未设置，不下发该字段 */
    public SpeedMode getSpeedMode() { return speedMode; }
    /** @param speedMode 速度模式；传 null 或 UNKNOWN 表示不设置 */
    public void setSpeedMode(SpeedMode speedMode) {
        this.speedMode = speedMode != null ? speedMode : SpeedMode.UNKNOWN;
    }
    /** 高度上限开关。-1 未设置；0=false；1=true */
    public int getRealHeightCapEnabled() { return realHeightCapEnabled; }
    /** @param realHeightCapEnabled -1 未设置；0=false；1=true */
    public void setRealHeightCapEnabled(int realHeightCapEnabled) {
        this.realHeightCapEnabled = realHeightCapEnabled;
    }
    /** 高度上限值（米），范围 20-1500。-1 表示未设置 */
    public int getRealHeightCapLimit() { return realHeightCapLimit; }
    /** @param realHeightCapLimit 高度上限值（米），20-1500；传 -1 表示不设置 */
    public void setRealHeightCapLimit(int realHeightCapLimit) {
        this.realHeightCapLimit = realHeightCapLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlyToPointParams that = (FlyToPointParams) o;
        return Objects.equals(startPoint, that.startPoint)
                && Objects.equals(points, that.points)
                && maxSpeed == that.maxSpeed
                && Objects.equals(speedMode, that.speedMode)
                && realHeightCapEnabled == that.realHeightCapEnabled
                && realHeightCapLimit == that.realHeightCapLimit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPoint, points, maxSpeed,
                speedMode, realHeightCapEnabled, realHeightCapLimit);
    }

    @Override
    public String toString() {
        return "FlyToPointParams{"
                + "startPoint=" + startPoint
                + ", points=" + points
                + ", maxSpeed=" + maxSpeed
                + ", speedMode=" + speedMode
                + ", realHeightCapEnabled=" + realHeightCapEnabled
                + ", realHeightCapLimit=" + realHeightCapLimit
                + '}';
    }
}
