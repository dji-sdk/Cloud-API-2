// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** eSIM 信息条目（dongle_infos）。EA340 消息推送补全（Phase D）。 */
public final class EsimInfoItem {

    private boolean enabled;
    private String iccid;
    private long telecomOperator;

    public EsimInfoItem() {
    }

    /**
     * 构造 EsimInfoItem。
     *
     * @param enabled 是否启用
     * @param iccid ICCID
     * @param telecomOperator 电信运营商
     */
    public EsimInfoItem(boolean enabled, String iccid, long telecomOperator) {
        this.enabled = enabled;
        this.iccid = iccid != null ? iccid : "";
        this.telecomOperator = telecomOperator;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return enabled;
    }

    /** 是否启用 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        EsimInfoItem that = (EsimInfoItem) o;
        return enabled == that.enabled
                && telecomOperator == that.telecomOperator
                && Objects.equals(iccid, that.iccid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, iccid, telecomOperator);
    }

    @Override
    public String toString() {
        return "EsimInfoItem{enabled=" + enabled
                + ", iccid='" + iccid + '\''
                + ", telecomOperator=" + telecomOperator + '}';
    }
}
