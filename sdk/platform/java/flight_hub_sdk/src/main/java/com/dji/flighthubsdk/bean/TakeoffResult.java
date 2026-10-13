// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 指令飞行 / 一键起飞结果。对应 CommandResponse / DroneTakeoffResponse。
 */
public class TakeoffResult {
    /** 飞行任务 ID */
    private String flightId;
    /** 指点飞行任务 ID */
    private String flyToId;

    public TakeoffResult() {
    }

    /**
     * @param flightId 飞行任务 ID
     * @param flyToId 指点飞行任务 ID
     */
    public TakeoffResult(String flightId, String flyToId) {
        this.flightId = flightId != null ? flightId : "";
        this.flyToId = flyToId != null ? flyToId : "";
    }

    /** 飞行任务 ID */
    public String getFlightId() { return flightId; }
    /** 指点飞行任务 ID */
    public String getFlyToId() { return flyToId; }

    /** @param flightId 飞行任务 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    /** @param flyToId 指点飞行任务 ID */
    public void setFlyToId(String flyToId) {
        this.flyToId = flyToId;
    }

    @Override
    public String toString() {
        return "TakeoffResult{flightId='" + flightId + "', flyToId='" + flyToId + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TakeoffResult that = (TakeoffResult) o;
        return Objects.equals(flightId, that.flightId)
                && Objects.equals(flyToId, that.flyToId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, flyToId);
    }
}
