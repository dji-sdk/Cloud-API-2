// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 手动换电发起结果。 */
public final class ManualBatterySwapResult {
    private String message;
    private String bid;
    public ManualBatterySwapResult() {
    }

    /**
     * 构造 ManualBatterySwapResult。
     *
     * @param message 消息
     * @param bid 业务ID
     */
    public ManualBatterySwapResult(String message, String bid) {
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
        ManualBatterySwapResult that = (ManualBatterySwapResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid);
    }

    @Override
    public String toString() {
        return "ManualBatterySwapResult{message='" + message + '\'' + ", bid='" + bid + '\'' + '}';
    }
}
