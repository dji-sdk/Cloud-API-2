// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 当前返航高度模式值。 */
public final class AircraftCurrentRthMode {

    /** int, 返航高度模式, { 0: 智能高度, 1: 设定高度 }; */
    private int currentRthMode;
    public AircraftCurrentRthMode() {
    }

/**
     * 构造 AircraftCurrentRthMode。
     *
     * @param currentRthMode 当前返航高度模式
     */
    public AircraftCurrentRthMode(int currentRthMode) {
        this.currentRthMode = currentRthMode;
    }

    /** int, 返航高度模式, { 0: 智能高度, 1: 设定高度 }; */
    public int getCurrentRthMode() {
        return currentRthMode;
    }

    /** @param currentRthMode int, 返航高度模式, { 0: 智能高度, 1: 设定高度 }; */
    public void setCurrentRthMode(int currentRthMode) {
        this.currentRthMode = currentRthMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftCurrentRthMode that = (AircraftCurrentRthMode) o;
        return currentRthMode == that.currentRthMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentRthMode);
    }

    @Override
    public String toString() {
        return "AircraftCurrentRthMode{currentRthMode=" + currentRthMode + '}';
    }
}
