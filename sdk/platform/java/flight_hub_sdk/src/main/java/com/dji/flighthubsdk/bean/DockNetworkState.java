// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场网络状态。 */
public final class DockNetworkState {

    /** int, "网络类型", { 1: 4G, 2: 以太网 };  默认无效值: -1; */
    private int type;
    /** int, "网络质量(当网络类型为4G时才具有网络质量信息)", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1; */
    private int quality;
    /** double, "网络速率(单位KB/秒)"; 默认值: 0.0; */
    private double rate;
    public DockNetworkState() {
    }

/**
     * 构造 DockNetworkState。
     *
     * @param type 类型
     * @param quality 质量
     * @param rate 速率
     */
    public DockNetworkState(int type, int quality, double rate) {
        this.type = type;
        this.quality = quality;
        this.rate = rate;
    }

    /** int, "网络类型", { 1: 4G, 2: 以太网 };  默认无效值: -1; */
    public int getType() {
        return type;
    }

    /** @param type int, "网络类型", { 1: 4G, 2: 以太网 };  默认无效值: -1; */
    public void setType(int type) {
        this.type = type;
    }

    /** int, "网络质量(当网络类型为4G时才具有网络质量信息)", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1; */
    public int getQuality() {
        return quality;
    }

    /** @param quality int, "网络质量(当网络类型为4G时才具有网络质量信息)", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1; */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    /** double, "网络速率(单位KB/秒)"; 默认值: 0.0; */
    public double getRate() {
        return rate;
    }

    /** @param rate double, "网络速率(单位KB/秒)"; 默认值: 0.0; */
    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockNetworkState that = (DockNetworkState) o;
        return type == that.type && quality == that.quality && Double.compare(that.rate, rate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, quality, rate);
    }

    @Override
    public String toString() {
        return "DockNetworkState{type=" + type + ", quality=" + quality + ", rate=" + rate + '}';
    }
}
