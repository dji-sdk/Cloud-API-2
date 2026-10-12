// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设备日志上报 - 域信息项参数 */
public class DeviceLogReportDomainItemParam {
    /** 领域 key */
    private long key;
    /** 领域值 */
    private String value = "";

    public DeviceLogReportDomainItemParam() {
    }


    /** 领域 key */
    public long getKey() { return key; }
    /** @param key 领域 key */
    public void setKey(long key) { this.key = key; }
    /** 领域值 */
    public String getValue() { return value; }
    /** @param value 领域值 */
    public void setValue(String value) { this.value = value != null ? value : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceLogReportDomainItemParam that = (DeviceLogReportDomainItemParam) o;
        return key == that.key
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "DeviceLogReportDomainItemParam{key=" + key + ", value='" + value + '\'' + '}';
    }
}
