// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 起飞进度。
 */
public final class AircraftTakeoffProgress {

    private String bid;
    private String tid;
    private String flightId;
    private String trackId;
    private String sn;
    private String status;
    private int result;
    private int waypointIndex;
    /** Takeoff/TakeoffWithFlyTo */
    private String takeoffMode;
    private long timestamp;
    public AircraftTakeoffProgress() {
    }

/**
     * 构造 AircraftTakeoffProgress。
     *
     * @param bid 业务标识(bid)
     * @param tid 事务标识(tid)
     * @param flightId 飞行任务 ID
     * @param trackId 航迹 ID
     * @param sn 设备序列号(SN)
     * @param status SDK 可见状态："task_ready"=已准备起飞（飞机就绪即将起飞）；"task_finish"=一键起飞流程结束（配合 result 判断成败）
     * @param result 结果码（0=成功，&gt;0=失败，具体错误码由设备/DTCP 返回；仅 task_finish 时有意义）
     * @param waypointIndex 当前航点索引
     * @param takeoffMode 起飞模式（Takeoff/TakeoffWithFlyTo）
     * @param timestamp 时间戳
     */
    public AircraftTakeoffProgress(
            String bid,
            String tid,
            String flightId,
            String trackId,
            String sn,
            String status,
            int result,
            int waypointIndex,
            String takeoffMode,
            long timestamp) {
        this.bid = bid;
        this.tid = tid;
        this.flightId = flightId;
        this.trackId = trackId;
        this.sn = sn;
        this.status = status;
        this.result = result;
        this.waypointIndex = waypointIndex;
        this.takeoffMode = takeoffMode;
        this.timestamp = timestamp;
    }

    /** 业务标识(bid) */
    public String getBid() {
        return bid;
    }

    /** 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 事务标识(tid) */
    public String getTid() {
        return tid;
    }

    /** 事务标识(tid) */
    public void setTid(String tid) {
        this.tid = tid;
    }

    /** 飞行任务 ID */
    public String getFlightId() {
        return flightId;
    }

    /** 飞行任务 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 航迹 ID */
    public String getTrackId() {
        return trackId;
    }

    /** 航迹 ID */
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    /** 设备序列号(SN) */
    public String getSn() {
        return sn;
    }

    /** 设备序列号(SN) */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /**
     * SDK 可见的状态枚举（pf-atc-service WebSocket 过滤后仅推送以下两种）：
     * <ul>
     *   <li>"task_ready" - 已准备起飞（飞机就绪，即将起飞）</li>
     *   <li>"task_finish" - 一键起飞流程结束（成功或失败，需配合 {@link #getResult()} 判断；失败时 status 仍为 task_finish）</li>
     * </ul>
     * 上游设备还可能上报 takeoff_prepare、wayline_progress、wayline_cancel、wayline_failed、wayline_ok，但不会推送给 SDK。
     */
    public String getStatus() {
        return status;
    }

    /**
     * SDK 可见的状态枚举（pf-atc-service WebSocket 过滤后仅推送以下两种）：
     * <ul>
     *   <li>"task_ready" - 已准备起飞（飞机就绪，即将起飞）</li>
     *   <li>"task_finish" - 一键起飞流程结束（成功或失败，需配合 {@link #getResult()} 判断；失败时 status 仍为 task_finish）</li>
     * </ul>
     * 上游设备还可能上报 takeoff_prepare、wayline_progress、wayline_cancel、wayline_failed、wayline_ok，但不会推送给 SDK。
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 结果码：0 表示成功，大于 0 表示失败（具体错误码由设备/DTCP 返回）。
     * 仅在 status 为 "task_finish" 时有意义。
     */
    public int getResult() {
        return result;
    }

    /**
     * 结果码：0 表示成功，大于 0 表示失败（具体错误码由设备/DTCP 返回）。
     * 仅在 status 为 "task_finish" 时有意义。
     */
    public void setResult(int result) {
        this.result = result;
    }

    /** 当前航点索引 */
    public int getWaypointIndex() {
        return waypointIndex;
    }

    /** 当前航点索引 */
    public void setWaypointIndex(int waypointIndex) {
        this.waypointIndex = waypointIndex;
    }

    /** Takeoff/TakeoffWithFlyTo */
    public String getTakeoffMode() {
        return takeoffMode;
    }

    /** @param takeoffMode Takeoff/TakeoffWithFlyTo */
    public void setTakeoffMode(String takeoffMode) {
        this.takeoffMode = takeoffMode;
    }

    /** 时间戳 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftTakeoffProgress that = (AircraftTakeoffProgress) o;
        return result == that.result
                && waypointIndex == that.waypointIndex
                && timestamp == that.timestamp
                && Objects.equals(bid, that.bid)
                && Objects.equals(tid, that.tid)
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(trackId, that.trackId)
                && Objects.equals(sn, that.sn)
                && Objects.equals(status, that.status)
                && Objects.equals(takeoffMode, that.takeoffMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, tid, flightId, trackId, sn, status, result, waypointIndex, takeoffMode, timestamp);
    }

    @Override
    public String toString() {
        return "AircraftTakeoffProgress{bid='" + bid + '\'' + ", tid='" + tid + '\''
                + ", flightId='" + flightId + '\'' + ", trackId='" + trackId + '\''
                + ", sn='" + sn + '\'' + ", status='" + status + '\'' + ", result=" + result
                + ", waypointIndex=" + waypointIndex + ", takeoffMode='" + takeoffMode + '\''
                + ", timestamp=" + timestamp + '}';
    }
}
