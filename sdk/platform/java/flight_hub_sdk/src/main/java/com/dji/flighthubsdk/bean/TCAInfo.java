// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** TCA信息。 */
public final class TCAInfo {
    /** 中心设备 SN */
    private String centerSn;
    /** TCAS 数据 JSON */
    private String dataJson;
    public TCAInfo() {
    }

/**
     * 构造 TCAInfo。
     *
     * @param centerSn 中继SN
     * @param dataJson 数据JSON
     */
    public TCAInfo(String centerSn, String dataJson) {
        this.centerSn = centerSn != null ? centerSn : "";
        this.dataJson = dataJson != null ? dataJson : "";
    }

    /** 中心设备 SN */
    public String getCenterSn() { return centerSn; }
    /** TCAS 数据 JSON */
    public String getDataJson() { return dataJson; }

    /** @param centerSn 中心设备 SN */
    public void setCenterSn(String centerSn) {
        this.centerSn = centerSn;
    }

    /** @param dataJson TCAS 数据 JSON */
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
        TCAInfo that = (TCAInfo) o;
        return Objects.equals(centerSn, that.centerSn)
                && Objects.equals(dataJson, that.dataJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerSn, dataJson);
    }

    @Override
    public String toString() {
        return "TCAInfo{centerSn='" + centerSn + '\'' + ", dataJson='" + dataJson + '\'' + '}';
    }
}
