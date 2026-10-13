// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 直播镜头信息。
 */
public final class LiveVideoInfo {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private String videoIndex;
    private int videoTypeSwig;
    private List<Integer> switchableVideoTypeSwigValues;
    public LiveVideoInfo() {
    }

/**
     * 构造 LiveVideoInfo。
     *
     * @param payloadIndex 负载索引
     * @param videoIndex 视频索引
     * @param videoTypeSwig 视频类型
     * @param switchableVideoTypeSwigValues 可切换视频类型列表
     */
    public LiveVideoInfo(
            String payloadIndex,
            String videoIndex,
            int videoTypeSwig,
            List<Integer> switchableVideoTypeSwigValues) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.videoIndex = videoIndex != null ? videoIndex : "";
        this.videoTypeSwig = videoTypeSwig;
        this.switchableVideoTypeSwigValues = switchableVideoTypeSwigValues != null ? switchableVideoTypeSwigValues : Collections.emptyList();
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** 视频索引 */
    public String getVideoIndex() { return videoIndex; }
    /** 视频类型 */
    public int getVideoTypeSwig() { return videoTypeSwig; }
    /** 可切换视频类型列表 */
    public List<Integer> getSwitchableVideoTypeSwigValues() { return switchableVideoTypeSwigValues; }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 视频索引 */
    public void setVideoIndex(String videoIndex) {
        this.videoIndex = videoIndex;
    }

    /** 视频类型 */
    public void setVideoTypeSwig(int videoTypeSwig) {
        this.videoTypeSwig = videoTypeSwig;
    }

    /** 可切换视频类型列表 */
    public void setSwitchableVideoTypeSwigValues(List<Integer> switchableVideoTypeSwigValues) {
        this.switchableVideoTypeSwigValues = switchableVideoTypeSwigValues;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveVideoInfo that = (LiveVideoInfo) o;
        return videoTypeSwig == that.videoTypeSwig
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(videoIndex, that.videoIndex)
                && Objects.equals(switchableVideoTypeSwigValues, that.switchableVideoTypeSwigValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, videoIndex, videoTypeSwig, switchableVideoTypeSwigValues);
    }

    @Override
    public String toString() {
        return "LiveVideoInfo{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", videoIndex='" + videoIndex + '\''
                + ", videoTypeSwig=" + videoTypeSwig
                + ", switchableVideoTypeSwigValues=" + switchableVideoTypeSwigValues
                + '}';
    }
}
