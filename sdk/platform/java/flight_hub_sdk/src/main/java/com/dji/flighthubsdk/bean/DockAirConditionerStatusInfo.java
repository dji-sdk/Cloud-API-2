// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.DockAirConditionerState;
import java.util.Objects;
/** 机场空调工作状态与切换时间。 */
public final class DockAirConditionerStatusInfo {

    /** `DockAirConditionerState`（`value.h`） */
    private DockAirConditionerState airConditionerState;
    private int switchTime;
    public DockAirConditionerStatusInfo() {
    }

/**
     * 构造 DockAirConditionerStatusInfo。
     *
     * @param airConditionerState 空调当前工作模式（制冷、制热、空闲等）
     * @param switchTime 切换时间
     */
    public DockAirConditionerStatusInfo(DockAirConditionerState airConditionerState, int switchTime) {
        this.airConditionerState = airConditionerState != null
                ? airConditionerState
                : DockAirConditionerState.DockAirConditionerState_UNKNOWN;
        this.switchTime = switchTime;
    }

    /** `DockAirConditionerState`（`value.h`） */
    public DockAirConditionerState getAirConditionerState() {
        return airConditionerState;
    }

    /** @param airConditionerState `DockAirConditionerState`（`value.h`） */
    public void setAirConditionerState(DockAirConditionerState airConditionerState) {
        this.airConditionerState = airConditionerState;
    }

    /** 切换时间 */
    public int getSwitchTime() {
        return switchTime;
    }

    /** 切换时间 */
    public void setSwitchTime(int switchTime) {
        this.switchTime = switchTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockAirConditionerStatusInfo that = (DockAirConditionerStatusInfo) o;
        return airConditionerState == that.airConditionerState
                && switchTime == that.switchTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(airConditionerState, switchTime);
    }

    @Override
    public String toString() {
        return "DockAirConditionerStatusInfo{airConditionerState=" + airConditionerState
                + ", switchTime=" + switchTime + '}';
    }
}
