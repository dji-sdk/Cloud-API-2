// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机曝光值参数 */
public class CameraExposureSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 曝光补偿值 */
    private int exposureValue = 0;

    public CameraExposureSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 曝光补偿值 */
    public int getExposureValue() { return exposureValue; }
    /** @param exposureValue 曝光补偿值 */
    public void setExposureValue(int exposureValue) { this.exposureValue = exposureValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraExposureSetParams that = (CameraExposureSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && exposureValue == that.exposureValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, exposureValue);
    }

    @Override
    public String toString() {
        return "CameraExposureSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", exposureValue=" + exposureValue + '}';
    }
}
