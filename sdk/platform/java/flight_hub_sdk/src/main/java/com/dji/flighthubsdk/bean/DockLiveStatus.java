// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 机场直播状态推送（live_status）。EA340 消息推送补全（Phase D）。 */
public final class DockLiveStatus {

    private List<LiveStatusItem> liveStatus;

    public DockLiveStatus() {
        this.liveStatus = Collections.emptyList();
    }

    /**
     * 构造 DockLiveStatus。
     *
     * @param liveStatus 直播状态列表
     */
    public DockLiveStatus(List<LiveStatusItem> liveStatus) {
        this.liveStatus = liveStatus != null ? liveStatus : Collections.emptyList();
    }

    /** 直播状态列表 */
    public List<LiveStatusItem> getLiveStatus() {
        return liveStatus;
    }

    /** 直播状态列表 */
    public void setLiveStatus(List<LiveStatusItem> liveStatus) {
        this.liveStatus = liveStatus != null ? liveStatus : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockLiveStatus that = (DockLiveStatus) o;
        return Objects.equals(liveStatus, that.liveStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(liveStatus);
    }

    @Override
    public String toString() {
        return "DockLiveStatus{liveStatus=" + liveStatus + '}';
    }
}
