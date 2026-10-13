// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 单条直播录制记录。对应 GetLiveRecordsBySNResponse.RecordTask。
 */
public final class LiveRecordItem {

    /** 业务标识(bid)（录制任务 ID） */
    private String bid;
    /** 设备 SN */
    private String sn;
    /** 录制状态，取值语义同 `RecordState`（见 `value.h`）：0 成功/已结束，1 录制中，2 初始化中，3 失败 */
    private int state;
    /** 摄像头标识 */
    private String camera;
    /** 创建者 */
    private String createdBy;
    /** 录制时长（秒） */
    private long duration;

    public LiveRecordItem() {
    }

    /**
     * @param bid 业务 ID
     * @param sn 设备 SN
     * @param state 录制状态
     * @param camera 摄像头标识
     * @param createdBy 创建者
     * @param duration 录制时长（秒）
     */
    public LiveRecordItem(
            String bid,
            String sn,
            int state,
            String camera,
            String createdBy,
            long duration) {
        this.bid = bid != null ? bid : "";
        this.sn = sn != null ? sn : "";
        this.state = state;
        this.camera = camera != null ? camera : "";
        this.createdBy = createdBy != null ? createdBy : "";
        this.duration = duration;
    }

    /** 业务标识(bid)（录制任务 ID） */
    public String getBid() { return bid; }
    /** 设备 SN */
    public String getSn() { return sn; }
    /** 录制状态，取值语义同 `RecordState`（见 `value.h`）：0 成功/已结束，1 录制中，2 初始化中，3 失败 */
    public int getState() { return state; }
    /** 摄像头标识 */
    public String getCamera() { return camera; }
    /** 创建者 */
    public String getCreatedBy() { return createdBy; }
    /** 录制时长（秒） */
    public long getDuration() { return duration; }

    /** @param bid 业务标识(bid)（录制任务 ID） */
    public void setBid(String bid) { this.bid = bid; }
    /** @param sn 设备 SN */
    public void setSn(String sn) { this.sn = sn; }
    /** @param state 录制状态，取值语义同 `RecordState`（见 `value.h`）：0 成功/已结束，1 录制中，2 初始化中，3 失败 */
    public void setState(int state) { this.state = state; }
    /** @param camera 摄像头标识 */
    public void setCamera(String camera) { this.camera = camera; }
    /** @param createdBy 创建者 */
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    /** @param duration 录制时长（秒） */
    public void setDuration(long duration) { this.duration = duration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiveRecordItem that = (LiveRecordItem) o;
        return state == that.state
                && duration == that.duration
                && Objects.equals(bid, that.bid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(camera, that.camera)
                && Objects.equals(createdBy, that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, sn, state, camera, createdBy, duration);
    }

    @Override
    public String toString() {
        return "LiveRecordItem{"
                + "bid='" + bid + '\''
                + ", sn='" + sn + '\''
                + ", state=" + state
                + ", camera='" + camera + '\''
                + ", createdBy='" + createdBy + '\''
                + ", duration=" + duration
                + '}';
    }
}
