// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 红外画中画状态（DRC {@code drc_camera_state_push.camera_state.ir_pip_state}）。 */
public final class CameraIrPipState {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int displayMode;
    private int enhanceMode;
    private int irMixRate;
    private CameraIrMeterRegion irMeterRegion;

    public CameraIrPipState() {}
    public CameraIrPipState(String payloadIndex, int displayMode, int enhanceMode, int irMixRate,
                            CameraIrMeterRegion irMeterRegion) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.displayMode = displayMode;
        this.enhanceMode = enhanceMode;
        this.irMixRate = irMixRate;
        this.irMeterRegion = irMeterRegion;
    }
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String v) { this.payloadIndex = v; }
    public int getDisplayMode() { return displayMode; }
    public void setDisplayMode(int v) { this.displayMode = v; }
    public int getEnhanceMode() { return enhanceMode; }
    public void setEnhanceMode(int v) { this.enhanceMode = v; }
    public int getIrMixRate() { return irMixRate; }
    public void setIrMixRate(int v) { this.irMixRate = v; }
    public CameraIrMeterRegion getIrMeterRegion() { return irMeterRegion; }
    public void setIrMeterRegion(CameraIrMeterRegion v) { this.irMeterRegion = v; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraIrPipState)) return false;
        CameraIrPipState that = (CameraIrPipState) o;
        return displayMode == that.displayMode && enhanceMode == that.enhanceMode
                && irMixRate == that.irMixRate
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(irMeterRegion, that.irMeterRegion);
    }
    @Override public int hashCode() { return Objects.hash(payloadIndex, displayMode, enhanceMode, irMixRate, irMeterRegion); }
    @Override public String toString() {
        return "CameraIrPipState{payloadIndex='" + payloadIndex + "', displayMode=" + displayMode
                + ", enhanceMode=" + enhanceMode + ", irMixRate=" + irMixRate
                + ", irMeterRegion=" + irMeterRegion + '}';
    }
}
