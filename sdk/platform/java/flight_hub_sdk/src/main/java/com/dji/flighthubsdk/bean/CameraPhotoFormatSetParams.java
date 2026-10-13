// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机照片格式参数 */
public class CameraPhotoFormatSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 照片格式（1=JPEG 16=DLT664） */
    private int photoFormat = 0;

    public CameraPhotoFormatSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 照片格式（1=JPEG 16=DLT664） */
    public int getPhotoFormat() { return photoFormat; }
    /** @param photoFormat 照片格式（1=JPEG 16=DLT664） */
    public void setPhotoFormat(int photoFormat) { this.photoFormat = photoFormat; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraPhotoFormatSetParams that = (CameraPhotoFormatSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && photoFormat == that.photoFormat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, photoFormat);
    }

    @Override
    public String toString() {
        return "CameraPhotoFormatSetParams{payloadIndex='" + payloadIndex + '\'' + ", photoFormat=" + photoFormat + '}';
    }
}
