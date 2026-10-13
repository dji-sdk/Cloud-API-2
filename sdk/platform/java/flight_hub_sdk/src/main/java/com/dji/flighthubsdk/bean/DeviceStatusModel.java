// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Dock/Drone 设备状态条目中的型号信息。 */
public final class DeviceStatusModel {

    private String deviceClass;
    private String key;
    private String name;
    public DeviceStatusModel() {
    }

/**
     * 构造 DeviceStatusModel。
     *
     * @param deviceClass 设备类别
     * @param key 键
     * @param name 名称
     */
    public DeviceStatusModel(String deviceClass, String key, String name) {
        this.deviceClass = deviceClass != null ? deviceClass : "";
        this.key = key != null ? key : "";
        this.name = name != null ? name : "";
    }

    /** 设备类别 */
    public String getDeviceClass() {
        return deviceClass;
    }

    /** 设备类别 */
    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    /** 键 */
    public String getKey() {
        return key;
    }

    /** 键 */
    public void setKey(String key) {
        this.key = key;
    }

    /** 名称 */
    public String getName() {
        return name;
    }

    /** 名称 */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceStatusModel that = (DeviceStatusModel) o;
        return Objects.equals(deviceClass, that.deviceClass)
                && Objects.equals(key, that.key)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceClass, key, name);
    }

    @Override
    public String toString() {
        return "DeviceStatusModel{deviceClass='" + deviceClass + '\'' + ", key='" + key + '\'' + ", name='" + name + '\'' + '}';
    }
}
