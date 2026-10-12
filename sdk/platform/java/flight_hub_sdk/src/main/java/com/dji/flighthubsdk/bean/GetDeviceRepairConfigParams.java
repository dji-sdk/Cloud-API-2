// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 获取设备报修配置请求参数 */
public class GetDeviceRepairConfigParams {
    /** 设备 SN; */
    private List<String> deviceSns = new ArrayList<>();

    public GetDeviceRepairConfigParams() {
    }


    /** 设备 SN; */
    public List<String> getDeviceSns() {
        return deviceSns;
    }

    /** @param deviceSns 设备 SN; */
    public void setDeviceSns(List<String> deviceSns) {
        this.deviceSns = deviceSns != null ? deviceSns : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetDeviceRepairConfigParams that = (GetDeviceRepairConfigParams) o;
        return Objects.equals(deviceSns, that.deviceSns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSns);
    }

    @Override
    public String toString() {
        return "GetDeviceRepairConfigParams{deviceSns=" + deviceSns + '}';
    }
}
