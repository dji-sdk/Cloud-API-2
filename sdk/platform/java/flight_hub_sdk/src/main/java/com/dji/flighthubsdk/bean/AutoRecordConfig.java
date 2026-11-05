// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** PAAS 自动录制配置（项目级配置，无设备/session 上下文；配置不存在时服务端返回全零值，非错误） */
public class AutoRecordConfig {
    private long id = 0;
    private String projectId = "";
    private long createdAt = 0;
    private long updatedAt = 0;
    private int autoCleaningTime = 0;
    private boolean disabledAutoClean = false;
    private boolean disableDockRecording = false;
    private boolean disableDroneRecording = false;
    private boolean disableBaseStationRecording = false;
    private boolean disableRtmpRecording = false;
    private List<AutoRecordItem> recordingItems = new ArrayList<>();

    public AutoRecordConfig() {
    }

    /** 配置 ID */
    public long getId() { return id; }
    /** @param id 配置 ID */
    public void setId(long id) { this.id = id; }
    /** 项目 ID */
    public String getProjectId() { return projectId; }
    /** @param projectId 项目 ID */
    public void setProjectId(String projectId) { this.projectId = projectId != null ? projectId : ""; }
    /** 创建时间(unix 秒) */
    public long getCreatedAt() { return createdAt; }
    /** @param createdAt 创建时间(unix 秒) */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    /** 更新时间(unix 秒) */
    public long getUpdatedAt() { return updatedAt; }
    /** @param updatedAt 更新时间(unix 秒) */
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    /** 自动清理保留天数 */
    public int getAutoCleaningTime() { return autoCleaningTime; }
    /** @param autoCleaningTime 自动清理保留天数 */
    public void setAutoCleaningTime(int autoCleaningTime) { this.autoCleaningTime = autoCleaningTime; }
    /** 是否禁用自动清理 */
    public boolean isDisabledAutoClean() { return disabledAutoClean; }
    public void setDisabledAutoClean(boolean disabledAutoClean) { this.disabledAutoClean = disabledAutoClean; }
    /** 是否禁用机场录制 */
    public boolean isDisableDockRecording() { return disableDockRecording; }
    public void setDisableDockRecording(boolean disableDockRecording) { this.disableDockRecording = disableDockRecording; }
    /** 是否禁用无人机录制 */
    public boolean isDisableDroneRecording() { return disableDroneRecording; }
    public void setDisableDroneRecording(boolean disableDroneRecording) { this.disableDroneRecording = disableDroneRecording; }
    /** 是否禁用基站录制 */
    public boolean isDisableBaseStationRecording() { return disableBaseStationRecording; }
    public void setDisableBaseStationRecording(boolean disableBaseStationRecording) { this.disableBaseStationRecording = disableBaseStationRecording; }
    /** 是否禁用 RTMP 推流录制 */
    public boolean isDisableRtmpRecording() { return disableRtmpRecording; }
    public void setDisableRtmpRecording(boolean disableRtmpRecording) { this.disableRtmpRecording = disableRtmpRecording; }
    /** 录制目标列表 */
    public List<AutoRecordItem> getRecordingItems() { return recordingItems; }
    /** @param recordingItems 录制目标列表 */
    public void setRecordingItems(List<AutoRecordItem> recordingItems) {
        this.recordingItems = recordingItems != null ? recordingItems : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AutoRecordConfig that = (AutoRecordConfig) o;
        return id == that.id
                && createdAt == that.createdAt
                && updatedAt == that.updatedAt
                && autoCleaningTime == that.autoCleaningTime
                && disabledAutoClean == that.disabledAutoClean
                && disableDockRecording == that.disableDockRecording
                && disableDroneRecording == that.disableDroneRecording
                && disableBaseStationRecording == that.disableBaseStationRecording
                && disableRtmpRecording == that.disableRtmpRecording
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(recordingItems, that.recordingItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, createdAt, updatedAt, autoCleaningTime, disabledAutoClean,
                disableDockRecording, disableDroneRecording, disableBaseStationRecording,
                disableRtmpRecording, recordingItems);
    }

    @Override
    public String toString() {
        return "AutoRecordConfig{id=" + id + ", projectId='" + projectId + '\''
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + ", autoCleaningTime=" + autoCleaningTime + ", disabledAutoClean=" + disabledAutoClean
                + ", disableDockRecording=" + disableDockRecording
                + ", disableDroneRecording=" + disableDroneRecording
                + ", disableBaseStationRecording=" + disableBaseStationRecording
                + ", disableRtmpRecording=" + disableRtmpRecording
                + ", recordingItems=" + recordingItems + '}';
    }
}
