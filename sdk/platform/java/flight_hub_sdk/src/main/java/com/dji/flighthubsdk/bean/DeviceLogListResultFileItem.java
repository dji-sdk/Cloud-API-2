// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 设备日志列表结果文件条目。 */
public final class DeviceLogListResultFileItem {
    /** 设备 SN */
    private String deviceSn;
    /** 模块名称 */
    private String module;
    /** 查询结果码 */
    private int result;
    /** 日志项列表 */
    private List<DeviceLogListResultLogItem> list;
    private String objectKey;
    /** 日志类型 */
    private int logType;
    /** 责任日志类型 */
    private int responsibilityLogType;
    public DeviceLogListResultFileItem() {
    }

/**
     * 构造 DeviceLogListResultFileItem。
     *
     * @param deviceSn 设备序列号(SN)
     * @param module 模块名称
     * @param result 结果码
     * @param list 日志项列表
     * @param objectKey 对象存储 key
     * @param logType 日志类型
     * @param responsibilityLogType 责任日志类型
     */
    public DeviceLogListResultFileItem(
            String deviceSn,
            String module,
            int result,
            List<DeviceLogListResultLogItem> list,
            String objectKey,
            int logType,
            int responsibilityLogType) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.module = module != null ? module : "";
        this.result = result;
        this.list = list != null ? list : Collections.emptyList();
        this.objectKey = objectKey != null ? objectKey : "";
        this.logType = logType;
        this.responsibilityLogType = responsibilityLogType;
    }

    /** 设备 SN */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备 SN */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 模块名称 */
    public String getModule() {
        return module;
    }

    /** 模块名称 */
    public void setModule(String module) {
        this.module = module;
    }

    /** 查询结果码 */
    public int getResult() {
        return result;
    }

    /** @param result 查询结果码 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 日志项列表 */
    public List<DeviceLogListResultLogItem> getList() {
        return list;
    }

    /** 日志项列表 */
    public void setList(List<DeviceLogListResultLogItem> list) {
        this.list = list;
    }

    /** 对象存储 key */
    public String getObjectKey() {
        return objectKey;
    }

    /** 对象存储 key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /** 日志类型 */
    public int getLogType() {
        return logType;
    }

    /** 日志类型 */
    public void setLogType(int logType) {
        this.logType = logType;
    }

    /** 责任日志类型 */
    public int getResponsibilityLogType() {
        return responsibilityLogType;
    }

    /** 责任日志类型 */
    public void setResponsibilityLogType(int responsibilityLogType) {
        this.responsibilityLogType = responsibilityLogType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogListResultFileItem that = (DeviceLogListResultFileItem) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(module, that.module)
                && result == that.result
                && Objects.equals(list, that.list)
                && Objects.equals(objectKey, that.objectKey)
                && logType == that.logType
                && responsibilityLogType == that.responsibilityLogType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceSn,
                module,
                result,
                list,
                objectKey,
                logType,
                responsibilityLogType);
    }

    @Override
    public String toString() {
        return "DeviceLogListResultFileItem{"
                + "deviceSn='" + deviceSn + '\''
                + ", module='" + module + '\''
                + ", result=" + result
                + ", list=" + list
                + ", objectKey='" + objectKey + '\''
                + ", logType=" + logType
                + ", responsibilityLogType=" + responsibilityLogType
                + '}';
    }
}
