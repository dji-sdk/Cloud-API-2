// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 历史HMS数据结果。 */
public final class HistoryHmsDataResult {
    /** 分页信息 */
    private HistoryHmsDataResultPagination pagination;
    /** HMS 数据项列表 */
    private List<HistoryHmsDataResultItem> list;
    public HistoryHmsDataResult() {
    }

/**
     * 构造 HistoryHmsDataResult。
     *
     * @param pagination 分页信息
     * @param list HMS 数据项列表
     */
    public HistoryHmsDataResult(HistoryHmsDataResultPagination pagination, List<HistoryHmsDataResultItem> list) {
        this.pagination = pagination;
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 分页信息 */
    public HistoryHmsDataResultPagination getPagination() { return pagination; }
    /** HMS 数据项列表 */
    public List<HistoryHmsDataResultItem> getList() { return list; }

    /** 分页信息 */
    public void setPagination(HistoryHmsDataResultPagination pagination) {
        this.pagination = pagination;
    }

    /** HMS 数据项列表 */
    public void setList(List<HistoryHmsDataResultItem> list) {
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
        HistoryHmsDataResult that = (HistoryHmsDataResult) o;
        return Objects.equals(pagination, that.pagination)
                && Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, list);
    }

    @Override
    public String toString() {
        return "HistoryHmsDataResult{pagination=" + pagination + ", list=" + list + '}';
    }
}
