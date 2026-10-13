// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机对焦模式参数 */
public class CameraFocusModeSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 对焦模式（0=MF 1=AFC 2=AFS） */
    private int focusMode = 0;

    public CameraFocusModeSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 对焦模式（0=MF 1=AFC 2=AFS） */
    public int getFocusMode() { return focusMode; }
    /** @param focusMode 对焦模式（0=MF 1=AFC 2=AFS） */
    public void setFocusMode(int focusMode) { this.focusMode = focusMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraFocusModeSetParams that = (CameraFocusModeSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && focusMode == that.focusMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, focusMode);
    }

    @Override
    public String toString() {
        return "CameraFocusModeSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", focusMode=" + focusMode + '}';
    }
}
