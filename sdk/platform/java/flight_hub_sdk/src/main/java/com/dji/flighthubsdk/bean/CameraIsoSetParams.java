// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机ISO参数 */
public class CameraIsoSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** ISO 值（如 100 / 200 / 400） */
    private int isoValue = 0;

    public CameraIsoSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** ISO 值（如 100 / 200 / 400） */
    public int getIsoValue() { return isoValue; }
    /** @param isoValue ISO 值（如 100 / 200 / 400） */
    public void setIsoValue(int isoValue) { this.isoValue = isoValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraIsoSetParams that = (CameraIsoSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && isoValue == that.isoValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, isoValue);
    }

    @Override
    public String toString() {
        return "CameraIsoSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", isoValue=" + isoValue + '}';
    }
}
