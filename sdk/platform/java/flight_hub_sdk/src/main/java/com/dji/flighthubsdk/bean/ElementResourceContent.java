// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 要素几何内容，GeoJSON Feature 结构（对齐 proto ElementResourceContent）
 * 用于 CreateElement.resource.content 和 UpdateElement.content
 */
public final class ElementResourceContent {

    private String type = "Feature";
    private ElementProperties properties;
    private ElementGeometry geometry;
    private ElementMediaResources mediaResources;

    public ElementResourceContent() {
        this.properties = new ElementProperties();
        this.geometry = new ElementGeometry();
        this.mediaResources = new ElementMediaResources();
    }

    /** GeoJSON 类型，固定传 "Feature" */
    public String getType() { return type; }

    /** GeoJSON 类型，固定传 "Feature" */
    public void setType(String type) {
        this.type = type != null ? type : "Feature";
    }

    /** 渲染属性 */
    public ElementProperties getProperties() { return properties; }

    /** 渲染属性 */
    public void setProperties(ElementProperties properties) {
        this.properties = properties != null ? properties : new ElementProperties();
    }

    /** 几何数据 */
    public ElementGeometry getGeometry() { return geometry; }

    /** 几何数据 */
    public void setGeometry(ElementGeometry geometry) {
        this.geometry = geometry != null ? geometry : new ElementGeometry();
    }

    /** 绑定的媒体资源；无绑定图片时不传 */
    public ElementMediaResources getMediaResources() { return mediaResources; }

    /** 绑定的媒体资源；无绑定图片时不传 */
    public void setMediaResources(ElementMediaResources mediaResources) {
        this.mediaResources = mediaResources != null ? mediaResources : new ElementMediaResources();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementResourceContent that = (ElementResourceContent) o;
        return Objects.equals(type, that.type)
                && Objects.equals(properties, that.properties)
                && Objects.equals(geometry, that.geometry)
                && Objects.equals(mediaResources, that.mediaResources);
    }

    @Override
    public int hashCode() { return Objects.hash(type, properties, geometry, mediaResources); }

    @Override
    public String toString() {
        return "ElementResourceContent{"
                + "type='" + type + '\''
                + ", properties=" + properties
                + ", geometry=" + geometry
                + ", mediaResources=" + mediaResources
                + '}';
    }
}
