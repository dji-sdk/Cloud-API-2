// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** ESDK 自定义数据透传（esdk custom data transmission）。 */
public final class EsdkCustomDataTransmission {

    private String value;

    public EsdkCustomDataTransmission() {
        this.value = "";
    }

    /**
     * 构造 EsdkCustomDataTransmission。
     *
     * @param value 自定义数据内容
     */
    public EsdkCustomDataTransmission(String value) {
        this.value = value != null ? value : "";
    }

    /** 自定义数据内容 */
    public String getValue() {
        return value;
    }

    /** 自定义数据内容 */
    public void setValue(String value) {
        this.value = value != null ? value : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EsdkCustomDataTransmission that = (EsdkCustomDataTransmission) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "EsdkCustomDataTransmission{value=" + value + '}';
    }
}
