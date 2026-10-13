// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 定时拍照设置参数 */
public class IntervalTakePhotoSetParam {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    private double interval;

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    public double getInterval() { return interval; }
    public void setInterval(double interval) { this.interval = interval; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalTakePhotoSetParam that = (IntervalTakePhotoSetParam) o;
        return Double.compare(that.interval, interval) == 0 && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() { return Objects.hash(payloadIndex, interval); }
}
