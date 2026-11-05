// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 新增子节点结果（对齐 v1.0 §1）。 */
public final class AddChildNodeResult {
    private String nodeId;
    private String grantId;
    private String parentId;
    private String executedBy;
    private boolean enabled;

    public AddChildNodeResult() {
        this("", "", "", "", true);
    }

    /**
     * 构造 AddChildNodeResult。
     *
     * @param nodeId     新节点 ID
     * @param grantId    授权 ID
     * @param parentId   父节点 ID
     * @param executedBy 执行方节点 ID
     * @param enabled    是否已启用（配网后默认启用）
     */
    public AddChildNodeResult(String nodeId, String grantId, String parentId,
                              String executedBy, boolean enabled) {
        this.nodeId = nodeId != null ? nodeId : "";
        this.grantId = grantId != null ? grantId : "";
        this.parentId = parentId != null ? parentId : "";
        this.executedBy = executedBy != null ? executedBy : "";
        this.enabled = enabled;
    }

    /** 新节点 ID */
    public String getNodeId() { return nodeId; }
    /** 新节点 ID */
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    /** 授权 ID */
    public String getGrantId() { return grantId; }
    /** 授权 ID */
    public void setGrantId(String grantId) { this.grantId = grantId; }

    /** 父节点 ID */
    public String getParentId() { return parentId; }
    /** 父节点 ID */
    public void setParentId(String parentId) { this.parentId = parentId; }

    /** 执行方节点 ID */
    public String getExecutedBy() { return executedBy; }
    /** 执行方节点 ID */
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    /** 是否已启用 */
    public boolean isEnabled() { return enabled; }
    /** 是否已启用 */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddChildNodeResult that = (AddChildNodeResult) o;
        return enabled == that.enabled && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(grantId, that.grantId) && Objects.equals(parentId, that.parentId)
                && Objects.equals(executedBy, that.executedBy);
    }

    @Override
    public int hashCode() { return Objects.hash(nodeId, grantId, parentId, executedBy, enabled); }

    @Override
    public String toString() {
        return "AddChildNodeResult{nodeId='" + nodeId + "', grantId='" + grantId
                + "', parentId='" + parentId + "', executedBy='" + executedBy
                + "', enabled=" + enabled + "}";
    }
}
