// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 导入解锁证书请求参数 */
public class ImportUnlockLicenseParam {
    private String deviceSn = "";
    private String certUrl = "";

    public ImportUnlockLicenseParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }
    /** 证书 URL */
    public String getCertUrl() { return certUrl; }
    /** @param certUrl 证书 URL */
    public void setCertUrl(String certUrl) { this.certUrl = certUrl != null ? certUrl : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportUnlockLicenseParam that = (ImportUnlockLicenseParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(certUrl, that.certUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, certUrl);
    }

    @Override
    public String toString() {
        return "ImportUnlockLicenseParam{deviceSn='" + deviceSn + '\'' + ", certUrl='" + certUrl + '\'' + '}';
    }
}
