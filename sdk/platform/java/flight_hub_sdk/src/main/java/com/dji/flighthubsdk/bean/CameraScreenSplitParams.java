// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机分屏显示参数 */
public class CameraScreenSplitParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 分屏开关（false=关 true=开） */
    private boolean enable = false;

    public CameraScreenSplitParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 分屏开关（false=关 true=开） */
    public boolean isEnable() { return enable; }
    /** @param enable 分屏开关（false=关 true=开） */
    public void setEnable(boolean enable) { this.enable = enable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraScreenSplitParams that = (CameraScreenSplitParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && enable == that.enable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, enable);
    }

    @Override
    public String toString() {
        return "CameraScreenSplitParams{payloadIndex='" + payloadIndex + '\'' + ", enable=" + enable + '}';
    }
}
