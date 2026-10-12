// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞机设备状态 — 避障 */
public final class DroneStatusObstacleAvoidanceResult {

    private int downside;
    private int horizon;
    private int upside;
    public DroneStatusObstacleAvoidanceResult() {
    }

/**
     * 构造 DroneStatusObstacleAvoidanceResult。
     *
     * @param downside 下方避障
     * @param horizon 水平方向避障
     * @param upside 上方避障
     */
    public DroneStatusObstacleAvoidanceResult(int downside, int horizon, int upside) {
        this.downside = downside;
        this.horizon = horizon;
        this.upside = upside;
    }

    /** 下方避障 */
    public int getDownside() {
        return downside;
    }

    /** 下方避障 */
    public void setDownside(int downside) {
        this.downside = downside;
    }

    /** 水平方向避障 */
    public int getHorizon() {
        return horizon;
    }

    /** 水平方向避障 */
    public void setHorizon(int horizon) {
        this.horizon = horizon;
    }

    /** 上方避障 */
    public int getUpside() {
        return upside;
    }

    /** 上方避障 */
    public void setUpside(int upside) {
        this.upside = upside;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneStatusObstacleAvoidanceResult that = (DroneStatusObstacleAvoidanceResult) o;
        return downside == that.downside
                && horizon == that.horizon
                && upside == that.upside;
    }

    @Override
    public int hashCode() {
        return Objects.hash(downside, horizon, upside);
    }

    @Override
    public String toString() {
        return "DroneStatusObstacleAvoidanceResult{downside=" + downside + ", horizon=" + horizon + ", upside=" + upside + '}';
    }
}
