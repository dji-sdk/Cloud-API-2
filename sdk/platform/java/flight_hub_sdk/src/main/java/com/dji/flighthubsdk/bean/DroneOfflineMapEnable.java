// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 飞行器离线地图开关（offline_map_enable）。EA340 消息推送补全（Phase D）。 */
public final class DroneOfflineMapEnable {

    private boolean offlineMapEnable;

    public DroneOfflineMapEnable() {
    }

    /**
     * 构造 DroneOfflineMapEnable。
     *
     * @param offlineMapEnable 离线地图开关
     */
    public DroneOfflineMapEnable(boolean offlineMapEnable) {
        this.offlineMapEnable = offlineMapEnable;
    }

    /** 离线地图开关 */
    public boolean isOfflineMapEnable() {
        return offlineMapEnable;
    }

    /** 离线地图开关 */
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
        DroneOfflineMapEnable that = (DroneOfflineMapEnable) o;
        return offlineMapEnable == that.offlineMapEnable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offlineMapEnable);
    }

    @Override
    public String toString() {
        return "DroneOfflineMapEnable{offlineMapEnable=" + offlineMapEnable + '}';
    }
}
