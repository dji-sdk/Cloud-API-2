// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取航线列表返回结果 */
public final class GetWaylineListResult {

    private WaylineListPaginationResult pagination;
    private List<WaylineListItemResult> items;
    public GetWaylineListResult() {
    }

/**
     * 构造 GetWaylineListResult。
     *
     * @param pagination 分页信息
     * @param items 航线列表项
     */
    public GetWaylineListResult(WaylineListPaginationResult pagination, List<WaylineListItemResult> items) {
        this.pagination = pagination;
        this.items = items != null ? items : Collections.emptyList();
    }

    /** 分页信息 */
    public WaylineListPaginationResult getPagination() { return pagination; }
    /** 航线列表项 */
    public List<WaylineListItemResult> getItems() { return items; }

    /** 分页信息 */
    public void setPagination(WaylineListPaginationResult pagination) {
        this.pagination = pagination;
    }

    /** 航线列表项 */
    public void setItems(List<WaylineListItemResult> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetWaylineListResult that = (GetWaylineListResult) o;
        return Objects.equals(pagination, that.pagination)
                && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, items);
    }

    @Override
    public String toString() {
        return "GetWaylineListResult{pagination=" + pagination + ", items=" + items + '}';
    }
}
