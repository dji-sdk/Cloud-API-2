// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 媒体文件 GPS 位置 */
public final class MediaFileGeoPosition {

    /** 纬度 */
    private double lat;
    /** 经度 */
    private double lng;
    /** 绝对高度（米） */
    private double absAlt;

    public MediaFileGeoPosition() {
    }

    /**
     * 构造 MediaFileGeoPosition。
     *
     * @param lat    纬度
     * @param lng    经度
     * @param absAlt 绝对高度（米）
     */
    public MediaFileGeoPosition(double lat, double lng, double absAlt) {
        this.lat = lat;
        this.lng = lng;
        this.absAlt = absAlt;
    }

    /** 纬度 */
    public double getLat() { return lat; }

    /** 纬度 */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /** 经度 */
    public double getLng() { return lng; }

    /** 经度 */
    public void setLng(double lng) {
        this.lng = lng;
    }

    /** 绝对高度（米） */
    public double getAbsAlt() { return absAlt; }

    /** 绝对高度（米） */
    public void setAbsAlt(double absAlt) {
        this.absAlt = absAlt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFileGeoPosition that = (MediaFileGeoPosition) o;
        return Double.compare(that.lat, lat) == 0
                && Double.compare(that.lng, lng) == 0
                && Double.compare(that.absAlt, absAlt) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng, absAlt);
    }

    @Override
    public String toString() {
        return "MediaFileGeoPosition{lat=" + lat + ", lng=" + lng + ", absAlt=" + absAlt + '}';
    }
}
