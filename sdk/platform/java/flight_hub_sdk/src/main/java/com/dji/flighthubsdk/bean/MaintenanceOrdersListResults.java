// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 分页查询报修订单列表返回结果 */
public final class MaintenanceOrdersListResults {
    private CommonPaginationData pagination;
    private List<MaintenanceOrdersListItem> list;
    public MaintenanceOrdersListResults() {
    }

/**
     * 构造 MaintenanceOrdersListResults。
     *
     * @param pagination 分页信息
     * @param list 工单列表
     */
    public MaintenanceOrdersListResults(CommonPaginationData pagination, List<MaintenanceOrdersListItem> list) {
        this.pagination = pagination;
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 分页信息 */
    public CommonPaginationData getPagination() { return pagination; }
    /** 工单列表 */
    public List<MaintenanceOrdersListItem> getList() { return list; }

    /** 分页信息 */
    public void setPagination(CommonPaginationData pagination) {
        this.pagination = pagination;
    }

    /** 工单列表 */
    public void setList(List<MaintenanceOrdersListItem> list) {
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
        MaintenanceOrdersListResults that = (MaintenanceOrdersListResults) o;
        return Objects.equals(pagination, that.pagination)
                && Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, list);
    }

    @Override
    public String toString() {
        return "MaintenanceOrdersListResults{pagination=" + pagination + ", list=" + list + '}';
    }
}
