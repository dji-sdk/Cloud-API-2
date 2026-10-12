// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 设备详情条目（机场 + 关联飞机） */
public class AIInsideDeviceItem {
    /** 机场 SN */
    private String deviceSn = "";
    /** 机场型号信息 */
    private AIInsideDeviceModel deviceModel;
    /** 整机铭牌 SN */
    private String completeMachineSn = "";
    /** 机场是否在线 */
    private boolean deviceOnlineStatus;
    /** 机场工作状态码 */
    private int deviceWorkingStatus;
    /** 机场当前算法操作进度 */
    private AIInsideOperateProgress operateAiModel;
    /** 机场关联的飞机信息 */
    private AIInsideAssociateDrone associateDroneDeviceInfo;

    public AIInsideDeviceItem() {}

    /** 机场 SN */
    public String getDeviceSn() { return deviceSn; }

    /** @param v 机场 SN */
    public void setDeviceSn(String v) { this.deviceSn = v != null ? v : ""; }

    /** 机场型号信息 */
    public AIInsideDeviceModel getDeviceModel() { return deviceModel; }

    /** @param v 机场型号信息 */
    public void setDeviceModel(AIInsideDeviceModel v) { this.deviceModel = v; }

    /** 整机铭牌 SN */
    public String getCompleteMachineSn() { return completeMachineSn; }

    /** @param v 整机铭牌 SN */
    public void setCompleteMachineSn(String v) { this.completeMachineSn = v != null ? v : ""; }

    /** 机场是否在线 */
    public boolean isDeviceOnlineStatus() { return deviceOnlineStatus; }

    /** @param v 机场是否在线 */
    public void setDeviceOnlineStatus(boolean v) { this.deviceOnlineStatus = v; }

    /** 机场工作状态码 */
    public int getDeviceWorkingStatus() { return deviceWorkingStatus; }

    /** @param v 机场工作状态码 */
    public void setDeviceWorkingStatus(int v) { this.deviceWorkingStatus = v; }

    /** 机场当前算法操作进度 */
    public AIInsideOperateProgress getOperateAiModel() { return operateAiModel; }

    /** @param v 机场当前算法操作进度 */
    public void setOperateAiModel(AIInsideOperateProgress v) { this.operateAiModel = v; }

    /** 机场关联的飞机信息 */
    public AIInsideAssociateDrone getAssociateDroneDeviceInfo() { return associateDroneDeviceInfo; }

    /** @param v 机场关联的飞机信息 */
    public void setAssociateDroneDeviceInfo(AIInsideAssociateDrone v) { this.associateDroneDeviceInfo = v; }
}
