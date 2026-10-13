// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁 - 高度解锁 */
public final class DeviceUnlockHeightUnlock {

    private double height;
    public DeviceUnlockHeightUnlock() {
    }

/**
     * 构造 DeviceUnlockHeightUnlock。
     *
     * @param height 高度
     */
    public DeviceUnlockHeightUnlock(double height) {
        this.height = height;
    }

    /** 高度 */
    public double getHeight() {
        return height;
    }

    /** 高度 */
    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockHeightUnlock that = (DeviceUnlockHeightUnlock) o;
        return Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height);
    }

    @Override
    public String toString() {
        return "DeviceUnlockHeightUnlock{height=" + height + '}';
    }
}
