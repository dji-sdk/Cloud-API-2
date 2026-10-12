// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 创建任务请求 */
public class CreateTaskReq {
    /** 任务名称（必填） */
    private String name = "";
    /** 起飞机场序列号（必填） */
    private String sn = "";
    /** 任务类型：1=立即任务 2=条件任务（须设置 begin_at/end_at 执行窗口）（必填） */
    private int taskType = 1;
    /** 时区（必填），如 Asia/Shanghai, UTC */
    private String timeZone = "";
    /** 航线 UUID（必填） */
    private String waylineUuid = "";
    /** 飞行中失控动作（必填）。枚举：1=返航, 2=继续执行 */
    private int outOfControlActionInFlight = 1;
    /** 返航高度，单位：米（必填） */
    private double rthAltitude;
    /** 返航高度模式。枚举：0=智能高度, 1=设定高度。-1 表示未设置（可选，不传则使用后端默认值） */
    private int rthMode = -1;
    /** 条件任务计划开始时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填。-1 未设置。创建条件任务时 begin_at 应不早于当前时间 2 分钟（最少提前 2 分钟预约）。服务端按分钟粒度每分钟扫描；超过 begin_at 后 timed_task_delay_threshold（默认 50 秒）仍未完成下发时任务为启动失败 */
    private Long beginAt;
    /** 条件任务执行窗口结束时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填，须 end_at > begin_at。-1 未设置 */
    private Long endAt;
    /** 设备类型。枚举：0=机场, 1=遥控器。-1 表示未设置（可选，不传则使用后端默认值） */
    private int deviceType = -1;
    /** 降落机场 SN。为空时代表和起飞机场一致 */
    private String landingDockSn = "";
    /** 条件任务最低电量百分比。范围：0-100。-1 表示未设置（可选，不传则使用后端默认值） */
    private int minBatteryCapacity = -1;
    /** 航线精度类型。枚举：0=GPS, 1=RTK。-1 表示未设置（可选，不传则使用后端默认值） */
    private int waylinePrecisionType = -1;
    /** 航线分段索引列表 */
    private List<Integer> waylineSegmentIndexes = new ArrayList<>();
    /** 航线预估时长，单位：秒。-1 表示未设置 */
    private double waylineDuration = -1;
    /** 任务 UUID（可选，用于指定 UUID） */
    private String uuid = "";
    /** 任务标签列表。最多 10 个，每个长度 1-45 字符 */
    private List<String> tags = new ArrayList<>();
    /** 是否为断点续飞任务。true 时须同时填写 break_point */
    private boolean isBreakPointResume;
    /** 断点信息（与 is_break_point_resume 一并写入 HTTP） */
    private WaylineBreakPoint breakPoint;
    /** 信号类型：0=未开启信号增强，1=开启信号增强（漫游）。仅当为 1 时写入 HTTP body */
    private int signalType;
    /** 续飞策略（可选）。枚举：auto=自动续飞, manual=手动续飞。为空时不写入 HTTP body */
    private String resumableStatus = "";

    public CreateTaskReq() {
    }


