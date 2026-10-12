// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 定损报告 - 错误码分级列表 */
public final class DiagnosisErrorCodeList {
    private List<DiagnosisErrorCodeItem> notice;
    private List<DiagnosisErrorCodeItem> warning;
    private List<DiagnosisErrorCodeItem> alert;
    public DiagnosisErrorCodeList() {
    }

/**
     * 构造 DiagnosisErrorCodeList。
     *
     * @param notice 通知级错误列表
     * @param warning 告警级错误列表
     * @param alert 告警信息
     */
    public DiagnosisErrorCodeList(List<DiagnosisErrorCodeItem> notice, List<DiagnosisErrorCodeItem> warning, List<DiagnosisErrorCodeItem> alert) {
        this.notice = notice != null ? notice : Collections.emptyList();
        this.warning = warning != null ? warning : Collections.emptyList();
        this.alert = alert != null ? alert : Collections.emptyList();
    }

    /** 通知级错误列表 */
    public List<DiagnosisErrorCodeItem> getNotice() { return notice; }
    /** 告警级错误列表 */
    public List<DiagnosisErrorCodeItem> getWarning() { return warning; }
    /** 告警信息 */
    public List<DiagnosisErrorCodeItem> getAlert() { return alert; }

    /** 通知级错误列表 */
    public void setNotice(List<DiagnosisErrorCodeItem> notice) {
        this.notice = notice;
    }

    /** 告警级错误列表 */
    public void setWarning(List<DiagnosisErrorCodeItem> warning) {
        this.warning = warning;
    }

    /** 告警信息 */
    public void setAlert(List<DiagnosisErrorCodeItem> alert) {
        this.alert = alert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiagnosisErrorCodeList that = (DiagnosisErrorCodeList) o;
        return Objects.equals(notice, that.notice)
                && Objects.equals(warning, that.warning)
                && Objects.equals(alert, that.alert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notice, warning, alert);
    }

    @Override
    public String toString() {
        return "DiagnosisErrorCodeList{notice=" + notice + ", warning=" + warning + ", alert=" + alert + '}';
    }
}
