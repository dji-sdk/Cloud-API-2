// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 推杆状态。 */
public final class DockPutterState {

    /** int, 推杆状态, 参考 EA210 {0: 关闭, 1: 打开, 2: 半开, 3: 推杆状态异常}; */
    private int putterState;
    public DockPutterState() {
    }

/**
     * 构造 DockPutterState。
     *
     * @param putterState 推杆状态
     */
    public DockPutterState(int putterState) {
        this.putterState = putterState;
    }

    /** int, 推杆状态, 参考 EA210 {0: 关闭, 1: 打开, 2: 半开, 3: 推杆状态异常}; */
    public int getPutterState() {
        return putterState;
    }

    /** @param putterState int, 推杆状态, 参考 EA210 {0: 关闭, 1: 打开, 2: 半开, 3: 推杆状态异常}; */
    public void setPutterState(int putterState) {
        this.putterState = putterState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockPutterState that = (DockPutterState) o;
        return putterState == that.putterState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(putterState);
    }

    @Override
    public String toString() {
        return "DockPutterState{putterState=" + putterState + '}';
    }
}
