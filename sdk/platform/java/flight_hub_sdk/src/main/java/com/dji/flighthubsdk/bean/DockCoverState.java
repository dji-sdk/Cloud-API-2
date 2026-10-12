// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场舱盖状态。 */
public final class DockCoverState {

    /** 0: 关闭, 1: 打开, 2: 半开, 3: 舱盖状态异常; */
    private int coverState;
    public DockCoverState() {
    }

/**
     * 构造 DockCoverState。
     *
     * @param coverState 舱盖状态（0: 关闭, 1: 打开, 2: 半开, 3: 舱盖状态异常）
     */
    public DockCoverState(int coverState) {
        this.coverState = coverState;
    }

    /** 0: 关闭, 1: 打开, 2: 半开, 3: 舱盖状态异常; */
    public int getCoverState() {
        return coverState;
    }

    /** @param coverState 0: 关闭, 1: 打开, 2: 半开, 3: 舱盖状态异常; */
    public void setCoverState(int coverState) {
        this.coverState = coverState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockCoverState that = (DockCoverState) o;
        return coverState == that.coverState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coverState);
    }

    @Override
    public String toString() {
        return "DockCoverState{coverState=" + coverState + '}';
    }
}
