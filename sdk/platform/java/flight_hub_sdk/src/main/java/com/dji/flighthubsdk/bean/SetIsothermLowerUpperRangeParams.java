// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置等温线上下范围参数 */
public class SetIsothermLowerUpperRangeParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 等温线下限温度 */
    private int lower = 0;
    /** 等温线上限温度 */
    private int upper = 0;

    public SetIsothermLowerUpperRangeParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 等温线下限温度 */
    public int getLower() { return lower; }
    /** @param lower 等温线下限温度 */
    public void setLower(int lower) { this.lower = lower; }
    /** 等温线上限温度 */
    public int getUpper() { return upper; }
    /** @param upper 等温线上限温度 */
    public void setUpper(int upper) { this.upper = upper; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIsothermLowerUpperRangeParams that = (SetIsothermLowerUpperRangeParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && lower == that.lower
                && upper == that.upper;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, lower, upper);
    }

    @Override
    public String toString() {
        return "SetIsothermLowerUpperRangeParams{payloadIndex='" + payloadIndex + '\'' + ", lower=" + lower + ", upper=" + upper + '}';
    }
}
