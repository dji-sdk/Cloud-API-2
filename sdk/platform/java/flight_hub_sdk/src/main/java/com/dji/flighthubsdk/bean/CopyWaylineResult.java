// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;

/** 航线复制返回结果 */
public final class CopyWaylineResult {

    /** 新复制的航线 UUID */
    private String waylineUuid;
    /** 新复制的航线名称 */
    private String waylineName;

    public CopyWaylineResult() {
    }

    public CopyWaylineResult(String waylineUuid, String waylineName) {
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.waylineName = waylineName != null ? waylineName : "";
    }

    /** 新复制的航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }

    /** @param waylineUuid 新复制的航线 UUID */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid; }

    /** 新复制的航线名称 */
    public String getWaylineName() { return waylineName; }

    /** @param waylineName 新复制的航线名称 */
    public void setWaylineName(String waylineName) { this.waylineName = waylineName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopyWaylineResult that = (CopyWaylineResult) o;
        return Objects.equals(waylineUuid, that.waylineUuid) && Objects.equals(waylineName, that.waylineName);
    }

    @Override
    public int hashCode() { return Objects.hash(waylineUuid, waylineName); }

    @Override
    public String toString() {
        return "CopyWaylineResult{waylineUuid='" + waylineUuid + '\'' + ", waylineName='" + waylineName + '\'' + '}';
    }
}
