// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 离线地图模型信息数据。 */
public final class OfflineMapsModelInfoData {
    private String uuid;
    public OfflineMapsModelInfoData() {
    }

/**
     * 构造 OfflineMapsModelInfoData。
     *
     * @param uuid UUID
     */
    public OfflineMapsModelInfoData(String uuid) {
        this.uuid = uuid != null ? uuid : "";
    }

    /** UUID */
    public String getUuid() {
        return uuid;
    }

    /** UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OfflineMapsModelInfoData that = (OfflineMapsModelInfoData) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "OfflineMapsModelInfoData{uuid='" + uuid + '\'' + '}';
    }
}
