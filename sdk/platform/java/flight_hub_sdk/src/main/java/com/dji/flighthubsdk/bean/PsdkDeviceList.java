// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** PSDK 设备列表。 */
public final class PsdkDeviceList {

    private List<PsdkDeviceInfo> deviceInfos;
    public PsdkDeviceList() {
    }

/**
     * 构造 PsdkDeviceList。
     *
     * @param deviceInfos PSDK设备列表
     */
    public PsdkDeviceList(List<PsdkDeviceInfo> deviceInfos) {
        this.deviceInfos = deviceInfos != null ? deviceInfos : Collections.emptyList();
    }

    /** PSDK设备列表 */
    public List<PsdkDeviceInfo> getDeviceInfos() {
        return deviceInfos;
    }

    /** PSDK设备列表 */
    public void setDeviceInfos(List<PsdkDeviceInfo> deviceInfos) {
        this.deviceInfos = deviceInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkDeviceList that = (PsdkDeviceList) o;
        return Objects.equals(deviceInfos, that.deviceInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceInfos);
    }

    @Override
    public String toString() {
        return "PsdkDeviceList{deviceInfos=" + deviceInfos + '}';
    }
}
