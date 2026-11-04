// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 联通性检测单条结果（v1.1 §8）。
 *
 * <p>后端 {@code rtt} 走 int64 → JSON 字符串编码，SDK 侧转为 long；不可达时为 0。
 */
public final class CascadeConnectivityItem {
    private String nodeId;
    private String name;
    private boolean reachable;
    private long rtt;

    public CascadeConnectivityItem() {
        this("", "", false, 0L);
    }

    /**
     * 构造 CascadeConnectivityItem。
     *
     * @param nodeId    下级节点 MDC
     * @param name      平台名称
     * @param reachable 是否可达
     * @param rtt       往返时延（毫秒），不可达时 0
     */
    public CascadeConnectivityItem(String nodeId, String name, boolean reachable, long rtt) {
        this.nodeId = nodeId != null ? nodeId : "";
        this.name = name != null ? name : "";
        this.reachable = reachable;
        this.rtt = rtt;
    }

    /** 下级节点 MDC */
    public String getNodeId() { return nodeId; }
    /** 下级节点 MDC */
    public void setNodeId(String nodeId) { this.nodeId = nodeId != null ? nodeId : ""; }

    /** 平台名称 */
    public String getName() { return name; }
    /** 平台名称 */
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** 是否可达 */
    public boolean isReachable() { return reachable; }
    /** 是否可达 */
    public void setReachable(boolean reachable) { this.reachable = reachable; }

    /** 往返时延（毫秒） */
    public long getRtt() { return rtt; }
    /** 往返时延（毫秒） */
    public void setRtt(long rtt) { this.rtt = rtt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeConnectivityItem that = (CascadeConnectivityItem) o;
        return reachable == that.reachable && rtt == that.rtt
                && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, name, reachable, rtt);
    }

    @Override
    public String toString() {
        return "CascadeConnectivityItem{nodeId='" + nodeId + "', name='" + name
                + "', reachable=" + reachable + ", rtt=" + rtt + "}";
    }
}
