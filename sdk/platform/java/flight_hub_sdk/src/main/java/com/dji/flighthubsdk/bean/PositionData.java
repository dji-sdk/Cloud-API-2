// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 媒体元数据中的经纬高。 */
public final class PositionData {
    /** 纬度; */
    private double lat;
    /** 经度; */
    private double lng;
    /** 绝对高度; */
    private double absAlt;
    /** 相对高度; */
    private double relAlt;
    public PositionData() {
    }

/**
     * 构造 PositionData。
     *
     * @param lat 纬度
     * @param lng 经度
     * @param absAlt 绝对高度
     * @param relAlt 相对高度
     */
    public PositionData(double lat, double lng, double absAlt, double relAlt) {
        this.lat = lat;
        this.lng = lng;
        this.absAlt = absAlt;
        this.relAlt = relAlt;
    }

    /** 纬度; */
    public double getLat() {
        return lat;
    }

    /** @param lat 纬度; */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /** 经度; */
    public double getLng() {
        return lng;
    }

    /** @param lng 经度; */
    public void setLng(double lng) {
        this.lng = lng;
    }

    /** 绝对高度; */
    public double getAbsAlt() {
        return absAlt;
    }

    /** @param absAlt 绝对高度; */
    public void setAbsAlt(double absAlt) {
        this.absAlt = absAlt;
    }

    /** 相对高度; */
    public double getRelAlt() {
        return relAlt;
    }

    /** @param relAlt 相对高度; */
    public void setRelAlt(double relAlt) {
        this.relAlt = relAlt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PositionData that = (PositionData) o;
        return Double.compare(that.lat, lat) == 0
                && Double.compare(that.lng, lng) == 0
                && Double.compare(that.absAlt, absAlt) == 0
                && Double.compare(that.relAlt, relAlt) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng, absAlt, relAlt);
    }

    @Override
    public String toString() {
        return "PositionData{"
                + "lat=" + lat
                + ", lng=" + lng
                + ", absAlt=" + absAlt
                + ", relAlt=" + relAlt
                + '}';
    }
}
