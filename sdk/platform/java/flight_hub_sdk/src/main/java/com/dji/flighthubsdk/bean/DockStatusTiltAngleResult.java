// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 倾角 */
public final class DockStatusTiltAngleResult {

    private int valid;
    private double value;
    public DockStatusTiltAngleResult() {
    }

/**
     * 构造 DockStatusTiltAngleResult。
     *
     * @param valid 是否有效
     * @param value 值
     */
    public DockStatusTiltAngleResult(int valid, double value) {
        this.valid = valid;
        this.value = value;
    }

    /** 是否有效 */
    public int getValid() {
        return valid;
    }

    /** 是否有效 */
    public void setValid(int valid) {
        this.valid = valid;
    }

    /** 值 */
    public double getValue() {
        return value;
    }

    /** 值 */
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusTiltAngleResult that = (DockStatusTiltAngleResult) o;
        return valid == that.valid
                && Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, value);
    }

    @Override
    public String toString() {
        return "DockStatusTiltAngleResult{valid=" + valid + ", value=" + value + '}';
    }
}
