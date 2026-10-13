// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
import com.dji.flighthubsdk.value.OutOfControlAction;
/** 指令任务数据。 */
public final class CommandTaskData {
    /** 数据库自增 ID */
    private int id;
    /** 创建时间（ISO 8601 格式） */
    private String createdAt;
    /** 任务 UUID */
    private String uuid;
    /** 项目 UUID */
    private String prjUuid;
    /** 机场序列号 */
    private String sn;
    /** 无人机序列号 */
    private String droneSn;
    /** 轨迹 ID */
    private String trackId;
    /** 用户 ID */
    private String userId;
    /** 任务状态：0-待执行，1-执行中，2-已完成，3-失败，4-已取消 */
    private int status;
    /** 错误码 */
    private int errCode;
    /** 起飞模式："Takeoff"-直接起飞，"TakeoffWithFlyTo"-起飞后飞向目标点 */
    private String takeoffMode;
    /** 安全起飞高度（米） */
    private double securityTakeoffHeight;
    /** 最大速度（米/秒） */
    private int maxSpeed;
    /** 失控动作：1-返航，2-继续执行，3-悬停 */
    private OutOfControlAction outOfControlAction = OutOfControlAction.UNKNOWN;
    /** 返航高度（米） */
    private int rthAltitude;
    /** 指点飞行模式 */
    private int commanderFlightMode;
    /** 指点飞行高度（米） */
    private double commanderFlightHeight;
    /** 返航模式 */
    private int rthMode;
    /** 云云对接 ID */
    private String cloudToCloudId;
    public CommandTaskData() {
    }

/**
     * 构造 CommandTaskData。
     *
     * @param id ID
     * @param createdAt 创建时间（ISO 8601）
     * @param uuid 任务 UUID
     * @param prjUuid 项目 UUID
     * @param sn 起飞机场序列号
     * @param droneSn 飞行器 SN
     * @param trackId 航迹 ID
     * @param userId 操作用户 ID
     * @param status 任务状态。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=已取消
     * @param errCode 错误码
     * @param takeoffMode 起飞模式。枚举：TAKEOFF=起飞, TAKEOFF_WITH_FLY_TO=起飞并指点飞行
     * @param securityTakeoffHeight 安全起飞高度（米）。范围：8-1500
     * @param maxSpeed 最大飞行速度，单位：m/s
     * @param outOfControlAction 失控动作。枚举：HOVER=悬停, RETURN_HOME=返航, CONTINUE=继续执行
     * @param rthAltitude 返航高度（米）。范围：15-1500
     * @param commanderFlightMode 指点飞行模式。枚举：0=平稳模式, 1=运动模式
     * @param commanderFlightHeight 指点飞行高度（米）。范围：20-1500
     * @param rthMode 返航高度模式。枚举：0=智能高度, 1=设定高度
     * @param cloudToCloudId 云云对接存储桶 ID
     */
    public CommandTaskData(int id, String createdAt, String uuid, String prjUuid,
            String sn, String droneSn, String trackId, String userId,
            int status, int errCode, String takeoffMode, double securityTakeoffHeight,
            int maxSpeed, OutOfControlAction outOfControlAction, int rthAltitude,
            int commanderFlightMode, double commanderFlightHeight, int rthMode,
            String cloudToCloudId) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : "";
        this.uuid = uuid != null ? uuid : "";
        this.prjUuid = prjUuid != null ? prjUuid : "";
        this.sn = sn != null ? sn : "";
        this.droneSn = droneSn != null ? droneSn : "";
        this.trackId = trackId != null ? trackId : "";
        this.userId = userId != null ? userId : "";
        this.status = status;
        this.errCode = errCode;
        this.takeoffMode = takeoffMode != null ? takeoffMode : "";
        this.securityTakeoffHeight = securityTakeoffHeight;
        this.maxSpeed = maxSpeed;
        this.outOfControlAction = outOfControlAction != null ? outOfControlAction : OutOfControlAction.UNKNOWN;
        this.rthAltitude = rthAltitude;
        this.commanderFlightMode = commanderFlightMode;
        this.commanderFlightHeight = commanderFlightHeight;
        this.rthMode = rthMode;
        this.cloudToCloudId = cloudToCloudId != null ? cloudToCloudId : "";
    }

    /** 数据库自增 ID */
    public int getId() { return id; }
    /** 创建时间（ISO 8601 格式） */
    public String getCreatedAt() { return createdAt; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 项目 UUID */
    public String getPrjUuid() { return prjUuid; }
    /** 机场序列号 */
    public String getSn() { return sn; }
    /** 无人机序列号 */
    public String getDroneSn() { return droneSn; }
    /** 轨迹 ID */
    public String getTrackId() { return trackId; }
    /** 用户 ID */
    public String getUserId() { return userId; }
    /** 任务状态：0-待执行，1-执行中，2-已完成，3-失败，4-已取消 */
    public int getStatus() { return status; }
    /** 错误码 */
    public int getErrCode() { return errCode; }
    /** 起飞模式："Takeoff"-直接起飞，"TakeoffWithFlyTo"-起飞后飞向目标点 */
    public String getTakeoffMode() { return takeoffMode; }
    /** 安全起飞高度（米） */
    public double getSecurityTakeoffHeight() { return securityTakeoffHeight; }
    /** 最大速度（米/秒） */
    public int getMaxSpeed() { return maxSpeed; }
    /** 失控动作：1-返航，2-继续执行，3-悬停 */
    public OutOfControlAction getOutOfControlAction() { return outOfControlAction; }
    /** 返航高度（米） */
    public int getRthAltitude() { return rthAltitude; }
    /** 指点飞行模式 */
    public int getCommanderFlightMode() { return commanderFlightMode; }
    /** 指点飞行高度（米） */
    public double getCommanderFlightHeight() { return commanderFlightHeight; }
    /** 返航模式 */
    public int getRthMode() { return rthMode; }
    /** 云云对接 ID */
    public String getCloudToCloudId() { return cloudToCloudId; }

    /** @param id 数据库自增 ID */
    public void setId(int id) {
        this.id = id;
    }

    /** @param createdAt 创建时间（ISO 8601 格式） */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    /** @param trackId 轨迹 ID */
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    /** @param userId 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @param status 任务状态：0-待执行，1-执行中，2-已完成，3-失败，4-已取消 */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 错误码 */
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    /** @param takeoffMode 起飞模式："Takeoff"-直接起飞，"TakeoffWithFlyTo"-起飞后飞向目标点 */
    public void setTakeoffMode(String takeoffMode) {
        this.takeoffMode = takeoffMode;
    }

    /** @param securityTakeoffHeight 安全起飞高度（米） */
    public void setSecurityTakeoffHeight(double securityTakeoffHeight) {
        this.securityTakeoffHeight = securityTakeoffHeight;
    }

    /** @param maxSpeed 最大速度（米/秒） */
    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /** @param outOfControlAction 失控动作：1-返航，2-继续执行，3-悬停 */
    public void setOutOfControlAction(OutOfControlAction outOfControlAction) {
        this.outOfControlAction = outOfControlAction;
    }

    /** @param rthAltitude 返航高度（米） */
    public void setRthAltitude(int rthAltitude) {
        this.rthAltitude = rthAltitude;
    }

    /** @param commanderFlightMode 指点飞行模式 */
    public void setCommanderFlightMode(int commanderFlightMode) {
        this.commanderFlightMode = commanderFlightMode;
    }

    /** @param commanderFlightHeight 指点飞行高度（米） */
    public void setCommanderFlightHeight(double commanderFlightHeight) {
        this.commanderFlightHeight = commanderFlightHeight;
    }

    /** @param rthMode 返航模式 */
    public void setRthMode(int rthMode) {
        this.rthMode = rthMode;
    }

    /** @param cloudToCloudId 云云对接 ID */
    public void setCloudToCloudId(String cloudToCloudId) {
        this.cloudToCloudId = cloudToCloudId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandTaskData that = (CommandTaskData) o;
        return id == that.id
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(prjUuid, that.prjUuid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(droneSn, that.droneSn)
                && Objects.equals(trackId, that.trackId)
                && Objects.equals(userId, that.userId)
                && status == that.status
                && errCode == that.errCode
                && Objects.equals(takeoffMode, that.takeoffMode)
                && Double.compare(that.securityTakeoffHeight, securityTakeoffHeight) == 0
                && maxSpeed == that.maxSpeed
                && outOfControlAction == that.outOfControlAction
                && rthAltitude == that.rthAltitude
                && commanderFlightMode == that.commanderFlightMode
                && Double.compare(that.commanderFlightHeight, commanderFlightHeight) == 0
                && rthMode == that.rthMode
                && Objects.equals(cloudToCloudId, that.cloudToCloudId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                createdAt,
                uuid,
                prjUuid,
                sn,
                droneSn,
                trackId,
                userId,
                status,
                errCode,
                takeoffMode,
                securityTakeoffHeight,
                maxSpeed,
                outOfControlAction,
                rthAltitude,
                commanderFlightMode,
                commanderFlightHeight,
                rthMode,
                cloudToCloudId);
    }

    @Override
    public String toString() {
        return "CommandTaskData{"
                + "id=" + id
                + ", createdAt='" + createdAt + '\''
                + ", uuid='" + uuid + '\''
                + ", prjUuid='" + prjUuid + '\''
                + ", sn='" + sn + '\''
                + ", droneSn='" + droneSn + '\''
                + ", trackId='" + trackId + '\''
                + ", userId='" + userId + '\''
                + ", status=" + status
                + ", errCode=" + errCode
                + ", takeoffMode='" + takeoffMode + '\''
                + ", securityTakeoffHeight=" + securityTakeoffHeight
                + ", maxSpeed=" + maxSpeed
                + ", outOfControlAction=" + outOfControlAction
                + ", rthAltitude=" + rthAltitude
                + ", commanderFlightMode=" + commanderFlightMode
                + ", commanderFlightHeight=" + commanderFlightHeight
                + ", rthMode=" + rthMode
                + ", cloudToCloudId='" + cloudToCloudId + '\''
                + '}';
    }
}
