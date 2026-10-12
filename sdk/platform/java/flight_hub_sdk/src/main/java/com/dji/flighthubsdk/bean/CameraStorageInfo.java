// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 相机存储信息（DRC {@code drc_camera_state_push.camera_state.storage}）。 */
public final class CameraStorageInfo {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 总容量 */
    private int total;
    /** 已用容量 */
    private int used;

    public CameraStorageInfo() {}
    public CameraStorageInfo(String payloadIndex, int total, int used) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.total = total;
        this.used = used;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    /** 总容量 */
    public int getTotal() { return total; }
    /** @param total 总容量 */
    public void setTotal(int v) { this.total = v; }
    /** 已用容量 */
    public int getUsed() { return used; }
    /** @param used 已用容量 */
    public void setUsed(int v) { this.used = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraStorageInfo)) return false;
        CameraStorageInfo that = (CameraStorageInfo) o;
        return total == that.total && used == that.used && Objects.equals(payloadIndex, that.payloadIndex);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, total, used); }
    @Override public String toString() {
        return "CameraStorageInfo{payloadIndex='" + payloadIndex + "', total=" + total + ", used=" + used + '}';
    }
}
