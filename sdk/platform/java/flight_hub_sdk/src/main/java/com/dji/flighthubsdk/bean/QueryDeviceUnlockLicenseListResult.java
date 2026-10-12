// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 查询设备解锁证书列表返回结果 */
public final class QueryDeviceUnlockLicenseListResult {

    private int result;
    private int deviceModelDomain;
    private boolean consistence;
    private List<DeviceUnlockLicenseItem> licenses;
    public QueryDeviceUnlockLicenseListResult() {
    }

/**
     * 构造 QueryDeviceUnlockLicenseListResult。
     *
     * @param result 结果
     * @param deviceModelDomain 设备模型域
     * @param consistence 一致性
     * @param licenses 证书列表
     */
    public QueryDeviceUnlockLicenseListResult(
            int result, int deviceModelDomain, boolean consistence, List<DeviceUnlockLicenseItem> licenses) {
        this.result = result;
        this.deviceModelDomain = deviceModelDomain;
        this.consistence = consistence;
        this.licenses = licenses != null ? licenses : Collections.emptyList();
    }

    /** 结果 */
    public int getResult() { return result; }
    /** 设备模型域 */
    public int getDeviceModelDomain() { return deviceModelDomain; }
    /** 一致性 */
    public boolean isConsistence() { return consistence; }
    /** 证书列表 */
    public List<DeviceUnlockLicenseItem> getLicenses() { return licenses; }

    /** 结果 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 设备模型域 */
    public void setDeviceModelDomain(int deviceModelDomain) {
        this.deviceModelDomain = deviceModelDomain;
    }

    /** 一致性 */
    public void setConsistence(boolean consistence) {
        this.consistence = consistence;
    }

    /** 证书列表 */
    public void setLicenses(List<DeviceUnlockLicenseItem> licenses) {
        this.licenses = licenses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryDeviceUnlockLicenseListResult that = (QueryDeviceUnlockLicenseListResult) o;
        return result == that.result
                && deviceModelDomain == that.deviceModelDomain
                && consistence == that.consistence
                && Objects.equals(licenses, that.licenses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, deviceModelDomain, consistence, licenses);
    }

    @Override
    public String toString() {
        return "QueryDeviceUnlockLicenseListResult{"
                + "result=" + result
                + ", deviceModelDomain=" + deviceModelDomain
                + ", consistence=" + consistence
                + ", licenses=" + licenses
                + '}';
    }
}
