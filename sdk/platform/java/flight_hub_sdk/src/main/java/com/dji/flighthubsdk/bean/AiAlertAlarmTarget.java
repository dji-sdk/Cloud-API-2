// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI 告警目标阈值配置 */
public class AiAlertAlarmTarget {
    /** 目标类型：0=人 / 1=车 / 2=船 */
    private int targetType;
    /** 检测置信度阈值（整数，如 80 表示 80%） */
    private int targetValue;
    /** 是否启用阈值下限 */
    private boolean useMinThreshold;
    /** 是否启用阈值上限 */
    private boolean useMaxThreshold;
    /** 阈值上限（整数） */
    private int targetMaxThreshold;
    /** 阈值下限（整数） */
    private int targetMinThreshold;

    public AiAlertAlarmTarget() {}

    /** 目标类型：0=人 / 1=车 / 2=船 */
    public int getTargetType() { return targetType; }
    /** @param targetType 目标类型：0=人 / 1=车 / 2=船 */
    public void setTargetType(int v) { this.targetType = v; }

    /** 检测置信度阈值（整数，如 80 表示 80%） */
    public int getTargetValue() { return targetValue; }
    /** @param targetValue 检测置信度阈值（整数，如 80 表示 80%） */
    public void setTargetValue(int v) { this.targetValue = v; }

    /** 是否启用阈值下限 */
    public boolean isUseMinThreshold() { return useMinThreshold; }
    /** @param useMinThreshold 是否启用阈值下限 */
    public void setUseMinThreshold(boolean v) { this.useMinThreshold = v; }

    /** 是否启用阈值上限 */
    public boolean isUseMaxThreshold() { return useMaxThreshold; }
    /** @param useMaxThreshold 是否启用阈值上限 */
    public void setUseMaxThreshold(boolean v) { this.useMaxThreshold = v; }

    /** 阈值上限（整数） */
    public int getTargetMaxThreshold() { return targetMaxThreshold; }
    /** @param targetMaxThreshold 阈值上限（整数） */
    public void setTargetMaxThreshold(int v) { this.targetMaxThreshold = v; }

    /** 阈值下限（整数） */
    public int getTargetMinThreshold() { return targetMinThreshold; }
    /** @param targetMinThreshold 阈值下限（整数） */
    public void setTargetMinThreshold(int v) { this.targetMinThreshold = v; }
}
