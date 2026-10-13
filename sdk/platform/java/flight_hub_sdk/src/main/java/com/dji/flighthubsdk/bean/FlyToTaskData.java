// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞行至任务数据。 */
public final class FlyToTaskData {
    /** 数据库自增 ID */
    private int id;
    /** 任务 UUID */
    private String uuid;
    /** 项目 UUID */
    private String prjUuid;
    /** 机场序列号 */
    private String sn;
    /** 无人机序列号 */
    private String droneSn;
    /** 用户 ID */
    private String userId;
    /** 序列号 */
    private int seqNo;
    /** 关联的飞行任务 UUID */
    private String flightTaskUuid;
    /** 飞行任务类型：1-航线任务，2-指令任务 */
    private int flightTaskType;
    /** 任务状态：0-待执行，1-执行中，2-成功，3-失败，4-取消，5-超时 */
    private int status;
    /** 错误码 */
    private int errCode;
    /** 当前航点索引 */
    private int wayPointIndex;
    /** 航点列表 */
    private List<FlyToWayPoint> wayPoints;
    /** 规划路径点 */
    private List<FlyToWayPoint> plannedPathPoints;
    /** 剩余距离（米） */
    private double remainingDistance;
    /** 剩余时间（秒） */
    private double remainingTime;
    public FlyToTaskData() {
    }

/**
     * 构造 FlyToTaskData。
     *
     * @param id ID
     * @param uuid 任务 UUID
     * @param prjUuid 项目 UUID
     * @param sn 起飞机场序列号
     * @param droneSn 飞行器 SN
     * @param userId 操作用户 ID
     * @param seqNo 序列号
     * @param flightTaskUuid 飞行任务 UUID
     * @param flightTaskType 飞行任务类型
     * @param status 状态
     * @param errCode 错误码
     * @param wayPointIndex 航点索引
     * @param wayPoints 航点列表
     * @param plannedPathPoints 规划路径点列表
     * @param remainingDistance 剩余飞行距离，单位：米
     * @param remainingTime 剩余飞行时间，单位：秒
     */
    public FlyToTaskData(int id, String uuid, String prjUuid, String sn, String droneSn,
            String userId, int seqNo, String flightTaskUuid, int flightTaskType,
            int status, int errCode, int wayPointIndex,
            List<FlyToWayPoint> wayPoints, List<FlyToWayPoint> plannedPathPoints,
            double remainingDistance, double remainingTime) {
        this.id = id;
        this.uuid = uuid != null ? uuid : "";
        this.prjUuid = prjUuid != null ? prjUuid : "";
        this.sn = sn != null ? sn : "";
        this.droneSn = droneSn != null ? droneSn : "";
        this.userId = userId != null ? userId : "";
        this.seqNo = seqNo;
        this.flightTaskUuid = flightTaskUuid != null ? flightTaskUuid : "";
        this.flightTaskType = flightTaskType;
        this.status = status;
        this.errCode = errCode;
        this.wayPointIndex = wayPointIndex;
        this.wayPoints = wayPoints != null ? wayPoints : Collections.emptyList();
        this.plannedPathPoints = plannedPathPoints != null ? plannedPathPoints : Collections.emptyList();
        this.remainingDistance = remainingDistance;
        this.remainingTime = remainingTime;
    }

