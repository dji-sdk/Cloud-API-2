// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 历史HMS数据结果分页。 */
public final class HistoryHmsDataResultPagination {
    /** 当前页码 */
    private int pageNum;
    /** 每页数量 */
    private int pageSize;
    /** 总记录数 */
    private int total;
    public HistoryHmsDataResultPagination() {
    }

/**
     * 构造 HistoryHmsDataResultPagination。
     *
     * @param pageNum 当前页码
     * @param pageSize 分页大小
     * @param total 总数量
     */
    public HistoryHmsDataResultPagination(int pageNum, int pageSize, int total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    /** 当前页码 */
    public int getPageNum() { return pageNum; }
    /** 每页数量 */
    public int getPageSize() { return pageSize; }
    /** 总记录数 */
    public int getTotal() { return total; }

    /** 当前页码 */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /** @param pageSize 每页数量 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** @param total 总记录数 */
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
        HistoryHmsDataResultPagination that = (HistoryHmsDataResultPagination) o;
        return pageNum == that.pageNum
                && pageSize == that.pageSize
                && total == that.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNum, pageSize, total);
    }

    @Override
    public String toString() {
        return "HistoryHmsDataResultPagination{pageNum=" + pageNum + ", pageSize=" + pageSize + ", total=" + total + '}';
    }
}
