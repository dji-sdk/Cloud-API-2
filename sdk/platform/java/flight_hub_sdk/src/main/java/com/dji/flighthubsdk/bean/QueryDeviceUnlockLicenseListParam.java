// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 查询设备解锁证书列表请求参数 */
public class QueryDeviceUnlockLicenseListParam {
    private String deviceSn = "";
    /** 设备类型域。枚举：0=飞机, 3=机场 */
    private int deviceModelDomain = 0;
    /** 证书类型过滤 */
    private String licenseType = "";
    /** 证书 ID 过滤 */
    private String licenseId = "";
    /** 是否返回详情 */
    private boolean detail = false;

    public QueryDeviceUnlockLicenseListParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 设备类型域。枚举：0=飞机, 3=机场 */
    public int getDeviceModelDomain() {
        return deviceModelDomain;
    }

    /** @param deviceModelDomain 设备类型域。枚举：0=飞机, 3=机场 */
    public void setDeviceModelDomain(int deviceModelDomain) {
        this.deviceModelDomain = deviceModelDomain;
    }

    /** 证书类型过滤 */
    public String getLicenseType() {
        return licenseType;
    }

    /** @param licenseType 证书类型过滤 */
    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType != null ? licenseType : "";
    }

    /** 证书 ID 过滤 */
    public String getLicenseId() {
        return licenseId;
    }

    /** @param licenseId 证书 ID 过滤 */
    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId != null ? licenseId : "";
    }

    /** 是否返回详情 */
    public boolean isDetail() {
        return detail;
    }

    /** @param detail 是否返回详情 */
    public void setDetail(boolean detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryDeviceUnlockLicenseListParam that = (QueryDeviceUnlockLicenseListParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && deviceModelDomain == that.deviceModelDomain
                && Objects.equals(licenseType, that.licenseType)
                && Objects.equals(licenseId, that.licenseId)
                && detail == that.detail;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceSn,
                deviceModelDomain,
                licenseType,
                licenseId,
                detail);
    }

    @Override
    public String toString() {
        return "QueryDeviceUnlockLicenseListParam{"
                + "deviceSn='" + deviceSn + '\''
                + ", deviceModelDomain=" + deviceModelDomain
                + ", licenseType='" + licenseType + '\''
                + ", licenseId='" + licenseId + '\''
                + ", detail=" + detail
                + '}';
    }
}
