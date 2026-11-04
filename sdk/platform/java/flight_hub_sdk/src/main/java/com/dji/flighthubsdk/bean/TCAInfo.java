// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/** TCA信息。 */
public final class TCAInfo {
    /** 中心设备 SN */
    private String centerSn;
    /** 起降区域状态列表 */
    private List<TCAZoneStatus> tca = new ArrayList<>();
    /** TCA 状态 */
    private String tcaStatus = "";
    /** 版本号 */
    private long version;
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
    /** 起降区域状态列表 */
    public List<TCAZoneStatus> getTca() { return tca; }
    /** TCA 状态 */
    public String getTcaStatus() { return tcaStatus; }
    /** 版本号 */
    public long getVersion() { return version; }
    /** TCAS 数据 JSON */
    public String getDataJson() { return dataJson; }

    /** @param centerSn 中心设备 SN */
    public void setCenterSn(String centerSn) {
        this.centerSn = centerSn;
    }

    /** @param tca 起降区域状态列表 */
    public void setTca(List<TCAZoneStatus> tca) {
        this.tca = tca != null ? tca : new ArrayList<>();
    }

    /** @param tcaStatus TCA 状态 */
    public void setTcaStatus(String tcaStatus) {
        this.tcaStatus = tcaStatus != null ? tcaStatus : "";
    }

    /** @param version 版本号 */
    public void setVersion(long version) {
        this.version = version;
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
                && Objects.equals(dataJson, that.dataJson)
                && Objects.equals(tca, that.tca)
                && Objects.equals(tcaStatus, that.tcaStatus)
                && version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerSn, dataJson, tca, tcaStatus, version);
    }

    @Override
    public String toString() {
        return "TCAInfo{centerSn='" + centerSn + '\''
                + ", tca=" + tca
                + ", tcaStatus='" + tcaStatus + '\''
                + ", version=" + version
                + ", dataJson='" + dataJson + '\'' + '}';
    }
}
