// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 红外测光点。 */
public final class CameraIrMeteringPoint {

    /** double, 温度; */
    private double temperature;
    /** double, x坐标(0.0~1.0); */
    private double x;
    /** double, y坐标(0.0~1.0); */
    private double y;
    public CameraIrMeteringPoint() {
    }

/**
     * 构造 CameraIrMeteringPoint。
     *
     * @param temperature 红外测温点温度
     * @param x 红外测温点 X 坐标
     * @param y 红外测温点 Y 坐标
     */
    public CameraIrMeteringPoint(double temperature, double x, double y) {
        this.temperature = temperature;
        this.x = x;
        this.y = y;
    }

    /** double, 温度; */
    public double getTemperature() {
        return temperature;
    }

    /** @param temperature double, 温度; */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /** double, x坐标(0.0~1.0); */
    public double getX() {
        return x;
    }

    /** @param x double, x坐标(0.0~1.0); */
    public void setX(double x) {
        this.x = x;
    }

    /** double, y坐标(0.0~1.0); */
    public double getY() {
        return y;
    }

    /** @param y double, y坐标(0.0~1.0); */
    public void setY(double y) {
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
        CameraIrMeteringPoint that = (CameraIrMeteringPoint) o;
        return Double.compare(that.temperature, temperature) == 0
                && Double.compare(that.x, x) == 0
                && Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, x, y);
    }

    @Override
    public String toString() {
        return "CameraIrMeteringPoint{temperature=" + temperature + ", x=" + x + ", y=" + y + '}';
    }
}
