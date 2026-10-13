// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 固件兼容性版本范围规则（最小版本 / 最大版本） */
public final class FirmwareCompatibilityRulesResult {

    private String minVersion;
    private String maxVersion;
    public FirmwareCompatibilityRulesResult() {
    }

/**
     * 构造 FirmwareCompatibilityRulesResult。
     *
     * @param minVersion 最低版本
     * @param maxVersion 最高版本
     */
    public FirmwareCompatibilityRulesResult(String minVersion, String maxVersion) {
        this.minVersion = minVersion != null ? minVersion : "";
        this.maxVersion = maxVersion != null ? maxVersion : "";
    }

    /** 最低版本 */
    public String getMinVersion() {
        return minVersion;
    }

    /** 最低版本 */
    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    /** 最高版本 */
    public String getMaxVersion() {
        return maxVersion;
    }

    /** 最高版本 */
    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FirmwareCompatibilityRulesResult that = (FirmwareCompatibilityRulesResult) o;
        return Objects.equals(minVersion, that.minVersion)
                && Objects.equals(maxVersion, that.maxVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minVersion, maxVersion);
    }

    @Override
    public String toString() {
        return "FirmwareCompatibilityRulesResult{minVersion='" + minVersion + '\'' + ", maxVersion='" + maxVersion + '\'' + '}';
    }
}
