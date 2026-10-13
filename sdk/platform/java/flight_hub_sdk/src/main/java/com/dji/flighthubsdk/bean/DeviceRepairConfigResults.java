// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取设备报修配置返回结果 */
public final class DeviceRepairConfigResults {
    private List<DeviceRepairConfig> data;
    public DeviceRepairConfigResults() {
    }

/**
     * 构造 DeviceRepairConfigResults。
     *
     * @param data 数据
     */
    public DeviceRepairConfigResults(List<DeviceRepairConfig> data) {
        this.data = data != null ? data : Collections.emptyList();
    }

    /** 数据 */
    public List<DeviceRepairConfig> getData() {
        return data;
    }

    /** 数据 */
    public void setData(List<DeviceRepairConfig> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceRepairConfigResults that = (DeviceRepairConfigResults) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "DeviceRepairConfigResults{data=" + data + '}';
    }
}
