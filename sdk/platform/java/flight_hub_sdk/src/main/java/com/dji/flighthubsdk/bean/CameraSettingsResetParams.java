// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 相机设置重置参数 */
public class CameraSettingsResetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";

    public CameraSettingsResetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraSettingsResetParams that = (CameraSettingsResetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex);
    }

    @Override
    public String toString() {
        return "CameraSettingsResetParams{payloadIndex='" + payloadIndex + '\'' + '}';
    }
}
