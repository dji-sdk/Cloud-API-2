// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 航线任务数据。 */
public final class WaylineTaskData {
    /** 任务名称 */
    private String name;
    /** 任务 UUID */
    private String uuid;
    /** 任务类型：1-立即任务，2-条件任务 */
    private int taskType;
    /** 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中 */
    private int status;
    /** 执行任务的设备序列号 */
    private String sn;
    /** 降落机场序列号（为空时与起飞机场一致） */
    private String landingDockSn;
    /** 航线 UUID */
    private String waylineUuid;
    /** 航线精度类型：0-GPS，1-RTK */
    private int waylinePrecisionType;
    /** 任务预估时长（秒） */
    private double waylineDuration;
    /** 执行航段索引数组 */
    private List<Integer> waylineSegmentIndexes;
    /** 飞行任务总航点数 */
    private int totalWaypoints;
    /** 已飞完的航点索引 */
    private int currentWaypointIndex;
    /** DTCP 返回的 bid */
    private String bid;
    /** 设定的开始时间（ISO 8601 格式） */
    private String beginAt;
    /** 设定的结束时间（ISO 8601 格式） */
    private String endAt;
    /** 任务实际执行时间（ISO 8601 格式） */
    private String runAt;
    /** 任务实际结束或完成时间（ISO 8601 格式） */
    private String completedAt;
    /** 失控动作：1-返航，2-继续执行 */
    private int outOfControlActionInFlight;
    /** 返航高度（米） */
    private int rthAltitude;
    /** 返航高度模式：0-智能高度，1-设定高度 */
    private int rthMode;
    /** 任务执行最低电量（50-100） */
    private int minBatteryCapacity;
    /** 云云对接标签 */
    private List<String> tags;
    /** 云云对接存储桶 ID（为空表示非云云对接任务） */
    private String cloudToCloudId;
    /** 断点信息 */
    private WaylineBreakPoint breakPoint;
    /** 是否为断点续飞任务 */
    private boolean isBreakPointResume;
    /** 媒体上传状态：0-待上传，1-上传中，2-上传结束 */
    private int mediaUploadStatus;
    /** 是否有快速建模任务 */
    private boolean fastReconstruction;
    /** 设备类型：0-机场，1-遥控器 */
    private int deviceType;
    /** 避障信息列表 */
    private List<WaylineObstacle> obstacles;
    /** 创建时间（ISO 8601 格式） */
    private String createdAt;
    /** 更新时间（ISO 8601 格式） */
    private String updatedAt;
    /** 删除时间（ISO 8601 格式） */
    private String deletedAt;
    public WaylineTaskData() {
    }

/**
     * 构造 WaylineTaskData。
     *
     * @param name 任务名称过滤（模糊搜索）
     * @param uuid 任务 UUID（可选，用于指定 UUID）
     * @param taskType 任务类型过滤。枚举：0=不筛选, 1=立即任务, 2=条件任务
     * @param status 任务状态过滤列表。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中
     * @param sn 起飞机场序列号
     * @param landingDockSn 降落机场序列号。为空时代表和起飞机场一致
     * @param waylineUuid 航线 UUID
     * @param waylinePrecisionType 航线精度类型。枚举：0=GPS, 1=RTK
     * @param waylineDuration 航线预估时长，单位：秒
     * @param waylineSegmentIndexes 航线分段索引列表
     * @param totalWaypoints 总航点数
     * @param currentWaypointIndex 已飞完的航点数
     * @param bid 业务标识(bid)
     * @param beginAt 任务开始时间（秒级时间戳）。条件任务（taskType=2）必填
     * @param endAt 任务结束时间（秒级时间戳）。条件任务（taskType=2）必填
     * @param runAt 实际执行时间（ISO 8601）
     * @param completedAt 完成时间（ISO 8601）
     * @param outOfControlActionInFlight 飞行中失控动作。枚举：1=返航, 2=继续执行
     * @param rthAltitude 返航高度（米）。范围：15-1500
     * @param rthMode 返航高度模式。枚举：0=智能高度, 1=设定高度
     * @param minBatteryCapacity 条件任务最低电量百分比。范围：0-100，0 表示不限制
     * @param tags 任务标签列表。最多 10 个，每个长度 1-45 字符
     * @param cloudToCloudId 云云对接存储桶 ID
     * @param breakPoint 断点续飞信息
     * @param isBreakPointResume Is断点Resume
     * @param mediaUploadStatus 媒体上传状态
     * @param fastReconstruction 是否快速重建
     * @param deviceType 设备类型。枚举：0=机场, 1=遥控器
     * @param obstacles 避障信息列表
     * @param createdAt 创建时间（ISO 8601）
     * @param updatedAt 更新时间（ISO 8601）
     * @param deletedAt 删除时间（ISO 8601）
     */
    public WaylineTaskData(String name, String uuid, int taskType, int status,
            String sn, String landingDockSn, String waylineUuid, int waylinePrecisionType,
            double waylineDuration, List<Integer> waylineSegmentIndexes,
            int totalWaypoints, int currentWaypointIndex, String bid,
            String beginAt, String endAt, String runAt, String completedAt,
            int outOfControlActionInFlight, int rthAltitude, int rthMode,
            int minBatteryCapacity, List<String> tags, String cloudToCloudId,
            WaylineBreakPoint breakPoint, boolean isBreakPointResume,
            int mediaUploadStatus, boolean fastReconstruction, int deviceType,
            List<WaylineObstacle> obstacles,
            String createdAt, String updatedAt, String deletedAt) {
        this.name = name != null ? name : "";
        this.uuid = uuid != null ? uuid : "";
        this.taskType = taskType;
        this.status = status;
        this.sn = sn != null ? sn : "";
        this.landingDockSn = landingDockSn != null ? landingDockSn : "";
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.waylinePrecisionType = waylinePrecisionType;
        this.waylineDuration = waylineDuration;
        this.waylineSegmentIndexes = waylineSegmentIndexes != null ? waylineSegmentIndexes : Collections.emptyList();
        this.totalWaypoints = totalWaypoints;
        this.currentWaypointIndex = currentWaypointIndex;
        this.bid = bid != null ? bid : "";
        this.beginAt = beginAt != null ? beginAt : "";
        this.endAt = endAt != null ? endAt : "";
        this.runAt = runAt != null ? runAt : "";
        this.completedAt = completedAt != null ? completedAt : "";
        this.outOfControlActionInFlight = outOfControlActionInFlight;
        this.rthAltitude = rthAltitude;
        this.rthMode = rthMode;
        this.minBatteryCapacity = minBatteryCapacity;
        this.tags = tags != null ? tags : Collections.emptyList();
        this.cloudToCloudId = cloudToCloudId != null ? cloudToCloudId : "";
        this.breakPoint = breakPoint;
        this.isBreakPointResume = isBreakPointResume;
        this.mediaUploadStatus = mediaUploadStatus;
        this.fastReconstruction = fastReconstruction;
        this.deviceType = deviceType;
        this.obstacles = obstacles != null ? obstacles : Collections.emptyList();
        this.createdAt = createdAt != null ? createdAt : "";
        this.updatedAt = updatedAt != null ? updatedAt : "";
        this.deletedAt = deletedAt != null ? deletedAt : "";
    }

