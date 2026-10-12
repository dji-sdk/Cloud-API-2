// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 设备信号条目。 */
public final class DeviceSignalItem {
    private String deviceSn;
    private List<GridSignalItem> grids;
    public DeviceSignalItem() {
    }

/**
     * 构造 DeviceSignalItem。
     *
     * @param deviceSn 设备序列号
     * @param grids grids
     */
    public DeviceSignalItem(String deviceSn, List<GridSignalItem> grids) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.grids = grids != null ? grids : Collections.emptyList();
    }

    /** 设备序列号 */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** grids */
    public List<GridSignalItem> getGrids() {
        return grids;
    }

    /** grids */
    public void setGrids(List<GridSignalItem> grids) {
        this.grids = grids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceSignalItem that = (DeviceSignalItem) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(grids, that.grids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, grids);
    }

    @Override
    public String toString() {
        return "DeviceSignalItem{deviceSn='" + deviceSn + '\'' + ", grids=" + grids + '}';
    }
}
