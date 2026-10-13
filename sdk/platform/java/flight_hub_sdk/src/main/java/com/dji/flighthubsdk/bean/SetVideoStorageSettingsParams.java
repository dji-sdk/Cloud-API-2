// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 录像存储设置参数 */
public class SetVideoStorageSettingsParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    private List<String> videoStorageSettings = new ArrayList<>();

    public SetVideoStorageSettingsParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
    }

    /** 录像存储设置 */
    public List<String> getVideoStorageSettings() {
        return videoStorageSettings;
    }

    /** @param videoStorageSettings 录像存储设置 */
    public void setVideoStorageSettings(List<String> videoStorageSettings) {
        this.videoStorageSettings = videoStorageSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetVideoStorageSettingsParams that = (SetVideoStorageSettingsParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(videoStorageSettings, that.videoStorageSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, videoStorageSettings);
    }

    @Override
    public String toString() {
        return "SetVideoStorageSettingsParams{payloadIndex='" + payloadIndex + '\'' + ", videoStorageSettings=" + videoStorageSettings + '}';
    }
}
