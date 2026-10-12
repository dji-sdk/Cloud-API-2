// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.NightSceneMode;

import java.util.Objects;
/** 设置夜景模式参数（与设备协议数值一致，语义见 {@link NightSceneMode}）。 */
public class SetNightSceneModeParams {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`） */
    private NightSceneMode mode = NightSceneMode.AUTO;

    public SetNightSceneModeParams() {
    }


    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
    }

    /** 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`） */
    public NightSceneMode getMode() {
        return mode;
    }

    /** @param mode 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`） */
    public void setMode(NightSceneMode mode) {
        this.mode = mode != null ? mode : NightSceneMode.AUTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetNightSceneModeParams that = (SetNightSceneModeParams) o;
        return Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(mode, that.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, mode);
    }

    @Override
    public String toString() {
        return "SetNightSceneModeParams{payloadIndex='" + payloadIndex + '\'' + ", mode=" + mode + '}';
    }
}
