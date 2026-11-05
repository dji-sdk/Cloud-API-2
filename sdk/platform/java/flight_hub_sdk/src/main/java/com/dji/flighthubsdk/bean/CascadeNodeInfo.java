// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 级联节点信息（对齐 v1.1 §NodeRef / §ChildNodeRef）。
 *
 * <p>v1.1 两个列表接口的节点对象差异字段：
 * <ul>
 *   <li>§5 下级列表（/children）出参带 {@code networked}（是否已组网），不带 {@code netStatus}；</li>
 *   <li>§8 组网列表（/nodes/index）出参带 {@code netStatus}（{@code accept}/{@code reject}/空），不带 {@code networked}。</li>
 * </ul>
 * SDK 复用同一 POJO，未由后端下发的差异字段保持默认值（{@code false} / 空串）。
 *
 * <p>v1.1 wire 变更：{@code online} 由 bool 改为 string 枚举，取值 {@code "online"} / {@code "offline"}。
 */
public final class CascadeNodeInfo {
    private String nodeId;
    private String name;
    private String code;
    private String parentId;
    private int level;
    private boolean enabled;
    private String online;
    private String address;
    private boolean networked;
    private String netStatus;

    public CascadeNodeInfo() {
        this("", "", "", "", 0, false, "", "", false, "");
    }

    /**
     * 构造 CascadeNodeInfo。
     *
     * @param nodeId    16 位节点 ID
     * @param name      节点名称
     * @param code      节点编码
     * @param parentId  父节点 ID
     * @param level     节点层级（总部=1，最大 5）
     * @param enabled   是否已启用（父节点授权）
     * @param online    在线态字符串：{@code "online"} / {@code "offline"}
     * @param address   节点接入地址（host:port 或 tls://host:port）
     * @param networked §5 下级列表差异字段：是否已组网（默认 {@code false}）
     * @param netStatus §8 组网列表差异字段：{@code "accept"} / {@code "reject"} / 空（无记录）
     */
    public CascadeNodeInfo(String nodeId, String name, String code, String parentId,
                           int level, boolean enabled, String online, String address,
                           boolean networked, String netStatus) {
        this.nodeId = nodeId != null ? nodeId : "";
        this.name = name != null ? name : "";
        this.code = code != null ? code : "";
        this.parentId = parentId != null ? parentId : "";
        this.level = level;
        this.enabled = enabled;
        this.online = online != null ? online : "";
        this.address = address != null ? address : "";
        this.networked = networked;
        this.netStatus = netStatus != null ? netStatus : "";
    }

    /** 节点 ID */
    public String getNodeId() { return nodeId; }
    /** 节点 ID */
    public void setNodeId(String nodeId) { this.nodeId = nodeId != null ? nodeId : ""; }

    /** 节点名称 */
    public String getName() { return name; }
    /** 节点名称 */
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** 节点编码 */
    public String getCode() { return code; }
    /** 节点编码 */
    public void setCode(String code) { this.code = code != null ? code : ""; }

    /** 父节点 ID */
    public String getParentId() { return parentId; }
    /** 父节点 ID */
    public void setParentId(String parentId) { this.parentId = parentId != null ? parentId : ""; }

    /** 节点层级 */
    public int getLevel() { return level; }
    /** 节点层级 */
    public void setLevel(int level) { this.level = level; }

    /** 是否已启用 */
    public boolean isEnabled() { return enabled; }
    /** 是否已启用 */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** 在线态字符串：{@code "online"} / {@code "offline"}（v1.1） */
    public String getOnline() { return online; }
    /** 在线态字符串：{@code "online"} / {@code "offline"}（v1.1） */
    public void setOnline(String online) { this.online = online != null ? online : ""; }

    /** 接入地址 */
    public String getAddress() { return address; }
    /** 接入地址 */
    public void setAddress(String address) { this.address = address != null ? address : ""; }

    /** §5 下级列表：是否已组网（组网列表接口下永远为默认 {@code false}） */
    public boolean isNetworked() { return networked; }
    /** §5 下级列表：是否已组网（组网列表接口下永远为默认 {@code false}） */
    public void setNetworked(boolean networked) { this.networked = networked; }

    /** §8 组网列表：组网状态 {@code "accept"} / {@code "reject"} / 空（下级列表接口下永远为空） */
    public String getNetStatus() { return netStatus; }
    /** §8 组网列表：组网状态 {@code "accept"} / {@code "reject"} / 空（下级列表接口下永远为空） */
    public void setNetStatus(String netStatus) { this.netStatus = netStatus != null ? netStatus : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeNodeInfo that = (CascadeNodeInfo) o;
        return level == that.level && enabled == that.enabled && networked == that.networked
                && Objects.equals(nodeId, that.nodeId) && Objects.equals(name, that.name)
                && Objects.equals(code, that.code) && Objects.equals(parentId, that.parentId)
                && Objects.equals(online, that.online) && Objects.equals(address, that.address)
                && Objects.equals(netStatus, that.netStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, name, code, parentId, level, enabled, online, address,
                networked, netStatus);
    }

    @Override
    public String toString() {
        return "CascadeNodeInfo{nodeId='" + nodeId + "', name='" + name + "', code='" + code
                + "', parentId='" + parentId + "', level=" + level + ", enabled=" + enabled
                + ", online='" + online + "', address='" + address + "'"
                + ", networked=" + networked + ", netStatus='" + netStatus + "'}";
    }
}