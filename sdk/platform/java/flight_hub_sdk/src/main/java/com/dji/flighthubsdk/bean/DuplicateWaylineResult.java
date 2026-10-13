// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 复制航线返回结果 */
public final class DuplicateWaylineResult {

    private String waylineUuid;
    private String waylineName;
    public DuplicateWaylineResult() {
    }

/**
     * 构造 DuplicateWaylineResult。
     *
     * @param waylineUuid 航线 UUID
     * @param waylineName 航线名称
     */
    public DuplicateWaylineResult(String waylineUuid, String waylineName) {
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.waylineName = waylineName != null ? waylineName : "";
    }

    /** 航线 UUID */
    public String getWaylineUuid() {
        return waylineUuid;
    }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** 航线名称 */
    public String getWaylineName() {
        return waylineName;
    }

    /** 航线名称 */
    public void setWaylineName(String waylineName) {
        this.waylineName = waylineName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DuplicateWaylineResult that = (DuplicateWaylineResult) o;
        return Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(waylineName, that.waylineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waylineUuid, waylineName);
    }

    @Override
    public String toString() {
        return "DuplicateWaylineResult{waylineUuid='" + waylineUuid + '\'' + ", waylineName='" + waylineName + '\'' + '}';
    }
}
