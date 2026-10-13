// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 机场子设备列表。 */
public final class DockSubDeviceInfoList {

    private List<DockSubDeviceInfo> subDeviceInfos;
    public DockSubDeviceInfoList() {
    }

/**
     * 构造 DockSubDeviceInfoList。
     *
     * @param subDeviceInfos 子设备信息
     */
    public DockSubDeviceInfoList(List<DockSubDeviceInfo> subDeviceInfos) {
        this.subDeviceInfos = subDeviceInfos != null ? subDeviceInfos : Collections.emptyList();
    }

    /** 子设备信息 */
    public List<DockSubDeviceInfo> getSubDeviceInfos() {
        return subDeviceInfos;
    }

    /** 子设备信息 */
    public void setSubDeviceInfos(List<DockSubDeviceInfo> subDeviceInfos) {
        this.subDeviceInfos = subDeviceInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockSubDeviceInfoList that = (DockSubDeviceInfoList) o;
        return Objects.equals(subDeviceInfos, that.subDeviceInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subDeviceInfos);
    }

    @Override
    public String toString() {
        return "DockSubDeviceInfoList{subDeviceInfos=" + subDeviceInfos + '}';
    }
}
