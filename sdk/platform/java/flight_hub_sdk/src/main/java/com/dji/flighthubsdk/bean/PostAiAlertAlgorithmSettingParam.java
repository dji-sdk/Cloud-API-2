// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** POST /drc/paas/v2/ai-alert/algorithm-setting 请求参数 */
public class PostAiAlertAlgorithmSettingParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";
    /** 算法类型编号（必填，唯一键之一） */
    private int algorithmType;
    /** 算法名称 */
    private String algorithmName = "";
    /** 目标告警阈值列表 */
    private List<AiAlertAlarmTarget> alarmTargets = new ArrayList<>();

    public PostAiAlertAlgorithmSettingParam() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    public int getAlgorithmType() { return algorithmType; }
    public void setAlgorithmType(int v) { this.algorithmType = v; }

    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String v) { this.algorithmName = v != null ? v : ""; }

    public List<AiAlertAlarmTarget> getAlarmTargets() { return alarmTargets; }
    public void setAlarmTargets(List<AiAlertAlarmTarget> v) { this.alarmTargets = v != null ? v : new ArrayList<>(); }
}
