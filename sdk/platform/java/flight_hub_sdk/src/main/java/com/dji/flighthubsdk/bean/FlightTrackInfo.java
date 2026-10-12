// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 飞行轨迹信息 */
public final class FlightTrackInfo {
    /** 轨迹 ID */
    private String trackId;
    /** 无人机 SN */
    private String droneSn;
    /** 飞行距离（米） */
    private int flightDistance;
    /** 飞行时长（秒） */
    private int flightDuration;
    /** 轨迹点列表 */
    private List<FlightTrackPoint> points;

    public FlightTrackInfo() {
    }

    /**
     * 构造 FlightTrackInfo。
     *
     * @param trackId 轨迹 ID
     * @param droneSn 无人机 SN
     * @param flightDistance 飞行距离（米）
     * @param flightDuration 飞行时长（秒）
     * @param points 轨迹点列表
     */
    public FlightTrackInfo(String trackId, String droneSn, int flightDistance, int flightDuration, List<FlightTrackPoint> points) {
        this.trackId = trackId;
        this.droneSn = droneSn;
        this.flightDistance = flightDistance;
        this.flightDuration = flightDuration;
        this.points = points != null ? points : Collections.emptyList();
    }

    /** 轨迹 ID */
    public String getTrackId() { return trackId; }
    /** @param trackId 轨迹 ID */
    public void setTrackId(String trackId) { this.trackId = trackId; }
    /** 无人机 SN */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 无人机 SN */
    public void setDroneSn(String droneSn) { this.droneSn = droneSn; }
    /** 飞行距离（米） */
    public int getFlightDistance() { return flightDistance; }
    /** @param flightDistance 飞行距离（米） */
    public void setFlightDistance(int flightDistance) { this.flightDistance = flightDistance; }
    /** 飞行时长（秒） */
    public int getFlightDuration() { return flightDuration; }
    /** @param flightDuration 飞行时长（秒） */
    public void setFlightDuration(int flightDuration) { this.flightDuration = flightDuration; }
    /** 轨迹点列表 */
    public List<FlightTrackPoint> getPoints() { return points; }
    /** @param points 轨迹点列表 */
    public void setPoints(List<FlightTrackPoint> points) { this.points = points != null ? points : Collections.emptyList(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTrackInfo that = (FlightTrackInfo) o;
        return flightDistance == that.flightDistance
                && flightDuration == that.flightDuration
                && Objects.equals(trackId, that.trackId)
                && Objects.equals(droneSn, that.droneSn)
                && Objects.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId, droneSn, flightDistance, flightDuration, points);
    }

    @Override
    public String toString() {
        return "FlightTrackInfo{"
                + "trackId='" + trackId + '\''
                + ", droneSn='" + droneSn + '\''
                + ", flightDistance=" + flightDistance
                + ", flightDuration=" + flightDuration
                + ", points=" + points
                + '}';
    }
}
