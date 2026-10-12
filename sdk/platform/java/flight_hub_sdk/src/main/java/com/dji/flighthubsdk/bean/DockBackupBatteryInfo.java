// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场备用电池信息。 */
public final class DockBackupBatteryInfo {

    /** 备用电池开关(0: 备用电池关闭, 1: 备用电池开启); */
    private int switchState;
    /** 备用电池电压(单位: 毫伏, mV); */
    private int voltage;
    /** 备用电池温度(单位: 摄氏度); */
    private double temperature;
    public DockBackupBatteryInfo() {
    }

/**
     * 构造 DockBackupBatteryInfo。
     *
     * @param switchState 切换状态
     * @param voltage 电压
     * @param temperature 温度
     */
    public DockBackupBatteryInfo(int switchState, int voltage, double temperature) {
        this.switchState = switchState;
        this.voltage = voltage;
        this.temperature = temperature;
    }

    /** 备用电池开关(0: 备用电池关闭, 1: 备用电池开启); */
    public int getSwitchState() {
        return switchState;
    }

    /** @param switchState 备用电池开关(0: 备用电池关闭, 1: 备用电池开启); */
    public void setSwitchState(int switchState) {
        this.switchState = switchState;
    }

    /** 备用电池电压(单位: 毫伏, mV); */
    public int getVoltage() {
        return voltage;
    }

    /** @param voltage 备用电池电压(单位: 毫伏, mV); */
    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    /** 备用电池温度(单位: 摄氏度); */
    public double getTemperature() {
        return temperature;
    }

    /** @param temperature 备用电池温度(单位: 摄氏度); */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockBackupBatteryInfo that = (DockBackupBatteryInfo) o;
        return switchState == that.switchState
                && voltage == that.voltage
                && Double.compare(that.temperature, temperature) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchState, voltage, temperature);
    }

    @Override
    public String toString() {
        return "DockBackupBatteryInfo{switchState=" + switchState
                + ", voltage=" + voltage
                + ", temperature=" + temperature + '}';
    }
}
