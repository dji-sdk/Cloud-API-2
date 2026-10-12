// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.CameraLensType;

import java.util.Objects;
/** 框选放大参数 */
public class ZoomAtTargetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    private CameraLensType cameraLensType = CameraLensType.UNKNOWN;
    /** 目标框左上角坐标x，0～1 */
    private double x;
    /** 目标框左上角坐标y，0～1 */
    private double y;
    /** 目标框宽度，0～1 */
    private double width;
    /** 目标框高度，0～1 */
    private double height;

    public ZoomAtTargetParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    public CameraLensType getCameraLensType() { return cameraLensType; }
    /** @param cameraLensType 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`） */
    public void setCameraLensType(CameraLensType cameraLensType) { this.cameraLensType = cameraLensType; }
    /** 目标框左上角坐标x，0～1 */
    public double getX() { return x; }
    /** @param x 目标框左上角坐标x，0～1 */
    public void setX(double x) { this.x = x; }
    /** 目标框左上角坐标y，0～1 */
    public double getY() { return y; }
    /** @param y 目标框左上角坐标y，0～1 */
    public void setY(double y) { this.y = y; }
    /** 目标框宽度，0～1 */
    public double getWidth() { return width; }
    /** @param width 目标框宽度，0～1 */
    public void setWidth(double width) { this.width = width; }
    /** 目标框高度，0～1 */
    public double getHeight() { return height; }
    /** @param height 目标框高度，0～1 */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZoomAtTargetParams that = (ZoomAtTargetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(cameraLensType, that.cameraLensType)
                && Double.compare(that.x, x) == 0
                && Double.compare(that.y, y) == 0
                && Double.compare(that.width, width) == 0
                && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                payloadIndex,
                cameraLensType,
                x,
                y,
                width,
                height);
    }

    @Override
    public String toString() {
        return "ZoomAtTargetParams{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", cameraLensType=" + cameraLensType
                + ", x=" + x
                + ", y=" + y
                + ", width=" + width
                + ", height=" + height
                + '}';
    }
}
