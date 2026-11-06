// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 上级平台信息（对齐 v1.1 §6）。
 *
 * <p>v1.1 变更：
 * <ul>
 *   <li>新增 {@code nodeID} 字段（无父时为空）；</li>
 *   <li>{@code online} 由 bool 改为 string 枚举，取值 {@code "online"} / {@code "offline"}；</li>
 *   <li>新增 {@code netStatus} 字段：{@code "accept"} / {@code "reject"} / 空（无记录）。</li>
 * </ul>
 */
public final class CascadeParentNodeInfo {
    private String nodeId;
    private String name;
    private String online;
    private int depth;
    private boolean enabled;
    private long heartbeatPeriod;
    private String netStatus;

    public CascadeParentNodeInfo() {
        this("", "", "", 1, false, 0L, "");
    }

    /**
     * 构造 CascadeParentNodeInfo。
     *
     * @param nodeId          父节点 ID（v1.1 新增；无父时为空）
     * @param name            父节点名称
     * @param online          在线态字符串：{@code "online"} / {@code "offline"}
     * @param depth           拓扑深度
     * @param enabled         父节点是否已启用
     * @param heartbeatPeriod 心跳周期（秒；后端 int64 → JSON 字符串编码）
     * @param netStatus       组网状态：{@code "accept"} / {@code "reject"} / 空（v1.1 新增）
     */
    public CascadeParentNodeInfo(String nodeId, String name, String online,
                                 int depth, boolean enabled, long heartbeatPeriod,
                                 String netStatus) {
        this.nodeId = nodeId != null ? nodeId : "";
        this.name = name != null ? name : "";
        this.online = online != null ? online : "";
        this.depth = depth;
        this.enabled = enabled;
        this.heartbeatPeriod = heartbeatPeriod;
        this.netStatus = netStatus != null ? netStatus : "";
    }

    /** 父节点 ID（v1.1 新增） */
    public String getNodeId() { return nodeId; }
    /** 父节点 ID（v1.1 新增） */
    public void setNodeId(String nodeId) { this.nodeId = nodeId != null ? nodeId : ""; }

    /** 父节点名称 */
    public String getName() { return name; }
    /** 父节点名称 */
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** 在线态字符串：{@code "online"} / {@code "offline"}（v1.1） */
    public String getOnline() { return online; }
    /** 在线态字符串：{@code "online"} / {@code "offline"}（v1.1） */
    public void setOnline(String online) { this.online = online != null ? online : ""; }

    /** 拓扑深度 */
    public int getDepth() { return depth; }
    /** 拓扑深度 */
    public void setDepth(int depth) { this.depth = depth; }

    /** 是否已启用 */
    public boolean isEnabled() { return enabled; }
    /** 是否已启用 */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** 心跳周期（秒） */
    public long getHeartbeatPeriod() { return heartbeatPeriod; }
    /** 心跳周期（秒） */
    public void setHeartbeatPeriod(long heartbeatPeriod) { this.heartbeatPeriod = heartbeatPeriod; }

    /** 组网状态：{@code "accept"} / {@code "reject"} / 空（v1.1 新增） */
    public String getNetStatus() { return netStatus; }
    /** 组网状态：{@code "accept"} / {@code "reject"} / 空（v1.1 新增） */
    public void setNetStatus(String netStatus) { this.netStatus = netStatus != null ? netStatus : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeParentNodeInfo that = (CascadeParentNodeInfo) o;
        return depth == that.depth && enabled == that.enabled
                && heartbeatPeriod == that.heartbeatPeriod
                && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(name, that.name)
                && Objects.equals(online, that.online)
                && Objects.equals(netStatus, that.netStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, name, online, depth, enabled, heartbeatPeriod, netStatus);
    }

    @Override
    public String toString() {
        return "CascadeParentNodeInfo{nodeId='" + nodeId + "', name='" + name + "', online='" + online
                + "', depth=" + depth + ", enabled=" + enabled
                + ", heartbeatPeriod=" + heartbeatPeriod
                + ", netStatus='" + netStatus + "'}";
    }
}
