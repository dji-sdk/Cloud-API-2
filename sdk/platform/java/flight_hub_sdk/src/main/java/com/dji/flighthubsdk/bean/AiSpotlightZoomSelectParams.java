// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** AI跟踪目标缩放框选择参数 */
public class AiSpotlightZoomSelectParams {
    /** 目标框中心 X 坐标（0.0~1.0） */
    private double centerX = 0.0;
    /** 目标框中心 Y 坐标（0.0~1.0） */
    private double centerY = 0.0;
    /** 目标框宽度（0.0~1.0） */
    private double width = 0.0;
    /** 目标框高度（0.0~1.0） */
    private double height = 0.0;

    public AiSpotlightZoomSelectParams() {
    }

    /** 目标框中心 X 坐标（0.0~1.0） */
    public double getCenterX() { return centerX; }
    /** @param centerX 目标框中心 X 坐标（0.0~1.0） */
    public void setCenterX(double centerX) { this.centerX = centerX; }
    /** 目标框中心 Y 坐标（0.0~1.0） */
    public double getCenterY() { return centerY; }
    /** @param centerY 目标框中心 Y 坐标（0.0~1.0） */
    public void setCenterY(double centerY) { this.centerY = centerY; }
    /** 目标框宽度（0.0~1.0） */
    public double getWidth() { return width; }
    /** @param width 目标框宽度（0.0~1.0） */
    public void setWidth(double width) { this.width = width; }
    /** 目标框高度（0.0~1.0） */
    public double getHeight() { return height; }
    /** @param height 目标框高度（0.0~1.0） */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiSpotlightZoomSelectParams that = (AiSpotlightZoomSelectParams) o;
        return Double.compare(centerX, that.centerX) == 0
                && Double.compare(centerY, that.centerY) == 0
                && Double.compare(width, that.width) == 0
                && Double.compare(height, that.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerX, centerY, width, height);
    }

    @Override
    public String toString() {
        return "AiSpotlightZoomSelectParams{centerX=" + centerX + ", centerY=" + centerY + ", width=" + width + ", height=" + height + '}';
    }
}
