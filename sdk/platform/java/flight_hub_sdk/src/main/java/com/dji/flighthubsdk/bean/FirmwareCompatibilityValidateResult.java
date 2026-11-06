// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 固件兼容配置校验项，对应后端 {@code validates[]} 中的一项。
 *
 * <p>示例：
 * <pre>
 * field=device_model_key, compare=in,      value=["3-2-0","0-91-0"]
 * field=thing_version,  compare=between, value=[["1.1.0","Infinity"]]
 * </pre>
 */
public final class FirmwareCompatibilityValidateResult {
    private String field = "";
    private String description = "";
    private String compare = "";
    private String value = "";

    public FirmwareCompatibilityValidateResult() {}

    /**
     * 构造固件兼容配置校验项。
     *
     * @param field 校验字段，如 {@code device_model_key} / {@code thing_version}
     * @param description 字段描述，如 {@code device.model.key}
     * @param compare 比较方式，如 {@code in} / {@code between}
     * @param value 原样 JSON 字符串
     */
    public FirmwareCompatibilityValidateResult(String field, String description,
            String compare, String value) {
        this.field = field != null ? field : "";
        this.description = description != null ? description : "";
        this.compare = compare != null ? compare : "";
        this.value = value != null ? value : "";
    }

    /** 校验字段，如 {@code device_model_key} / {@code thing_version}。 */
    public String getField() { return field; }

    /** @param v 校验字段，如 {@code device_model_key} / {@code thing_version} */
    public void setField(String v) { this.field = v != null ? v : ""; }

    /** 字段描述，如 {@code device.model.key}。 */
    public String getDescription() { return description; }

    /** @param v 字段描述，如 {@code device.model.key} */
    public void setDescription(String v) { this.description = v != null ? v : ""; }

    /** 比较方式，如 {@code in} / {@code between}。 */
    public String getCompare() { return compare; }

    /** @param v 比较方式，如 {@code in} / {@code between} */
    public void setCompare(String v) { this.compare = v != null ? v : ""; }

    /**
     * 原样 JSON 字符串。
     *
     * <p>例如：
     * <ul>
     *   <li>{@code ["3-2-0","0-91-0"]}（compare=in）</li>
     *   <li>{@code [["1.1.0","Infinity"]]}（compare=between）</li>
     * </ul>
     */
    public String getValue() { return value; }

    /** @param v 原样 JSON 字符串，如 {@code ["3-2-0"]} 或 {@code [["1.1.0","Infinity"]]} */
    public void setValue(String v) { this.value = v != null ? v : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirmwareCompatibilityValidateResult that = (FirmwareCompatibilityValidateResult) o;
        return Objects.equals(field, that.field)
                && Objects.equals(description, that.description)
                && Objects.equals(compare, that.compare)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(field, description, compare, value); }

    @Override
    public String toString() {
        return "FirmwareCompatibilityValidateResult{field='" + field + '\''
                + ", description='" + description + '\''
                + ", compare='" + compare + '\''
                + ", value='" + value + '\'' + '}';
    }
}
