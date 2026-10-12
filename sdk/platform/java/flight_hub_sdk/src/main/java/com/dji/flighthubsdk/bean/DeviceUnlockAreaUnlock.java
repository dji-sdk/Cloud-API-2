// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 设备解锁 - 区域解锁（包含区域 ID 列表） */
public final class DeviceUnlockAreaUnlock {

    private List<Integer> areaIds;
    public DeviceUnlockAreaUnlock() {
    }

/**
     * 构造 DeviceUnlockAreaUnlock。
     *
     * @param areaIds 区域 ID 列表
     */
    public DeviceUnlockAreaUnlock(List<Integer> areaIds) {
        this.areaIds = areaIds != null ? areaIds : Collections.emptyList();
    }

    /** 区域 ID 列表 */
    public List<Integer> getAreaIds() {
        return areaIds;
    }

    /** 区域 ID 列表 */
    public void setAreaIds(List<Integer> areaIds) {
        this.areaIds = areaIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockAreaUnlock that = (DeviceUnlockAreaUnlock) o;
        return Objects.equals(areaIds, that.areaIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaIds);
    }

    @Override
    public String toString() {
        return "DeviceUnlockAreaUnlock{areaIds=" + areaIds + '}';
    }
}
