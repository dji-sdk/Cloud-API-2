// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场电池存储模式。 */
public final class DockBatteryStoreMode {

    /** int, 电池(存储)模式, {1: 电池计划存储策略, 2: 电池应急存储策略}; */
    private int batteryStoreMode;
    public DockBatteryStoreMode() {
    }

/**
     * 构造 DockBatteryStoreMode。
     *
     * @param batteryStoreMode 电池存储模式
     */
    public DockBatteryStoreMode(int batteryStoreMode) {
        this.batteryStoreMode = batteryStoreMode;
    }

    /** int, 电池(存储)模式, {1: 电池计划存储策略, 2: 电池应急存储策略}; */
    public int getBatteryStoreMode() {
        return batteryStoreMode;
    }

    /** @param batteryStoreMode int, 电池(存储)模式, {1: 电池计划存储策略, 2: 电池应急存储策略}; */
    public void setBatteryStoreMode(int batteryStoreMode) {
        this.batteryStoreMode = batteryStoreMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockBatteryStoreMode that = (DockBatteryStoreMode) o;
        return batteryStoreMode == that.batteryStoreMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(batteryStoreMode);
    }

    @Override
    public String toString() {
        return "DockBatteryStoreMode{batteryStoreMode=" + batteryStoreMode + '}';
    }
}
