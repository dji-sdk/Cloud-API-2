// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 一键起飞任务列表项。 */
public final class CommandTaskListItemData {
    /** 任务 UUID */
    private String uuid;
    /** 设备 SN（网关） */
    private String sn;
    /** 飞机 SN */
    private String droneSn;
    /** 任务状态。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时 */
    private int status;
    /** 错误码（0 表示成功） */
    private int errCode;
    /** 起飞模式（"Takeoff" / "TakeoffWithFlyTo"） */
    private String takeoffMode;
    /** 安全起飞高度（米） */
    private double securityTakeoffHeight;
    /** 最大速度 */
    private int maxSpeed;
    /** 返航高度（米） */
    private int rthAltitude;
    /** 创建时间（ISO 8601） */
    private String createdAt;
    /** 用户 ID */
    private String userId;

    public CommandTaskListItemData() {
    }

    /**
     * 构造 CommandTaskListItemData。
     *
     * @param uuid 任务UUID
     * @param sn 设备SN（网关）
     * @param droneSn 飞机SN
     * @param status 任务状态（0=待执行,1=执行中,2=已完成,3=失败,4=超时）
     * @param errCode 错误码
     * @param takeoffMode 起飞模式（"Takeoff"/"TakeoffWithFlyTo"）
     * @param securityTakeoffHeight 安全起飞高度（米）
     * @param maxSpeed 最大速度
     * @param rthAltitude 返航高度（米）
     * @param createdAt 创建时间（ISO 8601）
     * @param userId 用户ID
     */
    public CommandTaskListItemData(String uuid, String sn, String droneSn, int status,
            int errCode, String takeoffMode, double securityTakeoffHeight,
            int maxSpeed, int rthAltitude, String createdAt, String userId) {
        this.uuid = uuid;
        this.sn = sn;
        this.droneSn = droneSn;
        this.status = status;
        this.errCode = errCode;
        this.takeoffMode = takeoffMode;
        this.securityTakeoffHeight = securityTakeoffHeight;
        this.maxSpeed = maxSpeed;
        this.rthAltitude = rthAltitude;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** @param uuid 任务 UUID */
    public void setUuid(String uuid) { this.uuid = uuid; }
    /** 设备 SN（网关） */
    public String getSn() { return sn; }
    /** @param sn 设备 SN（网关） */
    public void setSn(String sn) { this.sn = sn; }
    /** 飞机 SN */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 飞机 SN */
    public void setDroneSn(String droneSn) { this.droneSn = droneSn; }
    /** 任务状态。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时 */
    public int getStatus() { return status; }
    /** @param status 任务状态。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时 */
    public void setStatus(int status) { this.status = status; }
    /** 错误码（0 表示成功） */
    public int getErrCode() { return errCode; }
    /** @param errCode 错误码（0 表示成功） */
    public void setErrCode(int errCode) { this.errCode = errCode; }
    /** 起飞模式（"Takeoff" / "TakeoffWithFlyTo"） */
    public String getTakeoffMode() { return takeoffMode; }
    /** @param takeoffMode 起飞模式（"Takeoff" / "TakeoffWithFlyTo"） */
    public void setTakeoffMode(String takeoffMode) { this.takeoffMode = takeoffMode; }
    /** 安全起飞高度（米） */
    public double getSecurityTakeoffHeight() { return securityTakeoffHeight; }
    /** @param securityTakeoffHeight 安全起飞高度（米） */
    public void setSecurityTakeoffHeight(double securityTakeoffHeight) { this.securityTakeoffHeight = securityTakeoffHeight; }
    /** 最大速度 */
    public int getMaxSpeed() { return maxSpeed; }
    /** @param maxSpeed 最大速度 */
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }
    /** 返航高度（米） */
    public int getRthAltitude() { return rthAltitude; }
    /** @param rthAltitude 返航高度（米） */
    public void setRthAltitude(int rthAltitude) { this.rthAltitude = rthAltitude; }
    /** 创建时间（ISO 8601） */
    public String getCreatedAt() { return createdAt; }
    /** @param createdAt 创建时间（ISO 8601） */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    /** 用户 ID */
    public String getUserId() { return userId; }
    /** @param userId 用户 ID */
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommandTaskListItemData that = (CommandTaskListItemData) o;
        return status == that.status
                && errCode == that.errCode
                && Double.compare(that.securityTakeoffHeight, securityTakeoffHeight) == 0
                && maxSpeed == that.maxSpeed
                && rthAltitude == that.rthAltitude
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(droneSn, that.droneSn)
                && Objects.equals(takeoffMode, that.takeoffMode)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                uuid, sn, droneSn, status, errCode,
                takeoffMode, securityTakeoffHeight,
                maxSpeed, rthAltitude, createdAt, userId);
    }

    @Override
    public String toString() {
        return "CommandTaskListItemData{"
                + "uuid='" + uuid + '\''
                + ", sn='" + sn + '\''
                + ", droneSn='" + droneSn + '\''
                + ", status=" + status
                + ", errCode=" + errCode
                + ", takeoffMode='" + takeoffMode + '\''
                + ", securityTakeoffHeight=" + securityTakeoffHeight
                + ", maxSpeed=" + maxSpeed
                + ", rthAltitude=" + rthAltitude
                + ", createdAt='" + createdAt + '\''
                + ", userId='" + userId + '\''
                + '}';
    }
}
