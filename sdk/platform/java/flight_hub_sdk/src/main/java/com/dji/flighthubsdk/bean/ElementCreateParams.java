// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 创建要素请求参数（POST /pf-map/api/v1/elements/create） */
public final class ElementCreateParams {

    private String id = "";
    private int source;
    private ElementResourceContent resourceContent;
    private int resourceType;
    private String resourceRemark = "";
    private String name = "";
    private int elementSource;
    private String desc = "";

    public ElementCreateParams() {
    }

    /**
     * 简易构造（仅必填字段）。
     *
     * @param resourceContent 几何内容（GeoJSON Feature 结构），必填
     * @param resourceType    几何类型：0=点 1=线 2=面 7=圆，必填
     * @param name            要素名称
     */
    public ElementCreateParams(ElementResourceContent resourceContent, int resourceType, String name) {
        this.resourceContent = resourceContent;
        this.resourceType = resourceType;
        this.name = name != null ? name : "";
    }

    /**
     * 全参构造。
     *
     * @param id              自定义要素 UUID（可选，空时服务端自动生成）
     * @param source          要素来源类型（埋点用）
     * @param resourceContent 几何内容（GeoJSON Feature 结构），必填
     * @param resourceType    几何类型：0=点 1=线 2=面 7=圆，必填
     * @param resourceRemark  要素备注
     * @param name            要素名称
     * @param elementSource   埋点来源标记
     * @param desc            描述
     */
    public ElementCreateParams(String id, int source, ElementResourceContent resourceContent,
            int resourceType, String resourceRemark, String name, int elementSource, String desc) {
        this.id = id;
        this.source = source;
        this.resourceContent = resourceContent;
        this.resourceType = resourceType;
        this.resourceRemark = resourceRemark;
        this.name = name != null ? name : "";
        this.elementSource = elementSource;
        this.desc = desc;
    }

    /** 自定义要素 UUID（可选，空时服务端自动生成） */
    public String getId() { return id; }

    /** 自定义要素 UUID（可选，空时服务端自动生成） */
    public void setId(String id) { this.id = id; }

    /** 要素来源类型（埋点用） */
    public int getSource() { return source; }

    /** 要素来源类型（埋点用） */
    public void setSource(int source) { this.source = source; }

    /** 几何内容（GeoJSON Feature 结构），必填 */
    public ElementResourceContent getResourceContent() { return resourceContent; }

    /** 几何内容（GeoJSON Feature 结构），必填 */
    public void setResourceContent(ElementResourceContent resourceContent) { this.resourceContent = resourceContent; }

    /** 几何类型：0=点 1=线 2=面 7=圆，必填 */
    public int getResourceType() { return resourceType; }

    /** 几何类型：0=点 1=线 2=面 7=圆，必填 */
    public void setResourceType(int resourceType) { this.resourceType = resourceType; }

    /** 要素备注 */
    public String getResourceRemark() { return resourceRemark; }

    /** 要素备注 */
    public void setResourceRemark(String resourceRemark) { this.resourceRemark = resourceRemark; }

    /** 要素名称（空时服务端使用创建者昵称+自增序号） */
    public String getName() { return name; }

    /** 要素名称（空时服务端使用创建者昵称+自增序号） */
    public void setName(String name) { this.name = name != null ? name : ""; }

    /** 埋点来源标记 */
    public int getElementSource() { return elementSource; }

    /** 埋点来源标记 */
    public void setElementSource(int elementSource) { this.elementSource = elementSource; }

    /** 描述 */
    public String getDesc() { return desc; }

    /** 描述 */
    public void setDesc(String desc) { this.desc = desc; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementCreateParams that = (ElementCreateParams) o;
        return source == that.source
                && resourceType == that.resourceType
                && elementSource == that.elementSource
                && Objects.equals(id, that.id)
                && Objects.equals(resourceContent, that.resourceContent)
                && Objects.equals(resourceRemark, that.resourceRemark)
                && Objects.equals(name, that.name)
                && Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, resourceContent, resourceType, resourceRemark, name, elementSource, desc);
    }

    @Override
    public String toString() {
        return "ElementCreateParams{"
                + "id='" + id + '\''
                + ", source=" + source
                + ", resourceType=" + resourceType
                + ", resourceRemark='" + resourceRemark + '\''
                + ", name='" + name + '\''
                + ", elementSource=" + elementSource
                + ", desc='" + desc + '\''
                + '}';
    }
}
