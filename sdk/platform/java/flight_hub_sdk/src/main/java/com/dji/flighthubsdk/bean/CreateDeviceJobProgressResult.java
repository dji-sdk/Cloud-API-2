// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建设备作业进度结果。 */
public final class CreateDeviceJobProgressResult {
    /** 进度百分比 */
    private int percent;
    /** 当前步骤 */
    private int currentStep;
    /** 扩展信息 */
    private String ext;
    public CreateDeviceJobProgressResult() {
    }

/**
     * 构造 CreateDeviceJobProgressResult。
     *
     * @param percent 百分比
     * @param currentStep 当前步骤
     * @param ext ext
     */
    public CreateDeviceJobProgressResult(int percent, int currentStep, String ext) {
        this.percent = percent;
        this.currentStep = currentStep;
        this.ext = ext != null ? ext : "";
    }

    /** 进度百分比 */
    public int getPercent() {
        return percent;
    }

    /** @param percent 进度百分比 */
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

    /** 扩展信息 */
    public String getExt() {
        return ext;
    }

    /** @param ext 扩展信息 */
    public void setExt(String ext) {
        this.ext = ext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateDeviceJobProgressResult that = (CreateDeviceJobProgressResult) o;
        return percent == that.percent
                && currentStep == that.currentStep
                && Objects.equals(ext, that.ext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(percent, currentStep, ext);
    }

    @Override
    public String toString() {
        return "CreateDeviceJobProgressResult{percent=" + percent + ", currentStep=" + currentStep + ", ext='" + ext + '\'' + '}';
    }
}
