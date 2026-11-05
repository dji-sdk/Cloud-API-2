// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** GET /drc/paas/v2/ai-alert/algorithm-setting 请求参数 */
public class GetAiAlertAlgorithmSettingParam {
    /** 无人机序列号（必填） */
    private String droneSn = "";
    /** 算法类型编号：0=DJI官方智能算法, 1=DJI可见光检测, 2=DJI红外检测, 3=DJI红外温度检测, 5=机载红外算法, 255=机载可见光算法, 100001=云端智能算法, 100002=多模态目标检测 */
    private int algorithmType;

    public GetAiAlertAlgorithmSettingParam() {}

    public String getDroneSn() { return droneSn; }
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    /** 算法类型编号：0=DJI官方智能算法, 1=DJI可见光检测, 2=DJI红外检测, 3=DJI红外温度检测, 5=机载红外算法, 255=机载可见光算法, 100001=云端智能算法, 100002=多模态目标检测 */
    public int getAlgorithmType() { return algorithmType; }

    /** @param v 算法类型编号：0=DJI官方智能算法, 1=DJI可见光检测, 2=DJI红外检测, 3=DJI红外温度检测, 5=机载红外算法, 255=机载可见光算法, 100001=云端智能算法, 100002=多模态目标检测 */
    public void setAlgorithmType(int v) { this.algorithmType = v; }
}
