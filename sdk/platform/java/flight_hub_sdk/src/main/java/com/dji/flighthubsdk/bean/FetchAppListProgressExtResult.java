// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取应用列表进度扩展结果 */
public final class FetchAppListProgressExtResult {
    private String sn;
    private List<AIBoxAppInfoResult> aiBoxAppInfo;
    private String errorReason;
    public FetchAppListProgressExtResult() {
    }

/**
     * 构造 FetchAppListProgressExtResult。
     *
     * @param sn 设备序列号
     * @param aiBoxAppInfo AI 盒子应用信息列表
     * @param errorReason 错误原因
     */
    public FetchAppListProgressExtResult(String sn, List<AIBoxAppInfoResult> aiBoxAppInfo, String errorReason) {
        this.sn = sn != null ? sn : "";
        this.aiBoxAppInfo = aiBoxAppInfo != null ? aiBoxAppInfo : Collections.emptyList();
        this.errorReason = errorReason != null ? errorReason : "";
    }

    /** 设备序列号 */
    public String getSn() { return sn; }
    /** AI 盒子应用信息列表 */
    public List<AIBoxAppInfoResult> getAiBoxAppInfo() { return aiBoxAppInfo; }
    /** 错误原因 */
    public String getErrorReason() { return errorReason; }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** AI 盒子应用信息列表 */
    public void setAiBoxAppInfo(List<AIBoxAppInfoResult> aiBoxAppInfo) {
        this.aiBoxAppInfo = aiBoxAppInfo;
    }

    /** 错误原因 */
    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FetchAppListProgressExtResult that = (FetchAppListProgressExtResult) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(aiBoxAppInfo, that.aiBoxAppInfo)
                && Objects.equals(errorReason, that.errorReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, aiBoxAppInfo, errorReason);
    }

    @Override
    public String toString() {
        return "FetchAppListProgressExtResult{sn='" + sn + '\'' + ", aiBoxAppInfo=" + aiBoxAppInfo + ", errorReason='" + errorReason + '\'' + '}';
    }
}
