// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 飞行任务列表结果。 */
public final class FlightTaskListResult {
    /** 任务详情列表 */
    private List<TaskDetail> list;
    /** 分页信息 */
    private PaginationInfo pagination;
    public FlightTaskListResult() {
    }

/**
     * 构造 FlightTaskListResult。
     *
     * @param list 任务详情列表
     * @param pagination 分页信息
     */
    public FlightTaskListResult(List<TaskDetail> list, PaginationInfo pagination) {
        this.list = list != null ? list : Collections.emptyList();
        this.pagination = pagination;
    }

    /** 任务详情列表 */
    public List<TaskDetail> getList() { return list; }
    /** 分页信息 */
    public PaginationInfo getPagination() { return pagination; }

    /** 任务详情列表 */
    public void setList(List<TaskDetail> list) {
        this.list = list;
    }

    /** 分页信息 */
    public void setPagination(PaginationInfo pagination) {
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
        FlightTaskListResult that = (FlightTaskListResult) o;
        return Objects.equals(list, that.list)
                && Objects.equals(pagination, that.pagination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, pagination);
    }

    @Override
    public String toString() {
        return "FlightTaskListResult{list=" + list + ", pagination=" + pagination + '}';
    }
}
