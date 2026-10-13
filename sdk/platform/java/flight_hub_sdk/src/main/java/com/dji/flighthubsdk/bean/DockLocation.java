// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场位置。 */
public final class DockLocation {

    /** 朝向 */
    private double heading;
    /** 高度 */
    private double height;
    /** 纬度 */
    private double latitude;
    /** 经度 */
    private double longitude;
    public DockLocation() {
    }

/**
     * 构造 DockLocation。
     *
     * @param heading 航向角
     * @param height 高度
     * @param latitude 纬度
     * @param longitude 经度
     */
    public DockLocation(double heading, double height, double latitude, double longitude) {
        this.heading = heading;
        this.height = height;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** 朝向 */
    public double getHeading() {
        return heading;
    }

    /** @param heading 朝向 */
    public void setHeading(double heading) {
        this.heading = heading;
    }

    /** 高度 */
    public double getHeight() {
        return height;
    }

    /** 高度 */
    public void setHeight(double height) {
        this.height = height;
    }

    /** 纬度 */
    public double getLatitude() {
        return latitude;
    }

    /** 纬度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** 经度 */
    public double getLongitude() {
        return longitude;
    }

    /** 经度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockLocation that = (DockLocation) o;
        return Double.compare(that.heading, heading) == 0
                && Double.compare(that.height, height) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(heading, height, latitude, longitude);
    }

    @Override
    public String toString() {
        return "DockLocation{heading=" + heading + ", height=" + height + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
