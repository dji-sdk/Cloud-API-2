// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机当前控制源。
 */
public final class AircraftControlSource {

    /** 当前控制源; */
    private String controlSource;
    public AircraftControlSource() {
    }

/**
     * 构造 AircraftControlSource。
     *
     * @param controlSource 当前控制源
     */
    public AircraftControlSource(String controlSource) {
        this.controlSource = controlSource != null ? controlSource : "";
    }

    /** 当前控制源; */
    public String getControlSource() {
        return controlSource;
    }

    /** @param controlSource 当前控制源; */
    public void setControlSource(String controlSource) {
        this.controlSource = controlSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftControlSource that = (AircraftControlSource) o;
        return Objects.equals(controlSource, that.controlSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controlSource);
    }

    @Override
    public String toString() {
        return "AircraftControlSource{controlSource='" + controlSource + '\'' + '}';
    }
}
