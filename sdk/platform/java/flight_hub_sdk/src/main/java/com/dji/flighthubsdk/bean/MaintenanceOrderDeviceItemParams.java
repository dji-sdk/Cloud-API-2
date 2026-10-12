// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建报修订单 - 设备信息参数 */
public class MaintenanceOrderDeviceItemParams {
    /** 设备型号 key; */
    private String deviceModelKey = "";
    private String deviceSn = "";
    /** 设备机型领域; */
    private String module = "";
    /** 铭牌 SN，无则传空字符串; */
    private String nameplateSn = "";
    /** 报修来源类型：0=定损 2=日志反馈 3=直接报修; */
    private int sourceType;
    /** 来源关联 ID, 来源关联 ID，无则传空字符串; */
    private String sourceRefId = "";

    public MaintenanceOrderDeviceItemParams() {
    }


    /** 设备型号 key; */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** @param deviceModelKey 设备型号 key; */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
    }

    /** 设备序列号 */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 设备机型领域; */
    public String getModule() {
        return module;
    }

    /** @param module 设备机型领域; */
    public void setModule(String module) {
        this.module = module != null ? module : "";
    }

    /** 铭牌 SN，无则传空字符串; */
    public String getNameplateSn() {
        return nameplateSn;
    }

    /** @param nameplateSn 铭牌 SN，无则传空字符串; */
    public void setNameplateSn(String nameplateSn) {
        this.nameplateSn = nameplateSn != null ? nameplateSn : "";
    }

    /** 报修来源类型：0=定损 2=日志反馈 3=直接报修; */
    public int getSourceType() {
        return sourceType;
    }

    /** @param sourceType 报修来源类型：0=定损 2=日志反馈 3=直接报修; */
    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    /** 来源关联 ID, 来源关联 ID，无则传空字符串; */
    public String getSourceRefId() {
        return sourceRefId;
    }

    /** @param sourceRefId 来源关联 ID, 来源关联 ID，无则传空字符串; */
    public void setSourceRefId(String sourceRefId) {
        this.sourceRefId = sourceRefId != null ? sourceRefId : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderDeviceItemParams that = (MaintenanceOrderDeviceItemParams) o;
        return Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(module, that.module)
                && Objects.equals(nameplateSn, that.nameplateSn)
                && sourceType == that.sourceType
                && Objects.equals(sourceRefId, that.sourceRefId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceModelKey,
                deviceSn,
                module,
                nameplateSn,
                sourceType,
                sourceRefId);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderDeviceItemParams{"
                + "deviceModelKey='" + deviceModelKey + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", module='" + module + '\''
                + ", nameplateSn='" + nameplateSn + '\''
                + ", sourceType=" + sourceType
                + ", sourceRefId='" + sourceRefId + '\''
                + '}';
    }
}
