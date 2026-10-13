// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场内无人机状态。 */
public final class DroneInDock {

    private int droneInDock;
    public DroneInDock() {
    }

/**
     * 构造 DroneInDock。
     *
     * @param droneInDock 机场中的无人机
     */
    public DroneInDock(int droneInDock) {
        this.droneInDock = droneInDock;
    }

    /** 机场中的无人机 */
    public int getDroneInDock() {
        return droneInDock;
    }

    /** 机场中的无人机 */
    public void setDroneInDock(int droneInDock) {
        this.droneInDock = droneInDock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneInDock that = (DroneInDock) o;
        return droneInDock == that.droneInDock;
    }

    @Override
    public int hashCode() {
        return Objects.hash(droneInDock);
    }

    @Override
    public String toString() {
        return "DroneInDock{droneInDock=" + droneInDock + '}';
    }
}
