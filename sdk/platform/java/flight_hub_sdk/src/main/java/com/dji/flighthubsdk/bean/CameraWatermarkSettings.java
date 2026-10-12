// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 相机水印设置（DRC {@code drc_camera_state_push.camera_state.camera_watermark_settings}）。 */
public final class CameraWatermarkSettings {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int globalEnable;
    private int droneTypeEnable;
    private int manufacturerEnable;
    private int droneSnEnable;
    private int datetimeEnable;
    private int gpsEnable;
    private int userCustomStringEnable;
    private String userCustomString;
    private int layout;

    public CameraWatermarkSettings() {}
    public CameraWatermarkSettings(String payloadIndex, int globalEnable, int droneTypeEnable,
                                   int manufacturerEnable, int droneSnEnable, int datetimeEnable,
                                   int gpsEnable, int userCustomStringEnable, String userCustomString,
                                   int layout) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.globalEnable = globalEnable;
        this.droneTypeEnable = droneTypeEnable;
        this.manufacturerEnable = manufacturerEnable;
        this.droneSnEnable = droneSnEnable;
        this.datetimeEnable = datetimeEnable;
        this.gpsEnable = gpsEnable;
        this.userCustomStringEnable = userCustomStringEnable;
        this.userCustomString = userCustomString != null ? userCustomString : "";
        this.layout = layout;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public int getGlobalEnable() { return globalEnable; }
    public void setGlobalEnable(int v) { this.globalEnable = v; }
    public int getDroneTypeEnable() { return droneTypeEnable; }
    public void setDroneTypeEnable(int v) { this.droneTypeEnable = v; }
    public int getManufacturerEnable() { return manufacturerEnable; }
    public void setManufacturerEnable(int v) { this.manufacturerEnable = v; }
    public int getDroneSnEnable() { return droneSnEnable; }
    public void setDroneSnEnable(int v) { this.droneSnEnable = v; }
    public int getDatetimeEnable() { return datetimeEnable; }
    public void setDatetimeEnable(int v) { this.datetimeEnable = v; }
    public int getGpsEnable() { return gpsEnable; }
    public void setGpsEnable(int v) { this.gpsEnable = v; }
    public int getUserCustomStringEnable() { return userCustomStringEnable; }
    public void setUserCustomStringEnable(int v) { this.userCustomStringEnable = v; }
    public String getUserCustomString() { return userCustomString; }
    public void setUserCustomString(String v) { this.userCustomString = v; }
    public int getLayout() { return layout; }
    public void setLayout(int v) { this.layout = v; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraWatermarkSettings)) return false;
        CameraWatermarkSettings that = (CameraWatermarkSettings) o;
        return globalEnable == that.globalEnable && droneTypeEnable == that.droneTypeEnable
                && manufacturerEnable == that.manufacturerEnable && droneSnEnable == that.droneSnEnable
                && datetimeEnable == that.datetimeEnable && gpsEnable == that.gpsEnable
                && userCustomStringEnable == that.userCustomStringEnable && layout == that.layout
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(userCustomString, that.userCustomString);
    }
    @Override public int hashCode() {
        return Objects.hash(payloadIndex, globalEnable, droneTypeEnable, manufacturerEnable,
                droneSnEnable, datetimeEnable, gpsEnable, userCustomStringEnable, userCustomString, layout);
    }
    @Override public String toString() {
        return "CameraWatermarkSettings{payloadIndex='" + payloadIndex + "', globalEnable=" + globalEnable
                + ", droneTypeEnable=" + droneTypeEnable + ", manufacturerEnable=" + manufacturerEnable
                + ", droneSnEnable=" + droneSnEnable + ", datetimeEnable=" + datetimeEnable
                + ", gpsEnable=" + gpsEnable + ", userCustomStringEnable=" + userCustomStringEnable
                + ", userCustomString='" + userCustomString + "', layout=" + layout + '}';
    }
}
