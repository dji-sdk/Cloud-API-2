// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** POST /drc/paas/v2/ai-alert/device-setting 请求参数 */
public class PostAiAlertDeviceSettingParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";
    /** 是否开启目标检测告警 */
    private boolean enableTargetAlert;
    /** 告警方式与间隔配置 */
    private AiAlertAlarmInfo alarmInfo;

    public PostAiAlertDeviceSettingParam() {}

    /** 无人机序列号（必填） */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 无人机序列号（必填） */
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    /** 是否开启目标检测告警 */
    public boolean isEnableTargetAlert() { return enableTargetAlert; }
    /** @param enableTargetAlert 是否开启目标检测告警 */
    public void setEnableTargetAlert(boolean v) { this.enableTargetAlert = v; }

    /** 告警方式与间隔配置 */
    public AiAlertAlarmInfo getAlarmInfo() { return alarmInfo; }
    /** @param alarmInfo 告警方式与间隔配置 */
    public void setAlarmInfo(AiAlertAlarmInfo v) { this.alarmInfo = v; }
}
