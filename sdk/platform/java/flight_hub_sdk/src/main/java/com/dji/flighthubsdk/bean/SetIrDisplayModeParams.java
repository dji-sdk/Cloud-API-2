// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置红外显示模式参数 */
public class SetIrDisplayModeParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 显示模式（1=纯红外 2=PIP 3=融合） */
    private int displayMode = 0;

    public SetIrDisplayModeParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 显示模式（1=纯红外 2=PIP 3=融合） */
    public int getDisplayMode() { return displayMode; }
    /** @param displayMode 显示模式（1=纯红外 2=PIP 3=融合） */
    public void setDisplayMode(int displayMode) { this.displayMode = displayMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIrDisplayModeParams that = (SetIrDisplayModeParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && displayMode == that.displayMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, displayMode);
    }

    @Override
    public String toString() {
        return "SetIrDisplayModeParams{payloadIndex='" + payloadIndex + '\'' + ", displayMode=" + displayMode + '}';
    }
}
