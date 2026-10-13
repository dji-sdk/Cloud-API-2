// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机降噪级别参数 */
public class CameraDenoiseLevelSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 降噪等级（0=关 1=标准 2=增强 3=超强） */
    private int level = 0;

    public CameraDenoiseLevelSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 降噪等级（0=关 1=标准 2=增强 3=超强） */
    public int getLevel() { return level; }
    /** @param level 降噪等级（0=关 1=标准 2=增强 3=超强） */
    public void setLevel(int level) { this.level = level; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraDenoiseLevelSetParams that = (CameraDenoiseLevelSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, level);
    }

    @Override
    public String toString() {
        return "CameraDenoiseLevelSetParams{payloadIndex='" + payloadIndex + '\'' + ", level=" + level + '}';
    }
}
