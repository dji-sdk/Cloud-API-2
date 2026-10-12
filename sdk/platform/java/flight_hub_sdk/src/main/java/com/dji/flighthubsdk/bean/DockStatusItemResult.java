// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** Workspace 机场设备状态列表单条。 */
public final class DockStatusItemResult {

    private String sn;
    private boolean online;
    private long updatedTime;
    private DeviceStatusModel deviceModel;
    private DockStatusPropertiesResult properties;
    public DockStatusItemResult() {
    }

/**
     * 构造 DockStatusItemResult。
     *
     * @param sn 设备序列号
     * @param online 是否在线
     * @param updatedTime 更新时间
     * @param deviceModel 设备型号
     * @param properties 可能为 null
     */
    public DockStatusItemResult(
            String sn,
            boolean online,
            long updatedTime,
            DeviceStatusModel deviceModel,
            DockStatusPropertiesResult properties) {
        this.sn = sn != null ? sn : "";
        this.online = online;
        this.updatedTime = updatedTime;
        this.deviceModel = deviceModel;
        this.properties = properties;
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 是否在线 */
    public boolean isOnline() {
        return online;
    }

    /** 是否在线 */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /** 更新时间 */
    public long getUpdatedTime() {
        return updatedTime;
    }

    /** 更新时间 */
    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    /** 设备型号 */
    public DeviceStatusModel getDeviceModel() {
        return deviceModel;
    }

    /** 设备型号 */
    public void setDeviceModel(DeviceStatusModel deviceModel) {
        this.deviceModel = deviceModel;
    }

    /** 可能为 null */
    public DockStatusPropertiesResult getProperties() {
        return properties;
    }

    /** 可能为 null */
    public void setProperties(DockStatusPropertiesResult properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusItemResult that = (DockStatusItemResult) o;
        return Objects.equals(sn, that.sn)
                && online == that.online
                && updatedTime == that.updatedTime
                && Objects.equals(deviceModel, that.deviceModel)
                && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sn,
                online,
                updatedTime,
                deviceModel,
                properties);
    }

    @Override
    public String toString() {
        return "DockStatusItemResult{"
                + "sn='" + sn + '\''
                + ", online=" + online
                + ", updatedTime=" + updatedTime
                + ", deviceModel=" + deviceModel
                + ", properties=" + properties
                + '}';
    }
}
