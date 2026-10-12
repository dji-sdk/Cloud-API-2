// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 设置热成像当前调色板参数 */
public class ThermalCurrentPaletteSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 调色板样式。枚举：0=白热(WHITE_HOT), 1=黑热(BLACK_HOT), 2=红热(RED_HOT), 3=绿热(GREEN_HOT), 4=融合(FUSION), 5=彩虹(RAINBOW), 6=热铁1(IRONBOW1), 7=热铁2(IRONBOW2), 8=冰火(ICE_FIRE), 9=复古(SEPIA), 10=辉光(GLOWBOW), 11=色彩1(COLOR1), 12=色彩2(COLOR2), 13=雨(RAIN), 14=热点(HOT_SPOT), 15=彩虹2(RAINBOW2), 16=灰度(GRAY), 17=金属(METAL), 18=冷点(COLD_SPOT) */
    private int paletteStyle = 0;

    public ThermalCurrentPaletteSetParams() {
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 调色板样式。枚举：0=白热(WHITE_HOT), 1=黑热(BLACK_HOT), 2=红热(RED_HOT), 3=绿热(GREEN_HOT), 4=融合(FUSION), 5=彩虹(RAINBOW), 6=热铁1(IRONBOW1), 7=热铁2(IRONBOW2), 8=冰火(ICE_FIRE), 9=复古(SEPIA), 10=辉光(GLOWBOW), 11=色彩1(COLOR1), 12=色彩2(COLOR2), 13=雨(RAIN), 14=热点(HOT_SPOT), 15=彩虹2(RAINBOW2), 16=灰度(GRAY), 17=金属(METAL), 18=冷点(COLD_SPOT) */
    public int getPaletteStyle() { return paletteStyle; }
    /** @param paletteStyle 调色板样式。枚举：0=白热(WHITE_HOT), 1=黑热(BLACK_HOT), 2=红热(RED_HOT), 3=绿热(GREEN_HOT), 4=融合(FUSION), 5=彩虹(RAINBOW), 6=热铁1(IRONBOW1), 7=热铁2(IRONBOW2), 8=冰火(ICE_FIRE), 9=复古(SEPIA), 10=辉光(GLOWBOW), 11=色彩1(COLOR1), 12=色彩2(COLOR2), 13=雨(RAIN), 14=热点(HOT_SPOT), 15=彩虹2(RAINBOW2), 16=灰度(GRAY), 17=金属(METAL), 18=冷点(COLD_SPOT) */
    public void setPaletteStyle(int paletteStyle) { this.paletteStyle = paletteStyle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThermalCurrentPaletteSetParams that = (ThermalCurrentPaletteSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && paletteStyle == that.paletteStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, paletteStyle);
    }

    @Override
    public String toString() {
        return "ThermalCurrentPaletteSetParams{payloadIndex='" + payloadIndex + '\'' + ", paletteStyle=" + paletteStyle + '}';
    }
}
