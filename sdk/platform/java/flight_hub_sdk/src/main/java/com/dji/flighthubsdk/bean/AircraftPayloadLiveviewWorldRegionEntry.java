// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 单个载荷的直播画面世界区域条目。
 */
public final class AircraftPayloadLiveviewWorldRegionEntry {

    private String payloadIndex;
    private CameraLiveviewWorldRegion region;
    public AircraftPayloadLiveviewWorldRegionEntry() {
    }

/**
     * 构造 AircraftPayloadLiveviewWorldRegionEntry。
     *
     * @param payloadIndex 负载索引
     * @param region 世界区域信息
     */
    public AircraftPayloadLiveviewWorldRegionEntry(String payloadIndex, CameraLiveviewWorldRegion region) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.region = region;
    }

    /** 负载索引 */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** 负载索引 */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 世界区域信息 */
    public CameraLiveviewWorldRegion getRegion() {
        return region;
    }

    /** 世界区域信息 */
    public void setRegion(CameraLiveviewWorldRegion region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadLiveviewWorldRegionEntry that = (AircraftPayloadLiveviewWorldRegionEntry) o;
        return Objects.equals(payloadIndex, that.payloadIndex) && Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadIndex, region);
    }

    @Override
    public String toString() {
        return "AircraftPayloadLiveviewWorldRegionEntry{payloadIndex='" + payloadIndex + '\'' + ", region=" + region + '}';
    }
}
