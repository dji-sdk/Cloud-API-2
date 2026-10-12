// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 云控制权限状态条目。 */
public final class ControlRightsCloudControlStatusItem {

    /** flight/payload, 飞机还是负载 */
    private String type;
    /** 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index) */
    private String key;
    /** 用户 id */
    private String userId;
    private boolean locked;
    private double version;
    public ControlRightsCloudControlStatusItem() {
    }

/**
     * 构造 ControlRightsCloudControlStatusItem。
     *
     * @param type 类型（flight/payload）
     * @param key 控制权 key
     * @param userId 用户 ID
     * @param locked 是否锁定
     * @param version 版本号
     */
    public ControlRightsCloudControlStatusItem(
            String type, String key, String userId, boolean locked, double version) {
        this.type = type != null ? type : "";
        this.key = key != null ? key : "";
        this.userId = userId != null ? userId : "";
        this.locked = locked;
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

    /** 用户 id */
    public String getUserId() {
        return userId;
    }

    /** @param userId 用户 id */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** 是否锁定 */
    public boolean isLocked() {
        return locked;
    }

    /** 是否锁定 */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /** 版本号 */
    public double getVersion() {
        return version;
    }

    /** 版本号 */
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
        ControlRightsCloudControlStatusItem that = (ControlRightsCloudControlStatusItem) o;
        return Objects.equals(type, that.type)
                && Objects.equals(key, that.key)
                && Objects.equals(userId, that.userId)
                && locked == that.locked
                && Double.compare(that.version, version) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type,
                key,
                userId,
                locked,
                version);
    }

    @Override
    public String toString() {
        return "ControlRightsCloudControlStatusItem{"
                + "type='" + type + '\''
                + ", key='" + key + '\''
                + ", userId='" + userId + '\''
                + ", locked=" + locked
                + ", version=" + version
                + '}';
    }
}
