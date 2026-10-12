// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置预录时长参数 */
public class PreRecordedTimeSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 预录制时长（秒） */
    private int preRecordedTime = 0;

    public PreRecordedTimeSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 预录制时长（秒） */
    public int getPreRecordedTime() { return preRecordedTime; }
    /** @param preRecordedTime 预录制时长（秒） */
    public void setPreRecordedTime(int preRecordedTime) { this.preRecordedTime = preRecordedTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreRecordedTimeSetParams that = (PreRecordedTimeSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && preRecordedTime == that.preRecordedTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, preRecordedTime);
    }

    @Override
    public String toString() {
        return "PreRecordedTimeSetParams{payloadIndex='" + payloadIndex + '\'' + ", preRecordedTime=" + preRecordedTime + '}';
    }
}
