// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** PSDK 浮动窗口文本。 */
public final class PsdkFloatingWindowTextInfo {

    private int psdkIndex;
    private String floatingWindowText;
    public PsdkFloatingWindowTextInfo() {
    }

/**
     * 构造 PsdkFloatingWindowTextInfo。
     *
     * @param psdkIndex PSDK索引
     * @param floatingWindowText 浮窗文本
     */
    public PsdkFloatingWindowTextInfo(int psdkIndex, String floatingWindowText) {
        this.psdkIndex = psdkIndex;
        this.floatingWindowText = floatingWindowText != null ? floatingWindowText : "";
    }

    /** PSDK索引 */
    public int getPsdkIndex() {
        return psdkIndex;
    }

    /** PSDK索引 */
    public void setPsdkIndex(int psdkIndex) {
        this.psdkIndex = psdkIndex;
    }

    /** 浮窗文本 */
    public String getFloatingWindowText() {
        return floatingWindowText;
    }

    /** 浮窗文本 */
    public void setFloatingWindowText(String floatingWindowText) {
        this.floatingWindowText = floatingWindowText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkFloatingWindowTextInfo that = (PsdkFloatingWindowTextInfo) o;
        return psdkIndex == that.psdkIndex && Objects.equals(floatingWindowText, that.floatingWindowText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psdkIndex, floatingWindowText);
    }

    @Override
    public String toString() {
        return "PsdkFloatingWindowTextInfo{psdkIndex=" + psdkIndex + ", floatingWindowText='" + floatingWindowText + '\'' + '}';
    }
}
