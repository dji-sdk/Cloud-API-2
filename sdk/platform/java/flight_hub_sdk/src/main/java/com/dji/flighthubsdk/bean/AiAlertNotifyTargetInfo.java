// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/**
 * AI 告警推送 — 单条告警记录中的目标识别信息。
 *
 * <p>对应 WS topic {@code alert_records.alert_records_notify} 推送中
 * {@code target_alert_infos} 数组元素。
 */
public class AiAlertNotifyTargetInfo {

    /** 目标类型 */
    private int targetType;
    /** 目标置信度值 */
    private double targetValue;
    /** 是否启用最小阈值 */
    private boolean useMinThreshold;
    /** 是否启用最大阈值 */
    private boolean useMaxThreshold;
    /** 目标最大阈值 */
    private double targetMaxThreshold;
    /** 目标最小阈值 */
    private double targetMinThreshold;
    /** 目标标签名称 */
    private String label = "";

    public AiAlertNotifyTargetInfo() {}

    /** @return 目标类型 */
    public int getTargetType() { return targetType; }
    /** @param v 目标类型 */
    public void setTargetType(int v) { this.targetType = v; }

    /** @return 目标置信度值 */
    public double getTargetValue() { return targetValue; }
    /** @param v 目标置信度值 */
    public void setTargetValue(double v) { this.targetValue = v; }

    /** @return 是否启用最小阈值 */
    public boolean isUseMinThreshold() { return useMinThreshold; }
    /** @param v 是否启用最小阈值 */
    public void setUseMinThreshold(boolean v) { this.useMinThreshold = v; }

    /** @return 是否启用最大阈值 */
    public boolean isUseMaxThreshold() { return useMaxThreshold; }
    /** @param v 是否启用最大阈值 */
    public void setUseMaxThreshold(boolean v) { this.useMaxThreshold = v; }

    /** @return 目标最大阈值 */
    public double getTargetMaxThreshold() { return targetMaxThreshold; }
    /** @param v 目标最大阈值 */
    public void setTargetMaxThreshold(double v) { this.targetMaxThreshold = v; }

    /** @return 目标最小阈值 */
    public double getTargetMinThreshold() { return targetMinThreshold; }
    /** @param v 目标最小阈值 */
    public void setTargetMinThreshold(double v) { this.targetMinThreshold = v; }

    /** @return 目标标签名称 */
    public String getLabel() { return label; }
    /** @param v 目标标签名称 */
    public void setLabel(String v) { this.label = v != null ? v : ""; }
}
