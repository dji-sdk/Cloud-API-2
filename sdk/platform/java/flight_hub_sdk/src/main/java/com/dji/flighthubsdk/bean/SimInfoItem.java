// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** SIM 卡信息（dongle_infos）。EA340 消息推送补全（Phase D）。 */
public final class SimInfoItem {

    private String iccid;
    private long telecomOperator;

    public SimInfoItem() {
    }

    /**
     * 构造 SimInfoItem。
     *
     * @param iccid ICCID
     * @param telecomOperator 电信运营商
     */
    public SimInfoItem(String iccid, long telecomOperator) {
        this.iccid = iccid != null ? iccid : "";
        this.telecomOperator = telecomOperator;
    }

    /** ICCID */
    public String getIccid() {
        return iccid;
    }

    /** ICCID */
    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    /** 电信运营商 */
    public long getTelecomOperator() {
        return telecomOperator;
    }

    /** 电信运营商 */
    public void setTelecomOperator(long telecomOperator) {
        this.telecomOperator = telecomOperator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimInfoItem that = (SimInfoItem) o;
        return telecomOperator == that.telecomOperator && Objects.equals(iccid, that.iccid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iccid, telecomOperator);
    }

    @Override
    public String toString() {
        return "SimInfoItem{iccid='" + iccid + '\'' + ", telecomOperator=" + telecomOperator + '}';
    }
}
