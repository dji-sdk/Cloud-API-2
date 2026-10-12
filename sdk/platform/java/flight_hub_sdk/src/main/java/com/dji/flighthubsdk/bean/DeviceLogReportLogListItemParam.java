// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 设备日志上报 - 日志列表项参数 */
public class DeviceLogReportLogListItemParam {
    /** 设备 SN */
    private String deviceSn = "";
    /** 日志项列表 */
    private List<DeviceLogReportLogItemParam> list = new ArrayList<>();
    /** 模块名称 */
    private String module = "";

    public DeviceLogReportLogListItemParam() {
    }


    /** 设备 SN */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备 SN */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }
    /** 日志项列表 */
    public List<DeviceLogReportLogItemParam> getList() { return list; }
    /** @param list 日志项列表 */
    public void setList(List<DeviceLogReportLogItemParam> list) { this.list = list != null ? list : new ArrayList<>(); }
    /** 模块名称 */
    public String getModule() { return module; }
    /** @param module 模块名称 */
    public void setModule(String module) { this.module = module != null ? module : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogReportLogListItemParam that = (DeviceLogReportLogListItemParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(list, that.list)
                && Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, list, module);
    }

    @Override
    public String toString() {
        return "DeviceLogReportLogListItemParam{deviceSn='" + deviceSn + '\'' + ", list=" + list + ", module='" + module + '\'' + '}';
    }
}
