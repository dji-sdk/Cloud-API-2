// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.FlightRiskType;
import java.util.Objects;
/** 单条飞行安全风险 */
public final class FlightRiskItem {
    /** 风险类型 */
    private FlightRiskType riskType = FlightRiskType.UNSPECIFIED;
    /** 触发位置纬度 */
    private double latitude;
    /** 触发位置经度 */
    private double longitude;
    /** 触发位置高度（米） */
    private double height;

    public FlightRiskItem() {
    }

    /**
     * 构造 FlightRiskItem。
     *
     * @param riskType 风险类型
     * @param latitude 触发位置纬度
     * @param longitude 触发位置经度
     * @param height 触发位置高度（米）
     */
    public FlightRiskItem(FlightRiskType riskType, double latitude, double longitude, double height) {
        this.riskType = riskType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
    }

    /** 风险类型 */
    public FlightRiskType getRiskType() { return riskType; }
    /** @param riskType 风险类型 */
    public void setRiskType(FlightRiskType riskType) { this.riskType = riskType; }
    /** 触发位置纬度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 触发位置纬度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** 触发位置经度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 触发位置经度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /** 触发位置高度（米） */
    public double getHeight() { return height; }
    /** @param height 触发位置高度（米） */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightRiskItem that = (FlightRiskItem) o;
        return riskType == that.riskType
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(riskType, latitude, longitude, height);
    }

    @Override
    public String toString() {
        return "FlightRiskItem{"
                + "riskType=" + riskType
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + '}';
    }
}
