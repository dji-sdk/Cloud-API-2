// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI 告警目标识别信息 */
public class AiAlertTargetAlertInfo {
    /** 目标类型：0=人 / 1=车 / 2=船 */
    private int targetType;
    /** 识别置信度（整数） */
    private int targetValue;
    /** 目标标签名称 */
    private String label = "";

    public AiAlertTargetAlertInfo() {}

    /** 目标类型：0=人 / 1=车 / 2=船 */
    public int getTargetType() { return targetType; }
    /** @param targetType 目标类型：0=人 / 1=车 / 2=船 */
    public void setTargetType(int v) { this.targetType = v; }

    /** 识别置信度（整数） */
    public int getTargetValue() { return targetValue; }
    /** @param targetValue 识别置信度（整数） */
    public void setTargetValue(int v) { this.targetValue = v; }

    /** 目标标签名称 */
    public String getLabel() { return label; }
    /** @param label 目标标签名称 */
    public void setLabel(String v) { this.label = v != null ? v : ""; }
}
