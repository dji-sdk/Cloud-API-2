// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 云台画面拖拽参数（drc_camera_screen_drag） */
public class CameraScreenDragParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 是否锁机头 */
    private boolean locked = true;
    /** 俯仰速度，度/秒 */
    private double pitchSpeed;
    /** 偏航速度，度/秒 */
    private double yawSpeed;

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 是否锁机头 */
    public boolean isLocked() { return locked; }
    /** @param locked 是否锁机头 */
    public void setLocked(boolean locked) { this.locked = locked; }
    /** 俯仰速度，度/秒 */
    public double getPitchSpeed() { return pitchSpeed; }
    /** @param pitchSpeed 俯仰速度，度/秒 */
    public void setPitchSpeed(double pitchSpeed) { this.pitchSpeed = pitchSpeed; }
    /** 偏航速度，度/秒 */
    public double getYawSpeed() { return yawSpeed; }
    /** @param yawSpeed 偏航速度，度/秒 */
    public void setYawSpeed(double yawSpeed) { this.yawSpeed = yawSpeed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraScreenDragParams that = (CameraScreenDragParams) o;
        return locked == that.locked && Double.compare(that.pitchSpeed, pitchSpeed) == 0
                && Double.compare(that.yawSpeed, yawSpeed) == 0 && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() { return Objects.hash(payloadIndex, locked, pitchSpeed, yawSpeed); }
}
