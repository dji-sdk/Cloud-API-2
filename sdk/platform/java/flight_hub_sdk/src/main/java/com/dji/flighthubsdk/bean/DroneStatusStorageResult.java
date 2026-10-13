// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞机设备状态 — 存储 */
public final class DroneStatusStorageResult {

    private long total;
    private long used;
    public DroneStatusStorageResult() {
    }

/**
     * 构造 DroneStatusStorageResult。
     *
     * @param total 总数
     * @param used used
     */
    public DroneStatusStorageResult(long total, long used) {
        this.total = total;
        this.used = used;
    }

    /** 总数 */
    public long getTotal() {
        return total;
    }

    /** 总数 */
    public void setTotal(long total) {
        this.total = total;
    }

    /** used */
    public long getUsed() {
        return used;
    }

    /** used */
    public void setUsed(long used) {
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
        DroneStatusStorageResult that = (DroneStatusStorageResult) o;
        return total == that.total
                && used == that.used;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, used);
    }

    @Override
    public String toString() {
        return "DroneStatusStorageResult{total=" + total + ", used=" + used + '}';
    }
}
