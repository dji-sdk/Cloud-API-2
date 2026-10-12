// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备解锁证书公共字段（ID、名称、类型、分组、用户、设备 SN、有效期等） */
public final class DeviceUnlockLicenseCommonFields {

    private int id;
    private String name;
    private int type;
    private int groupId;
    private String userId;
    private String deviceSn;
    private long beginTime;
    private long endTime;
    private boolean userOnly;
    private boolean enabled;
    public DeviceUnlockLicenseCommonFields() {
    }

/**
     * 构造 DeviceUnlockLicenseCommonFields。
     *
     * @param id 许可证 ID
     * @param name 许可证名称
     * @param type 解锁类型
     * @param groupId 分组 ID
     * @param userId 用户 ID
     * @param deviceSn 设备序列号(SN)
     * @param beginTime 有效期开始时间
     * @param endTime 有效期结束时间
     * @param userOnly 是否仅限指定用户
     * @param enabled 是否启用
     */
    public DeviceUnlockLicenseCommonFields(
            int id,
            String name,
            int type,
            int groupId,
            String userId,
            String deviceSn,
            long beginTime,
            long endTime,
            boolean userOnly,
            boolean enabled) {
        this.id = id;
        this.name = name != null ? name : "";
        this.type = type;
        this.groupId = groupId;
        this.userId = userId != null ? userId : "";
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.userOnly = userOnly;
        this.enabled = enabled;
    }

    /** 许可证 ID */
    public int getId() {
        return id;
    }

    /** 许可证 ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 许可证名称 */
    public String getName() {
        return name;
    }

    /** 许可证名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 解锁类型 */
    public int getType() {
        return type;
    }

    /** 解锁类型 */
    public void setType(int type) {
        this.type = type;
    }

    /** 分组 ID */
    public int getGroupId() {
        return groupId;
    }

    /** 分组 ID */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /** 用户 ID */
    public String getUserId() {
        return userId;
    }

    /** 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** 设备序列号(SN) */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备序列号(SN) */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 有效期开始时间 */
    public long getBeginTime() {
        return beginTime;
    }

    /** 有效期开始时间 */
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    /** 有效期结束时间 */
    public long getEndTime() {
        return endTime;
    }

    /** 有效期结束时间 */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /** 是否仅限指定用户 */
    public boolean isUserOnly() {
        return userOnly;
    }

    /** 是否仅限指定用户 */
    public void setUserOnly(boolean userOnly) {
        this.userOnly = userOnly;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return enabled;
    }

    /** 是否启用 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceUnlockLicenseCommonFields that = (DeviceUnlockLicenseCommonFields) o;
        return id == that.id
                && Objects.equals(name, that.name)
                && type == that.type
                && groupId == that.groupId
                && Objects.equals(userId, that.userId)
                && Objects.equals(deviceSn, that.deviceSn)
                && beginTime == that.beginTime
                && endTime == that.endTime
                && userOnly == that.userOnly
                && enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                type,
                groupId,
                userId,
                deviceSn,
                beginTime,
                endTime,
                userOnly,
                enabled);
    }

    @Override
    public String toString() {
        return "DeviceUnlockLicenseCommonFields{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", type=" + type
                + ", groupId=" + groupId
                + ", userId='" + userId + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", beginTime=" + beginTime
                + ", endTime=" + endTime
                + ", userOnly=" + userOnly
                + ", enabled=" + enabled
                + '}';
    }
}
