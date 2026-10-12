// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 修改设备名称请求参数，包含待修改的设备列表 */
public class ModifyDeviceNameParam {
    private List<ModifyDeviceNameItem> deviceList = new ArrayList<>();

    public ModifyDeviceNameParam() {
    }


    /** 待修改的设备列表 */
    public List<ModifyDeviceNameItem> getDeviceList() {
        return deviceList;
    }

    /** @param deviceList 待修改的设备列表 */
    public void setDeviceList(List<ModifyDeviceNameItem> deviceList) {
        this.deviceList = deviceList != null ? deviceList : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModifyDeviceNameParam that = (ModifyDeviceNameParam) o;
        return Objects.equals(deviceList, that.deviceList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceList);
    }

    @Override
    public String toString() {
        return "ModifyDeviceNameParam{deviceList=" + deviceList + '}';
    }
}
