// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 等温线渲染多边形顶点（DRC 上行像素坐标，int）。 */
public final class CameraIsothermPolygonPoint {
    private int x;
    private int y;

    public CameraIsothermPolygonPoint() {}
    public CameraIsothermPolygonPoint(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public void setX(int v) { this.x = v; }
    public int getY() { return y; }
    public void setY(int v) { this.y = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraIsothermPolygonPoint)) return false;
        CameraIsothermPolygonPoint that = (CameraIsothermPolygonPoint) o;
        return x == that.x && y == that.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
    @Override public String toString() { return "CameraIsothermPolygonPoint{x=" + x + ", y=" + y + '}'; }
}
