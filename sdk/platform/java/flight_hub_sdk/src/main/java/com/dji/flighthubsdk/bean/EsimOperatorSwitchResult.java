// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** eSIM运营商切换结果。 */
public final class EsimOperatorSwitchResult {
    private String message;
    private String bid;
    public EsimOperatorSwitchResult() {
    }

/**
     * 构造 EsimOperatorSwitchResult。
     *
     * @param message 结果消息
     * @param bid 业务 ID
     */
    public EsimOperatorSwitchResult(String message, String bid) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
    }

    /** 结果消息 */
    public String getMessage() {
        return message;
    }

    /** 结果消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务 ID */
    public String getBid() {
        return bid;
    }

    /** 业务 ID */
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
        EsimOperatorSwitchResult that = (EsimOperatorSwitchResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid);
    }

    @Override
    public String toString() {
        return "EsimOperatorSwitchResult{message='" + message + '\'' + ", bid='" + bid + '\'' + '}';
    }
}
