// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 单机飞行任务状态。 */
public final class FlightTask {

    private String uuid;
    private String name;
    private int taskType;
    private int status;
    private int progress;
    private String sn;
    private String folderInfo;
    /** 任务实际运行时间 */
    private long runAt;
    /** 任务结束时间 */
    private long completeAt;
    /** 总航线数 */
    private int totalWaypoints;
    /** 已飞完的航点数 */
    private int currentWaypointIndex;
    /** 消息的时间戳，用来处理乱序问题 */
    private long progressVersion;
    private String resumableStatus;
    private String obstacleAvoidanceNotify;
    private String waylineUuid;
    public FlightTask() {
    }

/**
     * 构造 FlightTask。
     *
     * @param uuid 任务 UUID
     * @param name 任务名称
     * @param taskType 任务类型
     * @param status 任务状态
     * @param progress 任务进度
     * @param sn 设备序列号
     * @param folderInfo 文件夹信息
     * @param runAt 任务实际运行时间
     * @param completeAt 任务结束时间
     * @param totalWaypoints 总航点数
     * @param currentWaypointIndex 已飞完的航点数
     * @param progressVersion 消息时间戳，用于处理乱序
     * @param resumableStatus 断点续飞状态
     * @param obstacleAvoidanceNotify 避障通知类型
     * @param waylineUuid 航线 UUID
     */
    public FlightTask(
            String uuid,
            String name,
            int taskType,
            int status,
            int progress,
            String sn,
            String folderInfo,
            long runAt,
            long completeAt,
            int totalWaypoints,
            int currentWaypointIndex,
            long progressVersion,
            String resumableStatus,
            String obstacleAvoidanceNotify,
            String waylineUuid) {
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.taskType = taskType;
        this.status = status;
        this.progress = progress;
        this.sn = sn != null ? sn : "";
        this.folderInfo = folderInfo != null ? folderInfo : "";
        this.runAt = runAt;
        this.completeAt = completeAt;
        this.totalWaypoints = totalWaypoints;
        this.currentWaypointIndex = currentWaypointIndex;
        this.progressVersion = progressVersion;
        this.resumableStatus = resumableStatus != null ? resumableStatus : "";
        this.obstacleAvoidanceNotify = obstacleAvoidanceNotify != null ? obstacleAvoidanceNotify : "";
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
    }

    /** 任务 UUID */
    public String getUuid() {
        return uuid;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 任务名称 */
    public String getName() {
        return name;
    }

    /** 任务名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 任务类型 */
    public int getTaskType() {
        return taskType;
    }

    /** 任务类型 */
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    /** 任务状态 */
    public int getStatus() {
        return status;
    }

    /** 任务状态 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 任务进度 */
    public int getProgress() {
        return progress;
    }

    /** 任务进度 */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 文件夹信息 */
    public String getFolderInfo() {
        return folderInfo;
    }

    /** 文件夹信息 */
    public void setFolderInfo(String folderInfo) {
        this.folderInfo = folderInfo;
    }

    /** 任务实际运行时间 */
    public long getRunAt() {
        return runAt;
    }

    /** 任务实际运行时间 */
    public void setRunAt(long runAt) {
        this.runAt = runAt;
    }

    /** 任务结束时间 */
    public long getCompleteAt() {
        return completeAt;
    }

    /** 任务结束时间 */
    public void setCompleteAt(long completeAt) {
        this.completeAt = completeAt;
    }

    /** 总航线数 */
    public int getTotalWaypoints() {
        return totalWaypoints;
    }

    /** @param totalWaypoints 总航线数 */
    public void setTotalWaypoints(int totalWaypoints) {
        this.totalWaypoints = totalWaypoints;
    }

    /** 已飞完的航点数 */
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }

    /** 已飞完的航点数 */
    public void setCurrentWaypointIndex(int currentWaypointIndex) {
        this.currentWaypointIndex = currentWaypointIndex;
    }

    /** 消息的时间戳，用来处理乱序问题 */
    public long getProgressVersion() {
        return progressVersion;
    }

    /** @param progressVersion 消息的时间戳，用来处理乱序问题 */
    public void setProgressVersion(long progressVersion) {
        this.progressVersion = progressVersion;
    }

    /** 断点续飞状态 */
    public String getResumableStatus() {
        return resumableStatus;
    }

    /** 断点续飞状态 */
    public void setResumableStatus(String resumableStatus) {
        this.resumableStatus = resumableStatus;
    }

    /** 避障通知类型 */
    public String getObstacleAvoidanceNotify() {
        return obstacleAvoidanceNotify;
    }

    /** 避障通知类型 */
    public void setObstacleAvoidanceNotify(String obstacleAvoidanceNotify) {
        this.obstacleAvoidanceNotify = obstacleAvoidanceNotify;
    }

    /** 航线 UUID */
    public String getWaylineUuid() {
        return waylineUuid;
    }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTask that = (FlightTask) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && taskType == that.taskType
                && status == that.status
                && progress == that.progress
                && Objects.equals(sn, that.sn)
                && Objects.equals(folderInfo, that.folderInfo)
                && runAt == that.runAt
                && completeAt == that.completeAt
                && totalWaypoints == that.totalWaypoints
                && currentWaypointIndex == that.currentWaypointIndex
                && progressVersion == that.progressVersion
                && Objects.equals(resumableStatus, that.resumableStatus)
                && Objects.equals(obstacleAvoidanceNotify, that.obstacleAvoidanceNotify)
                && Objects.equals(waylineUuid, that.waylineUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid,
                name,
                taskType,
                status,
                progress,
                sn,
                folderInfo,
                runAt,
                completeAt,
                totalWaypoints,
                currentWaypointIndex,
                progressVersion,
                resumableStatus,
                obstacleAvoidanceNotify,
                waylineUuid);
    }

    @Override
    public String toString() {
        return "FlightTask{"
                + "uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", taskType=" + taskType
                + ", status=" + status
                + ", progress=" + progress
                + ", sn='" + sn + '\''
                + ", folderInfo='" + folderInfo + '\''
                + ", runAt=" + runAt
                + ", completeAt=" + completeAt
                + ", totalWaypoints=" + totalWaypoints
                + ", currentWaypointIndex=" + currentWaypointIndex
                + ", progressVersion=" + progressVersion
                + ", resumableStatus='" + resumableStatus + '\''
                + ", obstacleAvoidanceNotify='" + obstacleAvoidanceNotify + '\''
                + ", waylineUuid='" + waylineUuid + '\''
                + '}';
    }
}
