// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 紧急停止按钮状态。 */
public final class DockEmergencyStopState {

    /** int, 紧急停止按钮状态, 0: 关闭, 1: 开启; */
    private int emergencyStopState;
    public DockEmergencyStopState() {
    }

/**
     * 构造 DockEmergencyStopState。
     *
     * @param emergencyStopState 急停状态
     */
    public DockEmergencyStopState(int emergencyStopState) {
        this.emergencyStopState = emergencyStopState;
    }

    /** int, 紧急停止按钮状态, 0: 关闭, 1: 开启; */
    public int getEmergencyStopState() {
        return emergencyStopState;
    }

    /** @param emergencyStopState int, 紧急停止按钮状态, 0: 关闭, 1: 开启; */
    public void setEmergencyStopState(int emergencyStopState) {
        this.emergencyStopState = emergencyStopState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockEmergencyStopState that = (DockEmergencyStopState) o;
        return emergencyStopState == that.emergencyStopState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emergencyStopState);
    }

    @Override
    public String toString() {
        return "DockEmergencyStopState{emergencyStopState=" + emergencyStopState + '}';
    }
}
