// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 分页元数据。 */
public final class CascadePage {
    private int total;
    private int page;
    private int size;

    public CascadePage() {
        this(0, 1, 50);
    }

    /**
     * 构造 CascadePage。
     *
     * @param total 总数（后端 int64 → JSON 字符串编码；SDK 解析后回落到 int）
     * @param page  当前页，1-based
     * @param size  每页条数
     */
    public CascadePage(int total, int page, int size) {
        this.total = total;
        this.page = page;
        this.size = size;
    }

    /** 总数 */
    public int getTotal() { return total; }
    /** 总数 */
    public void setTotal(int total) { this.total = total; }

    /** 当前页 */
    public int getPage() { return page; }
    /** 当前页 */
    public void setPage(int page) { this.page = page; }

    /** 每页条数 */
    public int getSize() { return size; }
    /** 每页条数 */
    public void setSize(int size) { this.size = size; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadePage that = (CascadePage) o;
        return total == that.total && page == that.page && size == that.size;
    }

    @Override
    public int hashCode() { return Objects.hash(total, page, size); }

    @Override
    public String toString() {
        return "CascadePage{total=" + total + ", page=" + page + ", size=" + size + "}";
    }
}
