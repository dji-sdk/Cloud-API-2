// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞机设备状态 — 属性。 */
public final class DroneStatusPropertiesResult {

    private int heightLimit;
    private double homeDistance;
    private double homeLatitude;
    private double homeLongitude;
    private int horizontalSpeed;
    private int lowBatteryWarningThreshold;
    private int modeCode;
    private int modeCodeReason;
    private int nightLightsState;
    private int remainingPowerForReturnHome;
    private int seriousLowBatteryWarningThreshold;
    private double totalFlightDistance;
    private int totalFlightSorties;
    private double totalFlightTime;
    private int verticalSpeed;
    private DroneStatusBatteryResult battery;
    private DroneStatusDistanceLimitStatusResult distanceLimitStatus;
    private DroneStatusObstacleAvoidanceResult obstacleAvoidance;
    private DroneStatusPositionStateResult positionState;
    private DroneStatusStorageResult storage;
    public DroneStatusPropertiesResult() {
    }

/**
     * 构造 DroneStatusPropertiesResult。
     */
    public DroneStatusPropertiesResult(
            int heightLimit,
            double homeDistance,
            double homeLatitude,
            double homeLongitude,
            int horizontalSpeed,
            int lowBatteryWarningThreshold,
            int modeCode,
            int modeCodeReason,
            int nightLightsState,
            int remainingPowerForReturnHome,
            int seriousLowBatteryWarningThreshold,
            double totalFlightDistance,
            int totalFlightSorties,
            double totalFlightTime,
            int verticalSpeed,
            DroneStatusBatteryResult battery,
            DroneStatusDistanceLimitStatusResult distanceLimitStatus,
            DroneStatusObstacleAvoidanceResult obstacleAvoidance,
            DroneStatusPositionStateResult positionState,
            DroneStatusStorageResult storage) {
        this.heightLimit = heightLimit;
        this.homeDistance = homeDistance;
        this.homeLatitude = homeLatitude;
        this.homeLongitude = homeLongitude;
        this.horizontalSpeed = horizontalSpeed;
        this.lowBatteryWarningThreshold = lowBatteryWarningThreshold;
        this.modeCode = modeCode;
        this.modeCodeReason = modeCodeReason;
        this.nightLightsState = nightLightsState;
        this.remainingPowerForReturnHome = remainingPowerForReturnHome;
        this.seriousLowBatteryWarningThreshold = seriousLowBatteryWarningThreshold;
        this.totalFlightDistance = totalFlightDistance;
        this.totalFlightSorties = totalFlightSorties;
        this.totalFlightTime = totalFlightTime;
        this.verticalSpeed = verticalSpeed;
        this.battery = battery;
        this.distanceLimitStatus = distanceLimitStatus;
        this.obstacleAvoidance = obstacleAvoidance;
        this.positionState = positionState;
        this.storage = storage;
    }

    /** 限高(m) */
    public int getHeightLimit() {
        return heightLimit;
    }

    /** 限高(m) */
    public void setHeightLimit(int heightLimit) {
        this.heightLimit = heightLimit;
    }

    /** 距起飞点距离(m) */
    public double getHomeDistance() {
        return homeDistance;
    }

    /** 距起飞点距离(m) */
    public void setHomeDistance(double homeDistance) {
        this.homeDistance = homeDistance;
    }

    /** 起飞点纬度 */
    public double getHomeLatitude() {
        return homeLatitude;
    }

