// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 要素几何数据（GeoJSON Geometry，对齐 proto ElementGeometry）。
 * <p>
 * coordinates 和 breadCrumbs 为 raw JSON 字符串，多态结构由 type 决定：
 * <ul>
 *   <li>Point: {@code [lng, lat, alt]}</li>
 *   <li>LineString: {@code [[lng, lat, alt], ...]}</li>
 *   <li>Polygon: {@code [[[lng, lat, alt], ...]]}</li>
 * </ul>
 */
public final class ElementGeometry {

    private String type = "";
    private double radius;
    private String coordinates = "";
    private String breadCrumbs = "";

    public ElementGeometry() {
    }

    /**
     * 构造 ElementGeometry。
     *
     * @param type        GeoJSON 几何类型："Point"/"LineString"/"Polygon"
     * @param coordinates 坐标 raw JSON 字符串
     */
    public ElementGeometry(String type, String coordinates) {
        this.type = type != null ? type : "";
        this.coordinates = coordinates != null ? coordinates : "";
    }

    /** GeoJSON 几何类型："Point"/"LineString"/"Polygon" */
    public String getType() { return type; }

    /** GeoJSON 几何类型："Point"/"LineString"/"Polygon" */
    public void setType(String type) {
        this.type = type != null ? type : "";
    }

    /** 圆半径（米），仅圆要素（resource type=7）使用 */
    public double getRadius() { return radius; }

    /** 圆半径（米），仅圆要素（resource type=7）使用 */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * 坐标 raw JSON 字符串，格式由 type 决定：
     * <ul>
     *   <li>Point: {@code [lng, lat, alt]}，例: {@code [114.06, 22.55, 12.6]}</li>
     *   <li>LineString: {@code [[lng,lat,alt],...]}，例: {@code [[114.03,22.53,0],[114.04,22.53,0]]}</li>
     *   <li>Polygon: {@code [[[lng,lat,alt],...]]}，例: {@code [[[114.22,30.55,0],[114.22,30.55,0]]]}</li>
     *   <li>圆(resource type=7): 同 Point 格式，配合 radius 使用，例: {@code [114.06,22.53,0]}, radius=825.88</li>
     * </ul>
     */
    public String getCoordinates() { return coordinates; }

    /**
     * 坐标 raw JSON 字符串，格式由 type 决定：
     * <ul>
     *   <li>Point: {@code [lng, lat, alt]}</li>
     *   <li>LineString: {@code [[lng,lat,alt],...]}</li>
     *   <li>Polygon: {@code [[[lng,lat,alt],...]]}}</li>
     *   <li>圆(resource type=7): 同 Point 格式，配合 radius 使用</li>
     * </ul>
     */
    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates != null ? coordinates : "";
    }

    /** 面包屑轨迹点 raw JSON 字符串，结构同 coordinates */
    public String getBreadCrumbs() { return breadCrumbs; }

    /** 面包屑轨迹点 raw JSON 字符串，结构同 coordinates */
    public void setBreadCrumbs(String breadCrumbs) {
        this.breadCrumbs = breadCrumbs != null ? breadCrumbs : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementGeometry that = (ElementGeometry) o;
        return Double.compare(that.radius, radius) == 0
                && Objects.equals(type, that.type)
                && Objects.equals(coordinates, that.coordinates)
                && Objects.equals(breadCrumbs, that.breadCrumbs);
    }

    @Override
    public int hashCode() { return Objects.hash(type, radius, coordinates, breadCrumbs); }

    @Override
    public String toString() {
        return "ElementGeometry{"
                + "type='" + type + '\''
                + ", radius=" + radius
                + ", coordinates='" + coordinates + '\''
                + ", breadCrumbs='" + breadCrumbs + '\''
                + '}';
    }
}
