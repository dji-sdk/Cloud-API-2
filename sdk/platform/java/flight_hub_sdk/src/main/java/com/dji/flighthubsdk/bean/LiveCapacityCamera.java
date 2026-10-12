// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 机场直播能力-相机条目（live_capacity）。EA340 消息推送补全（Phase D）。 */
public final class LiveCapacityCamera {

    private String cameraIndex;
    private long availableVideoNumber;
    private long coexistVideoNumberMax;
    private List<LiveCapacityVideo> videoList;

    public LiveCapacityCamera() {
        this.videoList = Collections.emptyList();
    }

    /**
     * 构造 LiveCapacityCamera。
     *
     * @param cameraIndex 相机索引
     * @param availableVideoNumber 可用直播路数
     * @param coexistVideoNumberMax 最大并存直播路数
     * @param videoList 画面能力列表
     */
    public LiveCapacityCamera(
            String cameraIndex,
            long availableVideoNumber,
            long coexistVideoNumberMax,
            List<LiveCapacityVideo> videoList) {
        this.cameraIndex = cameraIndex != null ? cameraIndex : "";
        this.availableVideoNumber = availableVideoNumber;
        this.coexistVideoNumberMax = coexistVideoNumberMax;
        this.videoList = videoList != null ? videoList : Collections.emptyList();
    }

    /** 相机索引 */
    public String getCameraIndex() {
        return cameraIndex;
    }

    /** 相机索引 */
    public void setCameraIndex(String cameraIndex) {
        this.cameraIndex = cameraIndex;
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

    /** 画面能力列表 */
    public List<LiveCapacityVideo> getVideoList() {
        return videoList;
    }

    /** 画面能力列表 */
    public void setVideoList(List<LiveCapacityVideo> videoList) {
        this.videoList = videoList != null ? videoList : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveCapacityCamera that = (LiveCapacityCamera) o;
        return availableVideoNumber == that.availableVideoNumber
                && coexistVideoNumberMax == that.coexistVideoNumberMax
                && Objects.equals(cameraIndex, that.cameraIndex)
                && Objects.equals(videoList, that.videoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cameraIndex, availableVideoNumber, coexistVideoNumberMax, videoList);
    }

    @Override
    public String toString() {
        return "LiveCapacityCamera{cameraIndex='" + cameraIndex + '\''
                + ", availableVideoNumber=" + availableVideoNumber
                + ", coexistVideoNumberMax=" + coexistVideoNumberMax
                + ", videoList=" + videoList + '}';
    }
}
