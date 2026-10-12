// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Workspace 设备详情。
 */
public final class DeviceDetailInfo {

    private String deviceSn;
    private String nickname;
    private ModelInfo modelInfo;
    private int deviceAddTime;
    private boolean online;
    private String deviceFirmwareVersion;
    private int deviceFirmwareStatus;
    private int deviceWorkingStatus;
    private String completeMachineSn;
    private List<SubDeviceInfo> subDevices;
    public DeviceDetailInfo() {
    }

/**
     * 构造 DeviceDetailInfo。
     *
     * @param deviceSn 设备序列号(SN)
     * @param nickname 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源)
     * @param modelInfo 机型信息
     * @param deviceAddTime 设备添加时间
     * @param online 设备是否在线
     * @param deviceFirmwareVersion 设备固件版本
     * @param deviceFirmwareStatus 设备固件状态
     * @param deviceWorkingStatus 设备工作状态
     * @param completeMachineSn 整机序列号
     * @param subDevices 子设备列表
     */
    public DeviceDetailInfo(
            String deviceSn,
            String nickname,
            ModelInfo modelInfo,
            int deviceAddTime,
            boolean online,
            String deviceFirmwareVersion,
            int deviceFirmwareStatus,
            int deviceWorkingStatus,
            String completeMachineSn,
            List<SubDeviceInfo> subDevices) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.nickname = nickname != null ? nickname : "";
        this.modelInfo = modelInfo != null ? modelInfo : new ModelInfo("", "", "", "", "", "", "");
        this.deviceAddTime = deviceAddTime;
        this.online = online;
        this.deviceFirmwareVersion = deviceFirmwareVersion != null ? deviceFirmwareVersion : "";
        this.deviceFirmwareStatus = deviceFirmwareStatus;
        this.deviceWorkingStatus = deviceWorkingStatus;
        this.completeMachineSn = completeMachineSn != null ? completeMachineSn : "";
        this.subDevices = subDevices != null ? subDevices : Collections.emptyList();
    }

    /** 设备序列号(SN) */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备序列号(SN) */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源) */
    public String getNickname() {
        return nickname;
    }

    /** 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源) */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 机型信息 */
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    /** 机型信息 */
    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    /** 设备添加时间 */
    public int getDeviceAddTime() {
        return deviceAddTime;
    }

    /** 设备添加时间 */
    public void setDeviceAddTime(int deviceAddTime) {
        this.deviceAddTime = deviceAddTime;
    }

    /** 设备是否在线 */
    public boolean isOnline() {
        return online;
    }

    /** 设备是否在线 */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /** 设备固件版本 */
    public String getDeviceFirmwareVersion() {
        return deviceFirmwareVersion;
    }

    /** 设备固件版本 */
    public void setDeviceFirmwareVersion(String deviceFirmwareVersion) {
        this.deviceFirmwareVersion = deviceFirmwareVersion;
    }

    /** 设备固件状态 */
    public int getDeviceFirmwareStatus() {
        return deviceFirmwareStatus;
    }

    /** 设备固件状态 */
    public void setDeviceFirmwareStatus(int deviceFirmwareStatus) {
        this.deviceFirmwareStatus = deviceFirmwareStatus;
    }

    /** 设备工作状态 */
    public int getDeviceWorkingStatus() {
        return deviceWorkingStatus;
    }

    /** 设备工作状态 */
    public void setDeviceWorkingStatus(int deviceWorkingStatus) {
        this.deviceWorkingStatus = deviceWorkingStatus;
    }

    /** 整机序列号 */
    public String getCompleteMachineSn() {
        return completeMachineSn;
    }

    /** 整机序列号 */
    public void setCompleteMachineSn(String completeMachineSn) {
        this.completeMachineSn = completeMachineSn;
    }

    /** 子设备列表 */
    public List<SubDeviceInfo> getSubDevices() {
        return subDevices;
    }

    /** 子设备列表 */
    public void setSubDevices(List<SubDeviceInfo> subDevices) {
        this.subDevices = subDevices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceDetailInfo that = (DeviceDetailInfo) o;
        return deviceAddTime == that.deviceAddTime
                && online == that.online
                && deviceFirmwareStatus == that.deviceFirmwareStatus
                && deviceWorkingStatus == that.deviceWorkingStatus
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(nickname, that.nickname)
                && Objects.equals(modelInfo, that.modelInfo)
                && Objects.equals(deviceFirmwareVersion, that.deviceFirmwareVersion)
                && Objects.equals(completeMachineSn, that.completeMachineSn)
                && Objects.equals(subDevices, that.subDevices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                deviceSn,
                nickname,
                modelInfo,
                deviceAddTime,
                online,
                deviceFirmwareVersion,
                deviceFirmwareStatus,
                deviceWorkingStatus,
                completeMachineSn,
                subDevices);
    }

    @Override
    public String toString() {
        return "DeviceDetailInfo{"
                + "deviceSn='" + deviceSn + '\''
                + ", nickname='" + nickname + '\''
                + ", modelInfo=" + modelInfo
                + ", deviceAddTime=" + deviceAddTime
                + ", online=" + online
                + ", deviceFirmwareVersion='" + deviceFirmwareVersion + '\''
                + ", deviceFirmwareStatus=" + deviceFirmwareStatus
                + ", deviceWorkingStatus=" + deviceWorkingStatus
                + ", completeMachineSn='" + completeMachineSn + '\''
                + ", subDevices=" + subDevices
                + '}';
    }
}
