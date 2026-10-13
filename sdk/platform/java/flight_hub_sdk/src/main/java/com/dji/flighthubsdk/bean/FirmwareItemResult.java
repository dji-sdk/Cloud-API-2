// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件列表项（包含 ID、创建时间、设备型号、状态、版本、描述） */
public final class FirmwareItemResult {

    private int id;
    private String createdAt;
    private String deviceModelKey;
    private String status;
    private String version;
    private String desc;
    public FirmwareItemResult() {
    }

/**
     * 构造 FirmwareItemResult。
     *
     * @param id 固件 ID
     * @param createdAt 创建时间
     * @param deviceModelKey 设备型号 Key
     * @param status 状态
     * @param version 版本号
     * @param desc 描述
     */
    public FirmwareItemResult(
            int id, String createdAt, String deviceModelKey, String status, String version, String desc) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : "";
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.status = status != null ? status : "";
        this.version = version != null ? version : "";
        this.desc = desc != null ? desc : "";
    }

    /** 固件 ID */
    public int getId() { return id; }
    /** 创建时间 */
    public String getCreatedAt() { return createdAt; }
    /** 设备型号 Key */
    public String getDeviceModelKey() { return deviceModelKey; }
    /** 状态 */
    public String getStatus() { return status; }
    /** 版本号 */
    public String getVersion() { return version; }
    /** 描述 */
    public String getDesc() { return desc; }

    /** 固件 ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 创建时间 */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** 设备型号 Key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 版本号 */
    public void setVersion(String version) {
        this.version = version;
    }

    /** 描述 */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareItemResult that = (FirmwareItemResult) o;
        return id == that.id
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(status, that.status)
                && Objects.equals(version, that.version)
                && Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                createdAt,
                deviceModelKey,
                status,
                version,
                desc);
    }

    @Override
    public String toString() {
        return "FirmwareItemResult{"
                + "id=" + id
                + ", createdAt='" + createdAt + '\''
                + ", deviceModelKey='" + deviceModelKey + '\''
                + ", status='" + status + '\''
                + ", version='" + version + '\''
                + ", desc='" + desc + '\''
                + '}';
    }
}
