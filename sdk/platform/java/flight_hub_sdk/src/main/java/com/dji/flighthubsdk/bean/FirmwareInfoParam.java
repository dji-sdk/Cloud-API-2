// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件信息参数，用于上传/注册固件（设备型号、版本、描述、对象存储 key、MD5） */
public class FirmwareInfoParam {
    private String deviceModelKey = "";
    private String version = "";
    private String desc = "";
    private String objectKey = "";
    private String md5 = "";

    public FirmwareInfoParam() {
    }


    /** 设备型号 Key */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** @param deviceModelKey 设备型号 Key */
    public void setDeviceModelKey(String deviceModelKey) { this.deviceModelKey = deviceModelKey != null ? deviceModelKey : ""; }
    /** 版本号 */
    public String getVersion() { return version; }
    /** @param version 版本号 */
    public void setVersion(String version) { this.version = version != null ? version : ""; }
    /** 描述 */
    public String getDesc() { return desc; }
    /** @param desc 描述 */
    public void setDesc(String desc) { this.desc = desc != null ? desc : ""; }
    /** 对象存储 Key */
    public String getObjectKey() { return objectKey; }
    /** @param objectKey 对象存储 Key */
    public void setObjectKey(String objectKey) { this.objectKey = objectKey != null ? objectKey : ""; }
    /** MD5 校验值 */
    public String getMd5() { return md5; }
    /** @param md5 MD5 校验值 */
    public void setMd5(String md5) { this.md5 = md5 != null ? md5 : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareInfoParam that = (FirmwareInfoParam) o;
        return Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(version, that.version)
                && Objects.equals(desc, that.desc)
                && Objects.equals(objectKey, that.objectKey)
                && Objects.equals(md5, that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceModelKey,
                version,
                desc,
                objectKey,
                md5);
    }

    @Override
    public String toString() {
        return "FirmwareInfoParam{"
                + "deviceModelKey='" + deviceModelKey + '\''
                + ", version='" + version + '\''
                + ", desc='" + desc + '\''
                + ", objectKey='" + objectKey + '\''
                + ", md5='" + md5 + '\''
                + '}';
    }
}
