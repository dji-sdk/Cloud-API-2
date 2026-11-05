// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 贴近摄影模型文件信息。 */
public final class WaylineExtDataModel {

    /** 模型边界信息（中心点 + 半长）。 */
    public static final class Boundary {
        private double latitude;
        private double longitude;
        private double altitude;
        private double halfX;
        private double halfY;
        private double halfZ;
        private List<Double> transform = new ArrayList<>();

        public Boundary() {
        }

        /** 中心纬度 */
        public double getLatitude() { return latitude; }

        /** @param latitude 中心纬度 */
        public void setLatitude(double latitude) { this.latitude = latitude; }

        /** 中心经度 */
        public double getLongitude() { return longitude; }

        /** @param longitude 中心经度 */
        public void setLongitude(double longitude) { this.longitude = longitude; }

        /** 中心海拔高度 */
        public double getAltitude() { return altitude; }

        /** @param altitude 中心海拔高度 */
        public void setAltitude(double altitude) { this.altitude = altitude; }

        /** 经度距离半长（米） */
        public double getHalfX() { return halfX; }

        /** @param halfX 经度距离半长（米） */
        public void setHalfX(double halfX) { this.halfX = halfX; }

        /** 纬度距离半长（米） */
        public double getHalfY() { return halfY; }

        /** @param halfY 纬度距离半长（米） */
        public void setHalfY(double halfY) { this.halfY = halfY; }

        /** 海拔高度半长（米） */
        public double getHalfZ() { return halfZ; }

        /** @param halfZ 海拔高度半长（米） */
        public void setHalfZ(double halfZ) { this.halfZ = halfZ; }

        /** 变换矩阵 */
        public List<Double> getTransform() { return transform; }

        /** @param transform 变换矩阵 */
        public void setTransform(List<Double> transform) {
            this.transform = transform != null ? transform : new ArrayList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Boundary that = (Boundary) o;
            return Double.compare(that.latitude, latitude) == 0
                    && Double.compare(that.longitude, longitude) == 0
                    && Double.compare(that.altitude, altitude) == 0
                    && Double.compare(that.halfX, halfX) == 0
                    && Double.compare(that.halfY, halfY) == 0
                    && Double.compare(that.halfZ, halfZ) == 0
                    && Objects.equals(transform, that.transform);
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude, altitude, halfX, halfY, halfZ, transform);
        }

        @Override
        public String toString() {
            return "Boundary{latitude=" + latitude + ", longitude=" + longitude
                    + ", altitude=" + altitude + ", halfX=" + halfX
                    + ", halfY=" + halfY + ", halfZ=" + halfZ
                    + ", transform=" + transform + '}';
        }
    }

    /** 二维地理坐标点。 */
    public static final class BoundingPoint {
        private double lng;
        private double lat;

        public BoundingPoint() {
        }

        /** 经度 */
        public double getLng() { return lng; }

        /** @param lng 经度 */
        public void setLng(double lng) { this.lng = lng; }

        /** 纬度 */
        public double getLat() { return lat; }

        /** @param lat 纬度 */
        public void setLat(double lat) { this.lat = lat; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoundingPoint that = (BoundingPoint) o;
            return Double.compare(that.lng, lng) == 0 && Double.compare(that.lat, lat) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lng, lat);
        }

        @Override
        public String toString() {
            return "BoundingPoint{lng=" + lng + ", lat=" + lat + '}';
        }
    }

    /** 二维地理多边形。 */
    public static final class Bounding {
        private List<BoundingPoint> points = new ArrayList<>();

        public Bounding() {
        }

        /** 多边形顶点列表 */
        public List<BoundingPoint> getPoints() { return points; }

        /** @param points 多边形顶点列表 */
        public void setPoints(List<BoundingPoint> points) {
            this.points = points != null ? points : new ArrayList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bounding that = (Bounding) o;
            return Objects.equals(points, that.points);
        }

        @Override
        public int hashCode() {
            return Objects.hash(points);
        }

        @Override
        public String toString() {
            return "Bounding{points=" + points + '}';
        }
    }

    /** 模型 UUID */
    private String uuid;
    /** 文件存储 Key */
    private String fileKey;
    /** 模型边界信息 */
    private Boundary boundary;
    /** 模型包围多边形 */
    private Bounding bounding;

    public WaylineExtDataModel() {
    }

    /** 模型 UUID */
    public String getUuid() { return uuid; }

    /** @param uuid 模型 UUID */
    public void setUuid(String uuid) { this.uuid = uuid; }

    /** 文件存储 Key */
    public String getFileKey() { return fileKey; }

    /** @param fileKey 文件存储 Key */
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    /** 模型边界信息 */
    public Boundary getBoundary() { return boundary; }

    /** @param boundary 模型边界信息 */
    public void setBoundary(Boundary boundary) { this.boundary = boundary; }

    /** 模型包围多边形 */
    public Bounding getBounding() { return bounding; }

    /** @param bounding 模型包围多边形 */
    public void setBounding(Bounding bounding) { this.bounding = bounding; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaylineExtDataModel that = (WaylineExtDataModel) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(fileKey, that.fileKey)
                && Objects.equals(boundary, that.boundary)
                && Objects.equals(bounding, that.bounding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, fileKey, boundary, bounding);
    }

    @Override
    public String toString() {
        return "WaylineExtDataModel{uuid='" + uuid + '\''
                + ", fileKey='" + fileKey + '\''
                + ", boundary=" + boundary
                + ", bounding=" + bounding + '}';
    }
}
