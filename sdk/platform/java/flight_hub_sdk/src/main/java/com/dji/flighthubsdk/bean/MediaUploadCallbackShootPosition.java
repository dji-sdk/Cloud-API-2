// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传回调通知的拍摄位置（经纬度）。
 */
public final class MediaUploadCallbackShootPosition {
    private double lat;
    private double lng;
    public MediaUploadCallbackShootPosition() {
    }

/**
     * 构造 MediaUploadCallbackShootPosition。
     *
     * @param lat 纬度
     * @param lng 经度
     */
    public MediaUploadCallbackShootPosition(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    /** 纬度 */
    public double getLat() { return lat; }

    /** 经度 */
    public double getLng() { return lng; }

    /** 纬度 */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /** 经度 */
    public void setLng(double lng) {
        this.lng = lng;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadCallbackShootPosition that = (MediaUploadCallbackShootPosition) o;
        return Double.compare(that.lat, lat) == 0 && Double.compare(that.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }

    @Override
    public String toString() {
        return "MediaUploadCallbackShootPosition{lat=" + lat + ", lng=" + lng + '}';
    }
}
