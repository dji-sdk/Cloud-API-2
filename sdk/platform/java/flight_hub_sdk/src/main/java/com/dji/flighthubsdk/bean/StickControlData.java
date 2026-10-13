// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;

/**
 * DRC 虚拟摇杆杆量。
 * <p>中性值 1024（悬停/无输入）；建议操控范围 364–1684（1024±660，与司空前端一致）。
 * 松杆建议连续发送若干帧全 1024。建议发送频率约 10–20Hz。
 * SDK 透传不裁剪，客户端宜自行 clamp 到建议范围。</p>
 */
public class StickControlData {

    /** 横滚杆量，右为正；中性 1024，建议范围 364–1684 */
    private int roll = 1024;
    /** 俯仰杆量，前为正；中性 1024，建议范围 364–1684 */
    private int pitch = 1024;
    /** 油门杆量，上为正；中性 1024，建议范围 364–1684 */
    private int throttle = 1024;
    /** 偏航杆量，右为正；中性 1024，建议范围 364–1684 */
    private int yaw = 1024;
    /** 云台俯仰杆量；中性 1024，建议范围 364–1684 */
    private int gimbalPitch = 1024;

    public StickControlData() {
    }


    /** 横滚杆量，右为正；中性 1024，建议范围 364–1684 */
    public int getRoll() { return roll; }
    /** @param roll 横滚杆量，右为正；中性 1024，建议范围 364–1684 */
    public void setRoll(int roll) { this.roll = roll; }
    /** 俯仰杆量，前为正；中性 1024，建议范围 364–1684 */
    public int getPitch() { return pitch; }
    /** @param pitch 俯仰杆量，前为正；中性 1024，建议范围 364–1684 */
    public void setPitch(int pitch) { this.pitch = pitch; }
    /** 油门杆量，上为正；中性 1024，建议范围 364–1684 */
    public int getThrottle() { return throttle; }
    /** @param throttle 油门杆量，上为正；中性 1024，建议范围 364–1684 */
    public void setThrottle(int throttle) { this.throttle = throttle; }
    /** 偏航杆量，右为正；中性 1024，建议范围 364–1684 */
    public int getYaw() { return yaw; }
    /** @param yaw 偏航杆量，右为正；中性 1024，建议范围 364–1684 */
    public void setYaw(int yaw) { this.yaw = yaw; }
    /** 云台俯仰杆量；中性 1024，建议范围 364–1684 */
    public int getGimbalPitch() { return gimbalPitch; }

    /** @param gimbalPitch 云台俯仰杆量；中性 1024，建议范围 364–1684 */
    public void setGimbalPitch(int gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StickControlData that = (StickControlData) o;
        return roll == that.roll
                && pitch == that.pitch
                && throttle == that.throttle
                && yaw == that.yaw
                && gimbalPitch == that.gimbalPitch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                roll,
                pitch,
                throttle,
                yaw,
                gimbalPitch);
    }

    @Override
    public String toString() {
        return "StickControlData{"
                + "roll=" + roll
                + ", pitch=" + pitch
                + ", throttle=" + throttle
                + ", yaw=" + yaw
                + ", gimbalPitch=" + gimbalPitch
                + '}';
    }
}
