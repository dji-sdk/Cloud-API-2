// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场飞机充电状态。 */
public final class DockDroneChargeState {

    /** 0: 空闲, 1: 正在充电; */
    private int state;
    private int capacityPercent;
    public DockDroneChargeState() {
    }

/**
     * 构造 DockDroneChargeState。
     *
     * @param state 状态
     * @param capacityPercent 电量百分比
     */
    public DockDroneChargeState(int state, int capacityPercent) {
        this.state = state;
        this.capacityPercent = capacityPercent;
    }

    /** 0: 空闲, 1: 正在充电; */
    public int getState() {
        return state;
    }

    /** @param state 0: 空闲, 1: 正在充电; */
    public void setState(int state) {
        this.state = state;
    }

    /** 电量百分比 */
    public int getCapacityPercent() {
        return capacityPercent;
    }

    /** 电量百分比 */
    public void setCapacityPercent(int capacityPercent) {
        this.capacityPercent = capacityPercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockDroneChargeState that = (DockDroneChargeState) o;
        return state == that.state && capacityPercent == that.capacityPercent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, capacityPercent);
    }

    @Override
    public String toString() {
        return "DockDroneChargeState{state=" + state + ", capacityPercent=" + capacityPercent + '}';
    }
}
