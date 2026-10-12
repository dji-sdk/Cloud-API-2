// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** GET /drc/paas/v2/ai-alert/device-setting 响应 */
public class GetAiAlertDeviceSettingResult {
    /** 无人机序列号 */
    private String droneSn = "";
    /** 是否开启目标检测告警 */
    private boolean enableTargetAlert;
    /** 告警方式与间隔配置 */
    private AiAlertAlarmInfo alarmInfo;

    public GetAiAlertDeviceSettingResult() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    public boolean isEnableTargetAlert() { return enableTargetAlert; }
    public void setEnableTargetAlert(boolean v) { this.enableTargetAlert = v; }

    public AiAlertAlarmInfo getAlarmInfo() { return alarmInfo; }
    public void setAlarmInfo(AiAlertAlarmInfo v) { this.alarmInfo = v; }
}
