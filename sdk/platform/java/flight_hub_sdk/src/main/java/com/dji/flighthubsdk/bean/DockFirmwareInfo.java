// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场固件信息。 */
public final class DockFirmwareInfo {

    /** 固件版本, such as "00.01.0439"; */
    private String firmwareVersion;
    /** 固件一致性, { 0: 一致, 1: 不一致 }; */
    private int compatibleStatus;
    /** 固件升级状态, { 0: 未升级, 1: 升级中 }; */
    private int firmwareUpgradeStatus;
    public DockFirmwareInfo() {
    }

/**
     * 构造 DockFirmwareInfo。
     *
     * @param firmwareVersion 固件版本
     * @param compatibleStatus 兼容状态
     * @param firmwareUpgradeStatus 固件升级状态
     */
    public DockFirmwareInfo(String firmwareVersion, int compatibleStatus, int firmwareUpgradeStatus) {
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

    /** 固件一致性, { 0: 一致, 1: 不一致 }; */
    public int getCompatibleStatus() {
        return compatibleStatus;
    }

    /** @param compatibleStatus 固件一致性, { 0: 一致, 1: 不一致 }; */
    public void setCompatibleStatus(int compatibleStatus) {
        this.compatibleStatus = compatibleStatus;
    }

    /** 固件升级状态, { 0: 未升级, 1: 升级中 }; */
    public int getFirmwareUpgradeStatus() {
        return firmwareUpgradeStatus;
    }

    /** @param firmwareUpgradeStatus 固件升级状态, { 0: 未升级, 1: 升级中 }; */
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
        DockFirmwareInfo that = (DockFirmwareInfo) o;
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
        return "DockFirmwareInfo{firmwareVersion='" + firmwareVersion + '\''
                + ", compatibleStatus=" + compatibleStatus
                + ", firmwareUpgradeStatus=" + firmwareUpgradeStatus + '}';
    }
}
