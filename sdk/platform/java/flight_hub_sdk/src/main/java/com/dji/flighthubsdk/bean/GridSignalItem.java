// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 栅格信号条目。 */
public final class GridSignalItem {
    /** Uber H3网格下标，等级为 11; */
    private String h3Index;
    /** 平均sdr信号强度; */
    private int sdrAvg;
    /** 最大sdr信号强度; */
    private int sdrMax;
    /** 最小sdr信号强度; */
    private int sdrMin;
    /** 平均4g信号强度, 如果没有插入4G模块, 则无此字段; */
    private int signal4gAvg;
    /** 最大4g信号强度, 如果没有插入4G模块, 则无此字段; */
    private int signal4gMax;
    /** 最小4g信号强度, 如果没有插入4G模块, 则无此字段; */
    private int signal4gMin;
    public GridSignalItem() {
    }

/**
     * 构造 GridSignalItem。
     *
     * @param h3Index H3 网格索引
     * @param sdrAvg SDR 平均信号值
     * @param sdrMax SDR 最大信号值
     * @param sdrMin SDR 最小信号值
     * @param signal4gAvg 4G 平均信号值
     * @param signal4gMax 4G 最大信号值
     * @param signal4gMin 4G 最小信号值
     */
    public GridSignalItem(
            String h3Index,
            int sdrAvg,
            int sdrMax,
            int sdrMin,
            int signal4gAvg,
            int signal4gMax,
            int signal4gMin) {
        this.h3Index = h3Index != null ? h3Index : "";
        this.sdrAvg = sdrAvg;
        this.sdrMax = sdrMax;
        this.sdrMin = sdrMin;
        this.signal4gAvg = signal4gAvg;
        this.signal4gMax = signal4gMax;
        this.signal4gMin = signal4gMin;
    }

    /** Uber H3网格下标，等级为 11; */
    public String getH3Index() { return h3Index; }
    /** 平均sdr信号强度; */
    public int getSdrAvg() { return sdrAvg; }
    /** 最大sdr信号强度; */
    public int getSdrMax() { return sdrMax; }
    /** 最小sdr信号强度; */
    public int getSdrMin() { return sdrMin; }
    /** 平均4g信号强度, 如果没有插入4G模块, 则无此字段; */
    public int getSignal4gAvg() { return signal4gAvg; }
    /** 最大4g信号强度, 如果没有插入4G模块, 则无此字段; */
    public int getSignal4gMax() { return signal4gMax; }
    /** 最小4g信号强度, 如果没有插入4G模块, 则无此字段; */
    public int getSignal4gMin() { return signal4gMin; }

    /** @param h3Index Uber H3网格下标，等级为 11; */
    public void setH3Index(String h3Index) {
        this.h3Index = h3Index;
    }

    /** @param sdrAvg 平均sdr信号强度; */
    public void setSdrAvg(int sdrAvg) {
        this.sdrAvg = sdrAvg;
    }

    /** @param sdrMax 最大sdr信号强度; */
    public void setSdrMax(int sdrMax) {
        this.sdrMax = sdrMax;
    }

    /** @param sdrMin 最小sdr信号强度; */
    public void setSdrMin(int sdrMin) {
        this.sdrMin = sdrMin;
    }

    /** @param signal4gAvg 平均4g信号强度, 如果没有插入4G模块, 则无此字段; */
    public void setSignal4gAvg(int signal4gAvg) {
        this.signal4gAvg = signal4gAvg;
    }

    /** @param signal4gMax 最大4g信号强度, 如果没有插入4G模块, 则无此字段; */
    public void setSignal4gMax(int signal4gMax) {
        this.signal4gMax = signal4gMax;
    }

    /** @param signal4gMin 最小4g信号强度, 如果没有插入4G模块, 则无此字段; */
    public void setSignal4gMin(int signal4gMin) {
        this.signal4gMin = signal4gMin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GridSignalItem that = (GridSignalItem) o;
        return Objects.equals(h3Index, that.h3Index)
                && sdrAvg == that.sdrAvg
                && sdrMax == that.sdrMax
                && sdrMin == that.sdrMin
                && signal4gAvg == that.signal4gAvg
                && signal4gMax == that.signal4gMax
                && signal4gMin == that.signal4gMin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                h3Index,
                sdrAvg,
                sdrMax,
                sdrMin,
                signal4gAvg,
                signal4gMax,
                signal4gMin);
    }

    @Override
    public String toString() {
        return "GridSignalItem{"
                + "h3Index='" + h3Index + '\''
                + ", sdrAvg=" + sdrAvg
                + ", sdrMax=" + sdrMax
                + ", sdrMin=" + sdrMin
                + ", signal4gAvg=" + signal4gAvg
                + ", signal4gMax=" + signal4gMax
                + ", signal4gMin=" + signal4gMin
                + '}';
    }
}
