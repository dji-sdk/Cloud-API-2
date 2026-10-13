// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 设备拓扑变更推送。
 */
public final class DeviceTopoChange {

    private String gatewaySn;
    /** 设备列表 */
    private List<String> devices;
    public DeviceTopoChange() {
    }

/**
     * 构造 DeviceTopoChange。
     *
     * @param gatewaySn 网关序列号
     * @param devices 设备列表
     */
    public DeviceTopoChange(String gatewaySn, List<String> devices) {
        this.gatewaySn = gatewaySn != null ? gatewaySn : "";
        this.devices = devices != null ? devices : Collections.emptyList();
    }

    /** 网关序列号 */
    public String getGatewaySn() {
        return gatewaySn;
    }

    /** 网关序列号 */
    public void setGatewaySn(String gatewaySn) {
        this.gatewaySn = gatewaySn;
    }

    /** 设备列表 */
    public List<String> getDevices() {
        return devices;
    }

    /** 设备列表 */
    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceTopoChange that = (DeviceTopoChange) o;
        return Objects.equals(gatewaySn, that.gatewaySn) && Objects.equals(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewaySn, devices);
    }

    @Override
    public String toString() {
        return "DeviceTopoChange{gatewaySn='" + gatewaySn + '\'' + ", devices=" + devices + '}';
    }
}
