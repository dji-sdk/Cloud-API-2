// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 机场保养维护信息。 */
public final class DockMaintainStatusInfo {

    private List<DockMaintainStatus> maintainStatusInfos;
    public DockMaintainStatusInfo() {
    }

/**
     * 构造 DockMaintainStatusInfo。
     *
     * @param maintainStatusInfos 保养状态列表
     */
    public DockMaintainStatusInfo(List<DockMaintainStatus> maintainStatusInfos) {
        this.maintainStatusInfos = maintainStatusInfos != null ? maintainStatusInfos : Collections.emptyList();
    }

    /** 保养状态列表 */
    public List<DockMaintainStatus> getMaintainStatusInfos() {
        return maintainStatusInfos;
    }

    /** 保养状态列表 */
    public void setMaintainStatusInfos(List<DockMaintainStatus> maintainStatusInfos) {
        this.maintainStatusInfos = maintainStatusInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockMaintainStatusInfo that = (DockMaintainStatusInfo) o;
        return Objects.equals(maintainStatusInfos, that.maintainStatusInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maintainStatusInfos);
    }

    @Override
    public String toString() {
        return "DockMaintainStatusInfo{maintainStatusInfos=" + maintainStatusInfos + '}';
    }
}
