// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 信号地图结果。 */
public final class SignalMapsResults {
    private List<DeviceSignalItem> lists;
    public SignalMapsResults() {
    }

/**
     * 构造 SignalMapsResults。
     *
     * @param lists 设备信号列表
     */
    public SignalMapsResults(List<DeviceSignalItem> lists) {
        this.lists = lists != null ? lists : Collections.emptyList();
    }

    /** 设备信号列表 */
    public List<DeviceSignalItem> getLists() {
        return lists;
    }

    /** 设备信号列表 */
    public void setLists(List<DeviceSignalItem> lists) {
        this.lists = lists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SignalMapsResults that = (SignalMapsResults) o;
        return Objects.equals(lists, that.lists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lists);
    }

    @Override
    public String toString() {
        return "SignalMapsResults{lists=" + lists + '}';
    }
}
