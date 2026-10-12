// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场声光报警状态。 */
public final class DockAlarmState {

    /** int, 声光报警状态, {0: 声光报警关闭, 1: 声光报警开启}; */
    private int alarmState;
    public DockAlarmState() {
    }

/**
     * 构造 DockAlarmState。
     *
     * @param alarmState 告警状态
     */
    public DockAlarmState(int alarmState) {
        this.alarmState = alarmState;
    }

    /** int, 声光报警状态, {0: 声光报警关闭, 1: 声光报警开启}; */
    public int getAlarmState() {
        return alarmState;
    }

    /** @param alarmState int, 声光报警状态, {0: 声光报警关闭, 1: 声光报警开启}; */
    public void setAlarmState(int alarmState) {
        this.alarmState = alarmState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockAlarmState that = (DockAlarmState) o;
        return alarmState == that.alarmState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alarmState);
    }

    @Override
    public String toString() {
        return "DockAlarmState{alarmState=" + alarmState + '}';
    }
}
