// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 空调信息 */
public final class DockStatusAirConditionerResult {

    private int airConditionerState;
    private int switchTime;
    public DockStatusAirConditionerResult() {
    }

/**
     * 构造 DockStatusAirConditionerResult。
     *
     * @param airConditionerState 空调状态
     * @param switchTime 切换时间
     */
    public DockStatusAirConditionerResult(int airConditionerState, int switchTime) {
        this.airConditionerState = airConditionerState;
        this.switchTime = switchTime;
    }

    /** 空调状态 */
    public int getAirConditionerState() {
        return airConditionerState;
    }

    /** 空调状态 */
    public void setAirConditionerState(int airConditionerState) {
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
        DockStatusAirConditionerResult that = (DockStatusAirConditionerResult) o;
        return airConditionerState == that.airConditionerState
                && switchTime == that.switchTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(airConditionerState, switchTime);
    }

    @Override
    public String toString() {
        return "DockStatusAirConditionerResult{airConditionerState=" + airConditionerState + ", switchTime=" + switchTime + '}';
    }
}
