// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** Dongle 电源开关结果。 */
public final class DonglePowerSwitchResult {
    private String message;
    private String bid;
    public DonglePowerSwitchResult() {
    }

    /**
     * 构造 DonglePowerSwitchResult。
     *
     * @param message 消息
     * @param bid 业务ID
     */
    public DonglePowerSwitchResult(String message, String bid) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
    }

    /** 消息 */
    public String getMessage() { return message; }
    /** 业务ID */
    public String getBid() { return bid; }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DonglePowerSwitchResult that = (DonglePowerSwitchResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid);
    }

    @Override
    public String toString() {
        return "DonglePowerSwitchResult{message='" + message + '\'' + ", bid='" + bid + '\'' + '}';
    }
}
