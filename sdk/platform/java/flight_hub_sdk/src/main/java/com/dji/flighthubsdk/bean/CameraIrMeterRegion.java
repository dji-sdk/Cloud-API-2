// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 红外测温区域（像素坐标；DRC {@code ir_pip_state.ir_meter_region}）。 */
public final class CameraIrMeterRegion {
    private int topLeftX;
    private int topLeftY;
    private int bottomRightX;
    private int bottomRightY;

    public CameraIrMeterRegion() {}
    public CameraIrMeterRegion(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
    }
    public int getTopLeftX() { return topLeftX; }
    public void setTopLeftX(int v) { this.topLeftX = v; }
    public int getTopLeftY() { return topLeftY; }
    public void setTopLeftY(int v) { this.topLeftY = v; }
    public int getBottomRightX() { return bottomRightX; }
    public void setBottomRightX(int v) { this.bottomRightX = v; }
    public int getBottomRightY() { return bottomRightY; }
    public void setBottomRightY(int v) { this.bottomRightY = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraIrMeterRegion)) return false;
        CameraIrMeterRegion that = (CameraIrMeterRegion) o;
        return topLeftX == that.topLeftX && topLeftY == that.topLeftY
                && bottomRightX == that.bottomRightX && bottomRightY == that.bottomRightY;
    }
    @Override public int hashCode() { return Objects.hash(topLeftX, topLeftY, bottomRightX, bottomRightY); }
    @Override public String toString() {
        return "CameraIrMeterRegion{tl=(" + topLeftX + "," + topLeftY + "), br=(" + bottomRightX + "," + bottomRightY + ")}";
    }
}
