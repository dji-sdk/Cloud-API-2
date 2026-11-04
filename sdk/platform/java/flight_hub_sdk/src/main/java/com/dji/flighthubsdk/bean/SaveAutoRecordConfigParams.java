// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** PAAS 保存自动录制配置的入参 */
public class SaveAutoRecordConfigParams {
    private int autoCleaningTime = 0;
    private boolean disableDockRecording = false;
    private boolean disableDroneRecording = false;
    private boolean disableBaseStationRecording = false;
    private boolean disableRtmpRecording = false;
    private List<AutoRecordItem> recordingItems = new ArrayList<>();

    public SaveAutoRecordConfigParams() {
    }

    /** 自动清理保留天数 */
    public int getAutoCleaningTime() { return autoCleaningTime; }
    /** @param autoCleaningTime 自动清理保留天数 */
    public void setAutoCleaningTime(int autoCleaningTime) { this.autoCleaningTime = autoCleaningTime; }
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
        SaveAutoRecordConfigParams that = (SaveAutoRecordConfigParams) o;
        return autoCleaningTime == that.autoCleaningTime
                && disableDockRecording == that.disableDockRecording
                && disableDroneRecording == that.disableDroneRecording
                && disableBaseStationRecording == that.disableBaseStationRecording
                && disableRtmpRecording == that.disableRtmpRecording
                && Objects.equals(recordingItems, that.recordingItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoCleaningTime, disableDockRecording, disableDroneRecording,
                disableBaseStationRecording, disableRtmpRecording, recordingItems);
    }

    @Override
    public String toString() {
        return "SaveAutoRecordConfigParams{autoCleaningTime=" + autoCleaningTime
                + ", disableDockRecording=" + disableDockRecording
                + ", disableDroneRecording=" + disableDroneRecording
                + ", disableBaseStationRecording=" + disableBaseStationRecording
                + ", disableRtmpRecording=" + disableRtmpRecording
                + ", recordingItems=" + recordingItems + '}';
    }
}
