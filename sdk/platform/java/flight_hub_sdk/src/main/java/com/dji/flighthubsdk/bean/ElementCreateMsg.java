// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素创建 WS 推送消息（topic: topic_element_create） */
public final class ElementCreateMsg {

    private String id = "";
    private String groupId = "";
    private String name = "";
    private int display;

    public ElementCreateMsg() {
    }

    /**
     * 构造 ElementCreateMsg。
     *
     * @param id      新建要素 UUID
     * @param groupId 所属图层 UUID
     * @param name    要素名称
     * @param display 显示状态：1-显示 0-隐藏
     */
    public ElementCreateMsg(String id, String groupId, String name, int display) {
        this.id = id != null ? id : "";
        this.groupId = groupId != null ? groupId : "";
        this.name = name != null ? name : "";
        this.display = display;
    }

    /** 新建要素 UUID */
    public String getId() { return id; }

    /** 新建要素 UUID */
    public void setId(String id) {
        this.id = id;
    }

    /** 所属图层 UUID */
    public String getGroupId() { return groupId; }

    /** 所属图层 UUID */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /** 要素名称 */
    public String getName() { return name; }

    /** 要素名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 显示状态：1-显示 0-隐藏 */
    public int getDisplay() { return display; }

    /** 显示状态：1-显示 0-隐藏 */
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
        ElementCreateMsg that = (ElementCreateMsg) o;
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
        return "ElementCreateMsg{"
                + "id='" + id + '\''
                + ", groupId='" + groupId + '\''
                + ", name='" + name + '\''
                + ", display=" + display
                + '}';
    }
}