    /** 任务名称（必填） */
    public String getName() { return name; }
    /** @param name 任务名称（必填） */
    public void setName(String name) { this.name = name != null ? name : ""; }
    /** 起飞机场序列号（必填） */
    public String getSn() { return sn; }
    /** @param sn 起飞机场序列号（必填） */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }
    /** 任务类型：1=立即任务 2=条件任务（须设置 begin_at/end_at 执行窗口）（必填） */
    public int getTaskType() { return taskType; }
    /** @param taskType 任务类型：1=立即任务 2=条件任务（须设置 begin_at/end_at 执行窗口）（必填） */
    public void setTaskType(int taskType) { this.taskType = taskType; }
    /** 时区（必填），如 Asia/Shanghai, UTC */
    public String getTimeZone() { return timeZone; }
    /** @param timeZone 时区（必填），如 Asia/Shanghai, UTC */
    public void setTimeZone(String timeZone) { this.timeZone = timeZone != null ? timeZone : ""; }
    /** 航线 UUID（必填） */
    public String getWaylineUuid() { return waylineUuid; }
    /** @param waylineUuid 航线 UUID（必填） */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid != null ? waylineUuid : ""; }
    /** 飞行中失控动作（必填）。枚举：1=返航, 2=继续执行 */
    public int getOutOfControlActionInFlight() { return outOfControlActionInFlight; }
    /** @param outOfControlActionInFlight 飞行中失控动作（必填）。枚举：1=返航, 2=继续执行 */
    public void setOutOfControlActionInFlight(int outOfControlActionInFlight) { this.outOfControlActionInFlight = outOfControlActionInFlight; }
    /** 返航高度，单位：米（必填） */
    public double getRthAltitude() { return rthAltitude; }
    /** @param rthAltitude 返航高度，单位：米（必填） */
    public void setRthAltitude(double rthAltitude) { this.rthAltitude = rthAltitude; }
    /** 返航高度模式。枚举：0=智能高度, 1=设定高度。-1 表示未设置（可选，不传则使用后端默认值） */
    public int getRthMode() { return rthMode; }
    /** @param rthMode 返航高度模式。枚举：0=智能高度, 1=设定高度。-1 表示未设置（可选，不传则使用后端默认值） */
    public void setRthMode(int rthMode) { this.rthMode = rthMode; }
    /** 条件任务计划开始时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填。-1 未设置。创建条件任务时 begin_at 应不早于当前时间 2 分钟（最少提前 2 分钟预约）。服务端按分钟粒度每分钟扫描；超过 begin_at 后 timed_task_delay_threshold（默认 50 秒）仍未完成下发时任务为启动失败 */
    public Long getBeginAt() { return beginAt; }
    /** @param beginAt 条件任务计划开始时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填。-1 未设置。创建条件任务时 begin_at 应不早于当前时间 2 分钟（最少提前 2 分钟预约）。服务端按分钟粒度每分钟扫描；超过 begin_at 后 timed_task_delay_threshold（默认 50 秒）仍未完成下发时任务为启动失败 */
    public void setBeginAt(Long beginAt) { this.beginAt = beginAt; }
    /** 条件任务执行窗口结束时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填，须 end_at > begin_at。-1 未设置 */
    public Long getEndAt() { return endAt; }
    /** @param endAt 条件任务执行窗口结束时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填，须 end_at > begin_at。-1 未设置 */
    public void setEndAt(Long endAt) { this.endAt = endAt; }
    /** 设备类型。枚举：0=机场, 1=遥控器。-1 表示未设置（可选，不传则使用后端默认值） */
    public int getDeviceType() { return deviceType; }
    /** @param deviceType 设备类型。枚举：0=机场, 1=遥控器。-1 表示未设置（可选，不传则使用后端默认值） */
    public void setDeviceType(int deviceType) { this.deviceType = deviceType; }
    /** 降落机场 SN。为空时代表和起飞机场一致 */
    public String getLandingDockSn() { return landingDockSn; }
    /** @param landingDockSn 降落机场 SN。为空时代表和起飞机场一致 */
    public void setLandingDockSn(String landingDockSn) { this.landingDockSn = landingDockSn != null ? landingDockSn : ""; }
    /** 条件任务最低电量百分比。范围：0-100。-1 表示未设置（可选，不传则使用后端默认值） */
    public int getMinBatteryCapacity() { return minBatteryCapacity; }
    /** @param minBatteryCapacity 条件任务最低电量百分比。范围：0-100。-1 表示未设置（可选，不传则使用后端默认值） */
    public void setMinBatteryCapacity(int minBatteryCapacity) { this.minBatteryCapacity = minBatteryCapacity; }
    /** 航线精度类型。枚举：0=GPS, 1=RTK。-1 表示未设置（可选，不传则使用后端默认值） */
    public int getWaylinePrecisionType() { return waylinePrecisionType; }
    /** @param waylinePrecisionType 航线精度类型。枚举：0=GPS, 1=RTK。-1 表示未设置（可选，不传则使用后端默认值） */
    public void setWaylinePrecisionType(int waylinePrecisionType) { this.waylinePrecisionType = waylinePrecisionType; }
    /** 航线分段索引列表 */
    public List<Integer> getWaylineSegmentIndexes() { return waylineSegmentIndexes; }
    /** @param waylineSegmentIndexes 航线分段索引列表 */
    public void setWaylineSegmentIndexes(List<Integer> waylineSegmentIndexes) { this.waylineSegmentIndexes = waylineSegmentIndexes != null ? waylineSegmentIndexes : new ArrayList<>(); }
    /** 航线预估时长，单位：秒。-1 表示未设置 */
    public double getWaylineDuration() { return waylineDuration; }
    /** @param waylineDuration 航线预估时长，单位：秒。-1 表示未设置 */
    public void setWaylineDuration(double waylineDuration) { this.waylineDuration = waylineDuration; }
    /** 任务 UUID（可选，用于指定 UUID） */
    public String getUuid() { return uuid; }
    /** @param uuid 任务 UUID（可选，用于指定 UUID） */
    public void setUuid(String uuid) { this.uuid = uuid != null ? uuid : ""; }
    /** 任务标签列表。最多 10 个，每个长度 1-45 字符 */
    public List<String> getTags() { return tags; }
    /** @param tags 任务标签列表。最多 10 个，每个长度 1-45 字符 */
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
    /** 是否为断点续飞任务；为 true 时须同时设置 breakPoint */
    public boolean isBreakPointResume() { return isBreakPointResume; }
    /** @param breakPointResume 是否为断点续飞任务；为 true 时须同时设置 breakPoint */
    public void setBreakPointResume(boolean breakPointResume) { isBreakPointResume = breakPointResume; }
    /** 断点信息（与 is_break_point_resume 一并写入 HTTP） */
    public WaylineBreakPoint getBreakPoint() { return breakPoint; }
    /** @param breakPoint 断点信息（与 is_break_point_resume 一并写入 HTTP） */
    public void setBreakPoint(WaylineBreakPoint breakPoint) { this.breakPoint = breakPoint; }
    /** 信号类型：0=未开启信号增强，1=开启信号增强（漫游）。仅当为 1 时写入 HTTP body */
    public int getSignalType() { return signalType; }
    /** @param signalType 信号类型：0=未开启信号增强，1=开启信号增强（漫游）。仅当为 1 时写入 HTTP body */
    public void setSignalType(int signalType) { this.signalType = signalType; }
    /** 续飞策略（可选）。枚举：auto=自动续飞, manual=手动续飞。为空时不写入 HTTP body */
    public String getResumableStatus() { return resumableStatus; }
    /** @param resumableStatus 续飞策略（可选）。枚举：auto=自动续飞, manual=手动续飞。为空时不写入 HTTP body */
    public void setResumableStatus(String resumableStatus) { this.resumableStatus = resumableStatus != null ? resumableStatus : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateTaskReq that = (CreateTaskReq) o;
        return Objects.equals(name, that.name)
                && Objects.equals(sn, that.sn)
                && taskType == that.taskType
                && Objects.equals(timeZone, that.timeZone)
                && Objects.equals(waylineUuid, that.waylineUuid)
                && outOfControlActionInFlight == that.outOfControlActionInFlight
                && Double.compare(that.rthAltitude, rthAltitude) == 0
                && rthMode == that.rthMode
                && Objects.equals(beginAt, that.beginAt)
                && Objects.equals(endAt, that.endAt)
                && deviceType == that.deviceType
                && Objects.equals(landingDockSn, that.landingDockSn)
                && minBatteryCapacity == that.minBatteryCapacity
                && waylinePrecisionType == that.waylinePrecisionType
                && Objects.equals(waylineSegmentIndexes, that.waylineSegmentIndexes)
                && Double.compare(that.waylineDuration, waylineDuration) == 0
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(tags, that.tags)
                && isBreakPointResume == that.isBreakPointResume
                && Objects.equals(breakPoint, that.breakPoint)
                && signalType == that.signalType
                && Objects.equals(resumableStatus, that.resumableStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                sn,
                taskType,
                timeZone,
                waylineUuid,
                outOfControlActionInFlight,
                rthAltitude,
                rthMode,
                beginAt,
                endAt,
                deviceType,
                landingDockSn,
                minBatteryCapacity,
                waylinePrecisionType,
                waylineSegmentIndexes,
                waylineDuration,
                uuid,
                tags,
                isBreakPointResume,
                breakPoint,
                signalType,
                resumableStatus);
    }

    @Override
    public String toString() {
        return "CreateTaskReq{"
                + "name='" + name + '\''
                + ", sn='" + sn + '\''
                + ", taskType=" + taskType
                + ", timeZone='" + timeZone + '\''
                + ", waylineUuid='" + waylineUuid + '\''
                + ", outOfControlActionInFlight=" + outOfControlActionInFlight
                + ", rthAltitude=" + rthAltitude
                + ", rthMode=" + rthMode
                + ", beginAt=" + beginAt
                + ", endAt=" + endAt
                + ", deviceType=" + deviceType
                + ", landingDockSn='" + landingDockSn + '\''
                + ", minBatteryCapacity=" + minBatteryCapacity
                + ", waylinePrecisionType=" + waylinePrecisionType
                + ", waylineSegmentIndexes=" + waylineSegmentIndexes
                + ", waylineDuration=" + waylineDuration
                + ", uuid='" + uuid + '\''
                + ", tags=" + tags
                + ", isBreakPointResume=" + isBreakPointResume
                + ", breakPoint=" + breakPoint
                + ", signalType=" + signalType
                + ", resumableStatus='" + resumableStatus + '\''
                + '}';
    }
}
