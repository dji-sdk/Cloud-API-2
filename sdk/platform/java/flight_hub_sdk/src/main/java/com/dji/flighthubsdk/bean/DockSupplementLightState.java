// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场补光灯状态。 */
public final class DockSupplementLightState {

    private int supplementLightState;
    public DockSupplementLightState() {
    }

/**
     * 构造 DockSupplementLightState。
     *
     * @param supplementLightState 补光灯状态
     */
    public DockSupplementLightState(int supplementLightState) {
        this.supplementLightState = supplementLightState;
    }

    /** 补光灯状态 */
    public int getSupplementLightState() {
        return supplementLightState;
    }

    /** 补光灯状态 */
    public void setSupplementLightState(int supplementLightState) {
        this.supplementLightState = supplementLightState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockSupplementLightState that = (DockSupplementLightState) o;
        return supplementLightState == that.supplementLightState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplementLightState);
    }

    @Override
    public String toString() {
        return "DockSupplementLightState{supplementLightState=" + supplementLightState + '}';
    }
}
