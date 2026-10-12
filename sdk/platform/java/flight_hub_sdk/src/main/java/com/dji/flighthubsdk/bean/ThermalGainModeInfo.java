// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 红外增益模式。
 */
public final class ThermalGainModeInfo {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int thermalGainModeSwig;
    public ThermalGainModeInfo() {
    }

/**
     * 构造 ThermalGainModeInfo。
     *
     * @param payloadIndex 负载索引
     * @param thermalGainModeSwig 红外增益模式
     */
    public ThermalGainModeInfo(String payloadIndex, int thermalGainModeSwig) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.thermalGainModeSwig = thermalGainModeSwig;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** 红外增益模式 */
    public int getThermalGainModeSwig() { return thermalGainModeSwig; }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 红外增益模式 */
    public void setThermalGainModeSwig(int thermalGainModeSwig) {
        this.thermalGainModeSwig = thermalGainModeSwig;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThermalGainModeInfo that = (ThermalGainModeInfo) o;
        return thermalGainModeSwig == that.thermalGainModeSwig
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, thermalGainModeSwig);
    }

    @Override
    public String toString() {
        return "ThermalGainModeInfo{payloadIndex='" + payloadIndex + '\'' + ", thermalGainModeSwig=" + thermalGainModeSwig + '}';
    }
}
