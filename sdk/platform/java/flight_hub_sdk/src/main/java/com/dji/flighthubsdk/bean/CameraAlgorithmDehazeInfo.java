// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 去雾算法信息（DRC {@code drc_camera_state_push.algorithm_dehaze_info}）。 */
public final class CameraAlgorithmDehazeInfo {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int mode;
    private int level;
    private boolean isWorking;

    public CameraAlgorithmDehazeInfo() {}
    public CameraAlgorithmDehazeInfo(String payloadIndex, int mode, int level, boolean isWorking) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.mode = mode;
        this.level = level;
        this.isWorking = isWorking;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public int getMode() { return mode; }
    public void setMode(int v) { this.mode = v; }
    public int getLevel() { return level; }
    public void setLevel(int v) { this.level = v; }
    public boolean isWorking() { return isWorking; }
    public void setWorking(boolean v) { this.isWorking = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraAlgorithmDehazeInfo)) return false;
        CameraAlgorithmDehazeInfo that = (CameraAlgorithmDehazeInfo) o;
        return mode == that.mode && level == that.level && isWorking == that.isWorking
                && Objects.equals(payloadIndex, that.payloadIndex);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, mode, level, isWorking); }
    @Override public String toString() {
        return "CameraAlgorithmDehazeInfo{payloadIndex='" + payloadIndex + "', mode=" + mode
                + ", level=" + level + ", isWorking=" + isWorking + '}';
    }
}
