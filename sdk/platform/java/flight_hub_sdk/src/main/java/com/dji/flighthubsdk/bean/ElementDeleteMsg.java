// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素删除 WS 推送消息（topic: topic_element_delete） */
public final class ElementDeleteMsg {

    private String id = "";
    private String groupId = "";

    public ElementDeleteMsg() {
    }

    /**
     * 构造 ElementDeleteMsg。
     *
     * @param id      被删除的要素 UUID
     * @param groupId 所属图层 UUID
     */
    public ElementDeleteMsg(String id, String groupId) {
        this.id = id != null ? id : "";
        this.groupId = groupId != null ? groupId : "";
    }

    /** 被删除的要素 UUID */
    public String getId() { return id; }

    /** 被删除的要素 UUID */
    public void setId(String id) {
        this.id = id;
    }

    /** 所属图层 UUID */
    public String getGroupId() { return groupId; }

    /** 所属图层 UUID */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementDeleteMsg that = (ElementDeleteMsg) o;
        return Objects.equals(id, that.id)
                && Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId);
    }

    @Override
    public String toString() {
        return "ElementDeleteMsg{"
                + "id='" + id + '\''
                + ", groupId='" + groupId + '\''
                + '}';
    }
}
