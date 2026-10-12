// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 校验解锁证书返回结果 */
public final class CheckUnlockLicenseResult {

    private List<String> errFileNames;
    private List<String> errCertMismatch;
    private List<CheckUnlockLicenseResultItem> items;
    public CheckUnlockLicenseResult() {
    }

/**
     * 构造 CheckUnlockLicenseResult。
     *
     * @param errFileNames 错误文件名列表
     * @param errCertMismatch 证书不匹配列表
     * @param items 校验结果项列表
     */
    public CheckUnlockLicenseResult(
            List<String> errFileNames, List<String> errCertMismatch, List<CheckUnlockLicenseResultItem> items) {
        this.errFileNames = errFileNames != null ? errFileNames : Collections.emptyList();
        this.errCertMismatch = errCertMismatch != null ? errCertMismatch : Collections.emptyList();
        this.items = items != null ? items : Collections.emptyList();
    }

    /** 错误文件名列表 */
    public List<String> getErrFileNames() {
        return errFileNames;
    }

    /** 错误文件名列表 */
    public void setErrFileNames(List<String> errFileNames) {
        this.errFileNames = errFileNames;
    }

    /** 证书不匹配列表 */
    public List<String> getErrCertMismatch() {
        return errCertMismatch;
    }

    /** 证书不匹配列表 */
    public void setErrCertMismatch(List<String> errCertMismatch) {
        this.errCertMismatch = errCertMismatch;
    }

    /** 校验结果项列表 */
    public List<CheckUnlockLicenseResultItem> getItems() {
        return items;
    }

    /** 校验结果项列表 */
    public void setItems(List<CheckUnlockLicenseResultItem> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckUnlockLicenseResult that = (CheckUnlockLicenseResult) o;
        return Objects.equals(errFileNames, that.errFileNames)
                && Objects.equals(errCertMismatch, that.errCertMismatch)
                && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errFileNames, errCertMismatch, items);
    }

    @Override
    public String toString() {
        return "CheckUnlockLicenseResult{errFileNames=" + errFileNames + ", errCertMismatch=" + errCertMismatch + ", items=" + items + '}';
    }
}
