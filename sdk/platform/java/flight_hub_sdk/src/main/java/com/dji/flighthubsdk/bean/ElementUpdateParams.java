// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 更新要素请求参数（PUT /pf-map/api/v1/elements/update/{id}） */
public final class ElementUpdateParams {

    private String id = "";
    private String name;
    private ElementResourceContent content;
    private Integer display;
    private String remark;
    private Integer status;
    private Integer elevationLoadStatus;
    private Integer userStatus;
    private String targetLayerId;
    private List<ElementResourceOrderItem> orders;

    public ElementUpdateParams() {
    }

    /**
     * 简易构造（仅必填字段）。
     *
     * @param id 要素 UUID，必填
     */
    public ElementUpdateParams(String id) {
        this.id = id != null ? id : "";
    }

    /**
     * 全参构造。
     *
     * @param id                  要素 UUID，必填
     * @param name                要素名称；null=不更新
     * @param content             几何数据（GeoJSON Feature）；null=不更新
     * @param display             显示状态：1=显示 0=隐藏；null=不更新
     * @param remark              备注；null=不更新
     * @param status              元素状态；null=不更新
     * @param elevationLoadStatus 高程加载状态；null=不更新
     * @param userStatus          用户个人显影状态；null=不更新
     * @param targetLayerId       目标图层 UUID；非空时触发移动操作
     * @param orders              移动后目标图层内的排序列表
     */
    public ElementUpdateParams(String id, String name, ElementResourceContent content,
            Integer display, String remark, Integer status, Integer elevationLoadStatus,
            Integer userStatus, String targetLayerId, List<ElementResourceOrderItem> orders) {
        this.id = id != null ? id : "";
        this.name = name;
        this.content = content;
        this.display = display;
        this.remark = remark;
        this.status = status;
        this.elevationLoadStatus = elevationLoadStatus;
        this.userStatus = userStatus;
        this.targetLayerId = targetLayerId;
        this.orders = orders;
    }

    /** 要素 UUID，必填 */
    public String getId() { return id; }

    /** 要素 UUID，必填 */
    public void setId(String id) {
        this.id = id;
    }

    /** 要素名称；null=不更新 */
    public String getName() { return name; }

    /** 要素名称；null=不更新 */
    public void setName(String name) {
        this.name = name;
    }

    /** 几何数据（GeoJSON Feature 结构），整体覆盖；null=不更新 */
    public ElementResourceContent getContent() { return content; }

    /** 几何数据（GeoJSON Feature 结构），整体覆盖；null=不更新 */
    public void setContent(ElementResourceContent content) {
        this.content = content;
    }

    /** 显示状态：1=显示 0=隐藏；null=不更新 */
    public Integer getDisplay() { return display; }

    /** 显示状态：1=显示 0=隐藏；null=不更新 */
    public void setDisplay(Integer display) {
        this.display = display;
    }

    /** 备注；null=不更新 */
    public String getRemark() { return remark; }

    /** 备注；null=不更新 */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /** 元素状态；null=不更新 */
    public Integer getStatus() { return status; }

    /** 元素状态；null=不更新 */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /** 高程加载状态（仅 Web）；null=不更新 */
    public Integer getElevationLoadStatus() { return elevationLoadStatus; }

    /** 高程加载状态（仅 Web）；null=不更新 */
    public void setElevationLoadStatus(Integer elevationLoadStatus) {
        this.elevationLoadStatus = elevationLoadStatus;
    }

    /** 用户个人显影状态；null=不更新 */
    public Integer getUserStatus() { return userStatus; }

    /** 用户个人显影状态；null=不更新 */
    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    /** 目标图层 UUID；非空时触发移动操作 */
    public String getTargetLayerId() { return targetLayerId; }

    /** 目标图层 UUID；非空时触发移动操作 */
    public void setTargetLayerId(String targetLayerId) {
        this.targetLayerId = targetLayerId;
    }

    /** 移动后目标图层内的排序列表 */
    public List<ElementResourceOrderItem> getOrders() { return orders; }

    /** 移动后目标图层内的排序列表 */
    public void setOrders(List<ElementResourceOrderItem> orders) {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementUpdateParams that = (ElementUpdateParams) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(content, that.content)
                && Objects.equals(display, that.display)
                && Objects.equals(remark, that.remark)
                && Objects.equals(status, that.status)
                && Objects.equals(elevationLoadStatus, that.elevationLoadStatus)
                && Objects.equals(userStatus, that.userStatus)
                && Objects.equals(targetLayerId, that.targetLayerId)
                && Objects.equals(orders, that.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, content, display, remark, status,
                elevationLoadStatus, userStatus, targetLayerId, orders);
    }

    @Override
    public String toString() {
        return "ElementUpdateParams{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", content=" + content
                + ", display=" + display
                + ", remark='" + remark + '\''
                + ", status=" + status
                + ", elevationLoadStatus=" + elevationLoadStatus
                + ", userStatus=" + userStatus
                + ", targetLayerId='" + targetLayerId + '\''
                + ", orders=" + orders
                + '}';
    }
}
