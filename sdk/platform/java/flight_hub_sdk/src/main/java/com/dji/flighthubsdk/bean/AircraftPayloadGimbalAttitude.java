// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 单个载荷云台姿态。 */
public final class AircraftPayloadGimbalAttitude {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private double gimbalPitch;
    private double gimbalRoll;
    private double gimbalYaw;
    public AircraftPayloadGimbalAttitude() {
    }

/**
     * 构造 AircraftPayloadGimbalAttitude。
     *
     * @param payloadIndex 负载索引
     * @param gimbalPitch 云台俯仰角
     * @param gimbalRoll 云台横滚角
     * @param gimbalYaw 云台偏航角
     */
    public AircraftPayloadGimbalAttitude(
            String payloadIndex, double gimbalPitch, double gimbalRoll, double gimbalYaw) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.gimbalPitch = gimbalPitch;
        this.gimbalRoll = gimbalRoll;
        this.gimbalYaw = gimbalYaw;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 云台俯仰角 */
    public double getGimbalPitch() {
        return gimbalPitch;
    }

    /** 云台俯仰角 */
    public void setGimbalPitch(double gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    /** 云台横滚角 */
    public double getGimbalRoll() {
        return gimbalRoll;
    }

    /** 云台横滚角 */
    public void setGimbalRoll(double gimbalRoll) {
        this.gimbalRoll = gimbalRoll;
    }

    /** 云台偏航角 */
    public double getGimbalYaw() {
        return gimbalYaw;
    }

    /** 云台偏航角 */
    public void setGimbalYaw(double gimbalYaw) {
        this.gimbalYaw = gimbalYaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadGimbalAttitude that = (AircraftPayloadGimbalAttitude) o;
        return Double.compare(that.gimbalPitch, gimbalPitch) == 0
                && Double.compare(that.gimbalRoll, gimbalRoll) == 0
                && Double.compare(that.gimbalYaw, gimbalYaw) == 0
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, gimbalPitch, gimbalRoll, gimbalYaw);
    }

    @Override
    public String toString() {
        return "AircraftPayloadGimbalAttitude{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", gimbalPitch=" + gimbalPitch
                + ", gimbalRoll=" + gimbalRoll
                + ", gimbalYaw=" + gimbalYaw
                + '}';
    }
}
