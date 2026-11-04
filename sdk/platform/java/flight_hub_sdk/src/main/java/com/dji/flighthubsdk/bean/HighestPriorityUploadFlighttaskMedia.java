// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 最高优先级上传飞行任务媒体（highest priority upload flighttask media）。 */
public final class HighestPriorityUploadFlighttaskMedia {

    private String flightId;

    public HighestPriorityUploadFlighttaskMedia() {
        this.flightId = "";
    }

    /**
     * 构造 HighestPriorityUploadFlighttaskMedia。
     *
     * @param flightId 飞行任务 ID
     */
    public HighestPriorityUploadFlighttaskMedia(String flightId) {
        this.flightId = flightId != null ? flightId : "";
    }

    /** 飞行任务 ID */
    public String getFlightId() {
        return flightId;
    }

    /** 飞行任务 ID */
    public void setFlightId(String flightId) {
        this.flightId = flightId != null ? flightId : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HighestPriorityUploadFlighttaskMedia that = (HighestPriorityUploadFlighttaskMedia) o;
        return Objects.equals(flightId, that.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId);
    }

    @Override
    public String toString() {
        return "HighestPriorityUploadFlighttaskMedia{flightId=" + flightId + '}';
    }
}
