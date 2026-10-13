// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 日志报告设备上传文件进度。 */
public final class LogReportListResultDeviceUploadFilesItemProgress {
    /** 当前步骤 */
    private int currentStep;
    /** 总步骤数 */
    private int totalStep;
    /** 上传进度（0-100） */
    private int progress;
    /** 完成时间（Unix 秒） */
    private long finishTime;
    /** 上传速率 */
    private int uploadRate;
    /** 结果 */
    private int result;
    /** 状态 */
    private String status;
    public LogReportListResultDeviceUploadFilesItemProgress() {
    }

    public LogReportListResultDeviceUploadFilesItemProgress(int currentStep, int totalStep, int progress,
            long finishTime, int uploadRate, int result, String status) {
        this.currentStep = currentStep;
        this.totalStep = totalStep;
        this.progress = progress;
        this.finishTime = finishTime;
        this.uploadRate = uploadRate;
        this.result = result;
        this.status = status != null ? status : "";
    }

    /** 当前步骤 */
    public int getCurrentStep() {
        return currentStep;
    }

    /** @param currentStep 当前步骤 */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /** 总步骤数 */
    public int getTotalStep() {
        return totalStep;
    }

    /** @param totalStep 总步骤数 */
    public void setTotalStep(int totalStep) {
        this.totalStep = totalStep;
    }

    /** 上传进度（0-100） */
    public int getProgress() {
        return progress;
    }

    /** @param progress 上传进度（0-100） */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /** 完成时间（Unix 秒） */
    public long getFinishTime() {
        return finishTime;
    }

    /** @param finishTime 完成时间（Unix 秒） */
    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    /** 上传速率 */
    public int getUploadRate() {
        return uploadRate;
    }

    /** @param uploadRate 上传速率 */
    public void setUploadRate(int uploadRate) {
        this.uploadRate = uploadRate;
    }

    /** 结果 */
    public int getResult() {
        return result;
    }

    /** @param result 结果 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 状态 */
    public String getStatus() {
        return status;
    }

    /** @param status 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultDeviceUploadFilesItemProgress that = (LogReportListResultDeviceUploadFilesItemProgress) o;
        return currentStep == that.currentStep
                && totalStep == that.totalStep
                && progress == that.progress
                && finishTime == that.finishTime
                && uploadRate == that.uploadRate
                && result == that.result
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStep, totalStep, progress, finishTime, uploadRate, result, status);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceUploadFilesItemProgress{currentStep=" + currentStep + ", totalStep=" + totalStep + ", progress=" + progress + ", finishTime=" + finishTime + ", uploadRate=" + uploadRate + ", result=" + result + ", status='" + status + '\'' + '}';
    }
}
