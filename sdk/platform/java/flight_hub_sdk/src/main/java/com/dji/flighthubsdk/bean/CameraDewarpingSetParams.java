// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置相机去畸变状态参数 */
public class CameraDewarpingSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 畸变矫正状态（0=关 1=开） */
    private int dewarpingState = 0;

    public CameraDewarpingSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 畸变矫正状态（0=关 1=开） */
    public int getDewarpingState() { return dewarpingState; }
    /** @param dewarpingState 畸变矫正状态（0=关 1=开） */
    public void setDewarpingState(int dewarpingState) { this.dewarpingState = dewarpingState; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraDewarpingSetParams that = (CameraDewarpingSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && dewarpingState == that.dewarpingState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, dewarpingState);
    }

    @Override
    public String toString() {
        return "CameraDewarpingSetParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", dewarpingState=" + dewarpingState + '}';
    }
}
