// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 指点飞行进度信息 */
public final class FlyToPointProgress {

    /** 业务标识(bid) */
    private String bid;
    /** 事务 ID */
    private String tid;
    /** 时间戳 */
    private long timestamp;
    /** 指点飞行任务 ID */
    private String flyToId;
    /** 飞行 ID */
    private String flightId;
    /** 任务序号 */
    private int taskSeq;
    /** 飞行器 SN */
    private String sn;
    /** 执行状态 */
    private String status;
    /** 执行结果 */
    private int result;
    /** 当前航点索引 */
    private int waypointIndex;
    public FlyToPointProgress() {
    }

/**
     * 构造 FlyToPointProgress。
     *
     * @param bid 业务 ID
     * @param tid 事务 ID
     * @param timestamp 时间戳
     * @param flyToId 指点飞行任务 ID
     * @param flightId 飞行 ID
     * @param taskSeq 任务序号
     * @param sn 飞行器 SN
     * @param status 执行状态
     * @param result 执行结果
     * @param waypointIndex 当前航点索引
     */
    public FlyToPointProgress(
            String bid,
            String tid,
            long timestamp,
            String flyToId,
            String flightId,
            int taskSeq,
            String sn,
            String status,
            int result,
            int waypointIndex) {
        this.bid = bid != null ? bid : "";
        this.tid = tid != null ? tid : "";
        this.timestamp = timestamp;
        this.flyToId = flyToId != null ? flyToId : "";
        this.flightId = flightId != null ? flightId : "";
        this.taskSeq = taskSeq;
        this.sn = sn != null ? sn : "";
        this.status = status != null ? status : "";
        this.result = result;
        this.waypointIndex = waypointIndex;
    }

    /** 业务标识(bid) */
    public String getBid() {
        return bid;
    }

    /** @param bid 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 事务 ID */
    public String getTid() {
        return tid;
    }

    /** 事务 ID */
    public void setTid(String tid) {
        this.tid = tid;
    }

    /** 时间戳 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 时间戳 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 指点飞行任务 ID */
    public String getFlyToId() {
        return flyToId;
    }

    /** 指点飞行任务 ID */
    public void setFlyToId(String flyToId) {
        this.flyToId = flyToId;
    }

    /** 飞行 ID */
    public String getFlightId() {
        return flightId;
    }

    /** 飞行 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** 任务序号 */
    public int getTaskSeq() {
        return taskSeq;
    }

    /** 任务序号 */
    public void setTaskSeq(int taskSeq) {
        this.taskSeq = taskSeq;
    }

    /** 飞行器 SN */
    public String getSn() {
        return sn;
    }

    /** 飞行器 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 执行状态 */
    public String getStatus() {
        return status;
    }

    /** 执行状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 执行结果 */
    public int getResult() {
        return result;
    }

    /** 执行结果 */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlyToPointProgress that = (FlyToPointProgress) o;
        return timestamp == that.timestamp
                && taskSeq == that.taskSeq
                && result == that.result
                && waypointIndex == that.waypointIndex
                && Objects.equals(bid, that.bid)
                && Objects.equals(tid, that.tid)
                && Objects.equals(flyToId, that.flyToId)
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(sn, that.sn)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, tid, timestamp, flyToId, flightId, taskSeq, sn, status, result, waypointIndex);
    }

    @Override
    public String toString() {
        return "FlyToPointProgress{bid='" + bid + '\''
                + ", tid='" + tid + '\''
                + ", timestamp=" + timestamp
                + ", flyToId='" + flyToId + '\''
                + ", flightId='" + flightId + '\''
                + ", taskSeq=" + taskSeq
                + ", sn='" + sn + '\''
                + ", status='" + status + '\''
                + ", result=" + result
                + ", waypointIndex=" + waypointIndex + '}';
    }
}
