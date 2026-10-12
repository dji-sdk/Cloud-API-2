// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** SEI 2D 坐标点 */
public class SeiPoint2D {
    /** x 坐标，单位：像素 */
    private int x;
    /** y 坐标，单位：像素 */
    private int y;

    /**
     * 构造 SeiPoint2D。
     */
    public SeiPoint2D() {
    }

/**
     * 构造 SeiPoint2D。
     *
     * @param x x 坐标，单位：像素
     * @param y y 坐标，单位：像素
     */
    public SeiPoint2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** x 坐标，单位：像素 */
    public int getX() { return x; }
    /** @param x x 坐标，单位：像素 */
    public void setX(int x) { this.x = x; }
    /** y 坐标，单位：像素 */
    public int getY() { return y; }
    /** @param y y 坐标，单位：像素 */
    public void setY(int y) { this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeiPoint2D that = (SeiPoint2D) o;
        return x == that.x
                && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "SeiPoint2D{x=" + x + ", y=" + y + '}';
    }
}
