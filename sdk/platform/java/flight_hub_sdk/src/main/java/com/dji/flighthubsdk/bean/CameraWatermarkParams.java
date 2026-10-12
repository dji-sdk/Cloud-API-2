// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 相机水印设置参数 */
public class CameraWatermarkParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 全局启用 */
    private int globalEnable = 0;
    /** 无人机类型启用 */
    private int droneTypeEnable = 0;
    /** 制造商启用 */
    private int manufacturerEnable = 0;
    private int droneSNEnable = 0;
    /** 日期时间启用 */
    private int datetimeEnable = 0;
    /** GPS启用 */
    private int gpsEnable = 0;
    /** 用户自定义字符串启用 */
    private int userCustomStringEnable = 0;
    /** 用户自定义字符串 */
    private String userCustomString = "";
    /** 布局 */
    private int layout = 0;

    public CameraWatermarkParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 全局启用 */
    public int getGlobalEnable() { return globalEnable; }
    /** @param globalEnable 全局启用 */
    public void setGlobalEnable(int globalEnable) { this.globalEnable = globalEnable; }
    /** 无人机类型启用 */
    public int getDroneTypeEnable() { return droneTypeEnable; }
    /** @param droneTypeEnable 无人机类型启用 */
    public void setDroneTypeEnable(int droneTypeEnable) { this.droneTypeEnable = droneTypeEnable; }
    /** 制造商启用 */
    public int getManufacturerEnable() { return manufacturerEnable; }
    /** @param manufacturerEnable 制造商启用 */
    public void setManufacturerEnable(int manufacturerEnable) { this.manufacturerEnable = manufacturerEnable; }
    /** 无人机SN水印开关 */
    public int getDroneSNEnable() { return droneSNEnable; }
    /** @param droneSNEnable 无人机SN水印开关 */
    public void setDroneSNEnable(int droneSNEnable) { this.droneSNEnable = droneSNEnable; }
    /** 日期时间启用 */
    public int getDatetimeEnable() { return datetimeEnable; }
    /** @param datetimeEnable 日期时间启用 */
    public void setDatetimeEnable(int datetimeEnable) { this.datetimeEnable = datetimeEnable; }
    /** GPS启用 */
    public int getGpsEnable() { return gpsEnable; }
    /** @param gpsEnable GPS启用 */
    public void setGpsEnable(int gpsEnable) { this.gpsEnable = gpsEnable; }
    /** 用户自定义字符串启用 */
    public int getUserCustomStringEnable() { return userCustomStringEnable; }
    /** @param userCustomStringEnable 用户自定义字符串启用 */
    public void setUserCustomStringEnable(int userCustomStringEnable) { this.userCustomStringEnable = userCustomStringEnable; }
    /** 用户自定义字符串 */
    public String getUserCustomString() { return userCustomString; }
    /** @param userCustomString 用户自定义字符串 */
    public void setUserCustomString(String userCustomString) { this.userCustomString = userCustomString != null ? userCustomString : ""; }
    /** 布局 */
    public int getLayout() { return layout; }
    /** @param layout 布局 */
    public void setLayout(int layout) { this.layout = layout; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraWatermarkParams that = (CameraWatermarkParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && globalEnable == that.globalEnable
                && droneTypeEnable == that.droneTypeEnable
                && manufacturerEnable == that.manufacturerEnable
                && droneSNEnable == that.droneSNEnable
                && datetimeEnable == that.datetimeEnable
                && gpsEnable == that.gpsEnable
                && userCustomStringEnable == that.userCustomStringEnable
                && Objects.equals(userCustomString, that.userCustomString)
                && layout == that.layout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, globalEnable, droneTypeEnable, manufacturerEnable, droneSNEnable, datetimeEnable, gpsEnable, userCustomStringEnable, userCustomString, layout);
    }

    @Override
    public String toString() {
        return "CameraWatermarkParams{payloadIndex='" + payloadIndex + '\'' + ", globalEnable=" + globalEnable + ", droneTypeEnable=" + droneTypeEnable + ", manufacturerEnable=" + manufacturerEnable + ", droneSNEnable=" + droneSNEnable + ", datetimeEnable=" + datetimeEnable + ", gpsEnable=" + gpsEnable + ", userCustomStringEnable=" + userCustomStringEnable + ", userCustomString='" + userCustomString + '\'' + ", layout=" + layout + '}';
    }
}
