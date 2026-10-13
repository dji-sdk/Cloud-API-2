// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 查询元素层级列表请求参数（GET /pf-map/api/v1/elements/lists） */
public final class ElementListHierarchyParams {

    private String groupId = "";
    private String pid = "";
    private Boolean isDistributed;
    private Integer type;
    private String elementUuid;
    private boolean excludeCopy;

    public ElementListHierarchyParams() {
    }

    /** 指定图层 UUID（空=查 workspace 下全部） */
    public String getGroupId() { return groupId; }

    /** 指定图层 UUID（空=查 workspace 下全部） */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /** 父目录 UUID（空=查根目录） */
    public String getPid() { return pid; }

    /** 父目录 UUID（空=查根目录） */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /** true=仅返回已分发的图层；null=不过滤 */
    public Boolean getIsDistributed() { return isDistributed; }

    /** true=仅返回已分发的图层；null=不过滤 */
    public void setIsDistributed(Boolean isDistributed) {
        this.isDistributed = isDistributed;
    }

    /** 图层类型筛选；null=不过滤 */
    public Integer getType() { return type; }

    /** 图层类型筛选；null=不过滤 */
    public void setType(Integer type) {
        this.type = type;
    }

    /** 定位特定元素，返回包含该元素的路径；null=不过滤 */
    public String getElementUuid() { return elementUuid; }

    /** 定位特定元素，返回包含该元素的路径；null=不过滤 */
    public void setElementUuid(String elementUuid) {
        this.elementUuid = elementUuid;
    }

    /** true=排除复制中的图层 */
    public boolean isExcludeCopy() { return excludeCopy; }

    /** true=排除复制中的图层 */
    public void setExcludeCopy(boolean excludeCopy) {
        this.excludeCopy = excludeCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementListHierarchyParams that = (ElementListHierarchyParams) o;
        return excludeCopy == that.excludeCopy
                && Objects.equals(groupId, that.groupId)
                && Objects.equals(pid, that.pid)
                && Objects.equals(isDistributed, that.isDistributed)
                && Objects.equals(type, that.type)
                && Objects.equals(elementUuid, that.elementUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, pid, isDistributed, type, elementUuid, excludeCopy);
    }

    @Override
    public String toString() {
        return "ElementListHierarchyParams{"
                + "groupId='" + groupId + '\''
                + ", pid='" + pid + '\''
                + ", excludeCopy=" + excludeCopy
                + '}';
    }
}
