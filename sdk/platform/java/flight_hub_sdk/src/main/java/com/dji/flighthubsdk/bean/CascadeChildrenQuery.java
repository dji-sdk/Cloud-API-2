// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 子节点查询参数（对齐 v1.0 §4）。 */
public final class CascadeChildrenQuery {
    private String nodeId;
    private int page;
    private int size;
    private String name;
    private String status;

    public CascadeChildrenQuery() {
        this("", 0, 0, "", "");
    }

    /**
     * 构造 CascadeChildrenQuery。
     *
     * @param nodeId 节点 ID（空表示查询当前节点）
     * @param page   页码，1-based；&lt;=0 表示不下发到后端
     * @param size   每页条数；&lt;=0 表示不下发到后端
     * @param name   名称过滤（模糊匹配，空表示不过滤）
     * @param status 状态过滤："normal" / "offline" / ""（空表示不过滤）
     */
    public CascadeChildrenQuery(String nodeId, int page, int size, String name, String status) {
        this.nodeId = nodeId != null ? nodeId : "";
        this.page = page;
        this.size = size;
        this.name = name != null ? name : "";
        this.status = status != null ? status : "";
    }

    /** 节点 ID */
    public String getNodeId() { return nodeId; }
    /** 节点 ID */
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    /** 页码，1-based；&lt;=0 表示不下发到后端 */
    public int getPage() { return page; }
    /** 页码，1-based；&lt;=0 表示不下发到后端 */
    public void setPage(int page) { this.page = page; }

    /** 每页条数；&lt;=0 表示不下发到后端 */
    public int getSize() { return size; }
    /** 每页条数；&lt;=0 表示不下发到后端 */
    public void setSize(int size) { this.size = size; }

    /** 名称过滤 */
    public String getName() { return name; }
    /** 名称过滤 */
    public void setName(String name) { this.name = name; }

    /** 状态过滤："normal" / "offline" / "" */
    public String getStatus() { return status; }
    /** 状态过滤："normal" / "offline" / "" */
    public void setStatus(String status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeChildrenQuery that = (CascadeChildrenQuery) o;
        return page == that.page && size == that.size && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(name, that.name) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() { return Objects.hash(nodeId, page, size, name, status); }

    @Override
    public String toString() {
        return "CascadeChildrenQuery{nodeId='" + nodeId + "', page=" + page + ", size=" + size
                + ", name='" + name + "', status='" + status + "'}";
    }
}
