// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.IrGainMode;

import java.util.Objects;
/** 设置红外增益模式参数 */
public class ThermalGainModeSetParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 取值语义同 `flight_hub_sdk::IrGainMode`（见 `value.h`） */
    private IrGainMode thermalGainMode = IrGainMode.Auto;

    public ThermalGainModeSetParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() { return payloadIndex; }
    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex != null ? payloadIndex : ""; }
    /** 取值语义同 `flight_hub_sdk::IrGainMode`（见 `value.h`） */
    public IrGainMode getThermalGainMode() { return thermalGainMode; }
    /** @param thermalGainMode 取值语义同 `flight_hub_sdk::IrGainMode`（见 `value.h`） */
    public void setThermalGainMode(IrGainMode thermalGainMode) {
        this.thermalGainMode = thermalGainMode != null ? thermalGainMode : IrGainMode.Auto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThermalGainModeSetParams that = (ThermalGainModeSetParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(thermalGainMode, that.thermalGainMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, thermalGainMode);
    }

    @Override
    public String toString() {
        return "ThermalGainModeSetParams{payloadIndex='" + payloadIndex + '\'' + ", thermalGainMode=" + thermalGainMode + '}';
    }
}
