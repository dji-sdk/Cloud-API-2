// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 通用分页数据。 */
public final class CommonPaginationData {
    /** 当前页码; */
    private int page;
    /** 分页大小; */
    private int pageSize;
    /** 总数量; */
    private int total;
    public CommonPaginationData() {
    }

/**
     * 构造 CommonPaginationData。
     *
     * @param page 当前页码
     * @param pageSize 分页大小
     * @param total 总数量
     */
    public CommonPaginationData(int page, int pageSize, int total) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    /** 当前页码; */
    public int getPage() {
        return page;
    }

    /** @param page 当前页码; */
    public void setPage(int page) {
        this.page = page;
    }

    /** 分页大小; */
    public int getPageSize() {
        return pageSize;
    }

    /** @param pageSize 分页大小; */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** 总数量; */
    public int getTotal() {
        return total;
    }

    /** @param total 总数量; */
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
        CommonPaginationData that = (CommonPaginationData) o;
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
        return "CommonPaginationData{page=" + page + ", pageSize=" + pageSize + ", total=" + total + '}';
    }
}
