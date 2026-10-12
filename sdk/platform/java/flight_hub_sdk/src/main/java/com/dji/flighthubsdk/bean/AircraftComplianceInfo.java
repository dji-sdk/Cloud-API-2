// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 合规信息。 */
public final class AircraftComplianceInfo {

    /** int, RID(Remote Identification)工作状态", {1: 正常, 0: 异常}; */
    private int ridState;
    /** string, 国家区域码; */
    private String country;
    public AircraftComplianceInfo() {
    }

/**
     * 构造 AircraftComplianceInfo。
     *
     * @param ridState RID工作状态
     * @param country 国家区域码
     */
    public AircraftComplianceInfo(int ridState, String country) {
        this.ridState = ridState;
        this.country = country != null ? country : "";
    }

    /** int, RID(Remote Identification)工作状态", {1: 正常, 0: 异常}; */
    public int getRidState() {
        return ridState;
    }

    /** @param ridState int, RID(Remote Identification)工作状态", {1: 正常, 0: 异常}; */
    public void setRidState(int ridState) {
        this.ridState = ridState;
    }

    /** string, 国家区域码; */
    public String getCountry() {
        return country;
    }

    /** @param country string, 国家区域码; */
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftComplianceInfo that = (AircraftComplianceInfo) o;
        return ridState == that.ridState && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ridState, country);
    }

    @Override
    public String toString() {
        return "AircraftComplianceInfo{ridState=" + ridState + ", country='" + country + '\'' + '}';
    }
}
