// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 单设备固件升级进度 */
public final class FirmwareUpgradeProgressItem {

    private FirmwareUpgradeAssociateDrone associateDrone;
    private FirmwareUpgradeAssociateRelay associateRelay;
    private int deviceCode;
    private int firmwareStatus;
    private FirmwareUpgradeProgressOTA ota;
    private String sn;
    private int workingStatus;
    public FirmwareUpgradeProgressItem() {
    }

/**
     * 构造 FirmwareUpgradeProgressItem。
     *
     * @param associateDrone 关联无人机
     * @param associateRelay 关联中继
     * @param deviceCode 设备编码
     * @param firmwareStatus 固件状态
     * @param ota ota
     * @param sn 设备序列号
     * @param workingStatus 工作状态
     */
    public FirmwareUpgradeProgressItem(
            FirmwareUpgradeAssociateDrone associateDrone,
            FirmwareUpgradeAssociateRelay associateRelay,
            int deviceCode,
            int firmwareStatus,
            FirmwareUpgradeProgressOTA ota,
            String sn,
            int workingStatus) {
        this.associateDrone = associateDrone;
        this.associateRelay = associateRelay;
        this.deviceCode = deviceCode;
        this.firmwareStatus = firmwareStatus;
        this.ota = ota;
        this.sn = sn != null ? sn : "";
        this.workingStatus = workingStatus;
    }

    /** 关联无人机 */
    public FirmwareUpgradeAssociateDrone getAssociateDrone() {
        return associateDrone;
    }

    /** 关联无人机 */
    public void setAssociateDrone(FirmwareUpgradeAssociateDrone associateDrone) {
        this.associateDrone = associateDrone;
    }

    /** 关联中继 */
    public FirmwareUpgradeAssociateRelay getAssociateRelay() {
        return associateRelay;
    }

    /** 关联中继 */
    public void setAssociateRelay(FirmwareUpgradeAssociateRelay associateRelay) {
        this.associateRelay = associateRelay;
    }

    /** 设备编码 */
    public int getDeviceCode() {
        return deviceCode;
    }

    /** 设备编码 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 固件状态 */
    public int getFirmwareStatus() {
        return firmwareStatus;
    }

    /** 固件状态 */
    public void setFirmwareStatus(int firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    /** ota */
    public FirmwareUpgradeProgressOTA getOta() {
        return ota;
    }

    /** ota */
    public void setOta(FirmwareUpgradeProgressOTA ota) {
        this.ota = ota;
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 工作状态 */
    public int getWorkingStatus() {
        return workingStatus;
    }

    /** 工作状态 */
    public void setWorkingStatus(int workingStatus) {
        this.workingStatus = workingStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareUpgradeProgressItem that = (FirmwareUpgradeProgressItem) o;
        return Objects.equals(associateDrone, that.associateDrone)
                && Objects.equals(associateRelay, that.associateRelay)
                && deviceCode == that.deviceCode
                && firmwareStatus == that.firmwareStatus
                && Objects.equals(ota, that.ota)
                && Objects.equals(sn, that.sn)
                && workingStatus == that.workingStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                associateDrone,
                associateRelay,
                deviceCode,
                firmwareStatus,
                ota,
                sn,
                workingStatus);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeProgressItem{"
                + "associateDrone=" + associateDrone
                + ", associateRelay=" + associateRelay
                + ", deviceCode=" + deviceCode
                + ", firmwareStatus=" + firmwareStatus
                + ", ota=" + ota
                + ", sn='" + sn + '\''
                + ", workingStatus=" + workingStatus
                + '}';
    }
}
