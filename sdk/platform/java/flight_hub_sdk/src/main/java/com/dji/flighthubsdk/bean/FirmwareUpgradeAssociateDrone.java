// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件升级关联的飞行器信息（固件状态、SN、工作状态） */
public final class FirmwareUpgradeAssociateDrone {

    private int firmwareStatus;
    private String sn;
    private int workingStatus;
    public FirmwareUpgradeAssociateDrone() {
    }

/**
     * 构造 FirmwareUpgradeAssociateDrone。
     *
     * @param firmwareStatus 固件状态
     * @param sn 设备序列号
     * @param workingStatus 工作状态
     */
    public FirmwareUpgradeAssociateDrone(int firmwareStatus, String sn, int workingStatus) {
        this.firmwareStatus = firmwareStatus;
        this.sn = sn != null ? sn : "";
        this.workingStatus = workingStatus;
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
        FirmwareUpgradeAssociateDrone that = (FirmwareUpgradeAssociateDrone) o;
        return firmwareStatus == that.firmwareStatus
                && Objects.equals(sn, that.sn)
                && workingStatus == that.workingStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firmwareStatus, sn, workingStatus);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeAssociateDrone{firmwareStatus=" + firmwareStatus + ", sn='" + sn + '\'' + ", workingStatus=" + workingStatus + '}';
    }
}
