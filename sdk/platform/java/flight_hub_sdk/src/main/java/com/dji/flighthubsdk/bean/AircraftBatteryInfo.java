// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 飞机汇总电量信息；{@link #getBatteries()} 为各电芯/电池条目。
 */
public final class AircraftBatteryInfo {

    private int capacityPercent;
    private int landingPower;
    private int remainFlightTime;
    private int returnHomePower;
    private List<BatteryInfo> batteries;
    public AircraftBatteryInfo() {
    }

/**
     * 构造 AircraftBatteryInfo。
     *
     * @param capacityPercent 电量百分比
     * @param landingPower 降落电量
     * @param remainFlightTime 剩余飞行时间
     * @param returnHomePower 返航电量
     * @param batteries 电池列表
     */
    public AircraftBatteryInfo(
            int capacityPercent,
            int landingPower,
            int remainFlightTime,
            int returnHomePower,
            List<BatteryInfo> batteries) {
        this.capacityPercent = capacityPercent;
        this.landingPower = landingPower;
        this.remainFlightTime = remainFlightTime;
        this.returnHomePower = returnHomePower;
        this.batteries = batteries != null ? batteries : Collections.emptyList();
    }

    /** 电量百分比 */
    public int getCapacityPercent() {
        return capacityPercent;
    }

    /** 电量百分比 */
    public void setCapacityPercent(int capacityPercent) {
        this.capacityPercent = capacityPercent;
    }

    /** 降落电量 */
    public int getLandingPower() {
        return landingPower;
    }

    /** 降落电量 */
    public void setLandingPower(int landingPower) {
        this.landingPower = landingPower;
    }

    /** 剩余飞行时间 */
    public int getRemainFlightTime() {
        return remainFlightTime;
    }

    /** 剩余飞行时间 */
    public void setRemainFlightTime(int remainFlightTime) {
        this.remainFlightTime = remainFlightTime;
    }

    /** 返航电量 */
    public int getReturnHomePower() {
        return returnHomePower;
    }

    /** 返航电量 */
    public void setReturnHomePower(int returnHomePower) {
        this.returnHomePower = returnHomePower;
    }

    /** 电池列表 */
    public List<BatteryInfo> getBatteries() {
        return batteries;
    }

    /** 电池列表 */
    public void setBatteries(List<BatteryInfo> batteries) {
        this.batteries = batteries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftBatteryInfo that = (AircraftBatteryInfo) o;
        return capacityPercent == that.capacityPercent
                && landingPower == that.landingPower
                && remainFlightTime == that.remainFlightTime
                && returnHomePower == that.returnHomePower
                && Objects.equals(batteries, that.batteries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capacityPercent, landingPower, remainFlightTime, returnHomePower, batteries);
    }

    @Override
    public String toString() {
        return "AircraftBatteryInfo{capacityPercent=" + capacityPercent + ", batteries.size=" + batteries.size() + '}';
    }
}
