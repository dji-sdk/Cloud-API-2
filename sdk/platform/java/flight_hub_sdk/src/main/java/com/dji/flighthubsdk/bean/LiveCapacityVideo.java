// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 机场直播能力-画面条目（live_capacity）。EA340 消息推送补全（Phase D）。 */
public final class LiveCapacityVideo {

    private String videoIndex;
    private String videoType;
    private List<String> switchableVideoTypes;

    public LiveCapacityVideo() {
        this.switchableVideoTypes = Collections.emptyList();
    }

    /**
     * 构造 LiveCapacityVideo。
     *
     * @param videoIndex 画面索引
     * @param videoType 画面类型
     * @param switchableVideoTypes 可切换的画面类型列表
     */
    public LiveCapacityVideo(
            String videoIndex,
            String videoType,
            List<String> switchableVideoTypes) {
        this.videoIndex = videoIndex != null ? videoIndex : "";
        this.videoType = videoType != null ? videoType : "";
        this.switchableVideoTypes = switchableVideoTypes != null ? switchableVideoTypes : Collections.emptyList();
    }

    /** 画面索引 */
    public String getVideoIndex() {
        return videoIndex;
    }

    /** 画面索引 */
    public void setVideoIndex(String videoIndex) {
        this.videoIndex = videoIndex;
    }

    /** 画面类型 */
    public String getVideoType() {
        return videoType;
    }

    /** 画面类型 */
    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    /** 可切换的画面类型列表 */
    public List<String> getSwitchableVideoTypes() {
        return switchableVideoTypes;
    }

    /** 可切换的画面类型列表 */
    public void setSwitchableVideoTypes(List<String> switchableVideoTypes) {
        this.switchableVideoTypes = switchableVideoTypes != null ? switchableVideoTypes : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveCapacityVideo that = (LiveCapacityVideo) o;
        return Objects.equals(videoIndex, that.videoIndex)
                && Objects.equals(videoType, that.videoType)
                && Objects.equals(switchableVideoTypes, that.switchableVideoTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoIndex, videoType, switchableVideoTypes);
    }

    @Override
    public String toString() {
        return "LiveCapacityVideo{videoIndex='" + videoIndex + '\''
                + ", videoType='" + videoType + '\''
                + ", switchableVideoTypes=" + switchableVideoTypes + '}';
    }
}
