// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 媒体存储列表（DRC {@code drc_camera_state_push.media_storage}）。 */
public final class CameraMediaStorage {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private List<String> photoStorageSettings;
    private List<String> videoStorageSettings;

    public CameraMediaStorage() {}
    public CameraMediaStorage(String payloadIndex, List<String> photoStorageSettings, List<String> videoStorageSettings) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.photoStorageSettings = photoStorageSettings;
        this.videoStorageSettings = videoStorageSettings;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public List<String> getPhotoStorageSettings() { return photoStorageSettings; }
    public void setPhotoStorageSettings(List<String> v) { this.photoStorageSettings = v; }
    public List<String> getVideoStorageSettings() { return videoStorageSettings; }
    public void setVideoStorageSettings(List<String> v) { this.videoStorageSettings = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraMediaStorage)) return false;
        CameraMediaStorage that = (CameraMediaStorage) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(photoStorageSettings, that.photoStorageSettings)
                && Objects.equals(videoStorageSettings, that.videoStorageSettings);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, photoStorageSettings, videoStorageSettings); }
    @Override public String toString() {
        return "CameraMediaStorage{payloadIndex='" + payloadIndex + "', photoStorageSettings="
                + photoStorageSettings + ", videoStorageSettings=" + videoStorageSettings + '}';
    }
}
