// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 日志报告设备日志文件。 */
public final class LogReportListResultDeviceLog {
    /** 设备序列号 */
    private String deviceSn;
    /** 模块编号 */
    private String module;
    /** 结果 */
    private int result;
    /** 日志分片列表 */
    private List<LogReportListResultDeviceLogItemList> list;
    /** 对象存储 Key */
    private String objectKey;
    public LogReportListResultDeviceLog() {
    }

    public LogReportListResultDeviceLog(String deviceSn, String module, int result,
            List<LogReportListResultDeviceLogItemList> list, String objectKey) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.module = module != null ? module : "";
        this.result = result;
        this.list = list != null ? list : Collections.emptyList();
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** 设备序列号 */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 模块编号 */
    public String getModule() {
        return module;
    }

    /** @param module 模块编号 */
    public void setModule(String module) {
        this.module = module;
    }

    /** 结果 */
    public int getResult() {
        return result;
    }

    /** @param result 结果 */
    public void setResult(int result) {
        this.result = result;
    }

    /** 日志分片列表 */
    public List<LogReportListResultDeviceLogItemList> getList() {
        return list;
    }

    /** @param list 日志分片列表 */
    public void setList(List<LogReportListResultDeviceLogItemList> list) {
        this.list = list;
    }

    /** 对象存储 Key */
    public String getObjectKey() {
        return objectKey;
    }

    /** @param objectKey 对象存储 Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultDeviceLog that = (LogReportListResultDeviceLog) o;
        return result == that.result
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(module, that.module)
                && Objects.equals(list, that.list)
                && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, module, result, list, objectKey);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceLog{deviceSn='" + deviceSn + '\'' + ", module='" + module + '\'' + ", result=" + result + ", list=" + list + ", objectKey='" + objectKey + '\'' + '}';
    }
}
