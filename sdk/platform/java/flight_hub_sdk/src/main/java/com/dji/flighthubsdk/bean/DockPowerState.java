// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场供电状态。 */
public final class DockPowerState {

    /** int, 市电电压, 单位：V; */
    private int electricSupplyVoltage;
    /** int, 工作电压(mV); */
    private int workingVoltage;
    /** int, 工作电流(mA); */
    private int workingCurrent;
    public DockPowerState() {
    }

/**
     * 构造 DockPowerState。
     *
     * @param electricSupplyVoltage 供电电压
     * @param workingVoltage 工作电压
     * @param workingCurrent 工作电流
     */
    public DockPowerState(int electricSupplyVoltage, int workingVoltage, int workingCurrent) {
        this.electricSupplyVoltage = electricSupplyVoltage;
        this.workingVoltage = workingVoltage;
        this.workingCurrent = workingCurrent;
    }

    /** int, 市电电压, 单位：V; */
    public int getElectricSupplyVoltage() {
        return electricSupplyVoltage;
    }

    /** @param electricSupplyVoltage int, 市电电压, 单位：V; */
    public void setElectricSupplyVoltage(int electricSupplyVoltage) {
        this.electricSupplyVoltage = electricSupplyVoltage;
    }

    /** int, 工作电压(mV); */
    public int getWorkingVoltage() {
        return workingVoltage;
    }

    /** @param workingVoltage int, 工作电压(mV); */
    public void setWorkingVoltage(int workingVoltage) {
        this.workingVoltage = workingVoltage;
    }

    /** int, 工作电流(mA); */
    public int getWorkingCurrent() {
        return workingCurrent;
    }

    /** @param workingCurrent int, 工作电流(mA); */
    public void setWorkingCurrent(int workingCurrent) {
        this.workingCurrent = workingCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockPowerState that = (DockPowerState) o;
        return electricSupplyVoltage == that.electricSupplyVoltage
                && workingVoltage == that.workingVoltage
                && workingCurrent == that.workingCurrent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(electricSupplyVoltage, workingVoltage, workingCurrent);
    }

    @Override
    public String toString() {
        return "DockPowerState{electricSupplyVoltage=" + electricSupplyVoltage
                + ", workingVoltage=" + workingVoltage
                + ", workingCurrent=" + workingCurrent + '}';
    }
}
