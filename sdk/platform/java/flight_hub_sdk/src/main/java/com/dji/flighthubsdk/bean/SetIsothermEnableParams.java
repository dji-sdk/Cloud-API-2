// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置等温线开关参数 */
public class SetIsothermEnableParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 等温线开关（0=关 1=开） */
    private int enable = 0;

    public SetIsothermEnableParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 等温线开关（0=关 1=开） */
    public int getEnable() { return enable; }
    /** @param enable 等温线开关（0=关 1=开） */
    public void setEnable(int enable) { this.enable = enable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIsothermEnableParams that = (SetIsothermEnableParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && enable == that.enable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, enable);
    }

    @Override
    public String toString() {
        return "SetIsothermEnableParams{payloadIndex='" + payloadIndex + '\'' + ", enable=" + enable + '}';
    }
}
