// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取区域同步状态结果。 */
public final class GetAreasSyncStatusResults {
    private List<DeviceSyncStatusResults> list;
    public GetAreasSyncStatusResults() {
    }

/**
     * 构造 GetAreasSyncStatusResults。
     *
     * @param list 设备同步状态列表
     */
    public GetAreasSyncStatusResults(List<DeviceSyncStatusResults> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 设备同步状态列表 */
    public List<DeviceSyncStatusResults> getList() {
        return list;
    }

    /** 设备同步状态列表 */
    public void setList(List<DeviceSyncStatusResults> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetAreasSyncStatusResults that = (GetAreasSyncStatusResults) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "GetAreasSyncStatusResults{list=" + list + '}';
    }
}
