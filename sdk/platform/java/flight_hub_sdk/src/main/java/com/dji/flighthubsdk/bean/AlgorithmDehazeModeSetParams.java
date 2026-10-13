// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置算法去雾模式参数 */
public class AlgorithmDehazeModeSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 去雾模式（0=关闭 1=使能 2=自动 255=未知） */
    private int mode = 0;

    public AlgorithmDehazeModeSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 去雾模式（0=关闭 1=使能 2=自动 255=未知） */
    public int getMode() { return mode; }
    /** @param mode 去雾模式（0=关闭 1=使能 2=自动 255=未知） */
    public void setMode(int mode) { this.mode = mode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlgorithmDehazeModeSetParams that = (AlgorithmDehazeModeSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, mode);
    }

    @Override
    public String toString() {
        return "AlgorithmDehazeModeSetParams{payloadIndex='" + payloadIndex + '\'' + ", mode=" + mode + '}';
    }
}
