// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件状态项，包含设备 SN、固件版本、固件状态、设备型号及关联的升级服务 */
public final class FirmwareStatusItem {

    /** 设备序列号 */
    private String sn;
    /** 固件版本 */
    private String firmwareVersion;
    /** 固件状态, {1: 无需升级, 2: 待升级, 3: 一致性升级, 4: 固件升级中}; */
    private int firmwareStatus;
    /** 设备型号 Key */
    private String deviceModelKey;
    /** 关联的升级服务 */
    private FirmwareService services;
    public FirmwareStatusItem() {
    }

/**
     * 构造 FirmwareStatusItem。
     *
     * @param sn 设备序列号
     * @param firmwareVersion 固件版本
     * @param firmwareStatus 固件状态。枚举：1=无需升级, 2=待升级, 3=一致性升级, 4=固件升级中
     * @param deviceModelKey 设备型号 Key
     * @param services 关联的升级服务
     */
    public FirmwareStatusItem(
            String sn,
            String firmwareVersion,
            int firmwareStatus,
            String deviceModelKey,
            FirmwareService services) {
        this.sn = sn != null ? sn : "";
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.firmwareStatus = firmwareStatus;
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.services = services;
    }

    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 固件版本 */
    public String getFirmwareVersion() { return firmwareVersion; }
    /** 固件状态, {1: 无需升级, 2: 待升级, 3: 一致性升级, 4: 固件升级中}; */
    public int getFirmwareStatus() { return firmwareStatus; }
    /** 设备型号 Key */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** 关联的升级服务 */
    public FirmwareService getServices() { return services; }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 固件版本 */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /** @param firmwareStatus 固件状态, {1: 无需升级, 2: 待升级, 3: 一致性升级, 4: 固件升级中}; */
    public void setFirmwareStatus(int firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    /** 设备型号 Key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 关联的升级服务 */
    public void setServices(FirmwareService services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareStatusItem that = (FirmwareStatusItem) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(firmwareVersion, that.firmwareVersion)
                && firmwareStatus == that.firmwareStatus
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(services, that.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sn,
                firmwareVersion,
                firmwareStatus,
                deviceModelKey,
                services);
    }

    @Override
    public String toString() {
        return "FirmwareStatusItem{"
                + "sn='" + sn + '\''
                + ", firmwareVersion='" + firmwareVersion + '\''
                + ", firmwareStatus=" + firmwareStatus
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", services=" + services
                + '}';
    }
}
