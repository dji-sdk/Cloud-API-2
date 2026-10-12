// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** HMS 参数块。 */
public final class HmsArgs {

    /** int, 文案变量, 默认无效值(-1); */
    private int componentIndex;
    /** int, 文案变量, 默认无效值(-1); */
    private int sensorIndex;
    /** string, 告警产生的设备ID, 无效值: 空值; */
    private String deviceId;
    /** string, 告警产生的模块ID, 无效值: 空值; */
    private String senderHostId;
    /** int, 告警是否忽略, {0:不忽略, 1: 忽略}; */
    private int ignore;
    /** int, 模块自定义的告警信息; */
    private int alarmInfo;
    /** long, 故障产生的UTC时间(单位秒); // TODO: long long 类型 AIGC 解析出错; */
    private long createTimestamp;
    /** int, 0: 正式错误码, 1: 调试错误码, 只用于排障; */
    private int displayScene;
    public HmsArgs() {
    }

/**
     * 构造 HmsArgs。
     *
     * @param componentIndex 组件索引
     * @param sensorIndex 传感器索引
     * @param deviceId 设备 ID
     * @param senderHostId 发送者主机 ID
     * @param ignore 是否忽略
     * @param alarmInfo 告警信息
     * @param createTimestamp 创建时间戳
     * @param displayScene 显示场景
     */
    public HmsArgs(
            int componentIndex,
            int sensorIndex,
            String deviceId,
            String senderHostId,
            int ignore,
            int alarmInfo,
            long createTimestamp,
            int displayScene) {
        this.componentIndex = componentIndex;
        this.sensorIndex = sensorIndex;
        this.deviceId = deviceId != null ? deviceId : "";
        this.senderHostId = senderHostId != null ? senderHostId : "";
        this.ignore = ignore;
        this.alarmInfo = alarmInfo;
        this.createTimestamp = createTimestamp;
        this.displayScene = displayScene;
    }

    /** int, 文案变量, 默认无效值(-1); */
    public int getComponentIndex() { return componentIndex; }
    /** int, 文案变量, 默认无效值(-1); */
    public int getSensorIndex() { return sensorIndex; }
    /** string, 告警产生的设备ID, 无效值: 空值; */
    public String getDeviceId() { return deviceId; }
    /** string, 告警产生的模块ID, 无效值: 空值; */
    public String getSenderHostId() { return senderHostId; }
    /** int, 告警是否忽略, {0:不忽略, 1: 忽略}; */
    public int getIgnore() { return ignore; }
    /** int, 模块自定义的告警信息; */
    public int getAlarmInfo() { return alarmInfo; }
    /** long, 故障产生的UTC时间(单位秒); // TODO: long long 类型 AIGC 解析出错; */
    public long getCreateTimestamp() { return createTimestamp; }
    /** int, 0: 正式错误码, 1: 调试错误码, 只用于排障; */
    public int getDisplayScene() { return displayScene; }

    /** @param componentIndex int, 文案变量, 默认无效值(-1); */
    public void setComponentIndex(int componentIndex) {
        this.componentIndex = componentIndex;
    }

    /** @param sensorIndex int, 文案变量, 默认无效值(-1); */
    public void setSensorIndex(int sensorIndex) {
        this.sensorIndex = sensorIndex;
    }

    /** @param deviceId string, 告警产生的设备ID, 无效值: 空值; */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /** @param senderHostId string, 告警产生的模块ID, 无效值: 空值; */
    public void setSenderHostId(String senderHostId) {
        this.senderHostId = senderHostId;
    }

    /** @param ignore int, 告警是否忽略, {0:不忽略, 1: 忽略}; */
    public void setIgnore(int ignore) {
        this.ignore = ignore;
    }

    /** @param alarmInfo int, 模块自定义的告警信息; */
    public void setAlarmInfo(int alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    /** @param createTimestamp long, 故障产生的UTC时间(单位秒); // TODO: long long 类型 AIGC 解析出错; */
    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    /** @param displayScene int, 0: 正式错误码, 1: 调试错误码, 只用于排障; */
    public void setDisplayScene(int displayScene) {
        this.displayScene = displayScene;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HmsArgs hmsArgs = (HmsArgs) o;
        return componentIndex == hmsArgs.componentIndex
                && sensorIndex == hmsArgs.sensorIndex
                && ignore == hmsArgs.ignore
                && alarmInfo == hmsArgs.alarmInfo
                && createTimestamp == hmsArgs.createTimestamp
                && displayScene == hmsArgs.displayScene
                && Objects.equals(deviceId, hmsArgs.deviceId)
                && Objects.equals(senderHostId, hmsArgs.senderHostId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                componentIndex, sensorIndex, deviceId, senderHostId, ignore, alarmInfo, createTimestamp, displayScene);
    }

    @Override
    public String toString() {
        return "HmsArgs{"
                + "componentIndex=" + componentIndex
                + ", sensorIndex=" + sensorIndex
                + ", deviceId='" + deviceId + '\''
                + ", senderHostId='" + senderHostId + '\''
                + ", ignore=" + ignore
                + ", alarmInfo=" + alarmInfo
                + ", createTimestamp=" + createTimestamp
                + ", displayScene=" + displayScene
                + '}';
    }
}
