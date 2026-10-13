// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** GET /drc/paas/v2/ai-alert/device-setting 请求参数 */
public class GetAiAlertDeviceSettingParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";

    public GetAiAlertDeviceSettingParam() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }
}
