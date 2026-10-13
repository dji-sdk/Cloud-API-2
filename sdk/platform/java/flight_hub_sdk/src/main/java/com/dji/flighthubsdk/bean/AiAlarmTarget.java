// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** AI 告警目标配置 */
public class AiAlarmTarget {
    /** 告警目标类型: 0 人, 1 车, 2 船, 3 绝对温度点, 4 移动目标, 128~137 自定义 */
    private int targetType = 0;
    /** 告警目标数值 */
    private double targetValue = 0.0;
    /** 是否使用 target_min_threshold */
    private boolean useMinThreshold = false;
    /** 是否使用 target_max_threshold */
    private boolean useMaxThreshold = false;
    /** 告警目标最小阈值 */
    private double targetMinThreshold = 0.0;
    /** 告警目标最大阈值 */
    private double targetMaxThreshold = 0.0;

    public AiAlarmTarget() {
    }

    /** 告警目标类型: 0 人, 1 车, 2 船, 3 绝对温度点, 4 移动目标, 128~137 自定义 */
    public int getTargetType() { return targetType; }
    /** @param targetType 告警目标类型: 0 人, 1 车, 2 船, 3 绝对温度点, 4 移动目标, 128~137 自定义 */
    public void setTargetType(int targetType) { this.targetType = targetType; }
    /** 告警目标数值 */
    public double getTargetValue() { return targetValue; }
    /** @param targetValue 告警目标数值 */
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }
    /** 是否使用 target_min_threshold */
    public boolean isUseMinThreshold() { return useMinThreshold; }
    /** @param useMinThreshold 是否使用 target_min_threshold */
    public void setUseMinThreshold(boolean useMinThreshold) { this.useMinThreshold = useMinThreshold; }
    /** 是否使用 target_max_threshold */
    public boolean isUseMaxThreshold() { return useMaxThreshold; }
    /** @param useMaxThreshold 是否使用 target_max_threshold */
    public void setUseMaxThreshold(boolean useMaxThreshold) { this.useMaxThreshold = useMaxThreshold; }
    /** 告警目标最小阈值 */
    public double getTargetMinThreshold() { return targetMinThreshold; }
    /** @param targetMinThreshold 告警目标最小阈值 */
    public void setTargetMinThreshold(double targetMinThreshold) { this.targetMinThreshold = targetMinThreshold; }
    /** 告警目标最大阈值 */
    public double getTargetMaxThreshold() { return targetMaxThreshold; }
    /** @param targetMaxThreshold 告警目标最大阈值 */
    public void setTargetMaxThreshold(double targetMaxThreshold) { this.targetMaxThreshold = targetMaxThreshold; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiAlarmTarget that = (AiAlarmTarget) o;
        return targetType == that.targetType
                && Double.compare(targetValue, that.targetValue) == 0
                && useMinThreshold == that.useMinThreshold
                && useMaxThreshold == that.useMaxThreshold
                && Double.compare(targetMinThreshold, that.targetMinThreshold) == 0
                && Double.compare(targetMaxThreshold, that.targetMaxThreshold) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType, targetValue, useMinThreshold, useMaxThreshold, targetMinThreshold, targetMaxThreshold);
    }
}
