// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** DRC 链路状态。 */
public final class DockDrcState {

    /** int, DRC 链路状态, { 0: 未连接, 1: 连接中, 2: 已连接; } */
    private int drcState;
    public DockDrcState() {
    }

/**
     * 构造 DockDrcState。
     *
     * @param drcState 远程调试状态
     */
    public DockDrcState(int drcState) {
        this.drcState = drcState;
    }

    /** int, DRC 链路状态, { 0: 未连接, 1: 连接中, 2: 已连接; } */
    public int getDrcState() {
        return drcState;
    }

    /** @param drcState int, DRC 链路状态, { 0: 未连接, 1: 连接中, 2: 已连接; } */
    public void setDrcState(int drcState) {
        this.drcState = drcState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockDrcState that = (DockDrcState) o;
        return drcState == that.drcState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(drcState);
    }

    @Override
    public String toString() {
        return "DockDrcState{drcState=" + drcState + '}';
    }
}
