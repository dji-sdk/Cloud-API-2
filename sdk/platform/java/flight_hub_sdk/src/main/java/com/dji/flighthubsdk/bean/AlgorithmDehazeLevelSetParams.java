// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置算法去雾强度参数 */
public class AlgorithmDehazeLevelSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 去雾等级（1~4 级，255=未知） */
    private int level = 0;

    public AlgorithmDehazeLevelSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 去雾等级（1~4 级，255=未知） */
    public int getLevel() { return level; }
    /** @param level 去雾等级（1~4 级，255=未知） */
    public void setLevel(int level) { this.level = level; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlgorithmDehazeLevelSetParams that = (AlgorithmDehazeLevelSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, level);
    }

    @Override
    public String toString() {
        return "AlgorithmDehazeLevelSetParams{payloadIndex='" + payloadIndex + '\'' + ", level=" + level + '}';
    }
}
