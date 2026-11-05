// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI 告警目标识别信息 */
public class AiAlertTargetAlertInfo {
    /** 目标类型：0=人 / 1=车 / 2=船 */
    private int targetType;
    /** 识别置信度（double；旧版本为 int，升级后需重新编译） */
    private double targetValue;
    /** 是否启用最小阈值 */
    private boolean useMinThreshold;
    /** 是否启用最大阈值 */
    private boolean useMaxThreshold;
    /** 最大阈值 */
    private double targetMaxThreshold;
    /** 最小阈值 */
    private double targetMinThreshold;
    /** 目标标签名称 */
    private String label = "";

    public AiAlertTargetAlertInfo() {}

    /** 目标类型：0=人 / 1=车 / 2=船 */
    public int getTargetType() { return targetType; }
    /** @param targetType 目标类型：0=人 / 1=车 / 2=船 */
    public void setTargetType(int v) { this.targetType = v; }

    /** 识别置信度（double；旧版本为 int，升级后需重新编译） */
    public double getTargetValue() { return targetValue; }
    /** @param targetValue 识别置信度（double；旧版本为 int，升级后需重新编译） */
    public void setTargetValue(double v) { this.targetValue = v; }

    /** 目标标签名称 */
    public String getLabel() { return label; }
    /** @param label 目标标签名称 */
    public void setLabel(String v) { this.label = v != null ? v : ""; }

    /** 是否启用最小阈值 */
    public boolean isUseMinThreshold() { return useMinThreshold; }
    /** @param useMinThreshold 是否启用最小阈值 */
    public void setUseMinThreshold(boolean v) { this.useMinThreshold = v; }

    /** 是否启用最大阈值 */
    public boolean isUseMaxThreshold() { return useMaxThreshold; }
    /** @param useMaxThreshold 是否启用最大阈值 */
    public void setUseMaxThreshold(boolean v) { this.useMaxThreshold = v; }

    /** 最大阈值 */
    public double getTargetMaxThreshold() { return targetMaxThreshold; }
    /** @param targetMaxThreshold 最大阈值 */
    public void setTargetMaxThreshold(double v) { this.targetMaxThreshold = v; }

    /** 最小阈值 */
    public double getTargetMinThreshold() { return targetMinThreshold; }
    /** @param targetMinThreshold 最小阈值 */
    public void setTargetMinThreshold(double v) { this.targetMinThreshold = v; }
}
