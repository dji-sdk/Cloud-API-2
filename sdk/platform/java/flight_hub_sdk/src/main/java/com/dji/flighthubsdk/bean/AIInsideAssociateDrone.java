// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 机场关联的飞机信息 */
public class AIInsideAssociateDrone {
    /** 飞机 SN */
    private String deviceSn = "";
    /** 飞机型号信息 */
    private AIInsideDeviceModel deviceModel;
    /** 飞机是否在线 */
    private boolean deviceOnlineStatus;
    /** 飞机工作状态码 */
    private int deviceWorkingStatus;
    /** 飞机当前算法装载状态 */
    private AIInsideModelInfo aiModelInfo;

    public AIInsideAssociateDrone() {}

    /** 飞机 SN */
    public String getDeviceSn() { return deviceSn; }

    /** @param v 飞机 SN */
    public void setDeviceSn(String v) { this.deviceSn = v != null ? v : ""; }

    /** 飞机型号信息 */
    public AIInsideDeviceModel getDeviceModel() { return deviceModel; }

    /** @param v 飞机型号信息 */
    public void setDeviceModel(AIInsideDeviceModel v) { this.deviceModel = v; }

    /** 飞机是否在线 */
    public boolean isDeviceOnlineStatus() { return deviceOnlineStatus; }

    /** @param v 飞机是否在线 */
    public void setDeviceOnlineStatus(boolean v) { this.deviceOnlineStatus = v; }

    /** 飞机工作状态码 */
    public int getDeviceWorkingStatus() { return deviceWorkingStatus; }

    /** @param v 飞机工作状态码 */
    public void setDeviceWorkingStatus(int v) { this.deviceWorkingStatus = v; }

    /** 飞机当前算法装载状态 */
    public AIInsideModelInfo getAiModelInfo() { return aiModelInfo; }

    /** @param v 飞机当前算法装载状态 */
    public void setAiModelInfo(AIInsideModelInfo v) { this.aiModelInfo = v; }
}
