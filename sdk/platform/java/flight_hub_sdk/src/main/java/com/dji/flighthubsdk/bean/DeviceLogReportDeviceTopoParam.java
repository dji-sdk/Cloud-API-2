// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 设备日志上报 - 设备拓扑参数 */
public class DeviceLogReportDeviceTopoParam {
    /** 父设备列表 */
    private List<DeviceLogReportDeviceTopoItemParam> parents = new ArrayList<>();
    /** 主机设备列表 */
    private List<DeviceLogReportDeviceTopoItemParam> hosts = new ArrayList<>();

    public DeviceLogReportDeviceTopoParam() {
    }


    /** 父设备列表 */
    public List<DeviceLogReportDeviceTopoItemParam> getParents() { return parents; }
    /** @param parents 父设备列表 */
    public void setParents(List<DeviceLogReportDeviceTopoItemParam> parents) { this.parents = parents != null ? parents : new ArrayList<>(); }
    /** 主机设备列表 */
    public List<DeviceLogReportDeviceTopoItemParam> getHosts() { return hosts; }
    /** @param hosts 主机设备列表 */
    public void setHosts(List<DeviceLogReportDeviceTopoItemParam> hosts) { this.hosts = hosts != null ? hosts : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogReportDeviceTopoParam that = (DeviceLogReportDeviceTopoParam) o;
        return Objects.equals(parents, that.parents)
                && Objects.equals(hosts, that.hosts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parents, hosts);
    }

    @Override
    public String toString() {
        return "DeviceLogReportDeviceTopoParam{parents=" + parents + ", hosts=" + hosts + '}';
    }
}
