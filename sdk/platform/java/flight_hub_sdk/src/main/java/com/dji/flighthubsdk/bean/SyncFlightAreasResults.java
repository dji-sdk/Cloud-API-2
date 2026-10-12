// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 同步飞行区域结果。 */
public final class SyncFlightAreasResults {
    private List<DeviceSyncResults> results;
    public SyncFlightAreasResults() {
    }

/**
     * 构造 SyncFlightAreasResults。
     *
     * @param results 同步结果列表
     */
    public SyncFlightAreasResults(List<DeviceSyncResults> results) {
        this.results = results != null ? results : Collections.emptyList();
    }

    /** 同步结果列表 */
    public List<DeviceSyncResults> getResults() {
        return results;
    }

    /** 同步结果列表 */
    public void setResults(List<DeviceSyncResults> results) {
        this.results = results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SyncFlightAreasResults that = (SyncFlightAreasResults) o;
        return Objects.equals(results, that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results);
    }

    @Override
    public String toString() {
        return "SyncFlightAreasResults{results=" + results + '}';
    }
}
