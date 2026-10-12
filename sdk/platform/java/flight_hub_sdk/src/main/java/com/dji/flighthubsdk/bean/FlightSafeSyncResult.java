// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 设备影子同步结果 */
public final class FlightSafeSyncResult {
    /** 整体同步状态: "all_synced" / "partial_synced" / "failed" */
    private String status;
    /** 各字段同步详情 */
    private List<FlightSafeSyncField> results;
    public FlightSafeSyncResult() {
    }

/**
     * 构造 FlightSafeSyncResult。
     *
     * @param status 整体同步状态: "all_synced" / "partial_synced" / "failed"
     * @param results 各字段同步详情
     */
    public FlightSafeSyncResult(String status, List<FlightSafeSyncField> results) {
        this.status = status != null ? status : "";
        this.results = results != null ? results : Collections.emptyList();
    }

    /** 整体同步状态: "all_synced" / "partial_synced" / "failed" */
    public String getStatus() { return status; }
    /** 各字段同步详情 */
    public List<FlightSafeSyncField> getResults() { return results; }

    /** 整体同步状态: "all_synced" / "partial_synced" / "failed" */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 各字段同步详情 */
    public void setResults(List<FlightSafeSyncField> results) {
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
        FlightSafeSyncResult that = (FlightSafeSyncResult) o;
        return Objects.equals(status, that.status)
                && Objects.equals(results, that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, results);
    }

    @Override
    public String toString() {
        return "FlightSafeSyncResult{status='" + status + '\'' + ", results=" + results + '}';
    }
}
