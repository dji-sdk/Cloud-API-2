// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 相机点对焦操作参数 */
public class CameraPointFocusActionParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 相机类型（镜头类型） */
    private int cameraLensType = 0;
    /** 对焦点 X 坐标（0.0~1.0） */
    private double x = 0.0;
    /** 对焦点 Y 坐标（0.0~1.0） */
    private double y = 0.0;

    public CameraPointFocusActionParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 相机类型（镜头类型） */
    public int getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 相机类型（镜头类型） */
    public void setCameraLensType(int cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 对焦点 X 坐标（0.0~1.0） */
    public double getX() { return x; }
    /** @param x 对焦点 X 坐标（0.0~1.0） */
    public void setX(double x) { this.x = x; }
    /** 对焦点 Y 坐标（0.0~1.0） */
    public double getY() { return y; }
    /** @param y 对焦点 Y 坐标（0.0~1.0） */
    public void setY(double y) { this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraPointFocusActionParams that = (CameraPointFocusActionParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && cameraLensType == that.cameraLensType
                && Double.compare(x, that.x) == 0
                && Double.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, cameraLensType, x, y);
    }

    @Override
    public String toString() {
        return "CameraPointFocusActionParams{payloadIndex='" + payloadIndex + '\'' + ", cameraLensType=" + cameraLensType + ", x=" + x + ", y=" + y + '}';
    }
}
