// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 相机 LookAt 参数 */
public class CameraLookAtParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 是否锁定 (false关闭 true开启) */
    private boolean locked = false;
    /** 高度值 */
    private double height = 0.0;
    /** 纬度 */
    private double latitude = 0.0;
    /** 经度 */
    private double longitude = 0.0;

    public CameraLookAtParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 是否锁定 (false关闭 true开启) */
    public boolean isLocked() { return locked; }
    /** @param locked 是否锁定 (false关闭 true开启) */
    public void setLocked(boolean locked) { this.locked = locked; }
    /** 高度值 */
    public double getHeight() { return height; }
    /** @param height 高度值 */
    public void setHeight(double height) { this.height = height; }
    /** 纬度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 纬度 */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** 经度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 经度 */
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraLookAtParams that = (CameraLookAtParams) o;
        return locked == that.locked
                && Double.compare(height, that.height) == 0
                && Double.compare(latitude, that.latitude) == 0
                && Double.compare(longitude, that.longitude) == 0
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, locked, height, latitude, longitude);
    }

    @Override
    public String toString() {
        return "CameraLookAtParams{payloadIndex='" + payloadIndex + '\'' + ", locked=" + locked + ", height=" + height + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
