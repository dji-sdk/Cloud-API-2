// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Workspace 设备详情中的子设备。
 */
public final class SubDeviceInfo {

    private String deviceSn;
    private String nickname = "";
    private ModelInfo modelInfo;
    private boolean online;
    private String deviceFirmwareVersion;
    private int deviceFirmwareStatus;
    private int deviceWorkingStatus;
    private String flysafeDatabaseVersion;
    private List<String> parentSns;
    public SubDeviceInfo() {
    }

/**
     * 构造 SubDeviceInfo。
     *
     * @param deviceSn 设备序列号
     * @param modelInfo 型号信息
     * @param online 是否在线
     * @param deviceFirmwareVersion 设备固件版本
     * @param deviceFirmwareStatus 设备固件状态
     * @param deviceWorkingStatus 设备工作状态
     * @param flysafeDatabaseVersion 飞安数据库版本
     * @param parentSns 父设备序列号列表
     */
    public SubDeviceInfo(
            String deviceSn,
            ModelInfo modelInfo,
            boolean online,
            String deviceFirmwareVersion,
            int deviceFirmwareStatus,
            int deviceWorkingStatus,
            String flysafeDatabaseVersion,
            List<String> parentSns) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.modelInfo = modelInfo != null ? modelInfo : new ModelInfo("", "", "", "", "", "", "");
        this.online = online;
        this.deviceFirmwareVersion = deviceFirmwareVersion != null ? deviceFirmwareVersion : "";
        this.deviceFirmwareStatus = deviceFirmwareStatus;
        this.deviceWorkingStatus = deviceWorkingStatus;
        this.flysafeDatabaseVersion = flysafeDatabaseVersion != null ? flysafeDatabaseVersion : "";
        this.parentSns = parentSns != null ? parentSns : Collections.emptyList();
    }

    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源) */
    public String getNickname() { return nickname; }
    /** 型号信息 */
    public ModelInfo getModelInfo() { return modelInfo; }
    /** 是否在线 */
    public boolean isOnline() { return online; }
    /** 设备固件版本 */
    public String getDeviceFirmwareVersion() { return deviceFirmwareVersion; }
    /** 设备固件状态 */
    public int getDeviceFirmwareStatus() { return deviceFirmwareStatus; }
    /** 设备工作状态 */
    public int getDeviceWorkingStatus() { return deviceWorkingStatus; }
    /** 飞安数据库版本 */
    public String getFlysafeDatabaseVersion() { return flysafeDatabaseVersion; }
    /** 父设备序列号列表 */
    public List<String> getParentSns() { return parentSns; }

    /** 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源) */
    public void setNickname(String nickname) {
        this.nickname = nickname != null ? nickname : "";
    }

    /** 型号信息 */
    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /** 是否在线 */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /** 设备固件版本 */
    public void setDeviceFirmwareVersion(String deviceFirmwareVersion) {
        this.deviceFirmwareVersion = deviceFirmwareVersion;
    }

    /** 设备固件状态 */
    public void setDeviceFirmwareStatus(int deviceFirmwareStatus) {
        this.deviceFirmwareStatus = deviceFirmwareStatus;
    }

    /** 设备工作状态 */
    public void setDeviceWorkingStatus(int deviceWorkingStatus) {
        this.deviceWorkingStatus = deviceWorkingStatus;
    }

    /** 飞安数据库版本 */
    public void setFlysafeDatabaseVersion(String flysafeDatabaseVersion) {
        this.flysafeDatabaseVersion = flysafeDatabaseVersion;
    }

    /** 父设备序列号列表 */
    public void setParentSns(List<String> parentSns) {
        this.parentSns = parentSns;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubDeviceInfo that = (SubDeviceInfo) o;
        return online == that.online
                && deviceFirmwareStatus == that.deviceFirmwareStatus
                && deviceWorkingStatus == that.deviceWorkingStatus
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(nickname, that.nickname)
                && Objects.equals(modelInfo, that.modelInfo)
                && Objects.equals(deviceFirmwareVersion, that.deviceFirmwareVersion)
                && Objects.equals(flysafeDatabaseVersion, that.flysafeDatabaseVersion)
                && Objects.equals(parentSns, that.parentSns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceSn,
                nickname,
                modelInfo,
                online,
                deviceFirmwareVersion,
                deviceFirmwareStatus,
                deviceWorkingStatus,
                flysafeDatabaseVersion,
                parentSns);
    }

    @Override
    public String toString() {
        return "SubDeviceInfo{"
                + "deviceSn='" + deviceSn + '\''
                + ", nickname='" + nickname + '\''
                + ", modelInfo=" + modelInfo
                + ", online=" + online
                + ", deviceFirmwareVersion='" + deviceFirmwareVersion + '\''
                + ", deviceFirmwareStatus=" + deviceFirmwareStatus
                + ", deviceWorkingStatus=" + deviceWorkingStatus
                + ", flysafeDatabaseVersion='" + flysafeDatabaseVersion + '\''
                + ", parentSns=" + parentSns
                + '}';
    }
}
