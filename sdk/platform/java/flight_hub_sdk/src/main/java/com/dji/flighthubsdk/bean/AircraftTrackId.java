// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 航迹 ID。 */
public final class AircraftTrackId {

    /** string, 航迹id, such as "da247702-9636-4404-b98c-7e667efb4c4b"; */
    private String trackId;
    public AircraftTrackId() {
    }

/**
     * 构造 AircraftTrackId。
     *
     * @param trackId 航迹 ID
     */
    public AircraftTrackId(String trackId) {
        this.trackId = trackId != null ? trackId : "";
    }

    /** string, 航迹id, such as "da247702-9636-4404-b98c-7e667efb4c4b"; */
    public String getTrackId() {
        return trackId;
    }

    /** @param trackId string, 航迹id, such as "da247702-9636-4404-b98c-7e667efb4c4b"; */
    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftTrackId that = (AircraftTrackId) o;
        return Objects.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId);
    }

    @Override
    public String toString() {
        return "AircraftTrackId{trackId='" + trackId + '\'' + '}';
    }
}
