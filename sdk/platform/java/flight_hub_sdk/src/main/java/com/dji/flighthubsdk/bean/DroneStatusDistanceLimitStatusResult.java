// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞机设备状态 — 限远状态 */
public final class DroneStatusDistanceLimitStatusResult {

    private int distanceLimit;
    private int nearDistanceLimit;
    private int state;
    public DroneStatusDistanceLimitStatusResult() {
    }

/**
     * 构造 DroneStatusDistanceLimitStatusResult。
     *
     * @param distanceLimit 限远距离
     * @param nearDistanceLimit Near距离限制
     * @param state 状态
     */
    public DroneStatusDistanceLimitStatusResult(int distanceLimit, int nearDistanceLimit, int state) {
        this.distanceLimit = distanceLimit;
        this.nearDistanceLimit = nearDistanceLimit;
        this.state = state;
    }

    /** 限远距离 */
    public int getDistanceLimit() {
        return distanceLimit;
    }

    /** 限远距离 */
    public void setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

        /** Near距离限制 */
        public int getNearDistanceLimit() {
        return nearDistanceLimit;
    }

        /** Near距离限制 */
        public void setNearDistanceLimit(int nearDistanceLimit) {
            this.nearDistanceLimit = nearDistanceLimit;
        }

    /** 状态 */
    public int getState() {
        return state;
    }

    /** 状态 */
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneStatusDistanceLimitStatusResult that = (DroneStatusDistanceLimitStatusResult) o;
        return distanceLimit == that.distanceLimit
                && nearDistanceLimit == that.nearDistanceLimit
                && state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distanceLimit, nearDistanceLimit, state);
    }

    @Override
    public String toString() {
        return "DroneStatusDistanceLimitStatusResult{distanceLimit=" + distanceLimit + ", nearDistanceLimit=" + nearDistanceLimit + ", state=" + state + '}';
    }
}
