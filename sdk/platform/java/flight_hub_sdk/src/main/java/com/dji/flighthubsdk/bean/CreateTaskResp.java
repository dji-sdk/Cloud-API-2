// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 创建任务响应。 */
public final class CreateTaskResp {
    /** 任务 UUID */
    private String uuid;
    /** 航线有效性校验码列表 */
    private List<Integer> waylineValidityCheckCodes;
    public CreateTaskResp() {
    }

/**
     * 构造 CreateTaskResp。
     *
     * @param uuid UUID
     * @param waylineValidityCheckCodes 航线有效性校验码列表
     */
    public CreateTaskResp(String uuid, List<Integer> waylineValidityCheckCodes) {
        this.uuid = uuid != null ? uuid : "";
        this.waylineValidityCheckCodes =
                waylineValidityCheckCodes != null ? waylineValidityCheckCodes : Collections.emptyList();
    }

    /** 任务 UUID */
    public String getUuid() {
        return uuid;
    }

    /** @param uuid 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 航线有效性校验码列表 */
    public List<Integer> getWaylineValidityCheckCodes() {
        return waylineValidityCheckCodes;
    }

    /** 航线有效性校验码列表 */
    public void setWaylineValidityCheckCodes(List<Integer> waylineValidityCheckCodes) {
        this.waylineValidityCheckCodes = waylineValidityCheckCodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateTaskResp that = (CreateTaskResp) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(waylineValidityCheckCodes, that.waylineValidityCheckCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, waylineValidityCheckCodes);
    }

    @Override
    public String toString() {
        return "CreateTaskResp{uuid='" + uuid + '\'' + ", waylineValidityCheckCodes=" + waylineValidityCheckCodes + '}';
    }
}
