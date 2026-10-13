// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 关闭无人机结果。 */
public final class CloseDroneResult {
    private String message;
    private String bid;
    public CloseDroneResult() {
    }

/**
     * 构造 CloseDroneResult。
     *
     * @param message 消息
     * @param bid 业务标识
     */
    public CloseDroneResult(String message, String bid) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
    }

    /** 消息 */
    public String getMessage() {
        return message;
    }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务标识 */
    public String getBid() {
        return bid;
    }

    /** 业务标识 */
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
        CloseDroneResult that = (CloseDroneResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid);
    }

    @Override
    public String toString() {
        return "CloseDroneResult{message='" + message + '\'' + ", bid='" + bid + '\'' + '}';
    }
}
