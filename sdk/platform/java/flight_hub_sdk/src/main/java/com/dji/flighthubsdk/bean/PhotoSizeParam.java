// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 照片尺寸设置参数 */
public class PhotoSizeParam {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    private int photoSize;
    private String cameraType = "";

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    public int getPhotoSize() { return photoSize; }
    public void setPhotoSize(int photoSize) { this.photoSize = photoSize; }
    public String getCameraType() { return cameraType; }
    public void setCameraType(String cameraType) { this.cameraType = cameraType != null ? cameraType : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoSizeParam that = (PhotoSizeParam) o;
        return photoSize == that.photoSize && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(cameraType, that.cameraType);
    }

    @Override
    public int hashCode() { return Objects.hash(payloadIndex, photoSize, cameraType); }
}
