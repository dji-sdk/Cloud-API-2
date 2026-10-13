// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置红外混合率参数 */
public class SetIrMixRateParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 红外可见光融合比例（0~100） */
    private int irMixRate = 0;

    public SetIrMixRateParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 红外可见光融合比例（0~100） */
    public int getIrMixRate() { return irMixRate; }
    /** @param irMixRate 红外可见光融合比例（0~100） */
    public void setIrMixRate(int irMixRate) { this.irMixRate = irMixRate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIrMixRateParams that = (SetIrMixRateParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && irMixRate == that.irMixRate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, irMixRate);
    }

    @Override
    public String toString() {
        return "SetIrMixRateParams{payloadIndex='" + payloadIndex + '\'' + ", irMixRate=" + irMixRate + '}';
    }
}
