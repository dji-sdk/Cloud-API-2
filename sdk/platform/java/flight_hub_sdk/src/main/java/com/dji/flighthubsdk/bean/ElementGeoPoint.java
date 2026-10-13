// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 地理坐标点（对齐 proto ElementGeometry 坐标元素 {lng, lat, alt}） */
public final class ElementGeoPoint {

    private double lng;
    private double lat;
    private double alt;

    public ElementGeoPoint() {
    }

    /**
     * 构造 ElementGeoPoint。
     *
     * @param lng 经度
     * @param lat 纬度
     * @param alt 高度（米）
     */
    public ElementGeoPoint(double lng, double lat, double alt) {
        this.lng = lng;
        this.lat = lat;
        this.alt = alt;
    }

    /** 经度 */
    public double getLng() { return lng; }

    /** 经度 */
    public void setLng(double lng) {
        this.lng = lng;
    }

    /** 纬度 */
    public double getLat() { return lat; }

    /** 纬度 */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /** 高度（米） */
    public double getAlt() { return alt; }

    /** 高度（米） */
    public void setAlt(double alt) {
        this.alt = alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementGeoPoint that = (ElementGeoPoint) o;
        return Double.compare(that.lng, lng) == 0
                && Double.compare(that.lat, lat) == 0
                && Double.compare(that.alt, alt) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(lng, lat, alt); }

    @Override
    public String toString() {
        return "ElementGeoPoint{lng=" + lng + ", lat=" + lat + ", alt=" + alt + '}';
    }
}
