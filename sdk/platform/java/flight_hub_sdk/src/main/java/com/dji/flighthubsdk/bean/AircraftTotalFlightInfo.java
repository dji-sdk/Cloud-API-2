// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 累计飞行信息。 */
public final class AircraftTotalFlightInfo {

    /** double, 总飞行时长(飞行器累计飞行航时); */
    private double totalFlightTime;
    /** double, 总飞行距离; */
    private double totalFlightDistance;
    /** int, 总飞行架次; */
    private int totalFlightSorties;
    public AircraftTotalFlightInfo() {
    }

/**
     * 构造 AircraftTotalFlightInfo。
     *
     * @param totalFlightTime 总飞行时间(秒)
     * @param totalFlightDistance 总飞行距离(m)
     * @param totalFlightSorties 总飞行架次
     */
    public AircraftTotalFlightInfo(double totalFlightTime, double totalFlightDistance, int totalFlightSorties) {
        this.totalFlightTime = totalFlightTime;
        this.totalFlightDistance = totalFlightDistance;
        this.totalFlightSorties = totalFlightSorties;
    }

    /** double, 总飞行时长(飞行器累计飞行航时); */
    public double getTotalFlightTime() {
        return totalFlightTime;
    }

    /** @param totalFlightTime double, 总飞行时长(飞行器累计飞行航时); */
    public void setTotalFlightTime(double totalFlightTime) {
        this.totalFlightTime = totalFlightTime;
    }

    /** double, 总飞行距离; */
    public double getTotalFlightDistance() {
        return totalFlightDistance;
    }

    /** @param totalFlightDistance double, 总飞行距离; */
    public void setTotalFlightDistance(double totalFlightDistance) {
        this.totalFlightDistance = totalFlightDistance;
    }

    /** int, 总飞行架次; */
    public int getTotalFlightSorties() {
        return totalFlightSorties;
    }

    /** @param totalFlightSorties int, 总飞行架次; */
    public void setTotalFlightSorties(int totalFlightSorties) {
        this.totalFlightSorties = totalFlightSorties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftTotalFlightInfo that = (AircraftTotalFlightInfo) o;
        return Double.compare(that.totalFlightTime, totalFlightTime) == 0
                && Double.compare(that.totalFlightDistance, totalFlightDistance) == 0
                && totalFlightSorties == that.totalFlightSorties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalFlightTime, totalFlightDistance, totalFlightSorties);
    }

    @Override
    public String toString() {
        return "AircraftTotalFlightInfo{totalFlightTime=" + totalFlightTime
                + ", totalFlightDistance=" + totalFlightDistance
                + ", totalFlightSorties=" + totalFlightSorties + '}';
    }
}
