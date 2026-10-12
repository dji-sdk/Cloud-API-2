// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 载荷相机直播世界区域聚合。
 */
public final class AircraftPayloadsCameraLiveviewWorldRegion {

    private List<AircraftPayloadLiveviewWorldRegionEntry> payloadsList;
    public AircraftPayloadsCameraLiveviewWorldRegion() {
    }

/**
     * 构造 AircraftPayloadsCameraLiveviewWorldRegion。
     *
     * @param payloadsList 载荷列表
     */
    public AircraftPayloadsCameraLiveviewWorldRegion(List<AircraftPayloadLiveviewWorldRegionEntry> payloadsList) {
        this.payloadsList = payloadsList != null ? payloadsList : Collections.emptyList();
    }

    /** 载荷列表 */
    public List<AircraftPayloadLiveviewWorldRegionEntry> getPayloadsList() {
        return payloadsList;
    }

    /** 载荷列表 */
    public void setPayloadsList(List<AircraftPayloadLiveviewWorldRegionEntry> payloadsList) {
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
        AircraftPayloadsCameraLiveviewWorldRegion that = (AircraftPayloadsCameraLiveviewWorldRegion) o;
        return Objects.equals(payloadsList, that.payloadsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadsList);
    }

    @Override
    public String toString() {
        return "AircraftPayloadsCameraLiveviewWorldRegion{payloadsList=" + payloadsList + '}';
    }
}
