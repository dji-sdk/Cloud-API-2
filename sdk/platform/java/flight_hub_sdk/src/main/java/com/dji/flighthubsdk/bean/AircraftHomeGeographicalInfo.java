// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Home 点地理坐标。 */
public final class AircraftHomeGeographicalInfo {

    /** double, Home点经度, [-180, 180] 度; */
    private double homeLongitude;
    /** double, Home点纬度, [-90, 90] 度; */
    private double homeLatitude;
    public AircraftHomeGeographicalInfo() {
    }

/**
     * 构造 AircraftHomeGeographicalInfo。
     *
     * @param homeLongitude Home点经度
     * @param homeLatitude Home点纬度
     */
    public AircraftHomeGeographicalInfo(double homeLongitude, double homeLatitude) {
        this.homeLongitude = homeLongitude;
        this.homeLatitude = homeLatitude;
    }

    /** double, Home点经度, [-180, 180] 度; */
    public double getHomeLongitude() {
        return homeLongitude;
    }

    /** @param homeLongitude double, Home点经度, [-180, 180] 度; */
    public void setHomeLongitude(double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    /** double, Home点纬度, [-90, 90] 度; */
    public double getHomeLatitude() {
        return homeLatitude;
    }

    /** @param homeLatitude double, Home点纬度, [-90, 90] 度; */
    public void setHomeLatitude(double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftHomeGeographicalInfo that = (AircraftHomeGeographicalInfo) o;
        return Double.compare(that.homeLongitude, homeLongitude) == 0
                && Double.compare(that.homeLatitude, homeLatitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(homeLongitude, homeLatitude);
    }

    @Override
    public String toString() {
        return "AircraftHomeGeographicalInfo{homeLongitude=" + homeLongitude + ", homeLatitude=" + homeLatitude + '}';
    }
}
