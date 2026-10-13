// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设置离线地图开关请求参数 */
public class SetOfflineMapsParams {
    /** 离线地图开关; */
    private boolean offlineMapEnable;

    public SetOfflineMapsParams() {
    }


    /** 离线地图开关; */
    public boolean isOfflineMapEnable() {
        return offlineMapEnable;
    }

    /** @param offlineMapEnable 离线地图开关; */
    public void setOfflineMapEnable(boolean offlineMapEnable) {
        this.offlineMapEnable = offlineMapEnable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetOfflineMapsParams that = (SetOfflineMapsParams) o;
        return offlineMapEnable == that.offlineMapEnable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offlineMapEnable);
    }

    @Override
    public String toString() {
        return "SetOfflineMapsParams{offlineMapEnable=" + offlineMapEnable + '}';
    }
}
