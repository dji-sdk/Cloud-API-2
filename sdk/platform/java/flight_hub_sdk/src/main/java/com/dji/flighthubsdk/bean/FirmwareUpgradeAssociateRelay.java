// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件升级关联的中继器信息（固件状态、SN） */
public final class FirmwareUpgradeAssociateRelay {

    private int firmwareStatus;
    private String sn;
    public FirmwareUpgradeAssociateRelay() {
    }

/**
     * 构造 FirmwareUpgradeAssociateRelay。
     *
     * @param firmwareStatus 固件状态
     * @param sn 设备序列号
     */
    public FirmwareUpgradeAssociateRelay(int firmwareStatus, String sn) {
        this.firmwareStatus = firmwareStatus;
        this.sn = sn != null ? sn : "";
    }

    /** 固件状态 */
    public int getFirmwareStatus() {
        return firmwareStatus;
    }

    /** 固件状态 */
    public void setFirmwareStatus(int firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareUpgradeAssociateRelay that = (FirmwareUpgradeAssociateRelay) o;
        return firmwareStatus == that.firmwareStatus
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firmwareStatus, sn);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeAssociateRelay{firmwareStatus=" + firmwareStatus + ", sn='" + sn + '\'' + '}';
    }
}
