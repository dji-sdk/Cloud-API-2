// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设置中继占用状态结果 */
public final class SetRelayOccupiedStatusResult {
    private String bid;
    private String status;
    public SetRelayOccupiedStatusResult() {
    }

/**
     * 构造 SetRelayOccupiedStatusResult。
     *
     * @param bid 业务ID
     * @param status 状态
     */
    public SetRelayOccupiedStatusResult(String bid, String status) {
        this.bid = bid != null ? bid : "";
        this.status = status != null ? status : "";
    }

    /** 业务ID */
    public String getBid() { return bid; }
    /** 状态 */
    public String getStatus() { return status; }

    /** 业务ID */
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
        SetRelayOccupiedStatusResult that = (SetRelayOccupiedStatusResult) o;
        return Objects.equals(bid, that.bid)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, status);
    }

    @Override
    public String toString() {
        return "SetRelayOccupiedStatusResult{bid='" + bid + '\'' + ", status='" + status + '\'' + '}';
    }
}
