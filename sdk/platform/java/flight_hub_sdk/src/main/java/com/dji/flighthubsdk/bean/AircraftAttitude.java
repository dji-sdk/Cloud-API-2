// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机姿态。
 */
public final class AircraftAttitude {

    private double head;
    private double pitch;
    private double roll;
    public AircraftAttitude() {
    }

/**
     * 构造 AircraftAttitude。
     *
     * @param head 航向
     * @param pitch 俯仰角度
     * @param roll 横滚角度
     */
    public AircraftAttitude(double head, double pitch, double roll) {
        this.head = head;
        this.pitch = pitch;
        this.roll = roll;
    }

    /** 机头朝向角度 */
    public double getAttitudeHead() {
        return head;
    }

    /** @param attitudeHead 机头朝向角度 */
    public void setAttitudeHead(double attitudeHead) {
        this.head = attitudeHead;
    }

    /** 俯仰轴角度 */
    public double getAttitudePitch() {
        return pitch;
    }

    /** @param attitudePitch 俯仰轴角度 */
    public void setAttitudePitch(double attitudePitch) {
        this.pitch = attitudePitch;
    }

    /** 横滚轴角度 */
    public double getAttitudeRoll() {
        return roll;
    }

    /** @param attitudeRoll 横滚轴角度 */
    public void setAttitudeRoll(double attitudeRoll) {
        this.roll = attitudeRoll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftAttitude that = (AircraftAttitude) o;
        return Double.compare(that.head, head) == 0
                && Double.compare(that.pitch, pitch) == 0
                && Double.compare(that.roll, roll) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, pitch, roll);
    }

    @Override
    public String toString() {
        return "AircraftAttitude{head=" + head + ", pitch=" + pitch + ", roll=" + roll + '}';
    }
}
