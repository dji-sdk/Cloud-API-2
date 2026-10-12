// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 机场直播能力推送（live_capacity）。EA340 消息推送补全（Phase D）。 */
public final class DockLiveCapacity {

    private long availableVideoNumber;
    private long coexistVideoNumberMax;
    private List<LiveCapacityDevice> deviceList;

    public DockLiveCapacity() {
        this.deviceList = Collections.emptyList();
    }

    /**
     * 构造 DockLiveCapacity。
     *
     * @param availableVideoNumber 可用直播路数
     * @param coexistVideoNumberMax 最大并存直播路数
     * @param deviceList 设备直播能力列表
     */
    public DockLiveCapacity(
            long availableVideoNumber,
            long coexistVideoNumberMax,
            List<LiveCapacityDevice> deviceList) {
        this.availableVideoNumber = availableVideoNumber;
        this.coexistVideoNumberMax = coexistVideoNumberMax;
        this.deviceList = deviceList != null ? deviceList : Collections.emptyList();
    }

    /** 可用直播路数 */
    public long getAvailableVideoNumber() {
        return availableVideoNumber;
    }

    /** 可用直播路数 */
    public void setAvailableVideoNumber(long availableVideoNumber) {
        this.availableVideoNumber = availableVideoNumber;
    }

    /** 最大并存直播路数 */
    public long getCoexistVideoNumberMax() {
        return coexistVideoNumberMax;
    }

    /** 最大并存直播路数 */
    public void setCoexistVideoNumberMax(long coexistVideoNumberMax) {
        this.coexistVideoNumberMax = coexistVideoNumberMax;
    }

    /** 设备直播能力列表 */
    public List<LiveCapacityDevice> getDeviceList() {
        return deviceList;
    }

    /** 设备直播能力列表 */
    public void setDeviceList(List<LiveCapacityDevice> deviceList) {
        this.deviceList = deviceList != null ? deviceList : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockLiveCapacity that = (DockLiveCapacity) o;
        return availableVideoNumber == that.availableVideoNumber
                && coexistVideoNumberMax == that.coexistVideoNumberMax
                && Objects.equals(deviceList, that.deviceList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(availableVideoNumber, coexistVideoNumberMax, deviceList);
    }

    @Override
    public String toString() {
        return "DockLiveCapacity{availableVideoNumber=" + availableVideoNumber
                + ", coexistVideoNumberMax=" + coexistVideoNumberMax
                + ", deviceList=" + deviceList + '}';
    }
}
