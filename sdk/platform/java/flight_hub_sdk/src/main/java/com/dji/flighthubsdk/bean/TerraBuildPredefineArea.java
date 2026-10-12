// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 兴趣区域（多边形） */
public final class TerraBuildPredefineArea {
    private List<double[]> polygonPoints;

    public TerraBuildPredefineArea() {
    }

    /**
     * 构造 TerraBuildPredefineArea。
     *
     * @param polygonPoints 多边形坐标点列表，每个元素为 [lat, lng]
     */
    public TerraBuildPredefineArea(List<double[]> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    /** 多边形坐标点列表，每个元素为 [lat, lng] */
    public List<double[]> getPolygonPoints() { return polygonPoints; }
    /** 多边形坐标点列表，每个元素为 [lat, lng] */
    public void setPolygonPoints(List<double[]> polygonPoints) { this.polygonPoints = polygonPoints; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildPredefineArea that = (TerraBuildPredefineArea) o;
        return Objects.equals(polygonPoints, that.polygonPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(polygonPoints);
    }

    @Override
    public String toString() {
        return "TerraBuildPredefineArea{"
                + "polygonPoints=" + polygonPoints
                + '}';
    }
}
