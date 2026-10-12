// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞机设备状态 — 单块电池信息 */
public final class DroneStatusBatteryItemResult {

    private int capacityPercent;
    private String firmwareVersion;
    private int highVoltageStorageDays;
    private int index;
    private int loopTimes;
    private String sn;
    private int subType;
    private double temperature;
    private int type;
    private int voltage;
    public DroneStatusBatteryItemResult() {
    }

/**
     * 构造 DroneStatusBatteryItemResult。
     *
     * @param capacityPercent 电池剩余电量百分比
     * @param firmwareVersion 固件版本
     * @param highVoltageStorageDays 高压存储天数
     * @param index 电池序号
     * @param loopTimes 循环次数
     * @param sn 电池序列号(SN)
     * @param subType 电池子类型
     * @param temperature 温度（摄氏度）
     * @param type 电池类型
     * @param voltage 电压(mV)
     */
    public DroneStatusBatteryItemResult(
            int capacityPercent,
            String firmwareVersion,
            int highVoltageStorageDays,
            int index,
            int loopTimes,
            String sn,
            int subType,
            double temperature,
            int type,
            int voltage) {
        this.capacityPercent = capacityPercent;
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.highVoltageStorageDays = highVoltageStorageDays;
        this.index = index;
        this.loopTimes = loopTimes;
        this.sn = sn != null ? sn : "";
        this.subType = subType;
        this.temperature = temperature;
        this.type = type;
        this.voltage = voltage;
    }

    /** 电池剩余电量百分比 */
    public int getCapacityPercent() {
        return capacityPercent;
    }

    /** 电池剩余电量百分比 */
    public void setCapacityPercent(int capacityPercent) {
        this.capacityPercent = capacityPercent;
    }

    /** 固件版本 */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /** 固件版本 */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /** 高压存储天数 */
    public int getHighVoltageStorageDays() {
        return highVoltageStorageDays;
    }

    /** 高压存储天数 */
    public void setHighVoltageStorageDays(int highVoltageStorageDays) {
        this.highVoltageStorageDays = highVoltageStorageDays;
    }

    /** 电池序号 */
    public int getIndex() {
        return index;
    }

    /** 电池序号 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** 循环次数 */
    public int getLoopTimes() {
        return loopTimes;
    }

    /** 循环次数 */
    public void setLoopTimes(int loopTimes) {
        this.loopTimes = loopTimes;
    }

    /** 电池序列号(SN) */
    public String getSn() {
        return sn;
    }

    /** 电池序列号(SN) */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 电池子类型 */
    public int getSubType() {
        return subType;
    }

    /** 电池子类型 */
    public void setSubType(int subType) {
        this.subType = subType;
    }

    /** 温度（摄氏度） */
    public double getTemperature() {
        return temperature;
    }

    /** 温度（摄氏度） */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /** 电池类型 */
    public int getType() {
        return type;
    }

    /** 电池类型 */
    public void setType(int type) {
        this.type = type;
    }

    /** 电压(mV) */
    public int getVoltage() {
        return voltage;
    }

    /** 电压(mV) */
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
        DroneStatusBatteryItemResult that = (DroneStatusBatteryItemResult) o;
        return capacityPercent == that.capacityPercent
                && Objects.equals(firmwareVersion, that.firmwareVersion)
                && highVoltageStorageDays == that.highVoltageStorageDays
                && index == that.index
                && loopTimes == that.loopTimes
                && Objects.equals(sn, that.sn)
                && subType == that.subType
                && Double.compare(that.temperature, temperature) == 0
                && type == that.type
                && voltage == that.voltage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                capacityPercent,
                firmwareVersion,
                highVoltageStorageDays,
                index,
                loopTimes,
                sn,
                subType,
                temperature,
                type,
                voltage);
    }

    @Override
    public String toString() {
        return "DroneStatusBatteryItemResult{"
                + "capacityPercent=" + capacityPercent
                + ", firmwareVersion='" + firmwareVersion + '\''
                + ", highVoltageStorageDays=" + highVoltageStorageDays
                + ", index=" + index
                + ", loopTimes=" + loopTimes
                + ", sn='" + sn + '\''
                + ", subType=" + subType
                + ", temperature=" + temperature
                + ", type=" + type
                + ", voltage=" + voltage
                + '}';
    }
}
