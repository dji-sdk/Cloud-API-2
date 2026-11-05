// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** POST /drc/paas/v2/ai-alert/algorithm-setting 请求参数 */
public class PostAiAlertAlgorithmSettingParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";
    /** 算法类型编号（必填，唯一键之一）：0=DJI官方智能算法, 1=DJI可见光检测, 2=DJI红外检测, 3=DJI红外温度检测, 5=机载红外算法, 255=机载可见光算法, 100001=云端智能算法, 100002=多模态目标检测 */
    private int algorithmType;
    /** 算法名称 */
    private String algorithmName = "";
    /** 目标告警阈值列表 */
    private List<AiAlertAlarmTarget> alarmTargets = new ArrayList<>();

    public PostAiAlertAlgorithmSettingParam() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    /** 算法类型编号（必填，唯一键之一）：0=DJI官方智能算法, 1=DJI可见光检测, 2=DJI红外检测, 3=DJI红外温度检测, 5=机载红外算法, 255=机载可见光算法, 100001=云端智能算法, 100002=多模态目标检测 */
    public int getAlgorithmType() { return algorithmType; }

    /** @param v 算法类型编号（必填，唯一键之一）：0=DJI官方智能算法, 1=DJI可见光检测, 2=DJI红外检测, 3=DJI红外温度检测, 5=机载红外算法, 255=机载可见光算法, 100001=云端智能算法, 100002=多模态目标检测 */
    public void setAlgorithmType(int v) { this.algorithmType = v; }

    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String v) { this.algorithmName = v != null ? v : ""; }

    public List<AiAlertAlarmTarget> getAlarmTargets() { return alarmTargets; }
    public void setAlarmTargets(List<AiAlertAlarmTarget> v) { this.alarmTargets = v != null ? v : new ArrayList<>(); }
}
