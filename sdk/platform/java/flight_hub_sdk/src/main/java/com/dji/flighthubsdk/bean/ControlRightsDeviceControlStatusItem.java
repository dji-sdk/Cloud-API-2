// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备控制权限状态条目。 */
public final class ControlRightsDeviceControlStatusItem {

    /** flight/payload, 飞机还是负载 */
    private String type;
    /** 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index) */
    private String key;
    /** RC/MC/CCC */
    private String owner;
    private double version;
    public ControlRightsDeviceControlStatusItem() {
    }

/**
     * 构造 ControlRightsDeviceControlStatusItem。
     *
     * @param type 类型
     * @param key 键
     * @param owner owner
     * @param version 版本
     */
    public ControlRightsDeviceControlStatusItem(String type, String key, String owner, double version) {
        this.type = type != null ? type : "";
        this.key = key != null ? key : "";
        this.owner = owner != null ? owner : "";
        this.version = version;
    }

    /** flight/payload, 飞机还是负载 */
    public String getType() {
        return type;
    }

    /** @param type flight/payload, 飞机还是负载 */
    public void setType(String type) {
        this.type = type;
    }

    /** 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index) */
    public String getKey() {
        return key;
    }

    /** @param key 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index) */
    public void setKey(String key) {
        this.key = key;
    }

    /** RC/MC/CCC */
    public String getOwner() {
        return owner;
    }

    /** @param owner RC/MC/CCC */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /** 版本 */
    public double getVersion() {
        return version;
    }

    /** 版本 */
    public void setVersion(double version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ControlRightsDeviceControlStatusItem that = (ControlRightsDeviceControlStatusItem) o;
        return Objects.equals(type, that.type)
                && Objects.equals(key, that.key)
                && Objects.equals(owner, that.owner)
                && Double.compare(that.version, version) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key, owner, version);
    }

    @Override
    public String toString() {
        return "ControlRightsDeviceControlStatusItem{"
                + "type='" + type + '\''
                + ", key='" + key + '\''
                + ", owner='" + owner + '\''
                + ", version=" + version
                + '}';
    }
}
