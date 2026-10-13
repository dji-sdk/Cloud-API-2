// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场存储状态。 */
public final class DockStorageState {

    /** int, 总容量(单位KB); */
    private int total;
    /** int, 已使用(单位KB); */
    private int used;
    public DockStorageState() {
    }

/**
     * 构造 DockStorageState。
     *
     * @param total 总数
     * @param used used
     */
    public DockStorageState(int total, int used) {
        this.total = total;
        this.used = used;
    }

    /** int, 总容量(单位KB); */
    public int getTotal() {
        return total;
    }

    /** @param total int, 总容量(单位KB); */
    public void setTotal(int total) {
        this.total = total;
    }

    /** int, 已使用(单位KB); */
    public int getUsed() {
        return used;
    }

    /** @param used int, 已使用(单位KB); */
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
        DockStorageState that = (DockStorageState) o;
        return total == that.total && used == that.used;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, used);
    }

    @Override
    public String toString() {
        return "DockStorageState{total=" + total + ", used=" + used + '}';
    }
}
