// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 保养信息列表。 */
public final class AircraftMaintainStatusInfo {

    private List<AircraftMaintainStatus> maintainStatuses;
    public AircraftMaintainStatusInfo() {
    }

/**
     * 构造 AircraftMaintainStatusInfo。
     *
     * @param maintainStatuses 保养状态信息列表
     */
    public AircraftMaintainStatusInfo(List<AircraftMaintainStatus> maintainStatuses) {
        this.maintainStatuses = maintainStatuses != null ? maintainStatuses : Collections.emptyList();
    }

    /** 保养状态信息列表 */
    public List<AircraftMaintainStatus> getMaintainStatuses() {
        return maintainStatuses;
    }

    /** 保养状态信息列表 */
    public void setMaintainStatuses(List<AircraftMaintainStatus> maintainStatuses) {
        this.maintainStatuses = maintainStatuses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftMaintainStatusInfo that = (AircraftMaintainStatusInfo) o;
        return Objects.equals(maintainStatuses, that.maintainStatuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maintainStatuses);
    }

    @Override
    public String toString() {
        return "AircraftMaintainStatusInfo{count=" + maintainStatuses.size() + '}';
    }
}
