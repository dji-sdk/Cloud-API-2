// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 设置等温线渲染多边形参数 */
public class SetIsothermRenderingPolygonParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 多边形区域顶点坐标列表 */
    private List<IsothermPolygonPoint> polygon = new ArrayList<>();

    public SetIsothermRenderingPolygonParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 多边形区域顶点坐标列表 */
    public List<IsothermPolygonPoint> getPolygon() { return polygon; }
    /** @param polygon 多边形区域顶点坐标列表 */
    public void setPolygon(List<IsothermPolygonPoint> polygon) { this.polygon = polygon != null ? polygon : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetIsothermRenderingPolygonParams that = (SetIsothermRenderingPolygonParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(polygon, that.polygon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, polygon);
    }

    @Override
    public String toString() {
        return "SetIsothermRenderingPolygonParams{payloadIndex='" + payloadIndex + '\'' + ", polygon=" + polygon + '}';
    }
}
