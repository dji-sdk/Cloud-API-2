// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 相机激光测距/测量目标。 */
public final class CameraMeasureTarget {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private double altitude;
    private double distance;
    private double latitude;
    private double longitude;
    /** 取值语义同 `flight_hub_sdk::CameraLaserMeasureState`（见 `value.h`） */
    private int laserMeasureState;
    public CameraMeasureTarget() {
    }

/**
     * 构造 CameraMeasureTarget。
     *
     * @param payloadIndex 负载索引
     * @param altitude 目标海拔高度
     * @param distance 测量距离
     * @param latitude 目标纬度
     * @param longitude 目标经度
     * @param laserMeasureState 激光测量状态
     */
    public CameraMeasureTarget(
            String payloadIndex,
            double altitude,
            double distance,
            double latitude,
            double longitude,
            int laserMeasureState) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.altitude = altitude;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.laserMeasureState = laserMeasureState;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 目标海拔高度 */
    public double getAltitude() {
        return altitude;
    }

    /** 目标海拔高度 */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /** 测量距离 */
    public double getDistance() {
        return distance;
    }

    /** 测量距离 */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /** 目标纬度 */
    public double getLatitude() {
        return latitude;
    }

    /** 目标纬度 */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /** 目标经度 */
    public double getLongitude() {
        return longitude;
    }

    /** 目标经度 */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /** 取值语义同 `flight_hub_sdk::CameraLaserMeasureState`（见 `value.h`） */
    public int getLaserMeasureState() {
        return laserMeasureState;
    }

    /** @param laserMeasureState 取值语义同 `flight_hub_sdk::CameraLaserMeasureState`（见 `value.h`） */
    public void setLaserMeasureState(int laserMeasureState) {
        this.laserMeasureState = laserMeasureState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraMeasureTarget that = (CameraMeasureTarget) o;
        return Double.compare(that.altitude, altitude) == 0
                && Double.compare(that.distance, distance) == 0
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && laserMeasureState == that.laserMeasureState
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, altitude, distance, latitude, longitude, laserMeasureState);
    }

    @Override
    public String toString() {
        return "CameraMeasureTarget{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", altitude=" + altitude
                + ", distance=" + distance
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", laserMeasureState=" + laserMeasureState
                + '}';
    }
}