    /** 任务名称 */
    public String getName() { return name; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 任务类型：1-立即任务，2-条件任务 */
    public int getTaskType() { return taskType; }
    /** 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中 */
    public int getStatus() { return status; }
    /** 执行任务的设备序列号 */
    public String getSn() { return sn; }
    /** 降落机场序列号（为空时与起飞机场一致） */
    public String getLandingDockSn() { return landingDockSn; }
    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }
    /** 航线精度类型：0-GPS，1-RTK */
    public int getWaylinePrecisionType() { return waylinePrecisionType; }
    /** 任务预估时长（秒） */
    public double getWaylineDuration() { return waylineDuration; }
    /** 执行航段索引数组 */
    public List<Integer> getWaylineSegmentIndexes() { return waylineSegmentIndexes; }
    /** 飞行任务总航点数 */
    public int getTotalWaypoints() { return totalWaypoints; }
    /** 已飞完的航点索引 */
    public int getCurrentWaypointIndex() { return currentWaypointIndex; }
    /** DTCP 返回的 bid */
    public String getBid() { return bid; }
    /** 设定的开始时间（ISO 8601 格式） */
    public String getBeginAt() { return beginAt; }
    /** 设定的结束时间（ISO 8601 格式） */
    public String getEndAt() { return endAt; }
    /** 任务实际执行时间（ISO 8601 格式） */
    public String getRunAt() { return runAt; }
    /** 任务实际结束或完成时间（ISO 8601 格式） */
    public String getCompletedAt() { return completedAt; }
    /** 失控动作：1-返航，2-继续执行 */
    public int getOutOfControlActionInFlight() { return outOfControlActionInFlight; }
    /** 返航高度（米） */
    public int getRthAltitude() { return rthAltitude; }
    /** 返航高度模式：0-智能高度，1-设定高度 */
    public int getRthMode() { return rthMode; }
    /** 任务执行最低电量（50-100） */
    public int getMinBatteryCapacity() { return minBatteryCapacity; }
    /** 云云对接标签 */
    public List<String> getTags() { return tags; }
    /** 云云对接存储桶 ID（为空表示非云云对接任务） */
    public String getCloudToCloudId() { return cloudToCloudId; }
    /** 断点信息 */
    public WaylineBreakPoint getBreakPoint() { return breakPoint; }
    /** 是否支持断点续飞 */
    public boolean isBreakPointResume() { return isBreakPointResume; }
    /** 媒体上传状态：0-待上传，1-上传中，2-上传结束 */
    public int getMediaUploadStatus() { return mediaUploadStatus; }
    /** 是否有快速建模任务 */
    public boolean isFastReconstruction() { return fastReconstruction; }
    /** 设备类型：0-机场，1-遥控器 */
    public int getDeviceType() { return deviceType; }
    /** 避障信息列表 */
    public List<WaylineObstacle> getObstacles() { return obstacles; }
    /** 创建时间（ISO 8601 格式） */
    public String getCreatedAt() { return createdAt; }
    /** 更新时间（ISO 8601 格式） */
    public String getUpdatedAt() { return updatedAt; }
    /** 删除时间（ISO 8601 格式） */
    public String getDeletedAt() { return deletedAt; }

