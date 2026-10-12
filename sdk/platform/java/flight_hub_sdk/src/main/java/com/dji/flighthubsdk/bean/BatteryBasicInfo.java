// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 电池基础信息；
 * 用于 {@link DockDroneBatteryMaintenanceInfo#getBatteries()} 等元素。
 */
public final class BatteryBasicInfo {

    /** int, 电池序号, 0:左电池; 1:右电池； */
    private int index;
    /** int, 电池剩余电量,[0,100]; */
    private int capacityPercent;
    /** int, 电压(mV), 参考[0~28000mV]; */
    private int voltage;
    /** double, 温度(摄氏度); */
    private double temperature;
    public BatteryBasicInfo() {
    }

/**
     * 构造 BatteryBasicInfo。
     *
     * @param index 电池序号（0:左电池, 1:右电池）
     * @param capacityPercent 电池剩余电量百分比 [0,100]
     * @param voltage 电压(mV)
     * @param temperature 温度（摄氏度）
     */
    public BatteryBasicInfo(int index, int capacityPercent, int voltage, double temperature) {
        this.index = index;
        this.capacityPercent = capacityPercent;
        this.voltage = voltage;
        this.temperature = temperature;
    }

    /** int, 电池序号, 0:左电池; 1:右电池； */
    public int getIndex() {
        return index;
    }

    /** @param index int, 电池序号, 0:左电池; 1:右电池； */
    public void setIndex(int index) {
        this.index = index;
    }

    /** int, 电池剩余电量,[0,100]; */
    public int getCapacityPercent() {
        return capacityPercent;
    }

    /** @param capacityPercent int, 电池剩余电量,[0,100]; */
    public void setCapacityPercent(int capacityPercent) {
        this.capacityPercent = capacityPercent;
    }

    /** int, 电压(mV), 参考[0~28000mV]; */
    public int getVoltage() {
        return voltage;
    }

    /** @param voltage int, 电压(mV), 参考[0~28000mV]; */
    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    /** double, 温度(摄氏度); */
    public double getTemperature() {
        return temperature;
    }

    /** @param temperature double, 温度(摄氏度); */
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
        BatteryBasicInfo that = (BatteryBasicInfo) o;
        return index == that.index
                && capacityPercent == that.capacityPercent
                && voltage == that.voltage
                && Double.compare(that.temperature, temperature) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, capacityPercent, voltage, temperature);
    }

    @Override
    public String toString() {
        return "BatteryBasicInfo{index=" + index
                + ", capacityPercent=" + capacityPercent
                + ", voltage=" + voltage
                + ", temperature=" + temperature + '}';
    }
}
