// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI 告警定位信息 */
public class AiAlertLocation {
    /** 纬度 */
    private double latitude;
    /** 经度 */
    private double longitude;
    /** 海拔高度，单位米 */
    private double altitude;

    public AiAlertLocation() {}

    /** 纬度 */
    public double getLatitude() { return latitude; }
    /** @param latitude 纬度 */
    public void setLatitude(double v) { this.latitude = v; }

    /** 经度 */
    public double getLongitude() { return longitude; }
    /** @param longitude 经度 */
    public void setLongitude(double v) { this.longitude = v; }

    /** 海拔高度，单位米 */
    public double getAltitude() { return altitude; }
    /** @param altitude 海拔高度，单位米 */
    public void setAltitude(double v) { this.altitude = v; }
}
