// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 响应上级平台组网请求入参（对齐 v1.1 §7 POST /cascade/api/parent/respond）。
 *
 * <p>{@code status} 只允许 {@code "accept"} / {@code "reject"}，其它取值由 SDK/后端拒绝。
 */
public final class CascadeParentRespondRequest {
    private String nodeId;
    private String parentId;
    private String status;

    public CascadeParentRespondRequest() {
        this("", "", "");
    }

    /**
     * 构造 CascadeParentRespondRequest。
     *
     * @param nodeId   本节点 nodeID
     * @param parentId 上级节点 nodeID
     * @param status   {@code "accept"}（接受）/ {@code "reject"}（拒绝）
     */
    public CascadeParentRespondRequest(String nodeId, String parentId, String status) {
        this.nodeId = nodeId != null ? nodeId : "";
        this.parentId = parentId != null ? parentId : "";
        this.status = status != null ? status : "";
    }

    /** 本节点 nodeID */
    public String getNodeId() { return nodeId; }
    /** 本节点 nodeID */
    public void setNodeId(String nodeId) { this.nodeId = nodeId != null ? nodeId : ""; }

    /** 上级节点 nodeID */
    public String getParentId() { return parentId; }
    /** 上级节点 nodeID */
    public void setParentId(String parentId) { this.parentId = parentId != null ? parentId : ""; }

    /** {@code "accept"} / {@code "reject"} */
    public String getStatus() { return status; }
    /** {@code "accept"} / {@code "reject"} */
    public void setStatus(String status) { this.status = status != null ? status : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeParentRespondRequest that = (CascadeParentRespondRequest) o;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(parentId, that.parentId)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, parentId, status);
    }

    @Override
    public String toString() {
        return "CascadeParentRespondRequest{nodeId='" + nodeId
                + "', parentId='" + parentId + "', status='" + status + "'}";
    }
}