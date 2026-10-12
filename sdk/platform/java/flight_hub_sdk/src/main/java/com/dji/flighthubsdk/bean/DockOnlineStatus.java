// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 机场在线状态。
 */
public final class DockOnlineStatus {

    private boolean online;
    public DockOnlineStatus() {
    }

/**
     * @param online 是否在线
     */
    public DockOnlineStatus(boolean online) {
        this.online = online;
    }

    /** 是否在线 */
    public boolean isOnline() {
        return online;
    }

    /** 是否在线 */
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockOnlineStatus that = (DockOnlineStatus) o;
        return online == that.online;
    }

    @Override
    public int hashCode() {
        return Objects.hash(online);
    }

    @Override
    public String toString() {
        return "DockOnlineStatus{online=" + online + '}';
    }
}
