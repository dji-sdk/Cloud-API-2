// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 避障状态。 */
public final class AircraftObstacleAvoidanceState {

    /** int, 水平避障状态, {0:关闭, 1:开启}; */
    private int horizon;
    /** int, 上视避障状态, {0:关闭, 1:开启}; */
    private int upside;
    /** int, 下视避障状态, {0:关闭, 1:开启}; */
    private int downside;
    public AircraftObstacleAvoidanceState() {
    }

/**
     * 构造 AircraftObstacleAvoidanceState。
     *
     * @param horizon 水平避障状态。{0:关闭, 1:开启}
     * @param upside 上视避障状态。{0:关闭, 1:开启}
     * @param downside 下视避障状态。{0:关闭, 1:开启}
     */
    public AircraftObstacleAvoidanceState(int horizon, int upside, int downside) {
        this.horizon = horizon;
        this.upside = upside;
        this.downside = downside;
    }

    /** int, 水平避障状态, {0:关闭, 1:开启}; */
    public int getHorizon() {
        return horizon;
    }

    /** @param horizon int, 水平避障状态, {0:关闭, 1:开启}; */
    public void setHorizon(int horizon) {
        this.horizon = horizon;
    }

    /** int, 上视避障状态, {0:关闭, 1:开启}; */
    public int getUpside() {
        return upside;
    }

    /** @param upside int, 上视避障状态, {0:关闭, 1:开启}; */
    public void setUpside(int upside) {
        this.upside = upside;
    }

    /** int, 下视避障状态, {0:关闭, 1:开启}; */
    public int getDownside() {
        return downside;
    }

    /** @param downside int, 下视避障状态, {0:关闭, 1:开启}; */
    public void setDownside(int downside) {
        this.downside = downside;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftObstacleAvoidanceState that = (AircraftObstacleAvoidanceState) o;
        return horizon == that.horizon && upside == that.upside && downside == that.downside;
    }

    @Override
    public int hashCode() {
        return Objects.hash(horizon, upside, downside);
    }

    @Override
    public String toString() {
        return "AircraftObstacleAvoidanceState{horizon=" + horizon + ", upside=" + upside + ", downside=" + downside + '}';
    }
}
