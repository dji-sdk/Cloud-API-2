// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置红外测温模式参数 */
public class IrMeteringModeSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 测温模式（0=关闭 1=点测温 2=区域测温） */
    private int mode = 0;

    public IrMeteringModeSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 测温模式（0=关闭 1=点测温 2=区域测温） */
    public int getMode() { return mode; }
    /** @param mode 测温模式（0=关闭 1=点测温 2=区域测温） */
    public void setMode(int mode) { this.mode = mode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IrMeteringModeSetParams that = (IrMeteringModeSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, mode);
    }

    @Override
    public String toString() {
        return "IrMeteringModeSetParams{payloadIndex='" + payloadIndex + '\'' + ", mode=" + mode + '}';
    }
}
