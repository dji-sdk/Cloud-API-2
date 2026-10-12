// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

import com.dji.flighthubsdk.value.RecordState;
/**
 * 直播录制状态推送。
 */
public final class LiveRecordStateInfo {

    /** 业务标识(bid) */
    private String bid;
    /** 设备 SN */
    private String sn;
    /** 摄像头标识 */
    private String camera;
    /** 视频流标识 */
    private String video;
    /** 录制状态，取值语义同 `flight_hub_sdk::RecordState`（见 `value.h`） */
    private RecordState state;
    /** 创建者 */
    private String createdBy;
    /** 录制时长，单位：秒 */
    private int duration;
    /** 创建时间戳 */
    private long createTime;
    public LiveRecordStateInfo() {
    }

/**
     * 构造 LiveRecordStateInfo。
     *
     * @param bid 业务 ID
     * @param sn 设备序列号
     * @param camera 摄像头标识
     * @param video 视频流标识
     * @param state 录制状态
     * @param createdBy 创建者
     * @param duration 录制时长，单位：秒
     * @param createTime 创建时间
     */
    public LiveRecordStateInfo(
            String bid,
            String sn,
            String camera,
            String video,
            int state,
            String createdBy,
            int duration,
            long createTime) {
        this.bid = bid != null ? bid : "";
        this.sn = sn != null ? sn : "";
        this.camera = camera != null ? camera : "";
        this.video = video != null ? video : "";
        this.state = RecordState.fromValue(state);
        this.createdBy = createdBy != null ? createdBy : "";
        this.duration = duration;
        this.createTime = createTime;
    }

    /** 业务标识(bid) */
    public String getBid() { return bid; }
    /** 设备 SN */
    public String getSn() { return sn; }
    /** 摄像头标识 */
    public String getCamera() { return camera; }
    /** 视频流标识 */
    public String getVideo() { return video; }
    /** 录制状态，取值语义同 `flight_hub_sdk::RecordState`（见 `value.h`） */
    public RecordState getState() { return state; }
    /** 创建者 */
    public String getCreatedBy() { return createdBy; }
    /** 录制时长，单位：秒 */
    public int getDuration() { return duration; }
    /** 创建时间戳 */
    public long getCreateTime() { return createTime; }

    /** @param bid 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** @param sn 设备 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 摄像头标识 */
    public void setCamera(String camera) {
        this.camera = camera;
    }

    /** 视频流标识 */
    public void setVideo(String video) {
        this.video = video;
    }

    /** @param state 录制状态，取值语义同 `flight_hub_sdk::RecordState`（见 `value.h`） */
    public void setState(RecordState state) {
        this.state = state;
    }

    /** 创建者 */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** 录制时长，单位：秒 */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /** @param createTime 创建时间戳 */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveRecordStateInfo that = (LiveRecordStateInfo) o;
        return state == that.state
                && duration == that.duration
                && createTime == that.createTime
                && Objects.equals(bid, that.bid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(camera, that.camera)
                && Objects.equals(video, that.video)
                && Objects.equals(createdBy, that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, sn, camera, video, state, createdBy, duration, createTime);
    }

    @Override
    public String toString() {
        return "LiveRecordStateInfo{"
                + "bid='" + bid + '\''
                + ", sn='" + sn + '\''
                + ", camera='" + camera + '\''
                + ", video='" + video + '\''
                + ", state=" + state
                + ", createdBy='" + createdBy + '\''
                + ", duration=" + duration
                + ", createTime=" + createTime
                + '}';
    }
}
