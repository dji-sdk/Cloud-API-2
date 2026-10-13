// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置红外测温点参数 */
public class IrMeteringPointSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 测温点X坐标（归一化0~1） */
    private double x = 0.0;
    /** 测温点Y坐标（归一化0~1） */
    private double y = 0.0;

    public IrMeteringPointSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 测温点X坐标（归一化0~1） */
    public double getX() { return x; }
    /** @param x 测温点X坐标（归一化0~1） */
    public void setX(double x) { this.x = x; }
    /** 测温点Y坐标（归一化0~1） */
    public double getY() { return y; }
    /** @param y 测温点Y坐标（归一化0~1） */
    public void setY(double y) { this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IrMeteringPointSetParams that = (IrMeteringPointSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Double.compare(x, that.x) == 0
                && Double.compare(y, that.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, x, y);
    }

    @Override
    public String toString() {
        return "IrMeteringPointSetParams{payloadIndex='" + payloadIndex + '\'' + ", x=" + x + ", y=" + y + '}';
    }
}
