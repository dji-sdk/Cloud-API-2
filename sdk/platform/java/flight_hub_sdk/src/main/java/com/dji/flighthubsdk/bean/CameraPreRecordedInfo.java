// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 预录制信息（DRC {@code drc_camera_state_push.pre_recorded_info}）。 */
public final class CameraPreRecordedInfo {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int status;
    private int preRecordedTime;

    public CameraPreRecordedInfo() {}
    public CameraPreRecordedInfo(String payloadIndex, int status, int preRecordedTime) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.status = status;
        this.preRecordedTime = preRecordedTime;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public int getStatus() { return status; }
    public void setStatus(int v) { this.status = v; }
    public int getPreRecordedTime() { return preRecordedTime; }
    public void setPreRecordedTime(int v) { this.preRecordedTime = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraPreRecordedInfo)) return false;
        CameraPreRecordedInfo that = (CameraPreRecordedInfo) o;
        return status == that.status && preRecordedTime == that.preRecordedTime
                && Objects.equals(payloadIndex, that.payloadIndex);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, status, preRecordedTime); }
    @Override public String toString() {
        return "CameraPreRecordedInfo{payloadIndex='" + payloadIndex + "', status=" + status
                + ", preRecordedTime=" + preRecordedTime + '}';
    }
}