    /** 数据库自增 ID */
    public int getId() { return id; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 项目 UUID */
    public String getPrjUuid() { return prjUuid; }
    /** 机场序列号 */
    public String getSn() { return sn; }
    /** 无人机序列号 */
    public String getDroneSn() { return droneSn; }
    /** 用户 ID */
    public String getUserId() { return userId; }
    /** 序列号 */
    public int getSeqNo() { return seqNo; }
    /** 关联的飞行任务 UUID */
    public String getFlightTaskUuid() { return flightTaskUuid; }
    /** 飞行任务类型：1-航线任务，2-指令任务 */
    public int getFlightTaskType() { return flightTaskType; }
    /** 任务状态：0-待执行，1-执行中，2-成功，3-失败，4-取消，5-超时 */
    public int getStatus() { return status; }
    /** 错误码 */
    public int getErrCode() { return errCode; }
    /** 当前航点索引 */
    public int getWayPointIndex() { return wayPointIndex; }
    /** 航点列表 */
    public List<FlyToWayPoint> getWayPoints() { return wayPoints; }
    /** 规划路径点 */
    public List<FlyToWayPoint> getPlannedPathPoints() { return plannedPathPoints; }
    /** 剩余距离（米） */
    public double getRemainingDistance() { return remainingDistance; }
    /** 剩余时间（秒） */
    public double getRemainingTime() { return remainingTime; }

    /** @param id 数据库自增 ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 项目 UUID */
    public void setPrjUuid(String prjUuid) {
        this.prjUuid = prjUuid;
    }

    /** @param sn 机场序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** @param droneSn 无人机序列号 */
    public void setDroneSn(String droneSn) {
        this.droneSn = droneSn;
    }

    /** @param userId 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** 序列号 */
    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    /** @param flightTaskUuid 关联的飞行任务 UUID */
    public void setFlightTaskUuid(String flightTaskUuid) {
        this.flightTaskUuid = flightTaskUuid;
    }

    /** @param flightTaskType 飞行任务类型：1-航线任务，2-指令任务 */
    public void setFlightTaskType(int flightTaskType) {
        this.flightTaskType = flightTaskType;
    }

    /** @param status 任务状态：0-待执行，1-执行中，2-成功，3-失败，4-取消，5-超时 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 错误码 */
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    /** @param wayPointIndex 当前航点索引 */
    public void setWayPointIndex(int wayPointIndex) {
        this.wayPointIndex = wayPointIndex;
    }

    /** 航点列表 */
    public void setWayPoints(List<FlyToWayPoint> wayPoints) {
        this.wayPoints = wayPoints;
    }

    /** @param plannedPathPoints 规划路径点 */
    public void setPlannedPathPoints(List<FlyToWayPoint> plannedPathPoints) {
        this.plannedPathPoints = plannedPathPoints;
    }

    /** @param remainingDistance 剩余距离（米） */
    public void setRemainingDistance(double remainingDistance) {
        this.remainingDistance = remainingDistance;
    }

    /** @param remainingTime 剩余时间（秒） */
    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlyToTaskData that = (FlyToTaskData) o;
        return id == that.id
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(prjUuid, that.prjUuid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(droneSn, that.droneSn)
                && Objects.equals(userId, that.userId)
                && seqNo == that.seqNo
                && Objects.equals(flightTaskUuid, that.flightTaskUuid)
                && flightTaskType == that.flightTaskType
                && status == that.status
                && errCode == that.errCode
                && wayPointIndex == that.wayPointIndex
                && Objects.equals(wayPoints, that.wayPoints)
                && Objects.equals(plannedPathPoints, that.plannedPathPoints)
                && Double.compare(that.remainingDistance, remainingDistance) == 0
                && Double.compare(that.remainingTime, remainingTime) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                uuid,
                prjUuid,
                sn,
                droneSn,
                userId,
                seqNo,
                flightTaskUuid,
                flightTaskType,
                status,
                errCode,
                wayPointIndex,
                wayPoints,
                plannedPathPoints,
                remainingDistance,
                remainingTime);
    }

    @Override
    public String toString() {
        return "FlyToTaskData{"
                + "id=" + id
                + ", uuid='" + uuid + '\''
                + ", prjUuid='" + prjUuid + '\''
                + ", sn='" + sn + '\''
                + ", droneSn='" + droneSn + '\''
                + ", userId='" + userId + '\''
                + ", seqNo=" + seqNo
                + ", flightTaskUuid='" + flightTaskUuid + '\''
                + ", flightTaskType=" + flightTaskType
                + ", status=" + status
                + ", errCode=" + errCode
                + ", wayPointIndex=" + wayPointIndex
                + ", wayPoints=" + wayPoints
                + ", plannedPathPoints=" + plannedPathPoints
                + ", remainingDistance=" + remainingDistance
                + ", remainingTime=" + remainingTime
                + '}';
    }
}
