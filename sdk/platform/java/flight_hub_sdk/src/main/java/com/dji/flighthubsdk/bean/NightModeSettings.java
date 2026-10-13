// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.NightSceneMode;

import java.util.Objects;
/**
 * 夜景模式状态（来自 DRC 相机状态推送中的 night_mode_settings）。
 */
public class NightModeSettings {
    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex = "";
    /** 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`） */
    private NightSceneMode nightMode = NightSceneMode.AUTO;
    private boolean working;
    // ── B4 扩展 ──
    /** 降噪等级 */
    private int denoiseLevel;
    /** 夜视使能 */
    private boolean nightVisionEnable;
    /** 红外补光使能 */
    private boolean infraredFillLightEnable;
    /** 夜景模式建议 */
    private int nightSceneModeSuggestion;

    public NightModeSettings() {
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
    public NightSceneMode getNightMode() {
        return nightMode;
    }

    /** @param nightMode 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`） */
    public void setNightMode(NightSceneMode nightMode) {
        this.nightMode = nightMode != null ? nightMode : NightSceneMode.AUTO;
    }

    /** 是否工作中 */
    public boolean isWorking() {
        return working;
    }

    /** @param working 是否工作中 */
    public void setWorking(boolean working) {
        this.working = working;
    }

    /** 降噪等级 */
    public int getDenoiseLevel() { return denoiseLevel; }
    /** @param denoiseLevel 降噪等级 */
    public void setDenoiseLevel(int v) { this.denoiseLevel = v; }
    /** 夜视使能 */
    public boolean isNightVisionEnable() { return nightVisionEnable; }
    /** @param nightVisionEnable 夜视使能 */
    public void setNightVisionEnable(boolean v) { this.nightVisionEnable = v; }
    /** 红外补光使能 */
    public boolean isInfraredFillLightEnable() { return infraredFillLightEnable; }
    /** @param infraredFillLightEnable 红外补光使能 */
    public void setInfraredFillLightEnable(boolean v) { this.infraredFillLightEnable = v; }
    /** 夜景模式建议 */
    public int getNightSceneModeSuggestion() { return nightSceneModeSuggestion; }
    /** @param nightSceneModeSuggestion 夜景模式建议 */
    public void setNightSceneModeSuggestion(int v) { this.nightSceneModeSuggestion = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NightModeSettings that = (NightModeSettings) o;
        return working == that.working
                && denoiseLevel == that.denoiseLevel
                && nightVisionEnable == that.nightVisionEnable
                && infraredFillLightEnable == that.infraredFillLightEnable
                && nightSceneModeSuggestion == that.nightSceneModeSuggestion
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(nightMode, that.nightMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, nightMode, working, denoiseLevel,
                nightVisionEnable, infraredFillLightEnable, nightSceneModeSuggestion);
    }

    @Override
    public String toString() {
        return "NightModeSettings{payloadIndex='" + payloadIndex + '\''
                + ", nightMode=" + nightMode + ", working=" + working
                + ", denoiseLevel=" + denoiseLevel
                + ", nightVisionEnable=" + nightVisionEnable
                + ", infraredFillLightEnable=" + infraredFillLightEnable
                + ", nightSceneModeSuggestion=" + nightSceneModeSuggestion + '}';
    }
}
