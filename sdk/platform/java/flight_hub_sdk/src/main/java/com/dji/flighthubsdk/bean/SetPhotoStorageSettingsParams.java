// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 拍照存储设置参数 */
public class SetPhotoStorageSettingsParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    private List<String> photoStorageSettings = new ArrayList<>();

    public SetPhotoStorageSettingsParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
    }

    /** 照片存储设置 */
    public List<String> getPhotoStorageSettings() {
        return photoStorageSettings;
    }

    /** @param photoStorageSettings 照片存储设置 */
    public void setPhotoStorageSettings(List<String> photoStorageSettings) {
        this.photoStorageSettings = photoStorageSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetPhotoStorageSettingsParams that = (SetPhotoStorageSettingsParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(photoStorageSettings, that.photoStorageSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, photoStorageSettings);
    }

    @Override
    public String toString() {
        return "SetPhotoStorageSettingsParams{payloadIndex='" + payloadIndex + '\'' + ", photoStorageSettings=" + photoStorageSettings + '}';
    }
}
