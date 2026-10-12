// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建报修订单 - 日志信息参数 */
public class MaintenanceOrderLogInfoParams {
    /** 设备 SN; */
    private String deviceSn = "";
    /** 设备模块; */
    private String module = "";
    /** 日志文件 OSS 路径; */
    private String objectKey = "";

    public MaintenanceOrderLogInfoParams() {
    }


    /** 设备 SN; */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备 SN; */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 设备模块; */
    public String getModule() {
        return module;
    }

    /** @param module 设备模块; */
    public void setModule(String module) {
        this.module = module != null ? module : "";
    }

    /** 日志文件 OSS 路径; */
    public String getObjectKey() {
        return objectKey;
    }

    /** @param objectKey 日志文件 OSS 路径; */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey != null ? objectKey : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderLogInfoParams that = (MaintenanceOrderLogInfoParams) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(module, that.module)
                && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, module, objectKey);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderLogInfoParams{deviceSn='" + deviceSn + '\'' + ", module='" + module + '\'' + ", objectKey='" + objectKey + '\'' + '}';
    }
}
