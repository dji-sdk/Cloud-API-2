// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 机场直播状态条目（live_status）。EA340 消息推送补全（Phase D）。 */
public final class LiveStatusItem {

    private String videoId;
    private String videoType;
    private long videoQuality;
    private long status;
    private long errorStatus;

    public LiveStatusItem() {
    }

    /**
     * 构造 LiveStatusItem。
     *
     * @param videoId 直播画面 ID
     * @param videoType 直播画面类型
     * @param videoQuality 直播画质
     * @param status 直播状态
     * @param errorStatus 直播错误状态
     */
    public LiveStatusItem(
            String videoId,
            String videoType,
            long videoQuality,
            long status,
            long errorStatus) {
        this.videoId = videoId != null ? videoId : "";
        this.videoType = videoType != null ? videoType : "";
        this.videoQuality = videoQuality;
        this.status = status;
        this.errorStatus = errorStatus;
    }

    /** 直播画面 ID */
    public String getVideoId() {
        return videoId;
    }

    /** 直播画面 ID */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /** 直播画面类型 */
    public String getVideoType() {
        return videoType;
    }

    /** 直播画面类型 */
    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    /** 直播画质 */
    public long getVideoQuality() {
        return videoQuality;
    }

    /** 直播画质 */
    public void setVideoQuality(long videoQuality) {
        this.videoQuality = videoQuality;
    }

    /** 直播状态 */
    public long getStatus() {
        return status;
    }

    /** 直播状态 */
    public void setStatus(long status) {
        this.status = status;
    }

    /** 直播错误状态 */
    public long getErrorStatus() {
        return errorStatus;
    }

    /** 直播错误状态 */
    public void setErrorStatus(long errorStatus) {
        this.errorStatus = errorStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveStatusItem that = (LiveStatusItem) o;
        return videoQuality == that.videoQuality
                && status == that.status
                && errorStatus == that.errorStatus
                && Objects.equals(videoId, that.videoId)
                && Objects.equals(videoType, that.videoType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId, videoType, videoQuality, status, errorStatus);
    }

    @Override
    public String toString() {
        return "LiveStatusItem{videoId='" + videoId + '\''
                + ", videoType='" + videoType + '\''
                + ", videoQuality=" + videoQuality
                + ", status=" + status
                + ", errorStatus=" + errorStatus + '}';
    }
}
