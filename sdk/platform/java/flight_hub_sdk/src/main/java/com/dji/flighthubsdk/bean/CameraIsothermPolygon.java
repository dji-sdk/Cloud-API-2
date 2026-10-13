// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 等温线渲染多边形（DRC {@code camera_state.isotherm_rendering_polygon}）。 */
public final class CameraIsothermPolygon {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private List<CameraIsothermPolygonPoint> points;

    public CameraIsothermPolygon() {}
    public CameraIsothermPolygon(String payloadIndex, List<CameraIsothermPolygonPoint> points) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.points = points;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public List<CameraIsothermPolygonPoint> getPoints() { return points; }
    public void setPoints(List<CameraIsothermPolygonPoint> v) { this.points = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraIsothermPolygon)) return false;
        CameraIsothermPolygon that = (CameraIsothermPolygon) o;
        return Objects.equals(payloadIndex, that.payloadIndex) && Objects.equals(points, that.points);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, points); }
    @Override public String toString() {
        return "CameraIsothermPolygon{payloadIndex='" + payloadIndex + "', points=" + points + '}';
    }
}
