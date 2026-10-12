// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 更新航线返回结果 */
public final class UpdateWaylineResult {

    private String status;
    public UpdateWaylineResult() {
    }

/**
     * 构造 UpdateWaylineResult。
     *
     * @param status 状态
     */
    public UpdateWaylineResult(String status) {
        this.status = status != null ? status : "";
    }

    /** 状态 */
    public String getStatus() { return status; }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateWaylineResult that = (UpdateWaylineResult) o;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return "UpdateWaylineResult{status='" + status + '\'' + '}';
    }
}
