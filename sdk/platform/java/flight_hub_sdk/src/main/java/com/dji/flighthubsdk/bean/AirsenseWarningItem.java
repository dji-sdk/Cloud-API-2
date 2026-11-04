// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * AirSense 民航飞机告警单项。
 */
public final class AirsenseWarningItem {

    /** string, ICAO 民航飞机地址; */
    private String icao;
    /** int, 告警等级, {"0":"无危险","1":"等级一","2":"等级二","3":"等级三","4":"等级四"}; ≥3 建议避让 */
    private int warningLevel;
    /** double, 纬度, [-90,90]; */
    private double latitude;
    /** double, 经度, [-180,180]; */
    private double longitude;
    /** int, 绝对高度（米）; */
    private int altitude;
    /** int, 高度类型, {"0":"椭球高","1":"海拔高"}; */
    private int altitudeType;
    /** double, 航向角, 0 正北、90 正东; */
    private double heading;
    /** int, 相对无人机高度（米）; */
    private int relativeAltitude;
    /** int, 相对高度变化趋势, {"0":"不变","1":"上升","2":"下降"}; */
    private int vertTrend;
    /** int, 与无人机水平距离（米）; */
    private int distance;

    public AirsenseWarningItem() {
    }

    /**
     * 构造 AirsenseWarningItem。
     */
    public AirsenseWarningItem(
            String icao,
            int warningLevel,
            double latitude,
            double longitude,
            int altitude,
            int altitudeType,
            double heading,
            int relativeAltitude,
            int vertTrend,
            int distance) {
        this.icao = icao != null ? icao : "";
        this.warningLevel = warningLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeType = altitudeType;
        this.heading = heading;
        this.relativeAltitude = relativeAltitude;
        this.vertTrend = vertTrend;
        this.distance = distance;
    }

    /** string, ICAO 民航飞机地址; */
    public String getIcao() { return icao; }
    /** int, 告警等级; */
    public int getWarningLevel() { return warningLevel; }
    /** double, 纬度; */
    public double getLatitude() { return latitude; }
    /** double, 经度; */
    public double getLongitude() { return longitude; }
    /** int, 绝对高度（米）; */
    public int getAltitude() { return altitude; }
    /** int, 高度类型; */
    public int getAltitudeType() { return altitudeType; }
    /** double, 航向角; */
    public double getHeading() { return heading; }
    /** int, 相对无人机高度（米）; */
    public int getRelativeAltitude() { return relativeAltitude; }
    /** int, 相对高度变化趋势; */
    public int getVertTrend() { return vertTrend; }
    /** int, 与无人机水平距离（米）; */
    public int getDistance() { return distance; }

    /** @param icao ICAO 民航飞机地址; */
    public void setIcao(String icao) { this.icao = icao; }
    /** @param warningLevel 告警等级; */
    public void setWarningLevel(int warningLevel) { this.warningLevel = warningLevel; }
    /** @param latitude 纬度; */
    public void setLatitude(double latitude) { this.latitude = latitude; }
    /** @param longitude 经度; */
    public void setLongitude(double longitude) { this.longitude = longitude; }
    /** @param altitude 绝对高度（米）; */
    public void setAltitude(int altitude) { this.altitude = altitude; }
    /** @param altitudeType 高度类型; */
    public void setAltitudeType(int altitudeType) { this.altitudeType = altitudeType; }
    /** @param heading 航向角; */
    public void setHeading(double heading) { this.heading = heading; }
    /** @param relativeAltitude 相对无人机高度（米）; */
    public void setRelativeAltitude(int relativeAltitude) { this.relativeAltitude = relativeAltitude; }
    /** @param vertTrend 相对高度变化趋势; */
    public void setVertTrend(int vertTrend) { this.vertTrend = vertTrend; }
    /** @param distance 与无人机水平距离（米）; */
    public void setDistance(int distance) { this.distance = distance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AirsenseWarningItem that = (AirsenseWarningItem) o;
        return warningLevel == that.warningLevel
                && Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && altitude == that.altitude
                && altitudeType == that.altitudeType
                && Double.compare(that.heading, heading) == 0
                && relativeAltitude == that.relativeAltitude
                && vertTrend == that.vertTrend
                && distance == that.distance
                && Objects.equals(icao, that.icao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                icao,
                warningLevel,
                latitude,
                longitude,
                altitude,
                altitudeType,
                heading,
                relativeAltitude,
                vertTrend,
                distance);
    }

    @Override
    public String toString() {
        return "AirsenseWarningItem{"
                + "icao='" + icao + '\''
                + ", warningLevel=" + warningLevel
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", altitude=" + altitude
                + ", altitudeType=" + altitudeType
                + ", heading=" + heading
                + ", relativeAltitude=" + relativeAltitude
                + ", vertTrend=" + vertTrend
                + ", distance=" + distance
                + '}';
    }
}
