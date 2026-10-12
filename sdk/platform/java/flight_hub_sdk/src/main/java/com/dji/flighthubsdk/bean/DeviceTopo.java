// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Workspace 设备拓扑节点。
 * <p>主设备 {@link #getDevice()} + 子设备列表 {@link #getSubDevices()}。</p>
 */
public final class DeviceTopo {

    private Device device;
    private List<Device> subDevices;
    public DeviceTopo() {
    }

/**
     * 构造 DeviceTopo。
     *
     * @param device 设备信息
     * @param subDevices 子设备列表
     */
    public DeviceTopo(Device device, List<Device> subDevices) {
        this.device = device != null ? device : new Device("", "", "", "", "", false);
        this.subDevices = subDevices != null ? subDevices : Collections.emptyList();
    }

    /** 设备信息 */
    public Device getDevice() {
        return device;
    }

    /** 设备信息 */
    public void setDevice(Device device) {
        this.device = device;
    }

    /** 子设备列表 */
    public List<Device> getSubDevices() {
        return subDevices;
    }

    /** 子设备列表 */
    public void setSubDevices(List<Device> subDevices) {
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
        DeviceTopo that = (DeviceTopo) o;
        return Objects.equals(device, that.device) && Objects.equals(subDevices, that.subDevices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, subDevices);
    }

    @Override
    public String toString() {
        return "DeviceTopo{device=" + device + ", subDevices=" + subDevices + '}';
    }
}
