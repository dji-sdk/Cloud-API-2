// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取飞行区域结果。 */
public final class GetFlightAreasResults {
    private List<FlightAreaInfoItem> list;
    private FlightAreaPagination pagination;
    public GetFlightAreasResults() {
    }

/**
     * 构造 GetFlightAreasResults。
     *
     * @param list 飞行区列表
     * @param pagination 分页信息
     */
    public GetFlightAreasResults(List<FlightAreaInfoItem> list, FlightAreaPagination pagination) {
        this.list = list != null ? list : Collections.emptyList();
        this.pagination = pagination;
    }

    /** 飞行区列表 */
    public List<FlightAreaInfoItem> getList() { return list; }
    /** 分页信息 */
    public FlightAreaPagination getPagination() { return pagination; }

    /** 飞行区列表 */
    public void setList(List<FlightAreaInfoItem> list) {
        this.list = list;
    }

    /** 分页信息 */
    public void setPagination(FlightAreaPagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetFlightAreasResults that = (GetFlightAreasResults) o;
        return Objects.equals(list, that.list)
                && Objects.equals(pagination, that.pagination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, pagination);
    }

    @Override
    public String toString() {
        return "GetFlightAreasResults{list=" + list + ", pagination=" + pagination + '}';
    }
}
