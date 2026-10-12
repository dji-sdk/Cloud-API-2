// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 切换解锁证书启用状态请求参数 */
public class SwitchUnlockLicenseParam {
    private String deviceSn = "";
    private int licenseId = 0;
    private boolean enable = false;

    public SwitchUnlockLicenseParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }
    /** 证书ID */
    public int getLicenseId() { return licenseId; }
    /** @param licenseId 证书ID */
    public void setLicenseId(int licenseId) { this.licenseId = licenseId; }
    /** 是否启用 */
    public boolean isEnable() { return enable; }

    /** @param enable 是否启用 */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SwitchUnlockLicenseParam that = (SwitchUnlockLicenseParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && licenseId == that.licenseId
                && enable == that.enable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, licenseId, enable);
    }

    @Override
    public String toString() {
        return "SwitchUnlockLicenseParam{deviceSn='" + deviceSn + '\'' + ", licenseId=" + licenseId + ", enable=" + enable + '}';
    }
}
