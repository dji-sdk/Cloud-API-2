// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件服务升级进度（百分比、当前步骤） */
public final class FirmwareServiceProgress {

    private int percent;
    private int currentStep;
    public FirmwareServiceProgress() {
    }

/**
     * 构造 FirmwareServiceProgress。
     *
     * @param percent 百分比
     * @param currentStep 当前步骤
     */
    public FirmwareServiceProgress(int percent, int currentStep) {
        this.percent = percent;
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

    /** 当前步骤 */
    public int getCurrentStep() {
        return currentStep;
    }

    /** 当前步骤 */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareServiceProgress that = (FirmwareServiceProgress) o;
        return percent == that.percent
                && currentStep == that.currentStep;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percent, currentStep);
    }

    @Override
    public String toString() {
        return "FirmwareServiceProgress{percent=" + percent + ", currentStep=" + currentStep + '}';
    }
}
