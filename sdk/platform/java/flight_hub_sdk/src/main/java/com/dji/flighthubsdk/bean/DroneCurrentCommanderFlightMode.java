// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 指点飞行模式当前值（current_commander_flight_mode）。EA340 消息推送补全（Phase D）。 */
public final class DroneCurrentCommanderFlightMode {

    private long currentCommanderFlightMode;

    public DroneCurrentCommanderFlightMode() {
    }

    /**
     * 构造 DroneCurrentCommanderFlightMode。
     *
     * @param currentCommanderFlightMode 指点飞行模式当前值
     */
    public DroneCurrentCommanderFlightMode(long currentCommanderFlightMode) {
        this.currentCommanderFlightMode = currentCommanderFlightMode;
    }

    /** 指点飞行模式当前值 */
    public long getCurrentCommanderFlightMode() {
        return currentCommanderFlightMode;
    }

    /** 指点飞行模式当前值 */
    public void setCurrentCommanderFlightMode(long currentCommanderFlightMode) {
        this.currentCommanderFlightMode = currentCommanderFlightMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneCurrentCommanderFlightMode that = (DroneCurrentCommanderFlightMode) o;
        return currentCommanderFlightMode == that.currentCommanderFlightMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCommanderFlightMode);
    }

    @Override
    public String toString() {
        return "DroneCurrentCommanderFlightMode{currentCommanderFlightMode=" + currentCommanderFlightMode + '}';
    }
}
