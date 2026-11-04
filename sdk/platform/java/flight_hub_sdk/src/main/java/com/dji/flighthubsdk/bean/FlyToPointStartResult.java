// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 指点飞行启动结果 */
public final class FlyToPointStartResult {
    /** 指点飞行任务 ID */
    private String flyToId = "";
    /** 飞行任务 ID */
    private String flightId = "";

    public FlyToPointStartResult() {
    }

    public FlyToPointStartResult(String flyToId, String flightId) {
        this.flyToId = flyToId != null ? flyToId : "";
        this.flightId = flightId != null ? flightId : "";
    }

    public String getFlyToId() { return flyToId; }
    public void setFlyToId(String flyToId) { this.flyToId = flyToId != null ? flyToId : ""; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId != null ? flightId : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlyToPointStartResult that = (FlyToPointStartResult) o;
        return Objects.equals(flyToId, that.flyToId) && Objects.equals(flightId, that.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flyToId, flightId);
    }

    @Override
    public String toString() {
        return "FlyToPointStartResult{flyToId='" + flyToId + "', flightId='" + flightId + "'}";
    }
}
