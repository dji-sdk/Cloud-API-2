// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞行区域内容数据。 */
public final class FlightAreasContentData {
    /** 固定值 "Feature"; */
    private String type;
    private ContentsPropertyData properties;
    private ContentsGeometryData geometry;
    public FlightAreasContentData() {
    }

/**
     * 构造 FlightAreasContentData。
     *
     * @param type 类型
     * @param properties 属性数据
     * @param geometry 几何数据
     */
    public FlightAreasContentData(String type, ContentsPropertyData properties, ContentsGeometryData geometry) {
        this.type = type != null ? type : "";
        this.properties = properties;
        this.geometry = geometry;
    }

    /** 固定值 "Feature"; */
    public String getType() { return type; }
    /** 属性数据 */
    public ContentsPropertyData getProperties() { return properties; }
    /** 几何数据 */
    public ContentsGeometryData getGeometry() { return geometry; }

    /** @param type 固定值 "Feature"; */
    public void setType(String type) {
        this.type = type;
    }

    /** 属性数据 */
    public void setProperties(ContentsPropertyData properties) {
        this.properties = properties;
    }

    /** 几何数据 */
    public void setGeometry(ContentsGeometryData geometry) {
        this.geometry = geometry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightAreasContentData that = (FlightAreasContentData) o;
        return Objects.equals(type, that.type)
                && Objects.equals(properties, that.properties)
                && Objects.equals(geometry, that.geometry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, properties, geometry);
    }

    @Override
    public String toString() {
        return "FlightAreasContentData{type='" + type + '\'' + ", properties=" + properties + ", geometry=" + geometry + '}';
    }
}
