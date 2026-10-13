// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/**
 * 通用 HTTP/WS 鉴权头 key-value Bean。
 */
public class HeaderKeyValue {
    /** 头部字段名 */
    private String key = "";
    /** 头部字段值 */
    private String value = "";

    /**
     * 构造 HeaderKeyValue。
     */
    public HeaderKeyValue() {
    }

/**
     * 构造 HeaderKeyValue。
     *
     * @param key 头部键
     * @param value 头部值
     */
    public HeaderKeyValue(String key, String value) {
        this.key = key != null ? key : "";
        this.value = value != null ? value : "";
    }

    /** 头部字段名 */
    public String getKey() { return key; }
    /** @param key 头部字段名 */
    public void setKey(String key) { this.key = key != null ? key : ""; }
    /** 头部字段值 */
    public String getValue() { return value; }
    /** @param value 头部字段值 */
    public void setValue(String value) { this.value = value != null ? value : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HeaderKeyValue that = (HeaderKeyValue) o;
        return Objects.equals(key, that.key)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "HeaderKeyValue{key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }
}
