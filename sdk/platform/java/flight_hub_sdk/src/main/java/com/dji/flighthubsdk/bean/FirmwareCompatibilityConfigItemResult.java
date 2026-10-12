// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 固件兼容性配置项，包含设备型号、兼容版本列表及版本范围规则 */
public final class FirmwareCompatibilityConfigItemResult {

    private String deviceModelKey;
    private List<String> compatibleVersions;
    private FirmwareCompatibilityRulesResult rules;
    public FirmwareCompatibilityConfigItemResult() {
    }

/**
     * 构造 FirmwareCompatibilityConfigItemResult。
     *
     * @param deviceModelKey 设备型号标识
     * @param compatibleVersions 兼容版本列表
     * @param rules rules
     */
    public FirmwareCompatibilityConfigItemResult(
            String deviceModelKey, List<String> compatibleVersions, FirmwareCompatibilityRulesResult rules) {
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.compatibleVersions = compatibleVersions != null ? compatibleVersions : Collections.emptyList();
        this.rules = rules;
    }

    /** 设备型号标识 */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** 设备型号标识 */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /** 兼容版本列表 */
    public List<String> getCompatibleVersions() {
        return compatibleVersions;
    }

    /** 兼容版本列表 */
    public void setCompatibleVersions(List<String> compatibleVersions) {
        this.compatibleVersions = compatibleVersions;
    }

    /** rules */
    public FirmwareCompatibilityRulesResult getRules() {
        return rules;
    }

    /** rules */
    public void setRules(FirmwareCompatibilityRulesResult rules) {
        this.rules = rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareCompatibilityConfigItemResult that = (FirmwareCompatibilityConfigItemResult) o;
        return Objects.equals(deviceModelKey, that.deviceModelKey)
                && Objects.equals(compatibleVersions, that.compatibleVersions)
                && Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceModelKey, compatibleVersions, rules);
    }

    @Override
    public String toString() {
        return "FirmwareCompatibilityConfigItemResult{deviceModelKey='" + deviceModelKey + '\'' + ", compatibleVersions=" + compatibleVersions + ", rules=" + rules + '}';
    }
}
