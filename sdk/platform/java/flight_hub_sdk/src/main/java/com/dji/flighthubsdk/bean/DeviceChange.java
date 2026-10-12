// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 工作空间单次设备变更条目。
 * <p>{@code changeType} 通常为 {@code "add"} 或 {@code "reduce"}（与云端约定一致）。</p>
 */
public final class DeviceChange {

    /** "add" or "reduce" */
    private String changeType;
    private List<String> changeDevices;
    public DeviceChange() {
    }

/**
     * 构造 DeviceChange。
     *
     * @param changeType 变更类型
     * @param changeDevices 变更的设备
     */
    public DeviceChange(String changeType, List<String> changeDevices) {
        this.changeType = changeType != null ? changeType : "";
        this.changeDevices = changeDevices != null ? changeDevices : Collections.emptyList();
    }

    /** "add" or "reduce" */
    public String getChangeType() {
        return changeType;
    }

    /** @param changeType "add" or "reduce" */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    /** 变更的设备 */
    public List<String> getChangeDevices() {
        return changeDevices;
    }

    /** 变更的设备 */
    public void setChangeDevices(List<String> changeDevices) {
        this.changeDevices = changeDevices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceChange that = (DeviceChange) o;
        return Objects.equals(changeType, that.changeType) && Objects.equals(changeDevices, that.changeDevices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeType, changeDevices);
    }

    @Override
    public String toString() {
        return "DeviceChange{changeType='" + changeType + '\'' + ", changeDevices=" + changeDevices + '}';
    }
}
