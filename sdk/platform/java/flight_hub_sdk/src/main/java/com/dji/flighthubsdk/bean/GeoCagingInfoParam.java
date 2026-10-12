// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** Geo Caging 围栏信息参数 */
public class GeoCagingInfoParam {
    /** 飞行地理围栏经度 */
    private double fgLongitude;
    /** 飞行地理围栏纬度 */
    private double fgLatitude;
    /** 飞行地理围栏半径（米） */
    private int fgRadius;
    /** 飞行地理围栏高度（米） */
    private int fgHeight;
    /** 自定义电子围栏经度 */
    private double cvLongitude;
    /** 自定义电子围栏纬度 */
    private double cvLatitude;
    /** 自定义电子围栏半径（米） */
    private int cvRadius;
    /** 自定义电子围栏高度（米） */
    private int cvHeight;

    public GeoCagingInfoParam() {
    }


    /** 飞行地理围栏经度 */
    public double getFgLongitude() { return fgLongitude; }
    /** @param fgLongitude 飞行地理围栏经度 */
    public void setFgLongitude(double fgLongitude) { this.fgLongitude = fgLongitude; }
    /** 飞行地理围栏纬度 */
    public double getFgLatitude() { return fgLatitude; }
    /** @param fgLatitude 飞行地理围栏纬度 */
    public void setFgLatitude(double fgLatitude) { this.fgLatitude = fgLatitude; }
    /** 飞行地理围栏半径（米） */
    public int getFgRadius() { return fgRadius; }
    /** @param fgRadius 飞行地理围栏半径（米） */
    public void setFgRadius(int fgRadius) { this.fgRadius = fgRadius; }
    /** 飞行地理围栏高度（米） */
    public int getFgHeight() { return fgHeight; }
    /** @param fgHeight 飞行地理围栏高度（米） */
    public void setFgHeight(int fgHeight) { this.fgHeight = fgHeight; }
    /** 自定义电子围栏经度 */
    public double getCvLongitude() { return cvLongitude; }
    /** @param cvLongitude 自定义电子围栏经度 */
    public void setCvLongitude(double cvLongitude) { this.cvLongitude = cvLongitude; }
    /** 自定义电子围栏纬度 */
    public double getCvLatitude() { return cvLatitude; }
    /** @param cvLatitude 自定义电子围栏纬度 */
    public void setCvLatitude(double cvLatitude) { this.cvLatitude = cvLatitude; }
    /** 自定义电子围栏半径（米） */
    public int getCvRadius() { return cvRadius; }
    /** @param cvRadius 自定义电子围栏半径（米） */
    public void setCvRadius(int cvRadius) { this.cvRadius = cvRadius; }
    /** 自定义电子围栏高度（米） */
    public int getCvHeight() { return cvHeight; }
    /** @param cvHeight 自定义电子围栏高度（米） */
    public void setCvHeight(int cvHeight) { this.cvHeight = cvHeight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoCagingInfoParam that = (GeoCagingInfoParam) o;
        return Double.compare(that.fgLongitude, fgLongitude) == 0
                && Double.compare(that.fgLatitude, fgLatitude) == 0
                && fgRadius == that.fgRadius
                && fgHeight == that.fgHeight
                && Double.compare(that.cvLongitude, cvLongitude) == 0
                && Double.compare(that.cvLatitude, cvLatitude) == 0
                && cvRadius == that.cvRadius
                && cvHeight == that.cvHeight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fgLongitude,
                fgLatitude,
                fgRadius,
                fgHeight,
                cvLongitude,
                cvLatitude,
                cvRadius,
                cvHeight);
    }

    @Override
    public String toString() {
        return "GeoCagingInfoParam{"
                + "fgLongitude=" + fgLongitude
                + ", fgLatitude=" + fgLatitude
                + ", fgRadius=" + fgRadius
                + ", fgHeight=" + fgHeight
                + ", cvLongitude=" + cvLongitude
                + ", cvLatitude=" + cvLatitude
                + ", cvRadius=" + cvRadius
                + ", cvHeight=" + cvHeight
                + '}';
    }
}
