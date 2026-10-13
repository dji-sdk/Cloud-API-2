// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件升级设备项（SN、设备型号、升级类型、产品版本） */
public class FirmwareUpgradeDeviceItem {
    private String sn = "";
    private String deviceModelKey = "";
    /** 固件升级类型 */
    private int firmwareUpgradeType = 0;
    private String productVersion = "";

    public FirmwareUpgradeDeviceItem() {
    }


    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** @param sn 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn != null ? sn : "";
    }

    /** 设备型号标识 */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** @param deviceModelKey 设备型号标识 */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
    }

    /** 固件升级类型 */
    public int getFirmwareUpgradeType() {
        return firmwareUpgradeType;
    }

    /** @param firmwareUpgradeType 固件升级类型 */
    public void setFirmwareUpgradeType(int firmwareUpgradeType) {
        this.firmwareUpgradeType = firmwareUpgradeType;
    }

    /** 产品版本 */
    public String getProductVersion() {
        return productVersion;
    }

    /** @param productVersion 产品版本 */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion != null ? productVersion : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareUpgradeDeviceItem that = (FirmwareUpgradeDeviceItem) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && firmwareUpgradeType == that.firmwareUpgradeType
                && Objects.equals(productVersion, that.productVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, deviceModelKey, firmwareUpgradeType, productVersion);
    }

    @Override
    public String toString() {
        return "FirmwareUpgradeDeviceItem{"
                + "sn='" + sn + '\''
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", firmwareUpgradeType=" + firmwareUpgradeType
                + ", productVersion='" + productVersion + '\''
                + '}';
    }
}
