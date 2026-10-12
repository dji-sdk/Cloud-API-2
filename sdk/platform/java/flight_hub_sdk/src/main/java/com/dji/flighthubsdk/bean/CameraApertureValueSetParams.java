// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机光圈值参数 */
public class CameraApertureValueSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 光圈值（如 280 = F2.8） */
    private int apertureValue = 0;

    public CameraApertureValueSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 光圈值（如 280 = F2.8） */
    public int getApertureValue() { return apertureValue; }
    /** @param apertureValue 光圈值（如 280 = F2.8） */
    public void setApertureValue(int apertureValue) { this.apertureValue = apertureValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraApertureValueSetParams that = (CameraApertureValueSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && apertureValue == that.apertureValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, apertureValue);
    }

    @Override
    public String toString() {
        return "CameraApertureValueSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", apertureValue=" + apertureValue + '}';
    }
}
