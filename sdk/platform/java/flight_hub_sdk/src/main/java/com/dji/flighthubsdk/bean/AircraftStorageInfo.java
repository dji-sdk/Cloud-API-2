// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 存储容量。 */
public final class AircraftStorageInfo {

    /** 总容量; */
    private int total;
    /** 已使用; */
    private int used;
    public AircraftStorageInfo() {
    }

/**
     * 构造 AircraftStorageInfo。
     *
     * @param total 总容量(KB)
     * @param used 已使用容量(KB)
     */
    public AircraftStorageInfo(int total, int used) {
        this.total = total;
        this.used = used;
    }

    /** 总容量; */
    public int getTotal() {
        return total;
    }

    /** @param total 总容量; */
    public void setTotal(int total) {
        this.total = total;
    }

    /** 已使用; */
    public int getUsed() {
        return used;
    }

    /** @param used 已使用; */
    public void setUsed(int used) {
        this.used = used;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftStorageInfo that = (AircraftStorageInfo) o;
        return total == that.total && used == that.used;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, used);
    }

    @Override
    public String toString() {
        return "AircraftStorageInfo{total=" + total + ", used=" + used + '}';
    }
}