    /** @param name 任务名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** @param taskType 任务类型：1-立即任务，2-条件任务 */
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    /** @param status 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** @param sn 执行任务的设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** @param landingDockSn 降落机场序列号（为空时与起飞机场一致） */
    public void setLandingDockSn(String landingDockSn) {
        this.landingDockSn = landingDockSn;
    }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** @param waylinePrecisionType 航线精度类型：0-GPS，1-RTK */
    public void setWaylinePrecisionType(int waylinePrecisionType) {
        this.waylinePrecisionType = waylinePrecisionType;
    }

    /** @param waylineDuration 任务预估时长（秒） */
    public void setWaylineDuration(double waylineDuration) {
        this.waylineDuration = waylineDuration;
    }

    /** @param waylineSegmentIndexes 执行航段索引数组 */
    public void setWaylineSegmentIndexes(List<Integer> waylineSegmentIndexes) {
        this.waylineSegmentIndexes = waylineSegmentIndexes;
    }

    /** @param totalWaypoints 飞行任务总航点数 */
    public void setTotalWaypoints(int totalWaypoints) {
        this.totalWaypoints = totalWaypoints;
    }

    /** @param currentWaypointIndex 已飞完的航点索引 */
    public void setCurrentWaypointIndex(int currentWaypointIndex) {
        this.currentWaypointIndex = currentWaypointIndex;
    }

    /** @param bid DTCP 返回的 bid */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** @param beginAt 设定的开始时间（ISO 8601 格式） */
    public void setBeginAt(String beginAt) {
        this.beginAt = beginAt;
    }

    /** @param endAt 设定的结束时间（ISO 8601 格式） */
    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    /** @param runAt 任务实际执行时间（ISO 8601 格式） */
    public void setRunAt(String runAt) {
        this.runAt = runAt;
    }

    /** @param completedAt 任务实际结束或完成时间（ISO 8601 格式） */
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    /** @param outOfControlActionInFlight 失控动作：1-返航，2-继续执行 */
    public void setOutOfControlActionInFlight(int outOfControlActionInFlight) {
        this.outOfControlActionInFlight = outOfControlActionInFlight;
    }

    /** @param rthAltitude 返航高度（米） */
    public void setRthAltitude(int rthAltitude) {
        this.rthAltitude = rthAltitude;
    }

    /** @param rthMode 返航高度模式：0-智能高度，1-设定高度 */
    public void setRthMode(int rthMode) {
        this.rthMode = rthMode;
    }

    /** @param minBatteryCapacity 任务执行最低电量（50-100） */
    public void setMinBatteryCapacity(int minBatteryCapacity) {
        this.minBatteryCapacity = minBatteryCapacity;
    }

    /** @param tags 云云对接标签 */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /** @param cloudToCloudId 云云对接存储桶 ID（为空表示非云云对接任务） */
    public void setCloudToCloudId(String cloudToCloudId) {
        this.cloudToCloudId = cloudToCloudId;
    }

    /** @param breakPoint 断点信息 */
    public void setBreakPoint(WaylineBreakPoint breakPoint) {
        this.breakPoint = breakPoint;
    }

    /** 是否支持断点续飞 */
    public void setBreakPointResume(boolean breakPointResume) {
        this.isBreakPointResume = breakPointResume;
    }

    /** @param mediaUploadStatus 媒体上传状态：0-待上传，1-上传中，2-上传结束 */
    public void setMediaUploadStatus(int mediaUploadStatus) {
        this.mediaUploadStatus = mediaUploadStatus;
    }

