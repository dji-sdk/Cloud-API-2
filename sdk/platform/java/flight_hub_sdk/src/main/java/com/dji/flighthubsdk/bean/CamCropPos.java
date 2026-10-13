// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 相机裁剪坐标（EIS SEI 角点）。 */
public final class CamCropPos {

    /** x 坐标 */
    private int x;
    /** y 坐标 */
    private int y;
    public CamCropPos() {
    }

/**
     * 构造 CamCropPos。
     *
     * @param x X 坐标
     * @param y Y 坐标
     */
    public CamCropPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** x 坐标 */
    public int getX() {
        return x;
    }

    /** @param x x 坐标 */
    public void setX(int x) {
        this.x = x;
    }

    /** y 坐标 */
    public int getY() {
        return y;
    }

    /** @param y y 坐标 */
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CamCropPos that = (CamCropPos) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "CamCropPos{x=" + x + ", y=" + y + '}';
    }
}
