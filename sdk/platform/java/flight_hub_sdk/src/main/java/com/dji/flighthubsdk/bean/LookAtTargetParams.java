// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 云台朝向目标点参数 */
public class LookAtTargetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    private double latitude;
    private double longitude;
    private double height;

    public LookAtTargetParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 纬度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 纬度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** 经度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 经度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /** 高度 */
    public double getHeight() { return height; }
    /** @param height 高度 */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LookAtTargetParams that = (LookAtTargetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, latitude, longitude, height);
    }

    @Override
    public String toString() {
        return "LookAtTargetParams{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", height=" + height
                + '}';
    }
}
