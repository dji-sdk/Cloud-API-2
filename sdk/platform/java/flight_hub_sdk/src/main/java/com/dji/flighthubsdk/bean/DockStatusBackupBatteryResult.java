// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 备用电池 */
public final class DockStatusBackupBatteryResult {

    private int switchState;
    private double temperature;
    private int voltage;
    public DockStatusBackupBatteryResult() {
    }

/**
     * 构造 DockStatusBackupBatteryResult。
     *
     * @param switchState 切换状态
     * @param temperature 温度
     * @param voltage 电压
     */
    public DockStatusBackupBatteryResult(int switchState, double temperature, int voltage) {
        this.switchState = switchState;
        this.temperature = temperature;
        this.voltage = voltage;
    }

    /** 切换状态 */
    public int getSwitchState() {
        return switchState;
    }

    /** 切换状态 */
    public void setSwitchState(int switchState) {
        this.switchState = switchState;
    }

    /** 温度 */
    public double getTemperature() {
        return temperature;
    }

    /** 温度 */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /** 电压 */
    public int getVoltage() {
        return voltage;
    }

    /** 电压 */
    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusBackupBatteryResult that = (DockStatusBackupBatteryResult) o;
        return switchState == that.switchState
                && Double.compare(that.temperature, temperature) == 0
                && voltage == that.voltage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchState, temperature, voltage);
    }

    @Override
    public String toString() {
        return "DockStatusBackupBatteryResult{switchState=" + switchState + ", temperature=" + temperature + ", voltage=" + voltage + '}';
    }
}
