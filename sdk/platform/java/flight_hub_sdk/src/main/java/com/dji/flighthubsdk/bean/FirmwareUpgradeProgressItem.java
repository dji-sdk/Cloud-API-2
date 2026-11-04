// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 单设备固件升级进度 */
public final class FirmwareUpgradeProgressItem {

    /** 设备序列号 */
    private String sn;
    /** 固件版本 */
    private String firmwareVersion;
    /** 固件状态。枚举：1=无需升级, 2=待升级, 3=一致性升级, 4=固件升级中 */
    private int firmwareStatus;
    /** 设备型号 Key */
    private String deviceModelKey;
    /** 关联的升级服务，升级进度见 services.progress */
    private FirmwareService services;
    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    private FirmwareUpgradeAssociateDrone associateDrone;
    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    private FirmwareUpgradeAssociateRelay associateRelay;
    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    private int deviceCode;
    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    private FirmwareUpgradeProgressOTA ota;
    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    private int workingStatus;
    public FirmwareUpgradeProgressItem() {
    }

/**
     * 构造 FirmwareUpgradeProgressItem。
     *
     * @param associateDrone 历史字段：关联无人机
     * @param associateRelay 历史字段：关联中继
     * @param deviceCode 历史字段：设备编码
     * @param firmwareStatus 固件状态。枚举：1=无需升级, 2=待升级, 3=一致性升级, 4=固件升级中
     * @param ota 历史字段：OTA 进度
     * @param sn 设备序列号
     * @param workingStatus 历史字段：工作状态
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

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 固件版本 */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /** 固件版本 */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /** 固件状态。枚举：1=无需升级, 2=待升级, 3=一致性升级, 4=固件升级中 */
    public int getFirmwareStatus() {
        return firmwareStatus;
    }

    /** 固件状态。枚举：1=无需升级, 2=待升级, 3=一致性升级, 4=固件升级中 */
    public void setFirmwareStatus(int firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    /** 设备型号 Key */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** 设备型号 Key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 关联的升级服务，升级进度见 services.progress */
    public FirmwareService getServices() {
        return services;
    }

    /** 关联的升级服务，升级进度见 services.progress */
    public void setServices(FirmwareService services) {
        this.services = services;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public FirmwareUpgradeAssociateDrone getAssociateDrone() {
        return associateDrone;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public void setAssociateDrone(FirmwareUpgradeAssociateDrone associateDrone) {
        this.associateDrone = associateDrone;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public FirmwareUpgradeAssociateRelay getAssociateRelay() {
        return associateRelay;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public void setAssociateRelay(FirmwareUpgradeAssociateRelay associateRelay) {
        this.associateRelay = associateRelay;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public int getDeviceCode() {
        return deviceCode;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public FirmwareUpgradeProgressOTA getOta() {
        return ota;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public void setOta(FirmwareUpgradeProgressOTA ota) {
        this.ota = ota;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
    public int getWorkingStatus() {
        return workingStatus;
    }

    /** 历史字段：后端当前不返回，保留用于兼容旧调用方 */
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
        return Objects.equals(sn, that.sn)
                && Objects.equals(firmwareVersion, that.firmwareVersion)
                && firmwareStatus == that.firmwareStatus
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(services, that.services)
                && Objects.equals(associateDrone, that.associateDrone)
                && Objects.equals(associateRelay, that.associateRelay)
                && deviceCode == that.deviceCode
                && Objects.equals(ota, that.ota)
                && workingStatus == that.workingStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sn,
                firmwareVersion,
                firmwareStatus,
                deviceModelKey,
                services,
                associateDrone,
                associateRelay,
                deviceCode,
                ota,
                workingStatus);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeProgressItem{"
                + "sn='" + sn + '\''
                + ", firmwareVersion='" + firmwareVersion + '\''
                + ", firmwareStatus=" + firmwareStatus
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", services=" + services
                + ", associateDrone=" + associateDrone
                + ", associateRelay=" + associateRelay
                + ", deviceCode=" + deviceCode
                + ", ota=" + ota
                + ", workingStatus=" + workingStatus
                + '}';
    }
}
