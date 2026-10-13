// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * WGS84 二维坐标点（不含高程），用于地理边界多边形顶点
 */
public final class TerraGeo2DPoint {

    /** 经度（度） */
    private double lng;

    /** 纬度（度） */
    private double lat;

    public TerraGeo2DPoint() {}

    public TerraGeo2DPoint(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public double getLng() { return lng; }
    public void setLng(double v) { lng = v; }

    public double getLat() { return lat; }
    public void setLat(double v) { lat = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraGeo2DPoint that = (TerraGeo2DPoint) o;
        return Double.compare(that.lng, lng) == 0 && Double.compare(that.lat, lat) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(lng, lat); }

    @Override
    public String toString() { return "TerraGeo2DPoint{lng=" + lng + ", lat=" + lat + "}"; }
}
