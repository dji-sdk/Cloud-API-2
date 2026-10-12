// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置预录状态参数 */
public class PreRecordedStatusSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 预录制状态（0=关 1=开） */
    private int status = 0;

    public PreRecordedStatusSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 预录制状态（0=关 1=开） */
    public int getStatus() { return status; }
    /** @param status 预录制状态（0=关 1=开） */
    public void setStatus(int status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreRecordedStatusSetParams that = (PreRecordedStatusSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, status);
    }

    @Override
    public String toString() {
        return "PreRecordedStatusSetParams{payloadIndex='" + payloadIndex + '\'' + ", status=" + status + '}';
    }
}
