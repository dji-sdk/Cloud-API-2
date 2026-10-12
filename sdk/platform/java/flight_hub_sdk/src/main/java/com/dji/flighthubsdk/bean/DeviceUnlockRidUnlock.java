// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁 - RID 解锁（等级） */
public final class DeviceUnlockRidUnlock {

    private int level;
    public DeviceUnlockRidUnlock() {
    }

/**
     * 构造 DeviceUnlockRidUnlock。
     *
     * @param level level
     */
    public DeviceUnlockRidUnlock(int level) {
        this.level = level;
    }

    /** level */
    public int getLevel() {
        return level;
    }

    /** level */
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockRidUnlock that = (DeviceUnlockRidUnlock) o;
        return level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

    @Override
    public String toString() {
        return "DeviceUnlockRidUnlock{level=" + level + '}';
    }
}
