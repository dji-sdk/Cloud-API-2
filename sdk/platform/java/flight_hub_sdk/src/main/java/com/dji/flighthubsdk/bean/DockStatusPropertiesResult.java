// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 属性。 */
public final class DockStatusPropertiesResult {

    private int accTime;
    private double acdcPowerInput;
    private int electricSupplyVoltage;
    private double environmentTemperature;
    private int humidity;
    private int jobNumber;
    private int poeLinkStatus;
    private double poePowerOutput;
    private int rainfall;
    private double temperature;
    private int workingVoltage;
    private DockStatusAirConditionerResult airConditioner;
    private DockStatusAlternateLandPointResult alternateLandPoint;
    private DockStatusBackupBatteryResult backupBattery;
    private DockStatusNetworkStateResult networkState;
    private DockStatusPositionStateResult positionState;
    private DockStatusTiltAngleResult tiltAngle;
    public DockStatusPropertiesResult() {
    }

/**
     * 构造 DockStatusPropertiesResult。
     *
     * @param accTime 累计运行时长(秒)
     * @param acdcPowerInput AC/DC 电源输入
     * @param electricSupplyVoltage 市电电压(V)
     * @param environmentTemperature 环境温度（摄氏度）
     * @param humidity 湿度
     * @param jobNumber 作业次数
     * @param poeLinkStatus PoE 链路状态
     * @param poePowerOutput PoE 功率输出
     * @param rainfall 降雨量
     * @param temperature 舱内温度（摄氏度）
     * @param workingVoltage 工作电压(mV)
     * @param airConditioner 空调状态信息
     * @param alternateLandPoint 备降点信息
     * @param backupBattery 备用电池信息
     * @param networkState 网络状态信息
     * @param positionState 搜星状态信息
     * @param tiltAngle 倾斜角度信息
     */
    public DockStatusPropertiesResult(
            int accTime,
            double acdcPowerInput,
            int electricSupplyVoltage,
            double environmentTemperature,
            int humidity,
            int jobNumber,
            int poeLinkStatus,
            double poePowerOutput,
            int rainfall,
            double temperature,
            int workingVoltage,
            DockStatusAirConditionerResult airConditioner,
            DockStatusAlternateLandPointResult alternateLandPoint,
            DockStatusBackupBatteryResult backupBattery,
            DockStatusNetworkStateResult networkState,
            DockStatusPositionStateResult positionState,
            DockStatusTiltAngleResult tiltAngle) {
        this.accTime = accTime;
        this.acdcPowerInput = acdcPowerInput;
        this.electricSupplyVoltage = electricSupplyVoltage;
        this.environmentTemperature = environmentTemperature;
        this.humidity = humidity;
        this.jobNumber = jobNumber;
        this.poeLinkStatus = poeLinkStatus;
        this.poePowerOutput = poePowerOutput;
        this.rainfall = rainfall;
        this.temperature = temperature;
        this.workingVoltage = workingVoltage;
        this.airConditioner = airConditioner;
        this.alternateLandPoint = alternateLandPoint;
        this.backupBattery = backupBattery;
        this.networkState = networkState;
        this.positionState = positionState;
        this.tiltAngle = tiltAngle;
    }

    /** 累计运行时长(秒) */
    public int getAccTime() {
        return accTime;
    }

    /** 累计运行时长(秒) */
    public void setAccTime(int accTime) {
        this.accTime = accTime;
    }

    /** AC/DC 电源输入 */
    public double getAcdcPowerInput() {
        return acdcPowerInput;
    }

    /** AC/DC 电源输入 */
    public void setAcdcPowerInput(double acdcPowerInput) {
        this.acdcPowerInput = acdcPowerInput;
    }

    /** 市电电压(V) */
    public int getElectricSupplyVoltage() {
        return electricSupplyVoltage;
    }

    /** 市电电压(V) */
    public void setElectricSupplyVoltage(int electricSupplyVoltage) {
        this.electricSupplyVoltage = electricSupplyVoltage;
    }

    /** 环境温度（摄氏度） */
    public double getEnvironmentTemperature() {
        return environmentTemperature;
    }

    /** 环境温度（摄氏度） */
    public void setEnvironmentTemperature(double environmentTemperature) {
        this.environmentTemperature = environmentTemperature;
    }

    /** 湿度 */
    public int getHumidity() {
        return humidity;
    }

    /** 湿度 */
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    /** 作业次数 */
    public int getJobNumber() {
        return jobNumber;
    }

    /** 作业次数 */
    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    /** PoE 链路状态 */
    public int getPoeLinkStatus() {
        return poeLinkStatus;
    }

    /** PoE 链路状态 */
    public void setPoeLinkStatus(int poeLinkStatus) {
        this.poeLinkStatus = poeLinkStatus;
    }

    /** PoE 功率输出 */
    public double getPoePowerOutput() {
        return poePowerOutput;
    }

