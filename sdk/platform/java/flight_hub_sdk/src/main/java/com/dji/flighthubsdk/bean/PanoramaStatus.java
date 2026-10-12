// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 全景拍摄进度。
 */
public final class PanoramaStatus {

    /** 取值语义同 `flight_hub_sdk::PanoramaRunningState`（见 `value.h`） */
    private int currentStep;
    /** 全景拍照进度，0～100 */
    private int percent;
    private int photoState;
    public PanoramaStatus() {
    }

/**
     * 构造 PanoramaStatus。
     *
     * @param currentStep 全景拍照当前步骤
     * @param percent 全景拍照进度百分比
     * @param photoState 拍照状态
     */
    public PanoramaStatus(int currentStep, int percent, int photoState) {
        this.currentStep = currentStep;
        this.percent = percent;
        this.photoState = photoState;
    }

    /** 取值语义同 `flight_hub_sdk::PanoramaRunningState`（见 `value.h`） */
    public int getCurrentStep() {
        return currentStep;
    }

    /** @param currentStep 取值语义同 `flight_hub_sdk::PanoramaRunningState`（见 `value.h`） */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /** 全景拍照进度，0～100 */
    public int getPercent() {
        return percent;
    }

    /** @param percent 全景拍照进度，0～100 */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    /** 拍照状态 */
    public int getPhotoState() {
        return photoState;
    }

    /** 拍照状态 */
    public void setPhotoState(int photoState) {
        this.photoState = photoState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PanoramaStatus that = (PanoramaStatus) o;
        return currentStep == that.currentStep
                && percent == that.percent
                && photoState == that.photoState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStep, percent, photoState);
    }

    @Override
    public String toString() {
        return "PanoramaStatus{currentStep=" + currentStep + ", percent=" + percent + ", photoState=" + photoState + '}';
    }
}
