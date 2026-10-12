// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 定损报告 - 错误码信息 */
public final class DiagnosisErrorCodeItem {
    private String errorCode;
    private String errorCodeDescribe;
    private String errorCodeDescribeEn;
    private int level;
    public DiagnosisErrorCodeItem() {
    }

/**
     * 构造 DiagnosisErrorCodeItem。
     *
     * @param errorCode 错误码
     * @param errorCodeDescribe 错误码描述
     * @param errorCodeDescribeEn 错误码英文描述
     * @param level level
     */
    public DiagnosisErrorCodeItem(String errorCode, String errorCodeDescribe, String errorCodeDescribeEn, int level) {
        this.errorCode = errorCode;
        this.errorCodeDescribe = errorCodeDescribe;
        this.errorCodeDescribeEn = errorCodeDescribeEn;
        this.level = level;
    }

    /** 错误码 */
    public String getErrorCode() { return errorCode; }
    /** 错误码描述 */
    public String getErrorCodeDescribe() { return errorCodeDescribe; }
    /** 错误码英文描述 */
    public String getErrorCodeDescribeEn() { return errorCodeDescribeEn; }
    /** level */
    public int getLevel() { return level; }

    /** 错误码 */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /** 错误码描述 */
    public void setErrorCodeDescribe(String errorCodeDescribe) {
        this.errorCodeDescribe = errorCodeDescribe;
    }

    /** 错误码英文描述 */
    public void setErrorCodeDescribeEn(String errorCodeDescribeEn) {
        this.errorCodeDescribeEn = errorCodeDescribeEn;
    }

    /** level */
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiagnosisErrorCodeItem that = (DiagnosisErrorCodeItem) o;
        return Objects.equals(errorCode, that.errorCode)
                && Objects.equals(errorCodeDescribe, that.errorCodeDescribe)
                && Objects.equals(errorCodeDescribeEn, that.errorCodeDescribeEn)
                && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, errorCodeDescribe, errorCodeDescribeEn, level);
    }

    @Override
    public String toString() {
        return "DiagnosisErrorCodeItem{"
                + "errorCode='" + errorCode + '\''
                + ", errorCodeDescribe='" + errorCodeDescribe + '\''
                + ", errorCodeDescribeEn='" + errorCodeDescribeEn + '\''
                + ", level=" + level
                + '}';
    }
}