    /** 起飞点纬度 */
    public void setHomeLatitude(double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    /** 起飞点经度 */
    public double getHomeLongitude() {
        return homeLongitude;
    }

    /** 起飞点经度 */
    public void setHomeLongitude(double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    /** 水平速度(m/s) */
    public int getHorizontalSpeed() {
        return horizontalSpeed;
    }

    /** 水平速度(m/s) */
    public void setHorizontalSpeed(int horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
    }

    /** 低电量预警阈值 */
    public int getLowBatteryWarningThreshold() {
        return lowBatteryWarningThreshold;
    }

    /** 低电量预警阈值 */
    public void setLowBatteryWarningThreshold(int lowBatteryWarningThreshold) {
        this.lowBatteryWarningThreshold = lowBatteryWarningThreshold;
    }

    /** 飞行模式码 */
    public int getModeCode() {
        return modeCode;
    }

    /** 飞行模式码 */
    public void setModeCode(int modeCode) {
        this.modeCode = modeCode;
    }

    /** 飞行模式码原因 */
    public int getModeCodeReason() {
        return modeCodeReason;
    }

    /** 飞行模式码原因 */
    public void setModeCodeReason(int modeCodeReason) {
        this.modeCodeReason = modeCodeReason;
    }

    /** 夜间灯光状态 */
    public int getNightLightsState() {
        return nightLightsState;
    }

    /** 夜间灯光状态 */
    public void setNightLightsState(int nightLightsState) {
        this.nightLightsState = nightLightsState;
    }

    /** 返航剩余电量 */
    public int getRemainingPowerForReturnHome() {
        return remainingPowerForReturnHome;
    }

    /** 返航剩余电量 */
    public void setRemainingPowerForReturnHome(int remainingPowerForReturnHome) {
        this.remainingPowerForReturnHome = remainingPowerForReturnHome;
    }

    /** 严重低电量预警阈值 */
    public int getSeriousLowBatteryWarningThreshold() {
        return seriousLowBatteryWarningThreshold;
    }

    /** 严重低电量预警阈值 */
    public void setSeriousLowBatteryWarningThreshold(int seriousLowBatteryWarningThreshold) {
        this.seriousLowBatteryWarningThreshold = seriousLowBatteryWarningThreshold;
    }

    /** 总飞行距离(m) */
    public double getTotalFlightDistance() {
        return totalFlightDistance;
    }

    /** 总飞行距离(m) */
    public void setTotalFlightDistance(double totalFlightDistance) {
        this.totalFlightDistance = totalFlightDistance;
    }

    /** 总飞行架次 */
    public int getTotalFlightSorties() {
        return totalFlightSorties;
    }

    /** 总飞行架次 */
    public void setTotalFlightSorties(int totalFlightSorties) {
        this.totalFlightSorties = totalFlightSorties;
    }

    /** 总飞行时间(秒) */
    public double getTotalFlightTime() {
        return totalFlightTime;
    }

    /** 总飞行时间(秒) */
    public void setTotalFlightTime(double totalFlightTime) {
        this.totalFlightTime = totalFlightTime;
    }

    /** 垂直速度(m/s) */
    public int getVerticalSpeed() {
        return verticalSpeed;
    }

    /** 垂直速度(m/s) */
    public void setVerticalSpeed(int verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    /** 电池汇总信息 */
    public DroneStatusBatteryResult getBattery() {
        return battery;
    }

    /** 电池汇总信息 */
    public void setBattery(DroneStatusBatteryResult battery) {
        this.battery = battery;
    }

    /** 距离限制状态 */
    public DroneStatusDistanceLimitStatusResult getDistanceLimitStatus() {
        return distanceLimitStatus;
    }

    /** 距离限制状态 */
    public void setDistanceLimitStatus(DroneStatusDistanceLimitStatusResult distanceLimitStatus) {
        this.distanceLimitStatus = distanceLimitStatus;
    }

    /** 避障状态 */
    public DroneStatusObstacleAvoidanceResult getObstacleAvoidance() {
        return obstacleAvoidance;
    }

    /** 避障状态 */
    public void setObstacleAvoidance(DroneStatusObstacleAvoidanceResult obstacleAvoidance) {
        this.obstacleAvoidance = obstacleAvoidance;
    }

    /** 搜星状态 */
    public DroneStatusPositionStateResult getPositionState() {
        return positionState;
    }

    /** 搜星状态 */
    public void setPositionState(DroneStatusPositionStateResult positionState) {
        this.positionState = positionState;
    }

    /** 存储信息 */
    public DroneStatusStorageResult getStorage() {
        return storage;
    }

    /** 存储信息 */
    public void setStorage(DroneStatusStorageResult storage) {
        this.storage = storage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneStatusPropertiesResult that = (DroneStatusPropertiesResult) o;
        return heightLimit == that.heightLimit
                && Double.compare(that.homeDistance, homeDistance) == 0
                && Double.compare(that.homeLatitude, homeLatitude) == 0
                && Double.compare(that.homeLongitude, homeLongitude) == 0
                && horizontalSpeed == that.horizontalSpeed
                && lowBatteryWarningThreshold == that.lowBatteryWarningThreshold
                && modeCode == that.modeCode
                && modeCodeReason == that.modeCodeReason
                && nightLightsState == that.nightLightsState
                && remainingPowerForReturnHome == that.remainingPowerForReturnHome
                && seriousLowBatteryWarningThreshold == that.seriousLowBatteryWarningThreshold
                && Double.compare(that.totalFlightDistance, totalFlightDistance) == 0
                && totalFlightSorties == that.totalFlightSorties
                && Double.compare(that.totalFlightTime, totalFlightTime) == 0
                && verticalSpeed == that.verticalSpeed
                && Objects.equals(battery, that.battery)
                && Objects.equals(distanceLimitStatus, that.distanceLimitStatus)
                && Objects.equals(obstacleAvoidance, that.obstacleAvoidance)
                && Objects.equals(positionState, that.positionState)
                && Objects.equals(storage, that.storage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                heightLimit,
                homeDistance,
                homeLatitude,
                homeLongitude,
                horizontalSpeed,
                lowBatteryWarningThreshold,
                modeCode,
                modeCodeReason,
                nightLightsState,
                remainingPowerForReturnHome,
                seriousLowBatteryWarningThreshold,
                totalFlightDistance,
                totalFlightSorties,
                totalFlightTime,
                verticalSpeed,
                battery,
                distanceLimitStatus,
                obstacleAvoidance,
                positionState,
                storage);
    }

    @Override
    public String toString() {
        return "DroneStatusPropertiesResult{"
                + "heightLimit=" + heightLimit
                + ", homeDistance=" + homeDistance
                + ", homeLatitude=" + homeLatitude
                + ", homeLongitude=" + homeLongitude
                + ", horizontalSpeed=" + horizontalSpeed
                + ", lowBatteryWarningThreshold=" + lowBatteryWarningThreshold
                + ", modeCode=" + modeCode
                + ", modeCodeReason=" + modeCodeReason
                + ", nightLightsState=" + nightLightsState
                + ", remainingPowerForReturnHome=" + remainingPowerForReturnHome
                + ", seriousLowBatteryWarningThreshold=" + seriousLowBatteryWarningThreshold
                + ", totalFlightDistance=" + totalFlightDistance
                + ", totalFlightSorties=" + totalFlightSorties
                + ", totalFlightTime=" + totalFlightTime
                + ", verticalSpeed=" + verticalSpeed
                + ", battery=" + battery
                + ", distanceLimitStatus=" + distanceLimitStatus
                + ", obstacleAvoidance=" + obstacleAvoidance
                + ", positionState=" + positionState
                + ", storage=" + storage
                + '}';
    }
}
