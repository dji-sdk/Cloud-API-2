// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 航线任务列表分页。 */
public final class PaginationInfo {
    private int page;
    private int pageSize;
    private int total;
    public PaginationInfo() {
    }

/**
     * 构造 PaginationInfo。
     *
     * @param page 当前页码
     * @param pageSize 分页大小
     * @param total 总数量
     */
    public PaginationInfo(int page, int pageSize, int total) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    /** 当前页码 */
    public int getPage() {
        return page;
    }

    /** 当前页码 */
    public void setPage(int page) {
        this.page = page;
    }

    /** 分页大小 */
    public int getPageSize() {
        return pageSize;
    }

    /** 分页大小 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** 总数量 */
    public int getTotal() {
        return total;
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
        PaginationInfo that = (PaginationInfo) o;
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
        return "PaginationInfo{page=" + page + ", pageSize=" + pageSize + ", total=" + total + '}';
    }
}
