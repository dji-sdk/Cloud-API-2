// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场累计运行时长。 */
public final class DockAccTime {

    /** int, 机场累计运行时长(秒); */
    private int accTime;
    public DockAccTime() {
    }

/**
     * 构造 DockAccTime。
     *
     * @param accTime 累计运行时长
     */
    public DockAccTime(int accTime) {
        this.accTime = accTime;
    }

    /** int, 机场累计运行时长(秒); */
    public int getAccTime() {
        return accTime;
    }

    /** @param accTime int, 机场累计运行时长(秒); */
    public void setAccTime(int accTime) {
        this.accTime = accTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockAccTime that = (DockAccTime) o;
        return accTime == that.accTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accTime);
    }

    @Override
    public String toString() {
        return "DockAccTime{accTime=" + accTime + '}';
    }
}
