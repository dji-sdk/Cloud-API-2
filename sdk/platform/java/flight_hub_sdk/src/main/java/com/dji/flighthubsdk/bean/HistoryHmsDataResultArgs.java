// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 历史HMS数据结果Args。 */
public final class HistoryHmsDataResultArgs {
    /** 告警信息 */
    private int alarmInfo;
    /** 组件索引 */
    private int componentIndex;
    /** 创建时间戳 */
    private long createTimestamp;
    /** 设备 ID */
    private String deviceId;
    /** 显示场景 */
    private int displayScene;
    /** 是否忽略 */
    private int ignore;
    /** 发送方主机 ID */
    private String senderHostId;
    /** 传感器索引 */
    private int sensorIndex;
    public HistoryHmsDataResultArgs() {
    }

/**
     * 构造 HistoryHmsDataResultArgs。
     *
     * @param alarmInfo 告警信息
     * @param componentIndex 组件索引
     * @param createTimestamp 创建时间戳
     * @param deviceId 设备 ID
     * @param displayScene 显示场景
     * @param ignore 是否忽略
     * @param senderHostId 发送者主机 ID
     * @param sensorIndex 传感器索引
     */
    public HistoryHmsDataResultArgs(
            int alarmInfo,
            int componentIndex,
            long createTimestamp,
            String deviceId,
            int displayScene,
            int ignore,
            String senderHostId,
            int sensorIndex) {
        this.alarmInfo = alarmInfo;
        this.componentIndex = componentIndex;
        this.createTimestamp = createTimestamp;
        this.deviceId = deviceId != null ? deviceId : "";
        this.displayScene = displayScene;
        this.ignore = ignore;
        this.senderHostId = senderHostId != null ? senderHostId : "";
        this.sensorIndex = sensorIndex;
    }

    /** 告警信息 */
    public int getAlarmInfo() { return alarmInfo; }
    /** 组件索引 */
    public int getComponentIndex() { return componentIndex; }
    /** 创建时间戳 */
    public long getCreateTimestamp() { return createTimestamp; }
    /** 设备 ID */
    public String getDeviceId() { return deviceId; }
    /** 显示场景 */
    public int getDisplayScene() { return displayScene; }
    /** 是否忽略 */
    public int getIgnore() { return ignore; }
    /** 发送方主机 ID */
    public String getSenderHostId() { return senderHostId; }
    /** 传感器索引 */
    public int getSensorIndex() { return sensorIndex; }

    /** 告警信息 */
    public void setAlarmInfo(int alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    /** 组件索引 */
    public void setComponentIndex(int componentIndex) {
        this.componentIndex = componentIndex;
    }

    /** 创建时间戳 */
    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    /** 设备 ID */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /** 显示场景 */
    public void setDisplayScene(int displayScene) {
        this.displayScene = displayScene;
    }

    /** 是否忽略 */
    public void setIgnore(int ignore) {
        this.ignore = ignore;
    }

    /** @param senderHostId 发送方主机 ID */
    public void setSenderHostId(String senderHostId) {
        this.senderHostId = senderHostId;
    }

    /** 传感器索引 */
    public void setSensorIndex(int sensorIndex) {
        this.sensorIndex = sensorIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HistoryHmsDataResultArgs that = (HistoryHmsDataResultArgs) o;
        return alarmInfo == that.alarmInfo
                && componentIndex == that.componentIndex
                && createTimestamp == that.createTimestamp
                && Objects.equals(deviceId, that.deviceId)
                && displayScene == that.displayScene
                && ignore == that.ignore
                && Objects.equals(senderHostId, that.senderHostId)
                && sensorIndex == that.sensorIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                alarmInfo,
                componentIndex,
                createTimestamp,
                deviceId,
                displayScene,
                ignore,
                senderHostId,
                sensorIndex);
    }

    @Override
    public String toString() {
        return "HistoryHmsDataResultArgs{"
                + "alarmInfo=" + alarmInfo
                + ", componentIndex=" + componentIndex
                + ", createTimestamp=" + createTimestamp
                + ", deviceId='" + deviceId + '\''
                + ", displayScene=" + displayScene
                + ", ignore=" + ignore
                + ", senderHostId='" + senderHostId + '\''
                + ", sensorIndex=" + sensorIndex
                + '}';
    }
}
