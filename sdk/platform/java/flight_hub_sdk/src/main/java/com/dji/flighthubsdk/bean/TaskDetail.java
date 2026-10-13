// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 任务详情。 */
public final class TaskDetail {
    /** 任务 UUID */
    private String uuid;
    /** 任务名称 */
    private String name;
    /** 任务类型：1=立即任务 2=条件任务 */
    private int taskType;
    /** 任务状态。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中 */
    private int status;
    /** 设备 SN */
    private String sn;
    /** 降落机场 SN */
    private String landingDockSn;
    /** 航线 UUID */
    private String waylineUuid;
    /** 任务开始时间（ISO 8601） */
    private String beginAt;
    /** 任务结束时间（ISO 8601） */
    private String endAt;
    /** 飞行中失控动作。枚举：1=返航, 2=继续执行 */
    private int outOfControlActionInFlight;
    /** 返航高度，单位：米 */
    private double rthAltitude;
    /** 返航高度模式。枚举：0=智能高度, 1=设定高度 */
    private int rthMode;
    /** 最低电量要求，单位：% */
    private int minBatteryCapacity;
    /** 航线精度类型。枚举：0=GPS, 1=RTK */
    private int waylinePrecisionType;
    /** 航线分段索引列表 */
    private List<Integer> waylineSegmentIndexes;
    /** 当前航点索引 */
    private int currentWaypointIndex;
    /** 总航点数 */
    private int totalWaypoints;
    /** 时区 */
    private String timeZone;
    /** 设备类型。枚举：0=机场, 1=遥控器 */
    private int deviceType;
    /** 业务标识(bid) */
    private String bid;
    /** 断点续飞信息 */
    private WaylineBreakPoint breakPoint;
    /** 完成时间（ISO 8601） */
    private String completedAt;
    /** 创建时间（ISO 8601） */
    private String createdAt;
    /** 删除时间（ISO 8601） */
    private String deletedAt;
    /** 是否支持断点续飞 */
    private boolean isBreakPointResume;
    /** 媒体上传状态 */
    private int mediaUploadStatus;
    /** 避障信息列表 */
    private List<WaylineObstacle> obstacles;
    /** 实际执行时间（ISO 8601） */
    private String runAt;
    /** 任务标签列表 */
    private List<String> tags;
    /** 更新时间（ISO 8601） */
    private String updatedAt;
    /** 航线预估时长，单位：秒 */
    private double waylineDuration;
    public TaskDetail() {
    }

/**
     * 构造 TaskDetail。
     */
    public TaskDetail(
            String uuid,
            String name,
            int taskType,
            int status,
            String sn,
            String landingDockSn,
            String waylineUuid,
            String beginAt,
            String endAt,
            int outOfControlActionInFlight,
            double rthAltitude,
            int rthMode,
            int minBatteryCapacity,
            int waylinePrecisionType,
            List<Integer> waylineSegmentIndexes,
            int currentWaypointIndex,
            int totalWaypoints,
            String timeZone,
            int deviceType,
            String bid,
            WaylineBreakPoint breakPoint,
            String completedAt,
            String createdAt,
            String deletedAt,
            boolean isBreakPointResume,
            int mediaUploadStatus,
            List<WaylineObstacle> obstacles,
            String runAt,
            List<String> tags,
            String updatedAt,
            double waylineDuration) {
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.taskType = taskType;
        this.status = status;
        this.sn = sn != null ? sn : "";
        this.landingDockSn = landingDockSn != null ? landingDockSn : "";
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.beginAt = beginAt != null ? beginAt : "";
        this.endAt = endAt != null ? endAt : "";
        this.outOfControlActionInFlight = outOfControlActionInFlight;
        this.rthAltitude = rthAltitude;
        this.rthMode = rthMode;
        this.minBatteryCapacity = minBatteryCapacity;
        this.waylinePrecisionType = waylinePrecisionType;
        this.waylineSegmentIndexes =
                waylineSegmentIndexes != null ? waylineSegmentIndexes : Collections.emptyList();
        this.currentWaypointIndex = currentWaypointIndex;
        this.totalWaypoints = totalWaypoints;
        this.timeZone = timeZone != null ? timeZone : "";
        this.deviceType = deviceType;
        this.bid = bid != null ? bid : "";
        this.breakPoint = breakPoint;
        this.completedAt = completedAt != null ? completedAt : "";
        this.createdAt = createdAt != null ? createdAt : "";
        this.deletedAt = deletedAt != null ? deletedAt : "";
        this.isBreakPointResume = isBreakPointResume;
        this.mediaUploadStatus = mediaUploadStatus;
        this.obstacles =
                obstacles != null ? obstacles : Collections.emptyList();
        this.runAt = runAt != null ? runAt : "";
        this.tags = tags != null ? tags : Collections.emptyList();
        this.updatedAt = updatedAt != null ? updatedAt : "";
        this.waylineDuration = waylineDuration;
    }

    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 任务名称 */
    public String getName() { return name; }
    /** 任务类型：1=立即任务 2=条件任务 */
    public int getTaskType() { return taskType; }
    /** 任务状态。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中 */
    public int getStatus() { return status; }
    /** 设备 SN */
    public String getSn() { return sn; }
    /** 降落机场 SN */
    public String getLandingDockSn() { return landingDockSn; }
    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }
    /** 任务开始时间（ISO 8601） */
    public String getBeginAt() { return beginAt; }
    /** 任务结束时间（ISO 8601） */
    public String getEndAt() { return endAt; }
    /** 飞行中失控动作。枚举：1=返航, 2=继续执行 */
    public int getOutOfControlActionInFlight() { return outOfControlActionInFlight; }
    /** 返航高度，单位：米 */
    public double getRthAltitude() { return rthAltitude; }
    /** 返航高度模式。枚举：0=智能高度, 1=设定高度 */
    public int getRthMode() { return rthMode; }
    /** 最低电量要求，单位：% */
    public int getMinBatteryCapacity() { return minBatteryCapacity; }
    /** 航线精度类型。枚举：0=GPS, 1=RTK */
    public int getWaylinePrecisionType() { return waylinePrecisionType; }
    /** 航线分段索引列表 */
    public List<Integer> getWaylineSegmentIndexes() { return waylineSegmentIndexes; }
    /** 当前航点索引 */
    public int getCurrentWaypointIndex() { return currentWaypointIndex; }
    /** 总航点数 */
    public int getTotalWaypoints() { return totalWaypoints; }
    /** 时区 */
    public String getTimeZone() { return timeZone; }
    /** 设备类型。枚举：0=机场, 1=遥控器 */
    public int getDeviceType() { return deviceType; }
    /** 业务标识(bid) */
    public String getBid() { return bid; }
    /** 断点续飞信息 */
    public WaylineBreakPoint getBreakPoint() { return breakPoint; }
    /** 完成时间（ISO 8601） */
    public String getCompletedAt() { return completedAt; }
    /** 创建时间（ISO 8601） */
    public String getCreatedAt() { return createdAt; }
    /** 删除时间（ISO 8601） */
    public String getDeletedAt() { return deletedAt; }
    /** 是否支持断点续飞 */
    public boolean isBreakPointResume() { return isBreakPointResume; }
    /** 媒体上传状态 */
    public int getMediaUploadStatus() { return mediaUploadStatus; }
    /** 避障信息列表 */
    public List<WaylineObstacle> getObstacles() { return obstacles; }
    /** 实际执行时间（ISO 8601） */
    public String getRunAt() { return runAt; }
    /** 任务标签列表 */
    public List<String> getTags() { return tags; }
    /** 更新时间（ISO 8601） */
    public String getUpdatedAt() { return updatedAt; }
    /** 航线预估时长，单位：秒 */
    public double getWaylineDuration() { return waylineDuration; }

    /** @param uuid 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 任务名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @param taskType 任务类型：1=立即任务 2=条件任务 */
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    /** 任务状态。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** @param sn 设备 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** @param landingDockSn 降落机场 SN */
    public void setLandingDockSn(String landingDockSn) {
        this.landingDockSn = landingDockSn;
    }

    /** @param waylineUuid 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** 任务开始时间（ISO 8601） */
    public void setBeginAt(String beginAt) {
        this.beginAt = beginAt;
    }

    /** 任务结束时间（ISO 8601） */
    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    /** 飞行中失控动作。枚举：1=返航, 2=继续执行 */
    public void setOutOfControlActionInFlight(int outOfControlActionInFlight) {
        this.outOfControlActionInFlight = outOfControlActionInFlight;
    }

    /** 返航高度，单位：米 */
    public void setRthAltitude(double rthAltitude) {
        this.rthAltitude = rthAltitude;
    }

    /** 返航高度模式。枚举：0=智能高度, 1=设定高度 */
    public void setRthMode(int rthMode) {
        this.rthMode = rthMode;
    }

    /** 最低电量要求，单位：% */
    public void setMinBatteryCapacity(int minBatteryCapacity) {
        this.minBatteryCapacity = minBatteryCapacity;
    }

    /** 航线精度类型。枚举：0=GPS, 1=RTK */
    public void setWaylinePrecisionType(int waylinePrecisionType) {
        this.waylinePrecisionType = waylinePrecisionType;
    }

    /** 航线分段索引列表 */
    public void setWaylineSegmentIndexes(List<Integer> waylineSegmentIndexes) {
        this.waylineSegmentIndexes = waylineSegmentIndexes;
    }

    /** 当前航点索引 */
    public void setCurrentWaypointIndex(int currentWaypointIndex) {
        this.currentWaypointIndex = currentWaypointIndex;
    }

    /** 总航点数 */
    public void setTotalWaypoints(int totalWaypoints) {
        this.totalWaypoints = totalWaypoints;
    }

    /** 时区 */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /** 设备类型。枚举：0=机场, 1=遥控器 */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    /** @param bid 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 断点续飞信息 */
    public void setBreakPoint(WaylineBreakPoint breakPoint) {
        this.breakPoint = breakPoint;
    }

    /** 完成时间（ISO 8601） */
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    /** 创建时间（ISO 8601） */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** 删除时间（ISO 8601） */
    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    /** 是否支持断点续飞 */
    public void setBreakPointResume(boolean breakPointResume) {
        this.isBreakPointResume = breakPointResume;
    }

    /** 媒体上传状态 */
    public void setMediaUploadStatus(int mediaUploadStatus) {
        this.mediaUploadStatus = mediaUploadStatus;
    }

    /** 避障信息列表 */
    public void setObstacles(List<WaylineObstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** 实际执行时间（ISO 8601） */
    public void setRunAt(String runAt) {
        this.runAt = runAt;
    }

    /** 任务标签列表 */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /** 更新时间（ISO 8601） */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** 航线预估时长，单位：秒 */
    public void setWaylineDuration(double waylineDuration) {
        this.waylineDuration = waylineDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskDetail that = (TaskDetail) o;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && taskType == that.taskType
                && status == that.status
                && Objects.equals(sn, that.sn)
                && Objects.equals(landingDockSn, that.landingDockSn)
                && Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(beginAt, that.beginAt)
                && Objects.equals(endAt, that.endAt)
                && outOfControlActionInFlight == that.outOfControlActionInFlight
                && Double.compare(that.rthAltitude, rthAltitude) == 0
                && rthMode == that.rthMode
                && minBatteryCapacity == that.minBatteryCapacity
                && waylinePrecisionType == that.waylinePrecisionType
                && Objects.equals(waylineSegmentIndexes, that.waylineSegmentIndexes)
                && currentWaypointIndex == that.currentWaypointIndex
                && totalWaypoints == that.totalWaypoints
                && Objects.equals(timeZone, that.timeZone)
                && deviceType == that.deviceType
                && Objects.equals(bid, that.bid)
                && Objects.equals(breakPoint, that.breakPoint)
                && Objects.equals(completedAt, that.completedAt)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(deletedAt, that.deletedAt)
                && isBreakPointResume == that.isBreakPointResume
                && mediaUploadStatus == that.mediaUploadStatus
                && Objects.equals(obstacles, that.obstacles)
                && Objects.equals(runAt, that.runAt)
                && Objects.equals(tags, that.tags)
                && Objects.equals(updatedAt, that.updatedAt)
                && Double.compare(that.waylineDuration, waylineDuration) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid,
                name,
                taskType,
                status,
                sn,
                landingDockSn,
                waylineUuid,
                beginAt,
                endAt,
                outOfControlActionInFlight,
                rthAltitude,
                rthMode,
                minBatteryCapacity,
                waylinePrecisionType,
                waylineSegmentIndexes,
                currentWaypointIndex,
                totalWaypoints,
                timeZone,
                deviceType,
                bid,
                breakPoint,
                completedAt,
                createdAt,
                deletedAt,
                isBreakPointResume,
                mediaUploadStatus,
                obstacles,
                runAt,
                tags,
                updatedAt,
                waylineDuration);
    }

    @Override
    public String toString() {
        return "TaskDetail{"
                + "uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", taskType=" + taskType
                + ", status=" + status
                + ", sn='" + sn + '\''
                + ", landingDockSn='" + landingDockSn + '\''
                + ", waylineUuid='" + waylineUuid + '\''
                + ", beginAt='" + beginAt + '\''
                + ", endAt='" + endAt + '\''
                + ", outOfControlActionInFlight=" + outOfControlActionInFlight
                + ", rthAltitude=" + rthAltitude
                + ", rthMode=" + rthMode
                + ", minBatteryCapacity=" + minBatteryCapacity
                + ", waylinePrecisionType=" + waylinePrecisionType
                + ", waylineSegmentIndexes=" + waylineSegmentIndexes
                + ", currentWaypointIndex=" + currentWaypointIndex
                + ", totalWaypoints=" + totalWaypoints
                + ", timeZone='" + timeZone + '\''
                + ", deviceType=" + deviceType
                + ", bid='" + bid + '\''
                + ", breakPoint=" + breakPoint
                + ", completedAt='" + completedAt + '\''
                + ", createdAt='" + createdAt + '\''
                + ", deletedAt='" + deletedAt + '\''
                + ", isBreakPointResume=" + isBreakPointResume
                + ", mediaUploadStatus=" + mediaUploadStatus
                + ", obstacles=" + obstacles
                + ", runAt='" + runAt + '\''
                + ", tags=" + tags
                + ", updatedAt='" + updatedAt + '\''
                + ", waylineDuration=" + waylineDuration
                + '}';
    }
}
