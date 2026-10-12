// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 图层节点（对应服务端 ElementGroupOutput，树形结构，支持递归子图层） */
public final class ElementGroupOutput {

    private String id = "";
    private String name = "";
    private int order;
    private boolean isDistributed;
    private int type;
    private boolean isLock;
    private String triState = "";
    private long createTime;
    private String parentId = "";
    private List<ElementItem> elements;
    private List<ElementGroupOutput> children;

    public ElementGroupOutput() {
        this.elements = Collections.emptyList();
        this.children = Collections.emptyList();
    }

    /**
     * 构造 ElementGroupOutput。
     *
     * @param id            图层 UUID
     * @param name          图层名称
     * @param order         同级排序序号
     * @param isDistributed 是否已分发到子工作区
     * @param type          图层类型：0=普通 1=默认 2=共享 3=C2C 4=视锥 5=DataHub 6=自动派遣 7=大疆行业APP
     * @param isLock        图层是否被锁定（锁定后不可编辑/删除）
     * @param triState      显隐三态："all"=全显 "none"=全隐 "half"=部分显
     * @param createTime    创建时间（毫秒时间戳）
     * @param parentId      父图层 UUID（根图层为空）
     * @param elements      直属要素列表（不含子图层的要素）
     * @param children      子图层列表（递归树形结构）
     */
    public ElementGroupOutput(String id, String name, int order, boolean isDistributed,
            int type, boolean isLock, String triState, long createTime, String parentId,
            List<ElementItem> elements, List<ElementGroupOutput> children) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.order = order;
        this.isDistributed = isDistributed;
        this.type = type;
        this.isLock = isLock;
        this.triState = triState != null ? triState : "";
        this.createTime = createTime;
        this.parentId = parentId != null ? parentId : "";
        this.elements = elements != null ? elements : Collections.emptyList();
        this.children = children != null ? children : Collections.emptyList();
    }

    /** 图层 UUID */
    public String getId() { return id; }

    /** 图层 UUID */
    public void setId(String id) {
        this.id = id;
    }

    /** 图层名称 */
    public String getName() { return name; }

    /** 图层名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 同级排序序号 */
    public int getOrder() { return order; }

    /** 同级排序序号 */
    public void setOrder(int order) {
        this.order = order;
    }

    /** 是否已分发到子工作区 */
    public boolean isDistributed() { return isDistributed; }

    /** 是否已分发到子工作区 */
    public void setDistributed(boolean isDistributed) {
        this.isDistributed = isDistributed;
    }

    /** 图层类型：0=普通 1=默认 2=共享 3=C2C 4=视锥 5=DataHub 6=自动派遣 7=大疆行业APP */
    public int getType() { return type; }

    /** 图层类型：0=普通 1=默认 2=共享 3=C2C 4=视锥 5=DataHub 6=自动派遣 7=大疆行业APP */
    public void setType(int type) {
        this.type = type;
    }

    /** 图层是否被锁定（锁定后不可编辑/删除） */
    public boolean isLock() { return isLock; }

    /** 图层是否被锁定（锁定后不可编辑/删除） */
    public void setLock(boolean isLock) {
        this.isLock = isLock;
    }

    /** 显隐三态："all"=全显 "none"=全隐 "half"=部分显 */
    public String getTriState() { return triState; }

    /** 显隐三态："all"=全显 "none"=全隐 "half"=部分显 */
    public void setTriState(String triState) {
        this.triState = triState;
    }

    /** 创建时间（毫秒时间戳） */
    public long getCreateTime() { return createTime; }

    /** 创建时间（毫秒时间戳） */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /** 父图层 UUID（根图层为空） */
    public String getParentId() { return parentId; }

    /** 父图层 UUID（根图层为空） */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /** 直属要素列表（不含子图层的要素） */
    public List<ElementItem> getElements() { return elements; }

    /** 直属要素列表（不含子图层的要素） */
    public void setElements(List<ElementItem> elements) {
        this.elements = elements != null ? elements : Collections.emptyList();
    }

    /** 子图层列表（递归树形结构） */
    public List<ElementGroupOutput> getChildren() { return children; }

    /** 子图层列表（递归树形结构） */
    public void setChildren(List<ElementGroupOutput> children) {
        this.children = children != null ? children : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementGroupOutput that = (ElementGroupOutput) o;
        return order == that.order
                && isDistributed == that.isDistributed
                && type == that.type
                && isLock == that.isLock
                && createTime == that.createTime
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(triState, that.triState)
                && Objects.equals(parentId, that.parentId)
                && Objects.equals(elements, that.elements)
                && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, order, isDistributed, type, isLock,
                triState, createTime, parentId, elements, children);
    }

    @Override
    public String toString() {
        return "ElementGroupOutput{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", order=" + order
                + ", isDistributed=" + isDistributed
                + ", type=" + type
                + ", isLock=" + isLock
                + ", triState='" + triState + '\''
                + ", createTime=" + createTime
                + ", parentId='" + parentId + '\''
                + ", elements=" + (elements != null ? elements.size() : 0)
                + ", children=" + (children != null ? children.size() : 0)
                + '}';
    }
}
