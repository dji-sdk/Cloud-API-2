// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 设备型号信息 */
public class AIInsideDeviceModel {
    /** 设备型号枚举 Key（如 "EA230"） */
    private String deviceModelKey = "";
    /** 设备域（"dock" 表示机场，"drone" 表示飞机） */
    private String domain = "";
    /** 设备型号类型 */
    private String type = "";
    /** 设备型号子类型 */
    private String subType = "";
    /** 设备型号显示名称 */
    private String name = "";
    /** 设备型号 class */
    private String deviceModelClass = "";
    /** 设备型号开发代号 */
    private String developCode = "";

    public AIInsideDeviceModel() {}

    /** 设备型号枚举 Key（如 "EA230"） */
    public String getDeviceModelKey() { return deviceModelKey; }

    /** @param v 设备型号枚举 Key（如 "EA230"） */
    public void setDeviceModelKey(String v) { this.deviceModelKey = v != null ? v : ""; }

    /** 设备域（"dock" 表示机场，"drone" 表示飞机） */
    public String getDomain() { return domain; }

    /** @param v 设备域（"dock" 表示机场，"drone" 表示飞机） */
    public void setDomain(String v) { this.domain = v != null ? v : ""; }

    /** 设备型号类型 */
    public String getType() { return type; }

    /** @param v 设备型号类型 */
    public void setType(String v) { this.type = v != null ? v : ""; }

    /** 设备型号子类型 */
    public String getSubType() { return subType; }

    /** @param v 设备型号子类型 */
    public void setSubType(String v) { this.subType = v != null ? v : ""; }

    /** 设备型号显示名称 */
    public String getName() { return name; }

    /** @param v 设备型号显示名称 */
    public void setName(String v) { this.name = v != null ? v : ""; }

    /** 设备型号 class */
    public String getDeviceModelClass() { return deviceModelClass; }

    /** @param v 设备型号 class */
    public void setDeviceModelClass(String v) { this.deviceModelClass = v != null ? v : ""; }

    /** 设备型号开发代号 */
    public String getDevelopCode() { return developCode; }

    /** @param v 设备型号开发代号 */
    public void setDevelopCode(String v) { this.developCode = v != null ? v : ""; }
}
