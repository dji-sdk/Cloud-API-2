// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 航线列表分页信息 */
public final class WaylineListPaginationResult {

    private int page;
    private int pageSize;
    private int total;
    public WaylineListPaginationResult() {
    }

/**
     * 构造 WaylineListPaginationResult。
     *
     * @param page 当前页码
     * @param pageSize 分页大小
     * @param total 总数量
     */
    public WaylineListPaginationResult(int page, int pageSize, int total) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    /** 当前页码 */
    public int getPage() { return page; }
    /** 分页大小 */
    public int getPageSize() { return pageSize; }
    /** 总数量 */
    public int getTotal() { return total; }

    /** 当前页码 */
    public void setPage(int page) {
        this.page = page;
    }

    /** 分页大小 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** 总数量 */
    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineListPaginationResult that = (WaylineListPaginationResult) o;
        return page == that.page
                && pageSize == that.pageSize
                && total == that.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, pageSize, total);
    }

    @Override
    public String toString() {
        return "WaylineListPaginationResult{page=" + page + ", pageSize=" + pageSize + ", total=" + total + '}';
    }
}
