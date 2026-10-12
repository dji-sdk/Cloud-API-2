// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 红外镜头状态（来自 DRC 上行 OSD 的 ir_lense 块）。
 * <p>覆盖分屏、变焦、伪彩、增益、等温线、全局温度等 IR 相关状态。</p>
 */
public final class CameraIrLensState {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 分屏开关（画中画） */
    private boolean screenSplitEnable;
    /** IR 变焦倍率 */
    private int irZoomFactor;
    /** 当前调色板样式 */
    private int thermalCurrentPaletteStyle;
    /** 增益模式（对应 IrGainMode 枚举整型） */
    private int thermalGainMode;
    /** 全局最高温（°C） */
    private double thermalGlobalTemperatureMax;
    /** 全局最低温（°C） */
    private double thermalGlobalTemperatureMin;
    /** 等温线下限（%） */
    private int thermalIsothermLowerLimit;
    /** 等温线状态（0/1） */
    private int thermalIsothermState;
    /** 等温线上限（%） */
    private int thermalIsothermUpperLimit;
    /** IR 镜头 FOV 信息 */
    private CameraLensFovInfo irFovInfo;

    public CameraIrLensState() {
    }

    public CameraIrLensState(String payloadIndex,
                             boolean screenSplitEnable,
                             int irZoomFactor,
                             int thermalCurrentPaletteStyle,
                             int thermalGainMode,
                             double thermalGlobalTemperatureMax,
                             double thermalGlobalTemperatureMin,
                             int thermalIsothermLowerLimit,
                             int thermalIsothermState,
                             int thermalIsothermUpperLimit,
                             CameraLensFovInfo irFovInfo) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.screenSplitEnable = screenSplitEnable;
        this.irZoomFactor = irZoomFactor;
        this.thermalCurrentPaletteStyle = thermalCurrentPaletteStyle;
        this.thermalGainMode = thermalGainMode;
        this.thermalGlobalTemperatureMax = thermalGlobalTemperatureMax;
        this.thermalGlobalTemperatureMin = thermalGlobalTemperatureMin;
        this.thermalIsothermLowerLimit = thermalIsothermLowerLimit;
        this.thermalIsothermState = thermalIsothermState;
        this.thermalIsothermUpperLimit = thermalIsothermUpperLimit;
        this.irFovInfo = irFovInfo;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex; }

    /** 分屏开关（画中画） */
    public boolean isScreenSplitEnable() { return screenSplitEnable; }
    /** @param screenSplitEnable 分屏开关（画中画） */
    public void setScreenSplitEnable(boolean screenSplitEnable) { this.screenSplitEnable = screenSplitEnable; }

    /** IR 变焦倍率 */
    public int getIrZoomFactor() { return irZoomFactor; }
    /** @param irZoomFactor IR 变焦倍率 */
    public void setIrZoomFactor(int irZoomFactor) { this.irZoomFactor = irZoomFactor; }

    /** 当前调色板样式 */
    public int getThermalCurrentPaletteStyle() { return thermalCurrentPaletteStyle; }
    /** @param thermalCurrentPaletteStyle 当前调色板样式 */
    public void setThermalCurrentPaletteStyle(int v) { this.thermalCurrentPaletteStyle = v; }

    /** 增益模式（对应 IrGainMode 枚举整型） */
    public int getThermalGainMode() { return thermalGainMode; }
    /** @param thermalGainMode 增益模式（对应 IrGainMode 枚举整型） */
    public void setThermalGainMode(int v) { this.thermalGainMode = v; }

    /** 全局最高温（°C） */
    public double getThermalGlobalTemperatureMax() { return thermalGlobalTemperatureMax; }
    /** @param thermalGlobalTemperatureMax 全局最高温（°C） */
    public void setThermalGlobalTemperatureMax(double v) { this.thermalGlobalTemperatureMax = v; }

    /** 全局最低温（°C） */
    public double getThermalGlobalTemperatureMin() { return thermalGlobalTemperatureMin; }
    /** @param thermalGlobalTemperatureMin 全局最低温（°C） */
    public void setThermalGlobalTemperatureMin(double v) { this.thermalGlobalTemperatureMin = v; }

    /** 等温线下限（%） */
    public int getThermalIsothermLowerLimit() { return thermalIsothermLowerLimit; }
    /** @param thermalIsothermLowerLimit 等温线下限（%） */
    public void setThermalIsothermLowerLimit(int v) { this.thermalIsothermLowerLimit = v; }

    /** 等温线状态（0/1） */
    public int getThermalIsothermState() { return thermalIsothermState; }
    /** @param thermalIsothermState 等温线状态（0/1） */
    public void setThermalIsothermState(int v) { this.thermalIsothermState = v; }

    /** 等温线上限（%） */
    public int getThermalIsothermUpperLimit() { return thermalIsothermUpperLimit; }
    /** @param thermalIsothermUpperLimit 等温线上限（%） */
    public void setThermalIsothermUpperLimit(int v) { this.thermalIsothermUpperLimit = v; }

    /** IR 镜头 FOV 信息 */
    public CameraLensFovInfo getIrFovInfo() { return irFovInfo; }
    /** @param irFovInfo IR 镜头 FOV 信息 */
    public void setIrFovInfo(CameraLensFovInfo irFovInfo) { this.irFovInfo = irFovInfo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraIrLensState that = (CameraIrLensState) o;
        return screenSplitEnable == that.screenSplitEnable
                && irZoomFactor == that.irZoomFactor
                && thermalCurrentPaletteStyle == that.thermalCurrentPaletteStyle
                && thermalGainMode == that.thermalGainMode
                && Double.compare(that.thermalGlobalTemperatureMax, thermalGlobalTemperatureMax) == 0
                && Double.compare(that.thermalGlobalTemperatureMin, thermalGlobalTemperatureMin) == 0
                && thermalIsothermLowerLimit == that.thermalIsothermLowerLimit
                && thermalIsothermState == that.thermalIsothermState
                && thermalIsothermUpperLimit == that.thermalIsothermUpperLimit
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(irFovInfo, that.irFovInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, screenSplitEnable, irZoomFactor,
                thermalCurrentPaletteStyle, thermalGainMode,
                thermalGlobalTemperatureMax, thermalGlobalTemperatureMin,
                thermalIsothermLowerLimit, thermalIsothermState, thermalIsothermUpperLimit,
                irFovInfo);
    }

    @Override
    public String toString() {
        return "CameraIrLensState{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", screenSplitEnable=" + screenSplitEnable
                + ", irZoomFactor=" + irZoomFactor
                + ", thermalCurrentPaletteStyle=" + thermalCurrentPaletteStyle
                + ", thermalGainMode=" + thermalGainMode
                + ", thermalGlobalTemperatureMax=" + thermalGlobalTemperatureMax
                + ", thermalGlobalTemperatureMin=" + thermalGlobalTemperatureMin
                + ", thermalIsothermLowerLimit=" + thermalIsothermLowerLimit
                + ", thermalIsothermState=" + thermalIsothermState
                + ", thermalIsothermUpperLimit=" + thermalIsothermUpperLimit
                + ", irFovInfo=" + irFovInfo
                + '}';
    }
}
