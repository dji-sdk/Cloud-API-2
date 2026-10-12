// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 工作空间设备变更推送。
 */
public final class WorkspaceDeviceChange {

    private List<DeviceChange> deviceChanges;
    public WorkspaceDeviceChange() {
    }

/**
     * 构造 WorkspaceDeviceChange。
     *
     * @param deviceChanges 设备变更列表
     */
    public WorkspaceDeviceChange(List<DeviceChange> deviceChanges) {
        this.deviceChanges = deviceChanges != null ? deviceChanges : Collections.emptyList();
    }

    /** 设备变更列表 */
    public List<DeviceChange> getDeviceChanges() {
        return deviceChanges;
    }

    /** 设备变更列表 */
    public void setDeviceChanges(List<DeviceChange> deviceChanges) {
        this.deviceChanges = deviceChanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkspaceDeviceChange that = (WorkspaceDeviceChange) o;
        return Objects.equals(deviceChanges, that.deviceChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceChanges);
    }

    @Override
    public String toString() {
        return "WorkspaceDeviceChange{deviceChanges=" + deviceChanges + '}';
    }
}
