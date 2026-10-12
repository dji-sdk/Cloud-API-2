// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 单路可用直播源（LiveInfo），由 {@code listenLiveAvailableRange} 从设备 live_capacity 解析得到。
 * 用于 {@code getLiveStream} / {@code getLiveRecord}，不表示已开流。
 */
public final class LiveInfo {

    /** 设备序列号 */
    private String deviceSn;
    /** 负载/摄像头索引 */
    private String payloadIndex;
    /** 视频流索引 */
    private String videoId;
    private List<Integer> switchableLensTypeSwigValues;
    public LiveInfo() {
    }

/**
     * 构造 LiveInfo。
     *
     * @param deviceSn 设备序列号
     * @param payloadIndex 负载索引
     * @param videoId 视频 ID
     * @param switchableLensTypeSwigValues 可切换镜头类型列表
     */
    public LiveInfo(
            String deviceSn,
            String payloadIndex,
            String videoId,
            List<Integer> switchableLensTypeSwigValues) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.videoId = videoId != null ? videoId : "";
        this.switchableLensTypeSwigValues = switchableLensTypeSwigValues != null ? switchableLensTypeSwigValues : Collections.emptyList();
    }

    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** 负载/摄像头索引 */
    public String getPayloadIndex() { return payloadIndex; }
    /** 视频流索引 */
    public String getVideoId() { return videoId; }
    /** 可切换镜头类型列表 */
    public List<Integer> getSwitchableLensTypeSwigValues() { return switchableLensTypeSwigValues; }

    /** 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** @param payloadIndex 负载/摄像头索引 */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** @param videoId 视频流索引 */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /** 可切换镜头类型列表 */
    public void setSwitchableLensTypeSwigValues(List<Integer> switchableLensTypeSwigValues) {
        this.switchableLensTypeSwigValues = switchableLensTypeSwigValues;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveInfo liveInfo = (LiveInfo) o;
        return Objects.equals(deviceSn, liveInfo.deviceSn)
                && Objects.equals(payloadIndex, liveInfo.payloadIndex)
                && Objects.equals(videoId, liveInfo.videoId)
                && Objects.equals(switchableLensTypeSwigValues, liveInfo.switchableLensTypeSwigValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, payloadIndex, videoId, switchableLensTypeSwigValues);
    }

    @Override
    public String toString() {
        return "LiveInfo{"
                + "deviceSn='" + deviceSn + '\''
                + ", payloadIndex='" + payloadIndex + '\''
                + ", videoId='" + videoId + '\''
                + ", switchableLensTypeSwigValues=" + switchableLensTypeSwigValues
                + '}';
    }
}
