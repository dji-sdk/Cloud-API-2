// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 定损报告 - 自检诊断结果数据 */
public final class SelfDiagnosticResultData {
    private List<DiagnosisMaterialItem> high;
    private List<DiagnosisMaterialItem> mid;
    private List<DiagnosisMaterialItem> low;
    public SelfDiagnosticResultData() {
    }

/**
     * 构造 SelfDiagnosticResultData。
     *
     * @param high 高风险诊断项列表
     * @param mid 中风险诊断项列表
     * @param low 低风险诊断项列表
     */
    public SelfDiagnosticResultData(List<DiagnosisMaterialItem> high, List<DiagnosisMaterialItem> mid, List<DiagnosisMaterialItem> low) {
        this.high = high != null ? high : Collections.emptyList();
        this.mid = mid != null ? mid : Collections.emptyList();
        this.low = low != null ? low : Collections.emptyList();
    }

    /** 高风险诊断项列表 */
    public List<DiagnosisMaterialItem> getHigh() { return high; }
    /** 中风险诊断项列表 */
    public List<DiagnosisMaterialItem> getMid() { return mid; }
    /** 低风险诊断项列表 */
    public List<DiagnosisMaterialItem> getLow() { return low; }

    /** 高风险诊断项列表 */
    public void setHigh(List<DiagnosisMaterialItem> high) {
        this.high = high;
    }

    /** 中风险诊断项列表 */
    public void setMid(List<DiagnosisMaterialItem> mid) {
        this.mid = mid;
    }

    /** 低风险诊断项列表 */
    public void setLow(List<DiagnosisMaterialItem> low) {
        this.low = low;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelfDiagnosticResultData that = (SelfDiagnosticResultData) o;
        return Objects.equals(high, that.high)
                && Objects.equals(mid, that.mid)
                && Objects.equals(low, that.low);
    }

    @Override
    public int hashCode() {
        return Objects.hash(high, mid, low);
    }

    @Override
    public String toString() {
        return "SelfDiagnosticResultData{high=" + high + ", mid=" + mid + ", low=" + low + '}';
    }
}
