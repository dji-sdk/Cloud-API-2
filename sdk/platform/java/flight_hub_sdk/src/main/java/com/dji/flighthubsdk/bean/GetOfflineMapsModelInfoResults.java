// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 获取离线地图模型信息结果。 */
public final class GetOfflineMapsModelInfoResults {
    /** 是否被禁用; */
    private boolean disable;
    private OfflineMapsModelData offlineMap;
    public GetOfflineMapsModelInfoResults() {
    }

/**
     * 构造 GetOfflineMapsModelInfoResults。
     *
     * @param disable 是否禁用
     * @param offlineMap 离线地图数据
     */
    public GetOfflineMapsModelInfoResults(boolean disable, OfflineMapsModelData offlineMap) {
        this.disable = disable;
        this.offlineMap = offlineMap;
    }

    /** 是否被禁用; */
    public boolean isDisable() { return disable; }
    /** 离线地图数据 */
    public OfflineMapsModelData getOfflineMap() { return offlineMap; }

    /** @param disable 是否被禁用; */
    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    /** 离线地图数据 */
    public void setOfflineMap(OfflineMapsModelData offlineMap) {
        this.offlineMap = offlineMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetOfflineMapsModelInfoResults that = (GetOfflineMapsModelInfoResults) o;
        return disable == that.disable
                && Objects.equals(offlineMap, that.offlineMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disable, offlineMap);
    }

    @Override
    public String toString() {
        return "GetOfflineMapsModelInfoResults{disable=" + disable + ", offlineMap=" + offlineMap + '}';
    }
}
