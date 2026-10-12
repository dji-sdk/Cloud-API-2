// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 设备型号信息 */
public class AIInsideDeviceModel {
    /** 设备型号枚举 Key（如 "EA230"） */
    private String deviceModelKey = "";
    /** 设备域（"dock" 表示机场，"drone" 表示飞机） */
    private String domain = "";
    /** 设备型号显示名称 */
    private String name = "";

    public AIInsideDeviceModel() {}

    /** 设备型号枚举 Key（如 "EA230"） */
    public String getDeviceModelKey() { return deviceModelKey; }

    /** @param v 设备型号枚举 Key（如 "EA230"） */
    public void setDeviceModelKey(String v) { this.deviceModelKey = v != null ? v : ""; }

    /** 设备域（"dock" 表示机场，"drone" 表示飞机） */
    public String getDomain() { return domain; }

    /** @param v 设备域（"dock" 表示机场，"drone" 表示飞机） */
    public void setDomain(String v) { this.domain = v != null ? v : ""; }

    /** 设备型号显示名称 */
    public String getName() { return name; }

    /** @param v 设备型号显示名称 */
    public void setName(String v) { this.name = v != null ? v : ""; }
}
