// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 飞机固件状态。 */
public final class AircraftFirmwareStatus {

    /** 固件版本, such as "00.01.0439"; */
    private String firmwareVersion;
    /** 固件一致性, {"0: 一致, 1: 不一致"}; */
    private int compatibleStatus;
    /** 固件升级状态, {"0: 未升级, 1: 升级中"}; */
    private int firmwareUpgradeStatus;
    public AircraftFirmwareStatus() {
    }

/**
     * 构造 AircraftFirmwareStatus。
     *
     * @param firmwareVersion 固件版本
     * @param compatibleStatus 固件一致性
     * @param firmwareUpgradeStatus 固件升级状态
     */
    public AircraftFirmwareStatus(String firmwareVersion, int compatibleStatus, int firmwareUpgradeStatus) {
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.compatibleStatus = compatibleStatus;
        this.firmwareUpgradeStatus = firmwareUpgradeStatus;
    }

    /** 固件版本, such as "00.01.0439"; */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /** @param firmwareVersion 固件版本, such as "00.01.0439"; */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /** 固件一致性, {"0: 一致, 1: 不一致"}; */
    public int getCompatibleStatus() {
        return compatibleStatus;
    }

    /** @param compatibleStatus 固件一致性, {"0: 一致, 1: 不一致"}; */
    public void setCompatibleStatus(int compatibleStatus) {
        this.compatibleStatus = compatibleStatus;
    }

    /** 固件升级状态, {"0: 未升级, 1: 升级中"}; */
    public int getFirmwareUpgradeStatus() {
        return firmwareUpgradeStatus;
    }

    /** @param firmwareUpgradeStatus 固件升级状态, {"0: 未升级, 1: 升级中"}; */
    public void setFirmwareUpgradeStatus(int firmwareUpgradeStatus) {
        this.firmwareUpgradeStatus = firmwareUpgradeStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftFirmwareStatus that = (AircraftFirmwareStatus) o;
        return compatibleStatus == that.compatibleStatus
                && firmwareUpgradeStatus == that.firmwareUpgradeStatus
                && Objects.equals(firmwareVersion, that.firmwareVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firmwareVersion, compatibleStatus, firmwareUpgradeStatus);
    }

    @Override
    public String toString() {
        return "AircraftFirmwareStatus{firmwareVersion='" + firmwareVersion + '\''
                + ", compatibleStatus=" + compatibleStatus
                + ", firmwareUpgradeStatus=" + firmwareUpgradeStatus + '}';
    }
}
