// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 云台姿态。 */
public final class GimbalState {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private double yaw;
    private double pitch;
    private double roll;
    public GimbalState() {
    }

/**
     * 构造 GimbalState。
     *
     * @param payloadIndex 负载索引
     * @param yaw 偏航角度
     * @param pitch 俯仰角度
     * @param roll 横滚角度
     */
    public GimbalState(String payloadIndex, double yaw, double pitch, double roll) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** 偏航角度 */
    public double getYaw() { return yaw; }
    /** 俯仰角度 */
    public double getPitch() { return pitch; }
    /** 横滚角度 */
    public double getRoll() { return roll; }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 偏航角度 */
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    /** 俯仰角度 */
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    /** 横滚角度 */
    public void setRoll(double roll) {
        this.roll = roll;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GimbalState that = (GimbalState) o;
        return Double.compare(that.yaw, yaw) == 0
                && Double.compare(that.pitch, pitch) == 0
                && Double.compare(that.roll, roll) == 0
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, yaw, pitch, roll);
    }

    @Override
    public String toString() {
        return "GimbalState{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", yaw=" + yaw
                + ", pitch=" + pitch
                + ", roll=" + roll
                + '}';
    }
}
