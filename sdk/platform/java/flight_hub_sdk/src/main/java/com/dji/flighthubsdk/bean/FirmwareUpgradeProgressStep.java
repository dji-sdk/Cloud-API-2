// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件升级 OTA 进度子步骤 */
public final class FirmwareUpgradeProgressStep {

    private int currentStep;
    private int percent;
    public FirmwareUpgradeProgressStep() {
    }

/**
     * 构造 FirmwareUpgradeProgressStep。
     *
     * @param currentStep 当前步骤
     * @param percent 百分比
     */
    public FirmwareUpgradeProgressStep(int currentStep, int percent) {
        this.currentStep = currentStep;
        this.percent = percent;
    }

    /** 当前步骤 */
    public int getCurrentStep() {
        return currentStep;
    }

    /** 当前步骤 */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /** 百分比 */
    public int getPercent() {
        return percent;
    }

    /** 百分比 */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareUpgradeProgressStep that = (FirmwareUpgradeProgressStep) o;
        return currentStep == that.currentStep
                && percent == that.percent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStep, percent);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeProgressStep{currentStep=" + currentStep + ", percent=" + percent + '}';
    }
}
