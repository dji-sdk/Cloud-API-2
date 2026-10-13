// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场图传链路。 */
public final class DockWirelessLink {

    /** int: 0,1,2 飞机上 dongle 数量; */
    private int dongleNum;
    /** int: 4g 链路连接状态, 0-断开, 1-连接; */
    private int linkState4g;
    /** int, "sdr链路状态", { 0: 断开, 1: 连接 }; */
    private int sdrLinkState;
    /** int, "链路工作模式", { 0: sdr only, 1: 4G融合 }; */
    private int linkWorkmode;
    /** int: 标识sdr信号强弱, 0~5 一共六档; */
    private int sdrQuality;
    /** int: 标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality; */
    private int quality4g;
    /** int: 标识飞机端4G信号强弱,0~5 一共六档; */
    private int uavQuality4g;
    /** int: 标识地面端4G信号强弱,0~5 一共六档; */
    private int gndQuality4g;
    /** float: sdr 频段; */
    private float sdrFreqBand;
    /** float: 4g 频段; */
    private float freqBand4g;
    public DockWirelessLink() {
    }

/**
     * 构造 DockWirelessLink。
     *
     * @param dongleNum 飞机上 dongle 数量
     * @param linkState4g 4G 链路连接状态（0:断开, 1:连接）
     * @param sdrLinkState SDR 链路状态（0:断开, 1:连接）
     * @param linkWorkmode 链路工作模式（0:SDR only, 1:4G 融合）
     * @param sdrQuality SDR 信号质量（0~5 六档）
     * @param quality4g 总体 4G 信号质量（0~5 六档）
     * @param uavQuality4g 飞机端 4G 信号质量（0~5 六档）
     * @param gndQuality4g 地面端 4G 信号质量（0~5 六档）
     * @param sdrFreqBand SDR 频段
     * @param freqBand4g 4G 频段
     */
    public DockWirelessLink(
            int dongleNum,
            int linkState4g,
            int sdrLinkState,
            int linkWorkmode,
            int sdrQuality,
            int quality4g,
            int uavQuality4g,
            int gndQuality4g,
            float sdrFreqBand,
            float freqBand4g) {
        this.dongleNum = dongleNum;
        this.linkState4g = linkState4g;
        this.sdrLinkState = sdrLinkState;
        this.linkWorkmode = linkWorkmode;
        this.sdrQuality = sdrQuality;
        this.quality4g = quality4g;
        this.uavQuality4g = uavQuality4g;
        this.gndQuality4g = gndQuality4g;
        this.sdrFreqBand = sdrFreqBand;
        this.freqBand4g = freqBand4g;
    }

    /** int: 0,1,2 飞机上 dongle 数量; */
    public int getDongleNum() {
        return dongleNum;
    }

    /** @param dongleNum int: 0,1,2 飞机上 dongle 数量; */
    public void setDongleNum(int dongleNum) {
        this.dongleNum = dongleNum;
    }

    /** int: 4g 链路连接状态, 0-断开, 1-连接; */
    public int getLinkState4g() {
        return linkState4g;
    }

    /** @param linkState4g int: 4g 链路连接状态, 0-断开, 1-连接; */
    public void setLinkState4g(int linkState4g) {
        this.linkState4g = linkState4g;
    }

    /** int, "sdr链路状态", { 0: 断开, 1: 连接 }; */
    public int getSdrLinkState() {
        return sdrLinkState;
    }

    /** @param sdrLinkState int, "sdr链路状态", { 0: 断开, 1: 连接 }; */
    public void setSdrLinkState(int sdrLinkState) {
        this.sdrLinkState = sdrLinkState;
    }

    /** int, "链路工作模式", { 0: sdr only, 1: 4G融合 }; */
    public int getLinkWorkmode() {
        return linkWorkmode;
    }

    /** @param linkWorkmode int, "链路工作模式", { 0: sdr only, 1: 4G融合 }; */
    public void setLinkWorkmode(int linkWorkmode) {
        this.linkWorkmode = linkWorkmode;
    }

    /** int: 标识sdr信号强弱, 0~5 一共六档; */
    public int getSdrQuality() {
        return sdrQuality;
    }

    /** @param sdrQuality int: 标识sdr信号强弱, 0~5 一共六档; */
    public void setSdrQuality(int sdrQuality) {
        this.sdrQuality = sdrQuality;
    }

    /** int: 标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality; */
    public int getQuality4g() {
        return quality4g;
    }

    /** @param quality4g int: 标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality; */
    public void setQuality4g(int quality4g) {
        this.quality4g = quality4g;
    }

    /** int: 标识飞机端4G信号强弱,0~5 一共六档; */
    public int getUavQuality4g() {
        return uavQuality4g;
    }

    /** @param uavQuality4g int: 标识飞机端4G信号强弱,0~5 一共六档; */
    public void setUavQuality4g(int uavQuality4g) {
        this.uavQuality4g = uavQuality4g;
    }

    /** int: 标识地面端4G信号强弱,0~5 一共六档; */
    public int getGndQuality4g() {
        return gndQuality4g;
    }

    /** @param gndQuality4g int: 标识地面端4G信号强弱,0~5 一共六档; */
    public void setGndQuality4g(int gndQuality4g) {
        this.gndQuality4g = gndQuality4g;
    }

    /** float: sdr 频段; */
    public float getSdrFreqBand() {
        return sdrFreqBand;
    }

    /** @param sdrFreqBand float: sdr 频段; */
    public void setSdrFreqBand(float sdrFreqBand) {
        this.sdrFreqBand = sdrFreqBand;
    }

    /** float: 4g 频段; */
    public float getFreqBand4g() {
        return freqBand4g;
    }

    /** @param freqBand4g float: 4g 频段; */
    public void setFreqBand4g(float freqBand4g) {
        this.freqBand4g = freqBand4g;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockWirelessLink that = (DockWirelessLink) o;
        return dongleNum == that.dongleNum
                && linkState4g == that.linkState4g
                && sdrLinkState == that.sdrLinkState
                && linkWorkmode == that.linkWorkmode
                && sdrQuality == that.sdrQuality
                && quality4g == that.quality4g
                && uavQuality4g == that.uavQuality4g
                && gndQuality4g == that.gndQuality4g
                && Float.compare(that.sdrFreqBand, sdrFreqBand) == 0
                && Float.compare(that.freqBand4g, freqBand4g) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dongleNum,
                linkState4g,
                sdrLinkState,
                linkWorkmode,
                sdrQuality,
                quality4g,
                uavQuality4g,
                gndQuality4g,
                sdrFreqBand,
                freqBand4g);
    }

    @Override
    public String toString() {
        return "DockWirelessLink{"
                + "dongleNum=" + dongleNum
                + ", linkState4g=" + linkState4g
                + ", sdrLinkState=" + sdrLinkState
                + ", linkWorkmode=" + linkWorkmode
                + ", sdrQuality=" + sdrQuality
                + ", quality4g=" + quality4g
                + ", uavQuality4g=" + uavQuality4g
                + ", gndQuality4g=" + gndQuality4g
                + ", sdrFreqBand=" + sdrFreqBand
                + ", freqBand4g=" + freqBand4g
                + '}';
    }
}
