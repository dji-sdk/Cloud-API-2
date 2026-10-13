// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 相机拍照上行信息（DRC {@code drc_camera_photo_info_push} 全量透传，含 result）。
 * <p>{@code payloadIndex}：ME 无此字段；仅在本载荷 state 的 cameraMode 与推送一致时有值，否则可能为空，勿作可靠路由键。
 */
public final class CameraPhotoInfo {
    /** 见上：可能为空，非 ME 原字段 */
    private String payloadIndex;
    /** 相机模式 */
    private int cameraMode;
    /** 当前步骤（多步拍摄） */
    private int currentStep;
    /** 进度百分比 */
    private int percent;
    /** 倒计时（秒） */
    private int countdownTime;
    /** 拍照结果（0 成功 / 非 0 各种失败码） */
    private int result;
    /** 状态字符串（原始，未做枚举转换以避免丢信息） */
    private String status;

    public CameraPhotoInfo() {}
    public CameraPhotoInfo(String payloadIndex, int cameraMode, int currentStep, int percent,
                           int countdownTime, int result, String status) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.cameraMode = cameraMode;
        this.currentStep = currentStep;
        this.percent = percent;
        this.countdownTime = countdownTime;
        this.result = result;
        this.status = status != null ? status : "";
    }
    /** 见上：可能为空，非 ME 原字段 */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 见上：可能为空，非 ME 原字段 */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    /** 相机模式 */
    public int getCameraMode() { return cameraMode; }
    /** @param cameraMode 相机模式 */
    public void setCameraMode(int v) { this.cameraMode = v; }
    /** 当前步骤（多步拍摄） */
    public int getCurrentStep() { return currentStep; }
    /** @param currentStep 当前步骤（多步拍摄） */
    public void setCurrentStep(int v) { this.currentStep = v; }
    /** 进度百分比 */
    public int getPercent() { return percent; }
    /** @param percent 进度百分比 */
    public void setPercent(int v) { this.percent = v; }
    /** 倒计时（秒） */
    public int getCountdownTime() { return countdownTime; }
    /** @param countdownTime 倒计时（秒） */
    public void setCountdownTime(int v) { this.countdownTime = v; }
    /** 拍照结果（0 成功 / 非 0 各种失败码） */
    public int getResult() { return result; }
    /** @param result 拍照结果（0 成功 / 非 0 各种失败码） */
    public void setResult(int v) { this.result = v; }
    /** 状态字符串（原始，未做枚举转换以避免丢信息） */
    public String getStatus() { return status; }
    /** @param status 状态字符串（原始，未做枚举转换以避免丢信息） */
    public void setStatus(String v) { this.status = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraPhotoInfo)) return false;
        CameraPhotoInfo that = (CameraPhotoInfo) o;
        return cameraMode == that.cameraMode && currentStep == that.currentStep
                && percent == that.percent && countdownTime == that.countdownTime
                && result == that.result
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(status, that.status);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, cameraMode, currentStep, percent, countdownTime, result, status); }
    @Override public String toString() {
        return "CameraPhotoInfo{payloadIndex='" + payloadIndex + "', cameraMode=" + cameraMode
                + ", currentStep=" + currentStep + ", percent=" + percent
                + ", countdownTime=" + countdownTime + ", result=" + result
                + ", status='" + status + "'}";
    }
}
