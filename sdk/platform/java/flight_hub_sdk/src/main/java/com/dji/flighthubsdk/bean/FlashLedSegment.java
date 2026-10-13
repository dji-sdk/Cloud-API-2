// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** HTTP flash-configs 模板片段（on/off/interval 单位 ms） */
public class FlashLedSegment {
    /** 0关 1红 2绿 3蓝 4黄 5白 */
    private int color = 0;
    /** 闪烁次数 1..99 */
    private int count = 1;
    /** 亮灯时长 ms */
    private int on = 0;
    /** 灭灯时长 ms */
    private int off = 0;
    /** 组间停顿 ms */
    private int interval = 0;

    public FlashLedSegment() {
    }

    /** 0关 1红 2绿 3蓝 4黄 5白 */
    public int getColor() { return color; }
    /** @param color 0关 1红 2绿 3蓝 4黄 5白 */
    public void setColor(int color) { this.color = color; }
    /** 闪烁次数 1..99 */
    public int getCount() { return count; }
    /** @param count 闪烁次数 1..99 */
    public void setCount(int count) { this.count = count; }
    /** 亮灯时长 ms */
    public int getOn() { return on; }
    /** @param on 亮灯时长 ms */
    public void setOn(int on) { this.on = on; }
    /** 灭灯时长 ms */
    public int getOff() { return off; }
    /** @param off 灭灯时长 ms */
    public void setOff(int off) { this.off = off; }
    /** 组间停顿 ms */
    public int getInterval() { return interval; }
    /** @param interval 组间停顿 ms */
    public void setInterval(int interval) { this.interval = interval; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlashLedSegment that = (FlashLedSegment) o;
        return color == that.color
                && count == that.count
                && on == that.on
                && off == that.off
                && interval == that.interval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, count, on, off, interval);
    }

    @Override
    public String toString() {
        return "FlashLedSegment{color=" + color + ", count=" + count + ", on=" + on
                + ", off=" + off + ", interval=" + interval + '}';
    }
}
