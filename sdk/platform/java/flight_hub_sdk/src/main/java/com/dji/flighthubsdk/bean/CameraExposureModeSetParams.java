// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机曝光模式参数 */
public class CameraExposureModeSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 曝光模式（0=自动 1=快门优先 2=光圈优先 3=手动） */
    private int exposureMode = 0;

    public CameraExposureModeSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 曝光模式（0=自动 1=快门优先 2=光圈优先 3=手动） */
    public int getExposureMode() { return exposureMode; }
    /** @param exposureMode 曝光模式（0=自动 1=快门优先 2=光圈优先 3=手动） */
    public void setExposureMode(int exposureMode) { this.exposureMode = exposureMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraExposureModeSetParams that = (CameraExposureModeSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && exposureMode == that.exposureMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, exposureMode);
    }

    @Override
    public String toString() {
        return "CameraExposureModeSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", exposureMode=" + exposureMode + '}';
    }
}
