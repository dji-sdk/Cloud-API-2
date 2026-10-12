// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 日志报告列表结果设备拓扑。 */
public final class LogReportListResultDeviceTopo {
    /** 主机设备列表 */
    private List<LogReportListResultDeviceTopoItem> hosts;
    /** 父设备列表 */
    private List<LogReportListResultDeviceTopoItem> parents;
    public LogReportListResultDeviceTopo() {
    }

/**
     * 构造 LogReportListResultDeviceTopo。
     *
     * @param hosts hosts
     * @param parents 父节点列表
     */
    public LogReportListResultDeviceTopo(List<LogReportListResultDeviceTopoItem> hosts, List<LogReportListResultDeviceTopoItem> parents) {
        this.hosts = hosts != null ? hosts : Collections.emptyList();
        this.parents = parents != null ? parents : Collections.emptyList();
    }

    /** 主机设备列表 */
    public List<LogReportListResultDeviceTopoItem> getHosts() {
        return hosts;
    }

    /** @param hosts 主机设备列表 */
    public void setHosts(List<LogReportListResultDeviceTopoItem> hosts) {
        this.hosts = hosts;
    }

    /** 父设备列表 */
    public List<LogReportListResultDeviceTopoItem> getParents() {
        return parents;
    }

    /** @param parents 父设备列表 */
    public void setParents(List<LogReportListResultDeviceTopoItem> parents) {
        this.parents = parents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogReportListResultDeviceTopo that = (LogReportListResultDeviceTopo) o;
        return Objects.equals(hosts, that.hosts)
                && Objects.equals(parents, that.parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosts, parents);
    }

    @Override
    public String toString() {
        return "LogReportListResultDeviceTopo{hosts=" + hosts + ", parents=" + parents + '}';
    }
}
