// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 红外测温区域信息（DRC {@code ir_thermometry.ir_metering_area}）。
 * <p>坐标/尺寸均为归一化 {@code 0.0~1.0}，含区域内最高/最低温点嵌套。</p>
 */
public final class CameraIrMeteringArea {

    /** 区域平均温度（°C） */
    private double averTemperature;
    /** 区域高度（归一化 0.0~1.0） */
    private double height;
    /** 区域宽度（归一化 0.0~1.0） */
    private double width;
    /** 区域左上角 x（归一化 0.0~1.0） */
    private double x;
    /** 区域左上角 y（归一化 0.0~1.0） */
    private double y;
    /** 区域内最高温点 */
    private CameraIrMeteringPoint maxTemperaturePoint;
    /** 区域内最低温点 */
    private CameraIrMeteringPoint minTemperaturePoint;

    public CameraIrMeteringArea() {}

    public CameraIrMeteringArea(double averTemperature,
                                double height,
                                double width,
                                double x,
                                double y,
                                CameraIrMeteringPoint maxTemperaturePoint,
                                CameraIrMeteringPoint minTemperaturePoint) {
        this.averTemperature = averTemperature;
        this.height = height;
        this.width = width;
        this.x = x;
        this.y = y;
        this.maxTemperaturePoint = maxTemperaturePoint;
        this.minTemperaturePoint = minTemperaturePoint;
    }

    /** 区域平均温度（°C） */
    public double getAverTemperature() { return averTemperature; }
    /** @param averTemperature 区域平均温度（°C） */
    public void setAverTemperature(double v) { this.averTemperature = v; }

    /** 区域高度（归一化 0.0~1.0） */
    public double getHeight() { return height; }
    /** @param height 区域高度（归一化 0.0~1.0） */
    public void setHeight(double v) { this.height = v; }

    /** 区域宽度（归一化 0.0~1.0） */
    public double getWidth() { return width; }
    /** @param width 区域宽度（归一化 0.0~1.0） */
    public void setWidth(double v) { this.width = v; }

    /** 区域左上角 x（归一化 0.0~1.0） */
    public double getX() { return x; }
    /** @param x 区域左上角 x（归一化 0.0~1.0） */
    public void setX(double v) { this.x = v; }

    /** 区域左上角 y（归一化 0.0~1.0） */
    public double getY() { return y; }
    /** @param y 区域左上角 y（归一化 0.0~1.0） */
    public void setY(double v) { this.y = v; }

    /** 区域内最高温点 */
    public CameraIrMeteringPoint getMaxTemperaturePoint() { return maxTemperaturePoint; }
    /** @param maxTemperaturePoint 区域内最高温点 */
    public void setMaxTemperaturePoint(CameraIrMeteringPoint v) { this.maxTemperaturePoint = v; }

    /** 区域内最低温点 */
    public CameraIrMeteringPoint getMinTemperaturePoint() { return minTemperaturePoint; }
    /** @param minTemperaturePoint 区域内最低温点 */
    public void setMinTemperaturePoint(CameraIrMeteringPoint v) { this.minTemperaturePoint = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraIrMeteringArea that = (CameraIrMeteringArea) o;
        return Double.compare(that.averTemperature, averTemperature) == 0
                && Double.compare(that.height, height) == 0
                && Double.compare(that.width, width) == 0
                && Double.compare(that.x, x) == 0
                && Double.compare(that.y, y) == 0
                && Objects.equals(maxTemperaturePoint, that.maxTemperaturePoint)
                && Objects.equals(minTemperaturePoint, that.minTemperaturePoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(averTemperature, height, width, x, y,
                maxTemperaturePoint, minTemperaturePoint);
    }

    @Override
    public String toString() {
        return "CameraIrMeteringArea{"
                + "averTemperature=" + averTemperature
                + ", height=" + height
                + ", width=" + width
                + ", x=" + x
                + ", y=" + y
                + ", maxTemperaturePoint=" + maxTemperaturePoint
                + ", minTemperaturePoint=" + minTemperaturePoint
                + '}';
    }
}
