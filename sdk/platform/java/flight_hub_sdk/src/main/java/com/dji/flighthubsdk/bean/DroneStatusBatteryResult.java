// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞机设备状态 — 电池汇总 */
public final class DroneStatusBatteryResult {

    private List<DroneStatusBatteryItemResult> batteries;
    private int capacityPercent;
    private int landingPower;
    private int remainFlightTime;
    private int remainJobTime;
    private int returnHomePower;
    public DroneStatusBatteryResult() {
    }

/**
     * 构造 DroneStatusBatteryResult。
     *
     * @param batteries 电池列表
     * @param capacityPercent 电池剩余电量百分比
     * @param landingPower 降落所需电量
     * @param remainFlightTime 剩余飞行时间(秒)
     * @param remainJobTime 剩余作业时间(秒)
     * @param returnHomePower 返航所需电量
     */
    public DroneStatusBatteryResult(
            List<DroneStatusBatteryItemResult> batteries,
            int capacityPercent,
            int landingPower,
            int remainFlightTime,
            int remainJobTime,
            int returnHomePower) {
        this.batteries = batteries != null ? batteries : Collections.emptyList();
        this.capacityPercent = capacityPercent;
        this.landingPower = landingPower;
        this.remainFlightTime = remainFlightTime;
        this.remainJobTime = remainJobTime;
        this.returnHomePower = returnHomePower;
    }

    /** 电池列表 */
    public List<DroneStatusBatteryItemResult> getBatteries() {
        return batteries;
    }

    /** 电池列表 */
    public void setBatteries(List<DroneStatusBatteryItemResult> batteries) {
        this.batteries = batteries;
    }

    /** 电池剩余电量百分比 */
    public int getCapacityPercent() {
        return capacityPercent;
    }

    /** 电池剩余电量百分比 */
    public void setCapacityPercent(int capacityPercent) {
        this.capacityPercent = capacityPercent;
    }

    /** 降落所需电量 */
    public int getLandingPower() {
        return landingPower;
    }

    /** 降落所需电量 */
    public void setLandingPower(int landingPower) {
        this.landingPower = landingPower;
    }

    /** 剩余飞行时间(秒) */
    public int getRemainFlightTime() {
        return remainFlightTime;
    }

    /** 剩余飞行时间(秒) */
    public void setRemainFlightTime(int remainFlightTime) {
        this.remainFlightTime = remainFlightTime;
    }

    /** 剩余作业时间(秒) */
    public int getRemainJobTime() {
        return remainJobTime;
    }

    /** 剩余作业时间(秒) */
    public void setRemainJobTime(int remainJobTime) {
        this.remainJobTime = remainJobTime;
    }

    /** 返航所需电量 */
    public int getReturnHomePower() {
        return returnHomePower;
    }

    /** 返航所需电量 */
    public void setReturnHomePower(int returnHomePower) {
        this.returnHomePower = returnHomePower;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneStatusBatteryResult that = (DroneStatusBatteryResult) o;
        return Objects.equals(batteries, that.batteries)
                && capacityPercent == that.capacityPercent
                && landingPower == that.landingPower
                && remainFlightTime == that.remainFlightTime
                && remainJobTime == that.remainJobTime
                && returnHomePower == that.returnHomePower;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                batteries,
                capacityPercent,
                landingPower,
                remainFlightTime,
                remainJobTime,
                returnHomePower);
    }

    @Override
    public String toString() {
        return "DroneStatusBatteryResult{"
                + "batteries=" + batteries
                + ", capacityPercent=" + capacityPercent
                + ", landingPower=" + landingPower
                + ", remainFlightTime=" + remainFlightTime
                + ", remainJobTime=" + remainJobTime
                + ", returnHomePower=" + returnHomePower
                + '}';
    }
}
