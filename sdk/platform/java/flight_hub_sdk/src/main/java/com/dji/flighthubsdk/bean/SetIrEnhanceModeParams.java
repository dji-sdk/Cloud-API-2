// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置红外增强模式参数 */
public class SetIrEnhanceModeParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 增强模式（0=关闭 1=边缘增强 2=热源回帖） */
    private int enhanceMode = 0;

    public SetIrEnhanceModeParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 增强模式（0=关闭 1=边缘增强 2=热源回帖） */
    public int getEnhanceMode() { return enhanceMode; }
    /** @param enhanceMode 增强模式（0=关闭 1=边缘增强 2=热源回帖） */
    public void setEnhanceMode(int enhanceMode) { this.enhanceMode = enhanceMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIrEnhanceModeParams that = (SetIrEnhanceModeParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && enhanceMode == that.enhanceMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, enhanceMode);
    }

    @Override
    public String toString() {
        return "SetIrEnhanceModeParams{payloadIndex='" + payloadIndex + '\'' + ", enhanceMode=" + enhanceMode + '}';
    }
}
