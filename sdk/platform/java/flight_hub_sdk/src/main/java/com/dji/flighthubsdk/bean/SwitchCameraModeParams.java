// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.CameraMode;

import java.util.Objects;
/** 切换相机模式参数 */
public class SwitchCameraModeParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`） */
    private CameraMode cameraMode = CameraMode.UNKNOWN;

    public SwitchCameraModeParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`） */
    public CameraMode getCameraMode() { return cameraMode; }
    /** @param cameraMode 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`） */
    public void setCameraMode(CameraMode cameraMode) { this.cameraMode = cameraMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SwitchCameraModeParams that = (SwitchCameraModeParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(cameraMode, that.cameraMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraMode);
    }

    @Override
    public String toString() {
        return "SwitchCameraModeParams{payloadIndex='" + payloadIndex + '\'' + ", cameraMode=" + cameraMode + '}';
    }
}
