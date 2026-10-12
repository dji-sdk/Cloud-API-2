// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** PSDK 喊话器播放进度。 */
public final class PsdkSpeakerPlayProgressInfo {

    private int psdkIndex;
    private int result;
    private String status;
    private String stepKey;
    private int percent;
    private String md5;
    public PsdkSpeakerPlayProgressInfo() {
    }

/**
     * 构造 PsdkSpeakerPlayProgressInfo。
     *
     * @param psdkIndex PSDK索引
     * @param result 结果
     * @param status 状态
     * @param stepKey 步骤Key
     * @param percent 进度百分比
     * @param md5 MD5校验值
     */
    public PsdkSpeakerPlayProgressInfo(
            int psdkIndex, int result, String status, String stepKey, int percent, String md5) {
        this.psdkIndex = psdkIndex;
        this.result = result;
        this.status = status != null ? status : "";
        this.stepKey = stepKey != null ? stepKey : "";
        this.percent = percent;
        this.md5 = md5 != null ? md5 : "";
    }

    /** PSDK索引 */
    public int getPsdkIndex() {
        return psdkIndex;
    }

    /** PSDK索引 */
    public void setPsdkIndex(int psdkIndex) {
        this.psdkIndex = psdkIndex;
    }

    /** 结果 */
    public int getResult() {
        return result;
    }

    /** 结果 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 状态 */
    public String getStatus() {
        return status;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 步骤Key */
    public String getStepKey() {
        return stepKey;
    }

    /** 步骤Key */
    public void setStepKey(String stepKey) {
        this.stepKey = stepKey;
    }

    /** 进度百分比 */
    public int getPercent() {
        return percent;
    }

    /** 进度百分比 */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    /** MD5校验值 */
    public String getMd5() {
        return md5;
    }

    /** MD5校验值 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkSpeakerPlayProgressInfo that = (PsdkSpeakerPlayProgressInfo) o;
        return psdkIndex == that.psdkIndex
                && result == that.result
                && percent == that.percent
                && Objects.equals(status, that.status)
                && Objects.equals(stepKey, that.stepKey)
                && Objects.equals(md5, that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psdkIndex, result, status, stepKey, percent, md5);
    }

    @Override
    public String toString() {
        return "PsdkSpeakerPlayProgressInfo{"
                + "psdkIndex=" + psdkIndex
                + ", result=" + result
                + ", status='" + status + '\''
                + ", stepKey='" + stepKey + '\''
                + ", percent=" + percent
                + ", md5='" + md5 + '\''
                + '}';
    }
}
