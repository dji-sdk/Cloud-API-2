// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Objects;

/**
 * 固件兼容性配置项。
 *
 * <p>当前后端 {@code GET /device/api/v1/firmware_compatible/config/{userType}}
 * 返回特性开关配置，主要字段为：
 * <ul>
 *   <li>{@link #getKey()}：配置项标识</li>
 *   <li>{@link #getUserType()}：适用用户类型</li>
 *   <li>{@link #getDescription()}：配置描述</li>
 *   <li>{@link #getValidates()}：校验规则列表</li>
 * </ul>
 *
 * <p>历史字段 {@link #getDeviceModelKey()}、{@link #getCompatibleVersions()}、
 * {@link #getRules()} 对当前后端不返回，已废弃，仅保留兼容。
 */
public final class FirmwareCompatibilityConfigItemResult {

    private String deviceModelKey;
    private List<String> compatibleVersions;
    private FirmwareCompatibilityRulesResult rules;
    private String key = "";
    private List<String> userType = new ArrayList<>();
    private String description = "";
    private List<FirmwareCompatibilityValidateResult> validates = new ArrayList<>();
    public FirmwareCompatibilityConfigItemResult() {
    }

/**
     * 构造 FirmwareCompatibilityConfigItemResult。
     *
     * <p>该构造函数用于设置历史字段；新代码建议使用无参构造，并通过
     * {@link #setKey(String)}、{@link #setUserType(List)}、
     * {@link #setDescription(String)}、{@link #setValidates(List)} 设置实际字段。
     *
     * @param deviceModelKey 设备型号标识（历史字段）
     * @param compatibleVersions 兼容版本列表（历史字段）
     * @param rules 版本范围规则（历史字段）
     */
    public FirmwareCompatibilityConfigItemResult(
            String deviceModelKey, List<String> compatibleVersions, FirmwareCompatibilityRulesResult rules) {
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
        this.compatibleVersions = compatibleVersions != null ? compatibleVersions : Collections.emptyList();
        this.rules = rules;
    }

    /**
     * 设备型号标识。
     *
     * @deprecated 当前后端 firmware_compatible/config 返回特性开关配置，
     *             不提供 device_model_key；请使用 {@link #getKey()}、{@link #getUserType()}、
     *             {@link #getDescription()}、{@link #getValidates()}。
     */
    @Deprecated
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /**
     * @param deviceModelKey 设备型号标识（历史字段）
     * @deprecated 当前后端不返回该字段，请使用 {@link #setKey(String)} 等新字段。
     */
    @Deprecated
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey;
    }

    /**
     * 兼容版本列表。
     *
     * @deprecated 当前后端 firmware_compatible/config 不返回 compatible_versions；
     *             请使用 {@link #getValidates()}。
     */
    @Deprecated
    public List<String> getCompatibleVersions() {
        return compatibleVersions;
    }

    /**
     * @param compatibleVersions 兼容版本列表（历史字段）
     * @deprecated 当前后端不返回该字段，请使用 {@link #getValidates()} / {@link #setValidates(List)}。
     */
    @Deprecated
    public void setCompatibleVersions(List<String> compatibleVersions) {
        this.compatibleVersions = compatibleVersions;
    }

    /**
     * 版本范围规则。
     *
     * @deprecated 当前后端 firmware_compatible/config 不返回 rules；
     *             版本规则在 {@link #getValidates()} 中通过 field=thing_version 表达。
     */
    @Deprecated
    public FirmwareCompatibilityRulesResult getRules() {
        return rules;
    }

    /**
     * @param rules 版本范围规则（历史字段）
     * @deprecated 当前后端不返回该字段，请使用 {@link #getValidates()}。
     */
    @Deprecated
    public void setRules(FirmwareCompatibilityRulesResult rules) {
        this.rules = rules;
    }

    /** 配置项标识，如 {@code 220-230-devices}。 */
    public String getKey() { return key; }

    /** @param v 配置项标识，如 {@code 220-230-devices} */
    public void setKey(String v) { this.key = v != null ? v : ""; }

    /** 适用的用户类型列表，如 {@code [PC, Service]}。 */
    public List<String> getUserType() { return userType; }

    /** @param v 适用的用户类型列表，如 {@code [PC, Service]} */
    public void setUserType(List<String> v) { this.userType = v != null ? v : new ArrayList<>(); }

    /** 配置描述，如 {@code 220与230设备}。 */
    public String getDescription() { return description; }

    /** @param v 配置描述，如 {@code 220与230设备} */
    public void setDescription(String v) { this.description = v != null ? v : ""; }

    /**
     * 校验规则列表。
     *
     * <p>每项包含字段、比较方式和原样 JSON 值，例如：
     * <pre>
     * field=device_model_key, compare=in,      value=["3-2-0","0-91-0"]
     * field=thing_version,  compare=between, value=[["1.1.0","Infinity"]]
     * </pre>
     */
    public List<FirmwareCompatibilityValidateResult> getValidates() { return validates; }

    /** @param v 校验规则列表 */
    public void setValidates(List<FirmwareCompatibilityValidateResult> v) {
        this.validates = v != null ? v : new ArrayList<>();
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
                && Objects.equals(rules, that.rules)
                && Objects.equals(key, that.key)
                && Objects.equals(userType, that.userType)
                && Objects.equals(description, that.description)
                && Objects.equals(validates, that.validates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceModelKey, compatibleVersions, rules,
                key, userType, description, validates);
    }

    @Override
    public String toString() {
        return "FirmwareCompatibilityConfigItemResult{deviceModelKey='" + deviceModelKey + '\''
                + ", compatibleVersions=" + compatibleVersions
                + ", rules=" + rules
                + ", key='" + key + '\''
                + ", userType=" + userType
                + ", description='" + description + '\''
                + ", validates=" + validates + '}';
    }
}
