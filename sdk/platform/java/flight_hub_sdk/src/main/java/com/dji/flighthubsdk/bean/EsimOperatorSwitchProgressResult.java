// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** eSIM 运营商切换进度结果。 */
public final class EsimOperatorSwitchProgressResult {
    private String message;
    private String bid;
    private String dataJson;
    public EsimOperatorSwitchProgressResult() {
    }

    /**
     * 构造 EsimOperatorSwitchProgressResult。
     *
     * @param message 消息
     * @param bid 业务ID
     * @param dataJson 透传的原始 data JSON
     */
    public EsimOperatorSwitchProgressResult(String message, String bid, String dataJson) {
        this.message = message != null ? message : "";
        this.bid = bid != null ? bid : "";
        this.dataJson = dataJson != null ? dataJson : "";
    }

    /** 消息 */
    public String getMessage() { return message; }
    /** 业务ID */
    public String getBid() { return bid; }
    /** 透传的原始 data JSON */
    public String getDataJson() { return dataJson; }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 业务ID */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 透传的原始 data JSON */
    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EsimOperatorSwitchProgressResult that = (EsimOperatorSwitchProgressResult) o;
        return Objects.equals(message, that.message)
                && Objects.equals(bid, that.bid)
                && Objects.equals(dataJson, that.dataJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, bid, dataJson);
    }

    @Override
    public String toString() {
        return "EsimOperatorSwitchProgressResult{message='" + message + '\'' + ", bid='" + bid + '\'' + ", dataJson='" + dataJson + '\'' + '}';
    }
}
