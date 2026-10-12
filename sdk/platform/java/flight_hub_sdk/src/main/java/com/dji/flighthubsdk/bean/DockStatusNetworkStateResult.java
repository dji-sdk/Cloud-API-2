// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 机场设备状态 — 网络状态 */
public final class DockStatusNetworkStateResult {

    private int quality;
    private int rate;
    private int type;
    public DockStatusNetworkStateResult() {
    }

/**
     * 构造 DockStatusNetworkStateResult。
     *
     * @param quality 质量
     * @param rate 速率
     * @param type 类型
     */
    public DockStatusNetworkStateResult(int quality, int rate, int type) {
        this.quality = quality;
        this.rate = rate;
        this.type = type;
    }

    /** 质量 */
    public int getQuality() {
        return quality;
    }

    /** 质量 */
    public void setQuality(int quality) {
        this.quality = quality;
    }

    /** 速率 */
    public int getRate() {
        return rate;
    }

    /** 速率 */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /** 类型 */
    public int getType() {
        return type;
    }

    /** 类型 */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockStatusNetworkStateResult that = (DockStatusNetworkStateResult) o;
        return quality == that.quality
                && rate == that.rate
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quality, rate, type);
    }

    @Override
    public String toString() {
        return "DockStatusNetworkStateResult{quality=" + quality + ", rate=" + rate + ", type=" + type + '}';
    }
}
