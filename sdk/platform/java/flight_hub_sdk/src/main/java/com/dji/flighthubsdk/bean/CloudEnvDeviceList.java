// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 一组拓扑设备：host（飞行器）/ nodes（子节点，如电池、云台）/ parents（父级，如遥控器）。 */
public class CloudEnvDeviceList {
    private List<CloudEnvDeviceInfo> host = new ArrayList<>();
    private List<CloudEnvDeviceInfo> nodes = new ArrayList<>();
    private List<CloudEnvDeviceInfo> parents = new ArrayList<>();

    public CloudEnvDeviceList() {
    }

    /** 主机（飞行器）设备列表 */
    public List<CloudEnvDeviceInfo> getHost() {
        return host;
    }

    /** @param host 主机（飞行器）设备列表 */
    public void setHost(List<CloudEnvDeviceInfo> host) {
        this.host = host != null ? host : new ArrayList<>();
    }

    /** 子节点设备（电池、云台等）列表 */
    public List<CloudEnvDeviceInfo> getNodes() {
        return nodes;
    }

    /** @param nodes 子节点设备（电池、云台等）列表 */
    public void setNodes(List<CloudEnvDeviceInfo> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    /** 父级设备（遥控器等）列表 */
    public List<CloudEnvDeviceInfo> getParents() {
        return parents;
    }

    /** @param parents 父级设备（遥控器等）列表 */
    public void setParents(List<CloudEnvDeviceInfo> parents) {
        this.parents = parents != null ? parents : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvDeviceList that = (CloudEnvDeviceList) o;
        return Objects.equals(host, that.host)
                && Objects.equals(nodes, that.nodes)
                && Objects.equals(parents, that.parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, nodes, parents);
    }

    @Override
    public String toString() {
        return "CloudEnvDeviceList{host=" + host + ", nodes=" + nodes + ", parents=" + parents + '}';
    }
}
