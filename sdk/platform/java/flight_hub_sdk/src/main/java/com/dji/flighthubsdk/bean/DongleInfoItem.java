// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 4G Dongle 信息条目（dongle_infos，机场/飞机共用）。EA340 消息推送补全（Phase D）。 */
public final class DongleInfoItem {

    private long dongleType;
    private String eid;
    private long esimActivateState;
    private List<EsimInfoItem> esimInfos;
    private String imei;
    private long simCardState;
    private long simSlot;
    private SimInfoItem simInfo;
    private long telecomOperator;

    public DongleInfoItem() {
        this.esimInfos = Collections.emptyList();
        this.simInfo = new SimInfoItem();
    }

    /**
     * 构造 DongleInfoItem。
     *
     * @param dongleType Dongle 类型
     * @param eid EID
     * @param esimActivateState eSIM 激活状态
     * @param esimInfos eSIM 信息列表
     * @param imei IMEI
     * @param simCardState SIM 卡状态
     * @param simSlot SIM 卡槽位
     * @param simInfo SIM 卡信息
     * @param telecomOperator 电信运营商
     */
    public DongleInfoItem(
            long dongleType,
            String eid,
            long esimActivateState,
            List<EsimInfoItem> esimInfos,
            String imei,
            long simCardState,
            long simSlot,
            SimInfoItem simInfo,
            long telecomOperator) {
        this.dongleType = dongleType;
        this.eid = eid != null ? eid : "";
        this.esimActivateState = esimActivateState;
        this.esimInfos = esimInfos != null ? esimInfos : Collections.emptyList();
        this.imei = imei != null ? imei : "";
        this.simCardState = simCardState;
        this.simSlot = simSlot;
        this.simInfo = simInfo != null ? simInfo : new SimInfoItem();
        this.telecomOperator = telecomOperator;
    }

    /** Dongle 类型 */
    public long getDongleType() {
        return dongleType;
    }

    /** Dongle 类型 */
    public void setDongleType(long dongleType) {
        this.dongleType = dongleType;
    }

    /** EID */
    public String getEid() {
        return eid;
    }

    /** EID */
    public void setEid(String eid) {
        this.eid = eid;
    }

    /** eSIM 激活状态 */
    public long getEsimActivateState() {
        return esimActivateState;
    }

    /** eSIM 激活状态 */
    public void setEsimActivateState(long esimActivateState) {
        this.esimActivateState = esimActivateState;
    }

    /** eSIM 信息列表 */
    public List<EsimInfoItem> getEsimInfos() {
        return esimInfos;
    }

    /** eSIM 信息列表 */
    public void setEsimInfos(List<EsimInfoItem> esimInfos) {
        this.esimInfos = esimInfos != null ? esimInfos : Collections.emptyList();
    }

    /** IMEI */
    public String getImei() {
        return imei;
    }

    /** IMEI */
    public void setImei(String imei) {
        this.imei = imei;
    }

    /** SIM 卡状态 */
    public long getSimCardState() {
        return simCardState;
    }

    /** SIM 卡状态 */
    public void setSimCardState(long simCardState) {
        this.simCardState = simCardState;
    }

    /** SIM 卡槽位 */
    public long getSimSlot() {
        return simSlot;
    }

    /** SIM 卡槽位 */
    public void setSimSlot(long simSlot) {
        this.simSlot = simSlot;
    }

    /** SIM 卡信息 */
    public SimInfoItem getSimInfo() {
        return simInfo;
    }

    /** SIM 卡信息 */
    public void setSimInfo(SimInfoItem simInfo) {
        this.simInfo = simInfo != null ? simInfo : new SimInfoItem();
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
        DongleInfoItem that = (DongleInfoItem) o;
        return dongleType == that.dongleType
                && esimActivateState == that.esimActivateState
                && simCardState == that.simCardState
                && simSlot == that.simSlot
                && telecomOperator == that.telecomOperator
                && Objects.equals(eid, that.eid)
                && Objects.equals(esimInfos, that.esimInfos)
                && Objects.equals(imei, that.imei)
                && Objects.equals(simInfo, that.simInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dongleType, eid, esimActivateState, esimInfos, imei, simCardState, simSlot, simInfo,
                telecomOperator);
    }

    @Override
    public String toString() {
        return "DongleInfoItem{dongleType=" + dongleType
                + ", eid='" + eid + '\''
                + ", esimActivateState=" + esimActivateState
                + ", esimInfos=" + esimInfos
                + ", imei='" + imei + '\''
                + ", simCardState=" + simCardState
                + ", simSlot=" + simSlot
                + ", simInfo=" + simInfo
                + ", telecomOperator=" + telecomOperator + '}';
    }
}
