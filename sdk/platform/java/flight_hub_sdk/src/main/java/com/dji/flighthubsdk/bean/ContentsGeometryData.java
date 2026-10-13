// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 内容几何数据。 */
public final class ContentsGeometryData {
    private String type;
    private List<List<List<Double>>> coordinates;
    private List<Double> center;
    private double radius;
    public ContentsGeometryData() {
    }

/**
     * 构造 ContentsGeometryData。
     *
     * @param type 几何类型。枚举："Circle"=圆形（限飞区+作业区）, "Polygon"=多边形（限飞区+作业区）, "Polyline"=折线（仅禁降区）
     * @param coordinates 坐标数据（非圆形时必填）。格式 [经度,纬度]；多边形约束：≥3 点、顶点数 &lt; 255、首尾必须闭合
     * @param center 圆心坐标（圆形时必填）。格式：[经度, 纬度]
     * @param radius 半径（圆形时必填）。单位：米，需 &gt; 0
     */
    public ContentsGeometryData(
            String type, List<List<List<Double>>> coordinates, List<Double> center, double radius) {
        this.type = type != null ? type : "";
        this.coordinates = coordinates != null ? coordinates : Collections.emptyList();
        this.center = center != null ? center : Collections.emptyList();
        this.radius = radius;
    }

    /**
     * 几何类型枚举：
     * <ul>
     *   <li>"Circle" - 圆形（限飞区、作业区支持）</li>
     *   <li>"Polygon" - 多边形（限飞区、作业区支持）</li>
     *   <li>"Polyline" - 折线（仅禁降区支持）</li>
     * </ul>
     */
    public String getType() {
        return type;
    }

    /**
     * 几何类型枚举：
     * <ul>
     *   <li>"Circle" - 圆形（限飞区、作业区支持）</li>
     *   <li>"Polygon" - 多边形（限飞区、作业区支持）</li>
     *   <li>"Polyline" - 折线（仅禁降区支持）</li>
     * </ul>
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 坐标数据（非圆形时必填）。
     * <p>坐标格式：[经度, 纬度]（GeoJSON 标准，经度在前）。</p>
     * <p>多边形约束：至少 3 个顶点、每个环顶点数 &lt; 255、首尾坐标必须相同（闭合）。</p>
     */
    public List<List<List<Double>>> getCoordinates() {
        return coordinates;
    }

    /**
     * 坐标数据（非圆形时必填）。
     * <p>坐标格式：[经度, 纬度]（GeoJSON 标准，经度在前）。</p>
     * <p>多边形约束：至少 3 个顶点、每个环顶点数 &lt; 255、首尾坐标必须相同（闭合）。</p>
     */
    public void setCoordinates(List<List<List<Double>>> coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * 圆心坐标（圆形时必填）。
     * <p>格式：[经度, 纬度]。</p>
     */
    public List<Double> getCenter() {
        return center;
    }

    /**
     * 圆心坐标（圆形时必填）。
     * <p>格式：[经度, 纬度]。</p>
     */
    public void setCenter(List<Double> center) {
        this.center = center;
    }

    /**
     * 半径（圆形时必填）。
     * <p>单位：米，需大于 0。</p>
     */
    public double getRadius() {
        return radius;
    }

    /**
     * 半径（圆形时必填）。
     * <p>单位：米，需大于 0。</p>
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContentsGeometryData that = (ContentsGeometryData) o;
        return Objects.equals(type, that.type)
                && Objects.equals(coordinates, that.coordinates)
                && Objects.equals(center, that.center)
                && Double.compare(that.radius, radius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinates, center, radius);
    }

    @Override
    public String toString() {
        return "ContentsGeometryData{"
                + "type='" + type + '\''
                + ", coordinates=" + coordinates
                + ", center=" + center
                + ", radius=" + radius
                + '}';
    }
}
