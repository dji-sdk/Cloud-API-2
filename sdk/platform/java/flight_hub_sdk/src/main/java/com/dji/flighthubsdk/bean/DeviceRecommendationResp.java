// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.ArrayList;

import java.util.Objects;
/** 设备推荐响应结果。 */
public final class DeviceRecommendationResp {
    /** 推荐设备列表 */
    private List<RecommendedDevice> devices;
    public DeviceRecommendationResp() {
    }

/**
     * 构造 DeviceRecommendationResp。
     *
     * @param devices 设备列表
     */
    public DeviceRecommendationResp(List<RecommendedDevice> devices) {
        this.devices = devices != null ? devices : new ArrayList<>();
    }

    /** 推荐设备列表 */
    public List<RecommendedDevice> getDevices() {
        return devices;
    }

    /** @param devices 推荐设备列表 */
    public void setDevices(List<RecommendedDevice> devices) {
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
        DeviceRecommendationResp that = (DeviceRecommendationResp) o;
        return Objects.equals(devices, that.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devices);
    }

    @Override
    public String toString() {
        return "DeviceRecommendationResp{devices=" + devices + '}';
    }
}