    /** @param fastReconstruction 是否有快速建模任务 */
    public void setFastReconstruction(boolean fastReconstruction) {
        this.fastReconstruction = fastReconstruction;
    }

    /** @param deviceType 设备类型：0-机场，1-遥控器 */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    /** 避障信息列表 */
    public void setObstacles(List<WaylineObstacle> obstacles) {
        this.obstacles = obstacles;
    }

    /** @param createdAt 创建时间（ISO 8601 格式） */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** @param updatedAt 更新时间（ISO 8601 格式） */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** @param deletedAt 删除时间（ISO 8601 格式） */
    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineTaskData that = (WaylineTaskData) o;
        return Objects.equals(name, that.name)
                && Objects.equals(uuid, that.uuid)
                && taskType == that.taskType
                && status == that.status
                && Objects.equals(sn, that.sn)
                && Objects.equals(landingDockSn, that.landingDockSn)
                && Objects.equals(waylineUuid, that.waylineUuid)
                && waylinePrecisionType == that.waylinePrecisionType
                && Double.compare(that.waylineDuration, waylineDuration) == 0
                && Objects.equals(waylineSegmentIndexes, that.waylineSegmentIndexes)
                && totalWaypoints == that.totalWaypoints
                && currentWaypointIndex == that.currentWaypointIndex
                && Objects.equals(bid, that.bid)
                && Objects.equals(beginAt, that.beginAt)
                && Objects.equals(endAt, that.endAt)
                && Objects.equals(runAt, that.runAt)
                && Objects.equals(completedAt, that.completedAt)
                && outOfControlActionInFlight == that.outOfControlActionInFlight
                && rthAltitude == that.rthAltitude
                && rthMode == that.rthMode
                && minBatteryCapacity == that.minBatteryCapacity
                && Objects.equals(tags, that.tags)
                && Objects.equals(cloudToCloudId, that.cloudToCloudId)
                && Objects.equals(breakPoint, that.breakPoint)
                && isBreakPointResume == that.isBreakPointResume
                && mediaUploadStatus == that.mediaUploadStatus
                && fastReconstruction == that.fastReconstruction
                && deviceType == that.deviceType
                && Objects.equals(obstacles, that.obstacles)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt)
                && Objects.equals(deletedAt, that.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                uuid,
                taskType,
                status,
                sn,
                landingDockSn,
                waylineUuid,
                waylinePrecisionType,
                waylineDuration,
                waylineSegmentIndexes,
                totalWaypoints,
                currentWaypointIndex,
                bid,
                beginAt,
                endAt,
                runAt,
                completedAt,
                outOfControlActionInFlight,
                rthAltitude,
                rthMode,
                minBatteryCapacity,
                tags,
                cloudToCloudId,
                breakPoint,
                isBreakPointResume,
                mediaUploadStatus,
                fastReconstruction,
                deviceType,
                obstacles,
                createdAt,
                updatedAt,
                deletedAt);
    }

    @Override
    public String toString() {
        return "WaylineTaskData{"
                + "name='" + name + '\''
                + ", uuid='" + uuid + '\''
                + ", taskType=" + taskType
                + ", status=" + status
                + ", sn='" + sn + '\''
                + ", landingDockSn='" + landingDockSn + '\''
                + ", waylineUuid='" + waylineUuid + '\''
                + ", waylinePrecisionType=" + waylinePrecisionType
                + ", waylineDuration=" + waylineDuration
                + ", waylineSegmentIndexes=" + waylineSegmentIndexes
                + ", totalWaypoints=" + totalWaypoints
                + ", currentWaypointIndex=" + currentWaypointIndex
                + ", bid='" + bid + '\''
                + ", beginAt='" + beginAt + '\''
                + ", endAt='" + endAt + '\''
                + ", runAt='" + runAt + '\''
                + ", completedAt='" + completedAt + '\''
                + ", outOfControlActionInFlight=" + outOfControlActionInFlight
                + ", rthAltitude=" + rthAltitude
                + ", rthMode=" + rthMode
                + ", minBatteryCapacity=" + minBatteryCapacity
                + ", tags=" + tags
                + ", cloudToCloudId='" + cloudToCloudId + '\''
                + ", breakPoint=" + breakPoint
                + ", isBreakPointResume=" + isBreakPointResume
                + ", mediaUploadStatus=" + mediaUploadStatus
                + ", fastReconstruction=" + fastReconstruction
                + ", deviceType=" + deviceType
                + ", obstacles=" + obstacles
                + ", createdAt='" + createdAt + '\''
                + ", updatedAt='" + updatedAt + '\''
                + ", deletedAt='" + deletedAt + '\''
                + '}';
    }
}
