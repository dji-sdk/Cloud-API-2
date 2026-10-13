// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置红外测温区域参数 */
public class IrMeteringAreaSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 测温区域左上角X坐标（归一化0~1） */
    private double x = 0.0;
    /** 测温区域左上角Y坐标（归一化0~1） */
    private double y = 0.0;
    /** 测温区域宽度（归一化0~1） */
    private double width = 0.0;
    /** 测温区域高度（归一化0~1） */
    private double height = 0.0;

    public IrMeteringAreaSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 测温区域左上角X坐标（归一化0~1） */
    public double getX() { return x; }
    /** @param x 测温区域左上角X坐标（归一化0~1） */
    public void setX(double x) { this.x = x; }
    /** 测温区域左上角Y坐标（归一化0~1） */
    public double getY() { return y; }
    /** @param y 测温区域左上角Y坐标（归一化0~1） */
    public void setY(double y) { this.y = y; }
    /** 测温区域宽度（归一化0~1） */
    public double getWidth() { return width; }
    /** @param width 测温区域宽度（归一化0~1） */
    public void setWidth(double width) { this.width = width; }
    /** 测温区域高度（归一化0~1） */
    public double getHeight() { return height; }
    /** @param height 测温区域高度（归一化0~1） */
    public void setHeight(double height) { this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IrMeteringAreaSetParams that = (IrMeteringAreaSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Double.compare(x, that.x) == 0
                && Double.compare(y, that.y) == 0
                && Double.compare(width, that.width) == 0
                && Double.compare(height, that.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, x, y, width, height);
    }

    @Override
    public String toString() {
        return "IrMeteringAreaSetParams{payloadIndex='" + payloadIndex + '\'' + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + '}';
    }
}
