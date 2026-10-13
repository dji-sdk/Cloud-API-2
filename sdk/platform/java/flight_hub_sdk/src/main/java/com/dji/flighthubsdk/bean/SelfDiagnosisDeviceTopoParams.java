// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 设备定损 - 设备拓扑参数 */
public class SelfDiagnosisDeviceTopoParams {
    private String index = "";
    private List<SelfDiagnosisTopoNodeParams> hosts = new ArrayList<>();
    private List<SelfDiagnosisTopoNodeParams> parents = new ArrayList<>();
    private List<SelfDiagnosisTopoNodeParams> nodes = new ArrayList<>();
    private List<SelfDiagnosisTopoNodeParams> others = new ArrayList<>();
    private List<SelfDiagnosisTopoNodeParams> docks = new ArrayList<>();

    public SelfDiagnosisDeviceTopoParams() {
    }


    /** 索引 */
    public String getIndex() {
        return index;
    }

    /** @param index 索引 */
    public void setIndex(String index) {
        this.index = index != null ? index : "";
    }

    /** 主设备节点列表 */
    public List<SelfDiagnosisTopoNodeParams> getHosts() {
        return hosts;
    }

    /** @param hosts 主设备节点列表 */
    public void setHosts(List<SelfDiagnosisTopoNodeParams> hosts) {
        this.hosts = hosts != null ? hosts : new ArrayList<>();
    }

    /** 父设备节点列表 */
    public List<SelfDiagnosisTopoNodeParams> getParents() {
        return parents;
    }

    /** @param parents 父设备节点列表 */
    public void setParents(List<SelfDiagnosisTopoNodeParams> parents) {
        this.parents = parents != null ? parents : new ArrayList<>();
    }

    /** 子节点列表 */
    public List<SelfDiagnosisTopoNodeParams> getNodes() {
        return nodes;
    }

    /** @param nodes 子节点列表 */
    public void setNodes(List<SelfDiagnosisTopoNodeParams> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    /** 其他设备节点列表 */
    public List<SelfDiagnosisTopoNodeParams> getOthers() {
        return others;
    }

    /** @param others 其他设备节点列表 */
    public void setOthers(List<SelfDiagnosisTopoNodeParams> others) {
        this.others = others != null ? others : new ArrayList<>();
    }

    /** 机场节点列表 */
    public List<SelfDiagnosisTopoNodeParams> getDocks() {
        return docks;
    }

    /** @param docks 机场节点列表 */
    public void setDocks(List<SelfDiagnosisTopoNodeParams> docks) {
        this.docks = docks != null ? docks : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelfDiagnosisDeviceTopoParams that = (SelfDiagnosisDeviceTopoParams) o;
        return Objects.equals(index, that.index)
                && Objects.equals(hosts, that.hosts)
                && Objects.equals(parents, that.parents)
                && Objects.equals(nodes, that.nodes)
                && Objects.equals(others, that.others)
                && Objects.equals(docks, that.docks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                index,
                hosts,
                parents,
                nodes,
                others,
                docks);
    }

    @Override
    public String toString() {
        return "SelfDiagnosisDeviceTopoParams{"
                + "index='" + index + '\''
                + ", hosts=" + hosts
                + ", parents=" + parents
                + ", nodes=" + nodes
                + ", others=" + others
                + ", docks=" + docks
                + '}';
    }
}
