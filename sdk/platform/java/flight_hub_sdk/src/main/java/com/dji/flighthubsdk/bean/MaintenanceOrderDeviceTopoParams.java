// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 创建报修订单 - 设备拓扑参数 */
public class MaintenanceOrderDeviceTopoParams {
    private List<MaintenanceOrderTopoNodeParams> hosts = new ArrayList<>();
    private List<MaintenanceOrderTopoNodeParams> parents = new ArrayList<>();
    private List<MaintenanceOrderTopoNodeParams> nodes = new ArrayList<>();

    public MaintenanceOrderDeviceTopoParams() {
    }


    /** 主设备节点列表 */
    public List<MaintenanceOrderTopoNodeParams> getHosts() {
        return hosts;
    }

    /** @param hosts 主设备节点列表 */
    public void setHosts(List<MaintenanceOrderTopoNodeParams> hosts) {
        this.hosts = hosts != null ? hosts : new ArrayList<>();
    }

    /** 父设备节点列表 */
    public List<MaintenanceOrderTopoNodeParams> getParents() {
        return parents;
    }

    /** @param parents 父设备节点列表 */
    public void setParents(List<MaintenanceOrderTopoNodeParams> parents) {
        this.parents = parents != null ? parents : new ArrayList<>();
    }

    /** 子节点列表 */
    public List<MaintenanceOrderTopoNodeParams> getNodes() {
        return nodes;
    }

    /** @param nodes 子节点列表 */
    public void setNodes(List<MaintenanceOrderTopoNodeParams> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderDeviceTopoParams that = (MaintenanceOrderDeviceTopoParams) o;
        return Objects.equals(hosts, that.hosts)
                && Objects.equals(parents, that.parents)
                && Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hosts, parents, nodes);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderDeviceTopoParams{hosts=" + hosts + ", parents=" + parents + ", nodes=" + nodes + '}';
    }
}
