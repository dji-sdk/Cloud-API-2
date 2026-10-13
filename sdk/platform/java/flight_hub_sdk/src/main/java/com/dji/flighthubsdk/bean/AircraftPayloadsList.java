// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 飞机载荷索引列表。
 */
public final class AircraftPayloadsList {

    private List<String> payloadsList;
    public AircraftPayloadsList() {
    }

/**
     * 构造 AircraftPayloadsList。
     *
     * @param payloadsList 载荷列表
     */
    public AircraftPayloadsList(List<String> payloadsList) {
        this.payloadsList = payloadsList != null ? payloadsList : Collections.emptyList();
    }

    /** 载荷列表 */
    public List<String> getPayloadsList() {
        return payloadsList;
    }

    /** 载荷列表 */
    public void setPayloadsList(List<String> payloadsList) {
        this.payloadsList = payloadsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadsList that = (AircraftPayloadsList) o;
        return Objects.equals(payloadsList, that.payloadsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadsList);
    }

    @Override
    public String toString() {
        return "AircraftPayloadsList{payloadsList=" + payloadsList + '}';
    }
}
