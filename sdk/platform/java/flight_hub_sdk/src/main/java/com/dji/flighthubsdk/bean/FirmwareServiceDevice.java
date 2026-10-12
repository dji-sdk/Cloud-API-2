// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件服务中的单个设备信息（SN、升级类型、产品版本） */
public final class FirmwareServiceDevice {

    private String sn;
    private int firmwareUpgradeType;
    private String productVersion;
    public FirmwareServiceDevice() {
    }

/**
     * 构造 FirmwareServiceDevice。
     *
     * @param sn 设备序列号
     * @param firmwareUpgradeType 固件升级类型
     * @param productVersion 产品版本
     */
    public FirmwareServiceDevice(String sn, int firmwareUpgradeType, String productVersion) {
        this.sn = sn != null ? sn : "";
        this.firmwareUpgradeType = firmwareUpgradeType;
        this.productVersion = productVersion != null ? productVersion : "";
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 固件升级类型 */
    public int getFirmwareUpgradeType() {
        return firmwareUpgradeType;
    }

    /** 固件升级类型 */
    public void setFirmwareUpgradeType(int firmwareUpgradeType) {
        this.firmwareUpgradeType = firmwareUpgradeType;
    }

    /** 产品版本 */
    public String getProductVersion() {
        return productVersion;
    }

    /** 产品版本 */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareServiceDevice that = (FirmwareServiceDevice) o;
        return Objects.equals(sn, that.sn)
                && firmwareUpgradeType == that.firmwareUpgradeType
                && Objects.equals(productVersion, that.productVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, firmwareUpgradeType, productVersion);
    }

    @Override
    public String toString() {
        return "FirmwareServiceDevice{sn='" + sn + '\'' + ", firmwareUpgradeType=" + firmwareUpgradeType + ", productVersion='" + productVersion + '\'' + '}';
    }
}
