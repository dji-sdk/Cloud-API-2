// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 查询飞行任务轨迹结果。 */
public final class GetFlightTaskTrackResults {
    /** 任务 UUID */
    private String uuid;
    /** 任务执行时间（ISO 8601） */
    private String runAt;
    /** 任务完成时间（ISO 8601） */
    private String completedAt;
    /** 任务名称 */
    private String name;
    /** 任务状态（0~10，同航线任务状态枚举） */
    private int status;
    /** 航线 UUID */
    private String waylineUuid;
    /** 飞行轨迹信息 */
    private FlightTrackInfo track;

    public GetFlightTaskTrackResults() {
    }

    /**
     * 构造 GetFlightTaskTrackResults。
     *
     * @param uuid 任务 UUID
     * @param runAt 任务执行时间（ISO 8601）
     * @param completedAt 任务完成时间（ISO 8601）
     * @param name 任务名称
     * @param status 任务状态（0~10，同航线任务状态枚举）
     * @param waylineUuid 航线 UUID
     * @param track 飞行轨迹信息
     */
    public GetFlightTaskTrackResults(String uuid, String runAt, String completedAt, String name,
            int status, String waylineUuid, FlightTrackInfo track) {
        this.uuid = uuid;
        this.runAt = runAt;
        this.completedAt = completedAt;
        this.name = name;
        this.status = status;
        this.waylineUuid = waylineUuid;
        this.track = track;
    }

    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** @param uuid 任务 UUID */
    public void setUuid(String uuid) { this.uuid = uuid; }
    /** 任务执行时间（ISO 8601） */
    public String getRunAt() { return runAt; }
    /** @param runAt 任务执行时间（ISO 8601） */
    public void setRunAt(String runAt) { this.runAt = runAt; }
    /** 任务完成时间（ISO 8601） */
    public String getCompletedAt() { return completedAt; }
    /** @param completedAt 任务完成时间（ISO 8601） */
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    /** 任务名称 */
    public String getName() { return name; }
    /** @param name 任务名称 */
    public void setName(String name) { this.name = name; }
    /** 任务状态（0~10，同航线任务状态枚举） */
    public int getStatus() { return status; }
    /** @param status 任务状态（0~10，同航线任务状态枚举） */
    public void setStatus(int status) { this.status = status; }
    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }
    /** @param waylineUuid 航线 UUID */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid; }
    /** 飞行轨迹信息 */
    public FlightTrackInfo getTrack() { return track; }
    /** @param track 飞行轨迹信息 */
    public void setTrack(FlightTrackInfo track) { this.track = track; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetFlightTaskTrackResults that = (GetFlightTaskTrackResults) o;
        return status == that.status
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(runAt, that.runAt)
                && Objects.equals(completedAt, that.completedAt)
                && Objects.equals(name, that.name)
                && Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(track, that.track);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, runAt, completedAt, name, status, waylineUuid, track);
    }

    @Override
    public String toString() {
        return "GetFlightTaskTrackResults{"
                + "uuid='" + uuid + '\''
                + ", runAt='" + runAt + '\''
                + ", completedAt='" + completedAt + '\''
                + ", name='" + name + '\''
                + ", status=" + status
                + ", waylineUuid='" + waylineUuid + '\''
                + ", track=" + track
                + '}';
    }
}
