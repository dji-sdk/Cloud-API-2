// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 多机场返航信息。 */
public final class MultiDockHomeInfo {
    private String sn;
    private int planStatus;
    private int estimatedBatteryConsumption;
    private double homeDistance;

    public MultiDockHomeInfo() {
    }

    /**
     * 构造 MultiDockHomeInfo。
     *
     * @param sn 机场序列号
     * @param planStatus 规划状态：0-规划中/失败，1-不可达，2-电量不可达，3-可达
     * @param estimatedBatteryConsumption 预计电量消耗
     * @param homeDistance home 点水平距离
     */
    public MultiDockHomeInfo(String sn, int planStatus, int estimatedBatteryConsumption,
            double homeDistance) {
        this.sn = sn != null ? sn : "";
        this.planStatus = planStatus;
        this.estimatedBatteryConsumption = estimatedBatteryConsumption;
        this.homeDistance = homeDistance;
    }

    /** 机场序列号 */
    public String getSn() { return sn; }
    /** 规划状态：0-规划中/失败，1-不可达，2-电量不可达，3-可达 */
    public int getPlanStatus() { return planStatus; }
    /** 预计电量消耗 */
    public int getEstimatedBatteryConsumption() { return estimatedBatteryConsumption; }
    /** home 点水平距离 */
    public double getHomeDistance() { return homeDistance; }

    /** 机场序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 规划状态：0-规划中/失败，1-不可达，2-电量不可达，3-可达 */
    public void setPlanStatus(int planStatus) {
        this.planStatus = planStatus;
    }

    /** 预计电量消耗 */
    public void setEstimatedBatteryConsumption(int estimatedBatteryConsumption) {
        this.estimatedBatteryConsumption = estimatedBatteryConsumption;
    }

    /** home 点水平距离 */
    public void setHomeDistance(double homeDistance) {
        this.homeDistance = homeDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultiDockHomeInfo that = (MultiDockHomeInfo) o;
        return planStatus == that.planStatus
                && estimatedBatteryConsumption == that.estimatedBatteryConsumption
                && Double.compare(that.homeDistance, homeDistance) == 0
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, planStatus, estimatedBatteryConsumption, homeDistance);
    }

    @Override
    public String toString() {
        return "MultiDockHomeInfo{"
                + "sn='" + sn + '\''
                + ", planStatus=" + planStatus
                + ", estimatedBatteryConsumption=" + estimatedBatteryConsumption
                + ", homeDistance=" + homeDistance
                + '}';
    }
}
