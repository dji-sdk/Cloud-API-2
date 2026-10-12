// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 发起固件升级响应中的设备项 */
public final class FirmwareUpgradeResponseDeviceItemResult {

    private int firmwareUpgradeType;
    private String productVersion;
    private String sn;
    public FirmwareUpgradeResponseDeviceItemResult() {
    }

/**
     * 构造 FirmwareUpgradeResponseDeviceItemResult。
     *
     * @param firmwareUpgradeType 固件升级类型
     * @param productVersion 产品版本
     * @param sn 设备序列号
     */
    public FirmwareUpgradeResponseDeviceItemResult(int firmwareUpgradeType, String productVersion, String sn) {
        this.firmwareUpgradeType = firmwareUpgradeType;
        this.productVersion = productVersion != null ? productVersion : "";
        this.sn = sn != null ? sn : "";
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
        FirmwareUpgradeResponseDeviceItemResult that = (FirmwareUpgradeResponseDeviceItemResult) o;
        return firmwareUpgradeType == that.firmwareUpgradeType
                && Objects.equals(productVersion, that.productVersion)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firmwareUpgradeType, productVersion, sn);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeResponseDeviceItemResult{firmwareUpgradeType=" + firmwareUpgradeType + ", productVersion='" + productVersion + '\'' + ", sn='" + sn + '\'' + '}';
    }
}
