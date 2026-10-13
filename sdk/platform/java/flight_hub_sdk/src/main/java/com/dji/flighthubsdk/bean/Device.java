// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Workspace 设备拓扑中的单台设备。
 */
public final class Device {

    private String deviceSn;
    private String deviceClass;
    private String deviceName;
    /** 设备昵称(来自 devices: device_inside_callsign 字段) */
    private String nickname;
    /** 设备型号 key，用于机型判断（如网联飞机 EA340 系列） */
    private String modelKey;
    private boolean online;
    /** 父设备 SN 列表（relay 等子设备场景） */
    private List<String> parentSns = new ArrayList<>();
    public Device() {
    }

/**
     * 构造 Device。
     *
     * @param deviceSn 设备序列号(SN)
     * @param deviceClass 设备类别
     * @param deviceName 设备名称
     * @param nickname 设备昵称
     * @param modelKey 设备型号 key
     * @param online 设备是否在线
     */
    public Device(
            String deviceSn,
            String deviceClass,
            String deviceName,
            String nickname,
            String modelKey,
            boolean online) {
        this(deviceSn, deviceClass, deviceName, nickname, modelKey, online, Collections.emptyList());
    }

    /**
     * 构造 Device。
     *
     * @param deviceSn 设备序列号(SN)
     * @param deviceClass 设备类别
     * @param deviceName 设备名称
     * @param nickname 设备昵称
     * @param modelKey 设备型号 key
     * @param online 设备是否在线
     * @param parentSns 后端拓扑返回的父设备 SN 列表
     */
    public Device(
            String deviceSn,
            String deviceClass,
            String deviceName,
            String nickname,
            String modelKey,
            boolean online,
            List<String> parentSns) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.deviceClass = deviceClass != null ? deviceClass : "";
        this.deviceName = deviceName != null ? deviceName : "";
        this.nickname = nickname != null ? nickname : "";
        this.modelKey = modelKey != null ? modelKey : "";
        this.online = online;
        setParentSns(parentSns);
    }

    /** 设备序列号(SN) */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** 设备序列号(SN) */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 设备类别 */
    public String getDeviceClass() {
        return deviceClass;
    }

    /** 设备类别 */
    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    /** 设备名称 */
    public String getDeviceName() {
        return deviceName;
    }

    /** 设备名称 */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /** 设备昵称(来自 devices: device_inside_callsign 字段) */
    public String getNickname() {
        return nickname;
    }

    /** @param nickname 设备昵称(来自 devices: device_inside_callsign 字段) */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 设备型号 key，用于机型判断（如网联飞机 EA340 系列） */
    public String getModelKey() {
        return modelKey;
    }

    /** @param modelKey 设备型号 key，用于机型判断（如网联飞机 EA340 系列） */
    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }

    /** 设备是否在线 */
    public boolean isOnline() {
        return online;
    }

    /** 设备是否在线 */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /** 父设备 SN 列表（relay 等子设备场景） */
    public List<String> getParentSns() {
        return Collections.unmodifiableList(parentSns);
    }

    /** @param parentSns 父设备 SN 列表（relay 等子设备场景） */
    public void setParentSns(List<String> parentSns) {
        this.parentSns = parentSns != null ? new ArrayList<>(parentSns) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device that = (Device) o;
        return online == that.online
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(deviceClass, that.deviceClass)
                && Objects.equals(deviceName, that.deviceName)
                && Objects.equals(nickname, that.nickname)
                && Objects.equals(modelKey, that.modelKey)
                && Objects.equals(parentSns, that.parentSns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, deviceClass, deviceName, nickname, modelKey, online, parentSns);
    }

    @Override
    public String toString() {
        return "Device{"
                + "deviceSn='" + deviceSn + '\''
                + ", deviceClass='" + deviceClass + '\''
                + ", deviceName='" + deviceName + '\''
                + ", nickname='" + nickname + '\''
                + ", modelKey='" + modelKey + '\''
                + ", online=" + online
                + ", parentSns=" + parentSns
                + '}';
    }
}
