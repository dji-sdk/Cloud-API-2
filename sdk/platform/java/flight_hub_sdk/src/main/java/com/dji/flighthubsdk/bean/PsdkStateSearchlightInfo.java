// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.SearchLightWorkMode;
import java.util.Objects;
/** PSDK 探照灯状态。 */
public final class PsdkStateSearchlightInfo {

    /** `SearchLightWorkMode`（`value.h`） */
    private SearchLightWorkMode workMode;
    private int brightness;
    private int calibrationStatus;
    private int calibrationProgress;
    private double leftValue;
    private double rightValue;
    private int wideFieldMode;
    private int lightGimbalControl;
    private int lightingType;
    public PsdkStateSearchlightInfo() {
    }

/**
     * 构造 PsdkStateSearchlightInfo。
     *
     * @param workMode 探照灯工作模式
     * @param brightness 亮度
     * @param calibrationStatus 标定状态
     * @param calibrationProgress 标定进度
     * @param leftValue 左值
     * @param rightValue 右值
     * @param wideFieldMode 宽场模式
     * @param lightGimbalControl 灯光云台控制
     * @param lightingType 照明类型
     */
    public PsdkStateSearchlightInfo(
            SearchLightWorkMode workMode,
            int brightness,
            int calibrationStatus,
            int calibrationProgress,
            double leftValue,
            double rightValue,
            int wideFieldMode,
            int lightGimbalControl,
            int lightingType) {
        this.workMode = workMode != null ? workMode : SearchLightWorkMode.UNKNOWN;
        this.brightness = brightness;
        this.calibrationStatus = calibrationStatus;
        this.calibrationProgress = calibrationProgress;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.wideFieldMode = wideFieldMode;
        this.lightGimbalControl = lightGimbalControl;
        this.lightingType = lightingType;
    }

    /** `SearchLightWorkMode`（`value.h`） */
    public SearchLightWorkMode getWorkMode() {
        return workMode;
    }

    /** @param workMode `SearchLightWorkMode`（`value.h`） */
    public void setWorkMode(SearchLightWorkMode workMode) {
        this.workMode = workMode;
    }
    /** 亮度 */
    public int getBrightness() { return brightness; }
    /** 标定状态 */
    public int getCalibrationStatus() { return calibrationStatus; }
    /** 标定进度 */
    public int getCalibrationProgress() { return calibrationProgress; }
    /** 左值 */
    public double getLeftValue() { return leftValue; }
    /** 右值 */
    public double getRightValue() { return rightValue; }
    /** 宽场模式 */
    public int getWideFieldMode() { return wideFieldMode; }
    /** 灯光云台控制 */
    public int getLightGimbalControl() { return lightGimbalControl; }
    /** 照明类型 */
    public int getLightingType() { return lightingType; }

    /** 亮度 */
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    /** 标定状态 */
    public void setCalibrationStatus(int calibrationStatus) {
        this.calibrationStatus = calibrationStatus;
    }

    /** 标定进度 */
    public void setCalibrationProgress(int calibrationProgress) {
        this.calibrationProgress = calibrationProgress;
    }

    /** 左值 */
    public void setLeftValue(double leftValue) {
        this.leftValue = leftValue;
    }

    /** 右值 */
    public void setRightValue(double rightValue) {
        this.rightValue = rightValue;
    }

    /** 宽场模式 */
    public void setWideFieldMode(int wideFieldMode) {
        this.wideFieldMode = wideFieldMode;
    }

    /** 灯光云台控制 */
    public void setLightGimbalControl(int lightGimbalControl) {
        this.lightGimbalControl = lightGimbalControl;
    }

    /** 照明类型 */
    public void setLightingType(int lightingType) {
        this.lightingType = lightingType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkStateSearchlightInfo that = (PsdkStateSearchlightInfo) o;
        return workMode == that.workMode
                && brightness == that.brightness
                && calibrationStatus == that.calibrationStatus
                && calibrationProgress == that.calibrationProgress
                && Double.compare(that.leftValue, leftValue) == 0
                && Double.compare(that.rightValue, rightValue) == 0
                && wideFieldMode == that.wideFieldMode
                && lightGimbalControl == that.lightGimbalControl
                && lightingType == that.lightingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workMode, brightness, calibrationStatus, calibrationProgress,
                leftValue, rightValue, wideFieldMode, lightGimbalControl, lightingType);
    }

    @Override
    public String toString() {
        return "PsdkStateSearchlightInfo{"
                + "workMode=" + workMode
                + ", brightness=" + brightness
                + ", calibrationStatus=" + calibrationStatus
                + ", calibrationProgress=" + calibrationProgress
                + ", leftValue=" + leftValue
                + ", rightValue=" + rightValue
                + ", wideFieldMode=" + wideFieldMode
                + ", lightGimbalControl=" + lightGimbalControl
                + ", lightingType=" + lightingType
                + '}';
    }
}
