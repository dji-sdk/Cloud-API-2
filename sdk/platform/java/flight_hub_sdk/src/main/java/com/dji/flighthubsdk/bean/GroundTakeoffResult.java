// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 舱外起飞结果。对应 <b>GroundTakeoffResponse</b>。
 */
public class GroundTakeoffResult {
    /** 飞行任务 ID */
    private String flightId;

    public GroundTakeoffResult() {
    }

    /**
     * @param flightId 飞行任务 ID
     */
    public GroundTakeoffResult(String flightId) {
        this.flightId = flightId != null ? flightId : "";
    }

    /** 飞行任务 ID */
    public String getFlightId() { return flightId; }

    /** @param flightId 飞行任务 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    @Override
    public String toString() {
        return "GroundTakeoffResult{flightId='" + flightId + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroundTakeoffResult that = (GroundTakeoffResult) o;
        return Objects.equals(flightId, that.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId);
    }
}
