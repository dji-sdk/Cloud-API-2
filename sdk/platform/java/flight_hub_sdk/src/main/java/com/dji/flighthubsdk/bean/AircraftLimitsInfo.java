// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机限制类信息（高度 / 限飞区 / 限远等）。
 */
public final class AircraftLimitsInfo {

    /** 相对起飞点的飞行限制高度, 单位(m); */
    private int heightLimit;
    /** 是否接近设定的限制高度, {0: 未达到设定的限制高度, 1: 接近设定的限制高度}; */
    private int isNearHeightLimit;
    /** 是否接近限飞区, {0: 未达到限飞区, 1: 接近限飞区}; */
    private int isNearAreaLimit;
    /** 限远状态, {0: 未设置, 1: 有设置}; */
    private int distanceLimitState;
    /** 限远距离; */
    private int distanceLimit;
    /** 是否接近限远距离; */
    private int isNearDistanceLimit;
    public AircraftLimitsInfo() {
    }

/**
     * 构造 AircraftLimitsInfo。
     *
     * @param heightLimit 限制高度(m)
     * @param isNearHeightLimit 是否接近限制高度
     * @param isNearAreaLimit 是否接近限飞区
     * @param distanceLimitState 限远状态
     * @param distanceLimit 限远距离
     * @param isNearDistanceLimit 是否接近限远距离
     */
    public AircraftLimitsInfo(
            int heightLimit,
            int isNearHeightLimit,
            int isNearAreaLimit,
            int distanceLimitState,
            int distanceLimit,
            int isNearDistanceLimit) {
        this.heightLimit = heightLimit;
        this.isNearHeightLimit = isNearHeightLimit;
        this.isNearAreaLimit = isNearAreaLimit;
        this.distanceLimitState = distanceLimitState;
        this.distanceLimit = distanceLimit;
        this.isNearDistanceLimit = isNearDistanceLimit;
    }

    /** 相对起飞点的飞行限制高度, 单位(m); */
    public int getHeightLimit() {
        return heightLimit;
    }

    /** @param heightLimit 相对起飞点的飞行限制高度, 单位(m); */
    public void setHeightLimit(int heightLimit) {
        this.heightLimit = heightLimit;
    }

    /** 是否接近设定的限制高度, {0: 未达到设定的限制高度, 1: 接近设定的限制高度}; */
    public int getIsNearHeightLimit() {
        return isNearHeightLimit;
    }

    /** @param isNearHeightLimit 是否接近设定的限制高度, {0: 未达到设定的限制高度, 1: 接近设定的限制高度}; */
    public void setIsNearHeightLimit(int isNearHeightLimit) {
        this.isNearHeightLimit = isNearHeightLimit;
    }

    /** 是否接近限飞区, {0: 未达到限飞区, 1: 接近限飞区}; */
    public int getIsNearAreaLimit() {
        return isNearAreaLimit;
    }

    /** @param isNearAreaLimit 是否接近限飞区, {0: 未达到限飞区, 1: 接近限飞区}; */
    public void setIsNearAreaLimit(int isNearAreaLimit) {
        this.isNearAreaLimit = isNearAreaLimit;
    }

    /** 限远状态, {0: 未设置, 1: 有设置}; */
    public int getDistanceLimitState() {
        return distanceLimitState;
    }

    /** @param distanceLimitState 限远状态, {0: 未设置, 1: 有设置}; */
    public void setDistanceLimitState(int distanceLimitState) {
        this.distanceLimitState = distanceLimitState;
    }

    /** 限远距离; */
    public int getDistanceLimit() {
        return distanceLimit;
    }

    /** @param distanceLimit 限远距离; */
    public void setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

    /** 是否接近限远距离; */
    public int getIsNearDistanceLimit() {
        return isNearDistanceLimit;
    }

    /** @param isNearDistanceLimit 是否接近限远距离; */
    public void setIsNearDistanceLimit(int isNearDistanceLimit) {
        this.isNearDistanceLimit = isNearDistanceLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftLimitsInfo that = (AircraftLimitsInfo) o;
        return heightLimit == that.heightLimit
                && isNearHeightLimit == that.isNearHeightLimit
                && isNearAreaLimit == that.isNearAreaLimit
                && distanceLimitState == that.distanceLimitState
                && distanceLimit == that.distanceLimit
                && isNearDistanceLimit == that.isNearDistanceLimit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                heightLimit,
                isNearHeightLimit,
                isNearAreaLimit,
                distanceLimitState,
                distanceLimit,
                isNearDistanceLimit);
    }

    @Override
    public String toString() {
        return "AircraftLimitsInfo{heightLimit=" + heightLimit
                + ", isNearHeightLimit=" + isNearHeightLimit
                + ", isNearAreaLimit=" + isNearAreaLimit
                + ", distanceLimitState=" + distanceLimitState
                + ", distanceLimit=" + distanceLimit
                + ", isNearDistanceLimit=" + isNearDistanceLimit + '}';
    }
}
