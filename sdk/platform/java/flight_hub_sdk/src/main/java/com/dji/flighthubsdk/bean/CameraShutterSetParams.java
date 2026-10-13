// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机快门速度参数 */
public class CameraShutterSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 快门值（如 500 = 1/500s） */
    private int shutterValue = 0;

    public CameraShutterSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 快门值（如 500 = 1/500s） */
    public int getShutterValue() { return shutterValue; }
    /** @param shutterValue 快门值（如 500 = 1/500s） */
    public void setShutterValue(int shutterValue) { this.shutterValue = shutterValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraShutterSetParams that = (CameraShutterSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && shutterValue == that.shutterValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, shutterValue);
    }

    @Override
    public String toString() {
        return "CameraShutterSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", shutterValue=" + shutterValue + '}';
    }
}
