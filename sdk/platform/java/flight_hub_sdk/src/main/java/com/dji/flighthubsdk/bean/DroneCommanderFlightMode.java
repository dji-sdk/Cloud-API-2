// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 指点飞行模式设置值（commander_flight_mode）。EA340 消息推送补全（Phase D）。 */
public final class DroneCommanderFlightMode {

    private long commanderFlightMode;

    public DroneCommanderFlightMode() {
    }

    /**
     * 构造 DroneCommanderFlightMode。
     *
     * @param commanderFlightMode 指点飞行模式设置值
     */
    public DroneCommanderFlightMode(long commanderFlightMode) {
        this.commanderFlightMode = commanderFlightMode;
    }

    /** 指点飞行模式设置值 */
    public long getCommanderFlightMode() {
        return commanderFlightMode;
    }

    /** 指点飞行模式设置值 */
    public void setCommanderFlightMode(long commanderFlightMode) {
        this.commanderFlightMode = commanderFlightMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneCommanderFlightMode that = (DroneCommanderFlightMode) o;
        return commanderFlightMode == that.commanderFlightMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commanderFlightMode);
    }

    @Override
    public String toString() {
        return "DroneCommanderFlightMode{commanderFlightMode=" + commanderFlightMode + '}';
    }
}
