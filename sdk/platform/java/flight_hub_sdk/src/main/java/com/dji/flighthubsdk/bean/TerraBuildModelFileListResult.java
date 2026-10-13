// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Terra Build 模型文件列表返回结果 */
public final class TerraBuildModelFileListResult {

    private List<TerraBuildModelFileItem> list;
    /** 总记录数（对应 proto PaginationInfo.total，int64） */
    private long total;
    /** 当前页码，从 1 开始 */
    private int page;
    /** 每页数量 */
    private int pageSize;

    public TerraBuildModelFileListResult() {
    }

    /**
     * @param list     模型文件列表
     * @param total    总记录数
     * @param page     当前页码，从 1 开始
     * @param pageSize 每页数量
     */
    public TerraBuildModelFileListResult(List<TerraBuildModelFileItem> list, long total, int page, int pageSize) {
        this.list = list != null ? list : Collections.emptyList();
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<TerraBuildModelFileItem> getList() { return list; }
    public void setList(List<TerraBuildModelFileItem> list) { this.list = list; }

    /** 总记录数 */
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    /** 当前页码，从 1 开始 */
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    /** 每页数量 */
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildModelFileListResult that = (TerraBuildModelFileListResult) o;
        return total == that.total && page == that.page && pageSize == that.pageSize
                && Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, total, page, pageSize);
    }

    @Override
    public String toString() {
        return "TerraBuildModelFileListResult{"
                + "list=" + list
                + ", total=" + total
                + ", page=" + page
                + ", pageSize=" + pageSize
                + '}';
    }
}
