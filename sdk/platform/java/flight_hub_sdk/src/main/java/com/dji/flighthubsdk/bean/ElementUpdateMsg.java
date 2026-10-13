// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素更新 WS 推送消息（topic: topic_element_update） */
public final class ElementUpdateMsg {

    private String id = "";
    private String groupId = "";
    private String name = "";
    private int display;

    public ElementUpdateMsg() {
    }

    /**
     * 构造 ElementUpdateMsg。
     *
     * @param id      被更新的要素 UUID
     * @param groupId 所属图层 UUID
     * @param name    更新后的名称
     * @param display 更新后的显示状态：1-显示 0-隐藏
     */
    public ElementUpdateMsg(String id, String groupId, String name, int display) {
        this.id = id != null ? id : "";
        this.groupId = groupId != null ? groupId : "";
        this.name = name != null ? name : "";
        this.display = display;
    }

    /** 被更新的要素 UUID */
    public String getId() { return id; }

    /** 被更新的要素 UUID */
    public void setId(String id) {
        this.id = id;
    }

    /** 所属图层 UUID */
    public String getGroupId() { return groupId; }

    /** 所属图层 UUID */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /** 更新后的名称 */
    public String getName() { return name; }

    /** 更新后的名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 更新后的显示状态：1-显示 0-隐藏 */
    public int getDisplay() { return display; }

    /** 更新后的显示状态：1-显示 0-隐藏 */
    public void setDisplay(int display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementUpdateMsg that = (ElementUpdateMsg) o;
        return display == that.display
                && Objects.equals(id, that.id)
                && Objects.equals(groupId, that.groupId)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, name, display);
    }

    @Override
    public String toString() {
        return "ElementUpdateMsg{"
                + "id='" + id + '\''
                + ", groupId='" + groupId + '\''
                + ", name='" + name + '\''
                + ", display=" + display
                + '}';
    }
}
