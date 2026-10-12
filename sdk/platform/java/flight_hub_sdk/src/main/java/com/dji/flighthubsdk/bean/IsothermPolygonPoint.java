// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 等温线多边形顶点坐标 */
public class IsothermPolygonPoint {
    /** 顶点横坐标（取值范围：0~10000，精度4） */
    private double x = 0.0;
    /** 顶点纵坐标（取值范围：0~10000，精度4） */
    private double y = 0.0;

    public IsothermPolygonPoint() {
    }

    /** 顶点横坐标（取值范围：0~10000，精度4） */
    public double getX() { return x; }
    /** @param x 顶点横坐标（取值范围：0~10000，精度4） */
    public void setX(double x) { this.x = x; }
    /** 顶点纵坐标（取值范围：0~10000，精度4） */
    public double getY() { return y; }
    /** @param y 顶点纵坐标（取值范围：0~10000，精度4） */
    public void setY(double y) { this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IsothermPolygonPoint that = (IsothermPolygonPoint) o;
        return Double.compare(x, that.x) == 0
                && Double.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "IsothermPolygonPoint{x=" + x + ", y=" + y + '}';
    }
}
