// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取导出飞行记录进度结果。 */
public final class GetExportFlightRecordsProgressResults {
    private CommonPaginationData pagination;
    private List<FlightRecordExportProgressItemData> list;
    public GetExportFlightRecordsProgressResults() {
    }

/**
     * 构造 GetExportFlightRecordsProgressResults。
     *
     * @param pagination 分页信息
     * @param list 导出进度列表
     */
    public GetExportFlightRecordsProgressResults(
            CommonPaginationData pagination, List<FlightRecordExportProgressItemData> list) {
        this.pagination = pagination;
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 分页信息 */
    public CommonPaginationData getPagination() { return pagination; }
    /** 导出进度列表 */
    public List<FlightRecordExportProgressItemData> getList() { return list; }

    /** 分页信息 */
    public void setPagination(CommonPaginationData pagination) {
        this.pagination = pagination;
    }

    /** 导出进度列表 */
    public void setList(List<FlightRecordExportProgressItemData> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetExportFlightRecordsProgressResults that = (GetExportFlightRecordsProgressResults) o;
        return Objects.equals(pagination, that.pagination)
                && Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, list);
    }

    @Override
    public String toString() {
        return "GetExportFlightRecordsProgressResults{pagination=" + pagination + ", list=" + list + '}';
    }
}
