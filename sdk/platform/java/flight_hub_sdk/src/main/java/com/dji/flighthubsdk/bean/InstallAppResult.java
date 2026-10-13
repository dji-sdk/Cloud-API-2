// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 安装应用返回结果 */
public final class InstallAppResult {
    private String bid;
    private String status;
    public InstallAppResult() {
    }

/**
     * 构造 InstallAppResult。
     *
     * @param bid 业务 ID
     * @param status 状态
     */
    public InstallAppResult(String bid, String status) {
        this.bid = bid != null ? bid : "";
        this.status = status != null ? status : "";
    }

    /** 业务 ID */
    public String getBid() { return bid; }
    /** 状态 */
    public String getStatus() { return status; }

    /** 业务 ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

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
        InstallAppResult that = (InstallAppResult) o;
        return Objects.equals(bid, that.bid)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, status);
    }

    @Override
    public String toString() {
        return "InstallAppResult{bid='" + bid + '\'' + ", status='" + status + '\'' + '}';
    }
}
