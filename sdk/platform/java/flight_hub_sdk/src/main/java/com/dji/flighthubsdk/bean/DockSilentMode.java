// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场静音模式。 */
public final class DockSilentMode {

    /** int, 机场静音模式, 0: 静音模式关闭, 1: 静音模式开启; */
    private int silentMode;
    public DockSilentMode() {
    }

/**
     * 构造 DockSilentMode。
     *
     * @param silentMode 静音模式
     */
    public DockSilentMode(int silentMode) {
        this.silentMode = silentMode;
    }

    /** int, 机场静音模式, 0: 静音模式关闭, 1: 静音模式开启; */
    public int getSilentMode() {
        return silentMode;
    }

    /** @param silentMode int, 机场静音模式, 0: 静音模式关闭, 1: 静音模式开启; */
    public void setSilentMode(int silentMode) {
        this.silentMode = silentMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockSilentMode that = (DockSilentMode) o;
        return silentMode == that.silentMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(silentMode);
    }

    @Override
    public String toString() {
        return "DockSilentMode{silentMode=" + silentMode + '}';
    }
}
