// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** GET /drc/paas/v2/ai-alert/algorithm-setting 请求参数 */
public class GetAiAlertAlgorithmSettingParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";
    /** 算法类型编号 */
    private int algorithmType;

    public GetAiAlertAlgorithmSettingParam() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    public int getAlgorithmType() { return algorithmType; }
    public void setAlgorithmType(int v) { this.algorithmType = v; }
}
