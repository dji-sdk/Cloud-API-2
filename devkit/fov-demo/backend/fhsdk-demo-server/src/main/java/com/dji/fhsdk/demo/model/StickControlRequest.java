// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

public class StickControlRequest {

    private int roll = 1024;
    private int pitch = 1024;
    private int throttle = 1024;
    private int yaw = 1024;
    private int gimbalPitch = 1024;
    private int durationMs = 50;

    public int getRoll() { return roll; }
    public void setRoll(int roll) { this.roll = roll; }
    public int getPitch() { return pitch; }
    public void setPitch(int pitch) { this.pitch = pitch; }
    public int getThrottle() { return throttle; }
    public void setThrottle(int throttle) { this.throttle = throttle; }
    public int getYaw() { return yaw; }
    public void setYaw(int yaw) { this.yaw = yaw; }
    public int getGimbalPitch() { return gimbalPitch; }
    public void setGimbalPitch(int gimbalPitch) { this.gimbalPitch = gimbalPitch; }
    public int getDurationMs() { return durationMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
}
