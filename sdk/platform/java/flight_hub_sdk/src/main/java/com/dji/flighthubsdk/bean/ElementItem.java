// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 要素节点（对应服务端 ElementItem） */
public final class ElementItem {

    private String id = "";
    private String name = "";
    private String desc = "";
    private int source;
    private int order;
    private int status;
    private int display;
    private ElementResourceData resource;
    private long updateTime;
    private long createTime;
    private int elevationLoadStatus;
    private boolean notify;

    public ElementItem() {
    }

    /**
     * 构造 ElementItem。
     *
     * @param id                  要素 UUID
     * @param name                要素名称
     * @param desc                描述
     * @param source              资源来源：0=历史/app上传 1=地图创建 2=模型对比创建 3=KML导入
     * @param order               同图层内排序序号
     * @param status              管理员可见状态：0=隐藏 1=显示
     * @param display             前端渲染开关：0=不渲染 1=渲染
     * @param resource            要素资源体
     */
    public ElementItem(String id, String name, String desc, int source, int order,
            int status, int display, ElementResourceData resource, long updateTime, long createTime,
            int elevationLoadStatus, boolean notify) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.desc = desc != null ? desc : "";
        this.source = source;
        this.order = order;
        this.status = status;
        this.display = display;
        this.resource = resource;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.elevationLoadStatus = elevationLoadStatus;
        this.notify = notify;
    }

    /** 要素 UUID */
    public String getId() { return id; }

    /** 要素 UUID */
    public void setId(String id) {
        this.id = id;
    }

    /** 要素名称 */
    public String getName() { return name; }

    /** 要素名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 描述 */
    public String getDesc() { return desc; }

    /** 描述 */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /** 资源来源：0=历史/app上传 1=地图创建 2=模型对比创建 3=KML导入 */
    public int getSource() { return source; }

    /** 资源来源：0=历史/app上传 1=地图创建 2=模型对比创建 3=KML导入 */
    public void setSource(int source) {
        this.source = source;
    }

    /** 同图层内排序序号 */
    public int getOrder() { return order; }

    /** 同图层内排序序号 */
    public void setOrder(int order) {
        this.order = order;
    }

    /** 管理员可见状态：0=隐藏 1=显示 */
    public int getStatus() { return status; }

    /** 管理员可见状态：0=隐藏 1=显示 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 前端渲染开关：0=不渲染 1=渲染 */
    public int getDisplay() { return display; }

    /** 前端渲染开关：0=不渲染 1=渲染 */
    public void setDisplay(int display) {
        this.display = display;
    }

    /** 要素资源体 */
    public ElementResourceData getResource() { return resource; }

    /** 要素资源体 */
    public void setResource(ElementResourceData resource) {
        this.resource = resource;
    }

    /** 最后更新时间（毫秒时间戳） */
    public long getUpdateTime() { return updateTime; }

    /** 最后更新时间（毫秒时间戳） */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /** 创建时间（毫秒时间戳） */
    public long getCreateTime() { return createTime; }

    /** 创建时间（毫秒时间戳） */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /** 高程数据加载状态：0=未加载 1=已加载 */
    public int getElevationLoadStatus() { return elevationLoadStatus; }

    /** 高程数据加载状态：0=未加载 1=已加载 */
    public void setElevationLoadStatus(int elevationLoadStatus) {
        this.elevationLoadStatus = elevationLoadStatus;
    }

    /** 是否需要实时推送（图层类型为 C2C 时为 true） */
    public boolean isNotify() { return notify; }

    /** 是否需要实时推送（图层类型为 C2C 时为 true） */
    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementItem that = (ElementItem) o;
        return source == that.source
                && order == that.order
                && status == that.status
                && display == that.display
                && updateTime == that.updateTime
                && createTime == that.createTime
                && elevationLoadStatus == that.elevationLoadStatus
                && notify == that.notify
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(desc, that.desc)
                && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, desc, source, order, status, display,
                resource, updateTime, createTime, elevationLoadStatus, notify);
    }

    @Override
    public String toString() {
        return "ElementItem{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", desc='" + desc + '\''
                + ", source=" + source
                + ", order=" + order
                + ", status=" + status
                + ", display=" + display
                + ", resource=" + resource
                + ", updateTime=" + updateTime
                + ", createTime=" + createTime
                + ", elevationLoadStatus=" + elevationLoadStatus
                + ", notify=" + notify
                + '}';
    }
}
