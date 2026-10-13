// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁 - 功率解锁（等级） */
public final class DeviceUnlockPowerUnlock {

    private int level;
    public DeviceUnlockPowerUnlock() {
    }

/**
     * 构造 DeviceUnlockPowerUnlock。
     *
     * @param level level
     */
    public DeviceUnlockPowerUnlock(int level) {
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
        DeviceUnlockPowerUnlock that = (DeviceUnlockPowerUnlock) o;
        return level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

    @Override
    public String toString() {
        return "DeviceUnlockPowerUnlock{level=" + level + '}';
    }
}
