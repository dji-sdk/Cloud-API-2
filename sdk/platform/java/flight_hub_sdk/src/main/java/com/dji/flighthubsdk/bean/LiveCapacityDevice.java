// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 机场直播能力-设备条目（live_capacity）。EA340 消息推送补全（Phase D）。 */
public final class LiveCapacityDevice {

    private String sn;
    private long availableVideoNumber;
    private long coexistVideoNumberMax;
    private List<LiveCapacityCamera> cameraList;

    public LiveCapacityDevice() {
        this.cameraList = Collections.emptyList();
    }

    /**
     * 构造 LiveCapacityDevice。
     *
     * @param sn 设备序列号
     * @param availableVideoNumber 可用直播路数
     * @param coexistVideoNumberMax 最大并存直播路数
     * @param cameraList 相机能力列表
     */
    public LiveCapacityDevice(
            String sn,
            long availableVideoNumber,
            long coexistVideoNumberMax,
            List<LiveCapacityCamera> cameraList) {
        this.sn = sn != null ? sn : "";
        this.availableVideoNumber = availableVideoNumber;
        this.coexistVideoNumberMax = coexistVideoNumberMax;
        this.cameraList = cameraList != null ? cameraList : Collections.emptyList();
    }

    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
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

    /** 相机能力列表 */
    public List<LiveCapacityCamera> getCameraList() {
        return cameraList;
    }

    /** 相机能力列表 */
    public void setCameraList(List<LiveCapacityCamera> cameraList) {
        this.cameraList = cameraList != null ? cameraList : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveCapacityDevice that = (LiveCapacityDevice) o;
        return availableVideoNumber == that.availableVideoNumber
                && coexistVideoNumberMax == that.coexistVideoNumberMax
                && Objects.equals(sn, that.sn)
                && Objects.equals(cameraList, that.cameraList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, availableVideoNumber, coexistVideoNumberMax, cameraList);
    }

    @Override
    public String toString() {
        return "LiveCapacityDevice{sn='" + sn + '\''
                + ", availableVideoNumber=" + availableVideoNumber
                + ", coexistVideoNumberMax=" + coexistVideoNumberMax
                + ", cameraList=" + cameraList + '}';
    }
}
