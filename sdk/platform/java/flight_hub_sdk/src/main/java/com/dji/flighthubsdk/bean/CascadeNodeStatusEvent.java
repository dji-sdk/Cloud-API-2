// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * v1.1 事件推送 {@code topic_cascade_node_status} 事件体。
 *
 * <p>v1.1 wire 只承载操作结果，不含在线态：
 * <pre>
 * {
 *   "childNodeStatusList": [
 *     {"nodeID": "GD", "status": "enabled"},
 *     {"nodeID": "SZ", "status": "disabled"},
 *     {"nodeID": "BJ", "status": "deleted"}
 *   ],
 *   "parentNodeStatus": {"nodeID": "HQ", "status": "enabled"},
 *   "changedAt": 1756600000
 * }
 * </pre>
 *
 * <p>status 取值：{@code enabled} / {@code disabled} / {@code deleted}，仅承载操作结果，
 * 不承载在线态（在线态读 REST 接口）。事件体是全量快照，幂等自愈。
 *
 * <p>订阅入口：{@code CascadeModule.listenNodeStatusChanged(...)} / {@code cancelListenNodeStatusChanged(int)}。
 */
public final class CascadeNodeStatusEvent {

    /** Topic 常量（对齐 v1.1）。 */
    public static final String TOPIC = "topic_cascade_node_status";

    /** 子节点状态项。 */
    public static final class ChildStatus {
        private String nodeId;
        private String status;

        public ChildStatus() {
            this("", "");
        }

        /**
         * 构造 ChildStatus。
         *
         * @param nodeId 节点 ID（v1.1 wire: nodeID）
         * @param status 操作结果：{@code "enabled"} / {@code "disabled"} / {@code "deleted"}
         */
        public ChildStatus(String nodeId, String status) {
            this.nodeId = nodeId != null ? nodeId : "";
            this.status = status != null ? status : "";
        }

        /** 节点 ID */
        public String getNodeId() { return nodeId; }
        /** 节点 ID */
        public void setNodeId(String nodeId) { this.nodeId = nodeId != null ? nodeId : ""; }

        /** 操作结果字符串：{@code enabled} / {@code disabled} / {@code deleted} */
        public String getStatus() { return status; }
        /** 操作结果字符串：{@code enabled} / {@code disabled} / {@code deleted} */
        public void setStatus(String status) { this.status = status != null ? status : ""; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChildStatus that = (ChildStatus) o;
            return Objects.equals(nodeId, that.nodeId)
                    && Objects.equals(status, that.status);
        }

        @Override
        public int hashCode() { return Objects.hash(nodeId, status); }

        @Override
        public String toString() {
            return "ChildStatus{nodeId='" + nodeId + "', status='" + status + "'}";
        }
    }

    /** 父节点状态。 */
    public static final class ParentStatus {
        private String nodeId;
        private String status;

        public ParentStatus() {
            this("", "");
        }

        /**
         * 构造 ParentStatus。
         *
         * @param nodeId 节点 ID（v1.1 wire: nodeID）
         * @param status 操作结果：{@code "enabled"} / {@code "disabled"} / {@code "deleted"}
         */
        public ParentStatus(String nodeId, String status) {
            this.nodeId = nodeId != null ? nodeId : "";
            this.status = status != null ? status : "";
        }

        /** 节点 ID */
        public String getNodeId() { return nodeId; }
        /** 节点 ID */
        public void setNodeId(String nodeId) { this.nodeId = nodeId != null ? nodeId : ""; }

        /** 操作结果字符串：{@code enabled} / {@code disabled} / {@code deleted} */
        public String getStatus() { return status; }
        /** 操作结果字符串：{@code enabled} / {@code disabled} / {@code deleted} */
        public void setStatus(String status) { this.status = status != null ? status : ""; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParentStatus that = (ParentStatus) o;
            return Objects.equals(nodeId, that.nodeId)
                    && Objects.equals(status, that.status);
        }

        @Override
        public int hashCode() { return Objects.hash(nodeId, status); }

        @Override
        public String toString() {
            return "ParentStatus{nodeId='" + nodeId + "', status='" + status + "'}";
        }
    }

    private List<ChildStatus> childNodeStatusList;
    private ParentStatus parentNodeStatus;
    private long changedAt;

    public CascadeNodeStatusEvent() {
        this(Collections.emptyList(), new ParentStatus(), 0L);
    }

    /**
     * 构造 CascadeNodeStatusEvent。
     *
     * @param childNodeStatusList 子节点状态列表（不允许 null）
     * @param parentNodeStatus    父节点状态（不允许 null）
     * @param changedAt           事件时间（Unix 秒）
     */
    public CascadeNodeStatusEvent(List<ChildStatus> childNodeStatusList,
                                  ParentStatus parentNodeStatus,
                                  long changedAt) {
        this.childNodeStatusList = childNodeStatusList != null
                ? new ArrayList<>(childNodeStatusList) : new ArrayList<>();
        this.parentNodeStatus = parentNodeStatus != null ? parentNodeStatus : new ParentStatus();
        this.changedAt = changedAt;
    }

    /** 子节点状态列表 */
    public List<ChildStatus> getChildNodeStatusList() { return childNodeStatusList; }
    /** 子节点状态列表 */
    public void setChildNodeStatusList(List<ChildStatus> childNodeStatusList) {
        this.childNodeStatusList = childNodeStatusList != null
                ? childNodeStatusList : new ArrayList<>();
    }

    /** 父节点状态 */
    public ParentStatus getParentNodeStatus() { return parentNodeStatus; }
    /** 父节点状态 */
    public void setParentNodeStatus(ParentStatus parentNodeStatus) {
        this.parentNodeStatus = parentNodeStatus != null ? parentNodeStatus : new ParentStatus();
    }

    /** 事件时间（Unix 秒） */
    public long getChangedAt() { return changedAt; }
    /** 事件时间（Unix 秒） */
    public void setChangedAt(long changedAt) { this.changedAt = changedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeNodeStatusEvent that = (CascadeNodeStatusEvent) o;
        return changedAt == that.changedAt
                && Objects.equals(childNodeStatusList, that.childNodeStatusList)
                && Objects.equals(parentNodeStatus, that.parentNodeStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childNodeStatusList, parentNodeStatus, changedAt);
    }

    @Override
    public String toString() {
        return "CascadeNodeStatusEvent{childNodeStatusList=" + childNodeStatusList
                + ", parentNodeStatus=" + parentNodeStatus
                + ", changedAt=" + changedAt + "}";
    }
}