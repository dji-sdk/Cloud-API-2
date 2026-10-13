// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 校验解锁证书请求参数 */
public class CheckUnlockLicenseParam {
    private String fileKey = "";
    private String deviceSn = "";

    public CheckUnlockLicenseParam() {
    }


    /** 文件 key */
    public String getFileKey() {
        return fileKey;
    }

    /** @param fileKey 文件 key */
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey != null ? fileKey : "";
    }

    /** 设备序列号(SN) */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备序列号(SN) */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckUnlockLicenseParam that = (CheckUnlockLicenseParam) o;
        return Objects.equals(fileKey, that.fileKey)
                && Objects.equals(deviceSn, that.deviceSn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileKey, deviceSn);
    }

    @Override
    public String toString() {
        return "CheckUnlockLicenseParam{fileKey='" + fileKey + '\'' + ", deviceSn='" + deviceSn + '\'' + '}';
    }
}
