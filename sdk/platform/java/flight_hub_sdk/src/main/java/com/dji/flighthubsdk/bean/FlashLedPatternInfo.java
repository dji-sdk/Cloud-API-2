// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 爆闪灯 pattern 信息 */
public class FlashLedPatternInfo {
    /** 颜色 0:关灯 1:红 2:绿 3:蓝 4:黄 5:白 */
    private int color = 0;
    /** 周期(最小控灯周期50ms，输入1为50ms，2为100ms) */
    private int period = 0;
    /** 亮度(0-100百分比) */
    private int brightness = 0;

    public FlashLedPatternInfo() {
    }

    /** 颜色 0:关灯 1:红 2:绿 3:蓝 4:黄 5:白 */
    public int getColor() { return color; }
    /** @param color 颜色 0:关灯 1:红 2:绿 3:蓝 4:黄 5:白 */
    public void setColor(int color) { this.color = color; }
    /** 周期(最小控灯周期50ms，输入1为50ms，2为100ms) */
    public int getPeriod() { return period; }
    /** @param period 周期(最小控灯周期50ms，输入1为50ms，2为100ms) */
    public void setPeriod(int period) { this.period = period; }
    /** 亮度(0-100百分比) */
    public int getBrightness() { return brightness; }
    /** @param brightness 亮度(0-100百分比) */
    public void setBrightness(int brightness) { this.brightness = brightness; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlashLedPatternInfo that = (FlashLedPatternInfo) o;
        return color == that.color && period == that.period && brightness == that.brightness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, period, brightness);
    }

    @Override
    public String toString() {
        return "FlashLedPatternInfo{color=" + color + ", period=" + period + ", brightness=" + brightness + '}';
    }
}
