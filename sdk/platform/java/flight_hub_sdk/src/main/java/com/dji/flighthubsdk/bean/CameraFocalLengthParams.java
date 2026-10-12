// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.CameraLensType;

import java.util.Objects;
/** 设置变焦焦距参数 */
public class CameraFocalLengthParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    private CameraLensType cameraType = CameraLensType.UNKNOWN;
    /** 变焦倍数，可见光是2-200; */
    private int zoomFactor = 0;

    public CameraFocalLengthParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    public CameraLensType getCameraType() { return cameraType; }
    /** @param cameraType 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    public void setCameraType(CameraLensType cameraType) { this.cameraType = cameraType; }
    /** 变焦倍数，可见光是2-200; */
    public int getZoomFactor() { return zoomFactor; }
    /** @param zoomFactor 变焦倍数，可见光是2-200; */
    public void setZoomFactor(int zoomFactor) { this.zoomFactor = zoomFactor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraFocalLengthParams that = (CameraFocalLengthParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(cameraType, that.cameraType)
                && zoomFactor == that.zoomFactor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraType, zoomFactor);
    }

    @Override
    public String toString() {
        return "CameraFocalLengthParams{payloadIndex='" + payloadIndex + '\'' + ", cameraType=" + cameraType + ", zoomFactor=" + zoomFactor + '}';
    }
}