    /** PoE 功率输出 */
    public void setPoePowerOutput(double poePowerOutput) {
        this.poePowerOutput = poePowerOutput;
    }

    /** 降雨量 */
    public int getRainfall() {
        return rainfall;
    }

    /** 降雨量 */
    public void setRainfall(int rainfall) {
        this.rainfall = rainfall;
    }

    /** 舱内温度（摄氏度） */
    public double getTemperature() {
        return temperature;
    }

    /** 舱内温度（摄氏度） */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /** 工作电压(mV) */
    public int getWorkingVoltage() {
        return workingVoltage;
    }

    /** 工作电压(mV) */
    public void setWorkingVoltage(int workingVoltage) {
        this.workingVoltage = workingVoltage;
    }

    /** 空调状态信息 */
    public DockStatusAirConditionerResult getAirConditioner() {
        return airConditioner;
    }

    /** 空调状态信息 */
    public void setAirConditioner(DockStatusAirConditionerResult airConditioner) {
        this.airConditioner = airConditioner;
    }

    /** 备降点信息 */
    public DockStatusAlternateLandPointResult getAlternateLandPoint() {
        return alternateLandPoint;
    }

    /** 备降点信息 */
    public void setAlternateLandPoint(DockStatusAlternateLandPointResult alternateLandPoint) {
        this.alternateLandPoint = alternateLandPoint;
    }

    /** 备用电池信息 */
    public DockStatusBackupBatteryResult getBackupBattery() {
        return backupBattery;
    }

    /** 备用电池信息 */
    public void setBackupBattery(DockStatusBackupBatteryResult backupBattery) {
        this.backupBattery = backupBattery;
    }

    /** 网络状态信息 */
    public DockStatusNetworkStateResult getNetworkState() {
        return networkState;
    }

    /** 网络状态信息 */
    public void setNetworkState(DockStatusNetworkStateResult networkState) {
        this.networkState = networkState;
    }

    /** 搜星状态信息 */
    public DockStatusPositionStateResult getPositionState() {
        return positionState;
    }

    /** 搜星状态信息 */
    public void setPositionState(DockStatusPositionStateResult positionState) {
        this.positionState = positionState;
    }

    /** 倾斜角度信息 */
    public DockStatusTiltAngleResult getTiltAngle() {
        return tiltAngle;
    }

    /** 倾斜角度信息 */
    public void setTiltAngle(DockStatusTiltAngleResult tiltAngle) {
        this.tiltAngle = tiltAngle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusPropertiesResult that = (DockStatusPropertiesResult) o;
        return accTime == that.accTime
                && Double.compare(that.acdcPowerInput, acdcPowerInput) == 0
                && electricSupplyVoltage == that.electricSupplyVoltage
                && Double.compare(that.environmentTemperature, environmentTemperature) == 0
                && humidity == that.humidity
                && jobNumber == that.jobNumber
                && poeLinkStatus == that.poeLinkStatus
                && Double.compare(that.poePowerOutput, poePowerOutput) == 0
                && rainfall == that.rainfall
                && Double.compare(that.temperature, temperature) == 0
                && workingVoltage == that.workingVoltage
                && Objects.equals(airConditioner, that.airConditioner)
                && Objects.equals(alternateLandPoint, that.alternateLandPoint)
                && Objects.equals(backupBattery, that.backupBattery)
                && Objects.equals(networkState, that.networkState)
                && Objects.equals(positionState, that.positionState)
                && Objects.equals(tiltAngle, that.tiltAngle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                accTime,
                acdcPowerInput,
                electricSupplyVoltage,
                environmentTemperature,
                humidity,
                jobNumber,
                poeLinkStatus,
                poePowerOutput,
                rainfall,
                temperature,
                workingVoltage,
                airConditioner,
                alternateLandPoint,
                backupBattery,
                networkState,
                positionState,
                tiltAngle);
    }

    @Override
    public String toString() {
        return "DockStatusPropertiesResult{"
                + "accTime=" + accTime
                + ", acdcPowerInput=" + acdcPowerInput
                + ", electricSupplyVoltage=" + electricSupplyVoltage
                + ", environmentTemperature=" + environmentTemperature
                + ", humidity=" + humidity
                + ", jobNumber=" + jobNumber
                + ", poeLinkStatus=" + poeLinkStatus
                + ", poePowerOutput=" + poePowerOutput
                + ", rainfall=" + rainfall
                + ", temperature=" + temperature
                + ", workingVoltage=" + workingVoltage
                + ", airConditioner=" + airConditioner
                + ", alternateLandPoint=" + alternateLandPoint
                + ", backupBattery=" + backupBattery
                + ", networkState=" + networkState
                + ", positionState=" + positionState
                + ", tiltAngle=" + tiltAngle
                + '}';
    }
}
