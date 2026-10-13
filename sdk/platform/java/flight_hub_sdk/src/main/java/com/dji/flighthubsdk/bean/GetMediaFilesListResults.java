// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 获取媒体文件列表结果。 */
public final class GetMediaFilesListResults {
    /** 数据列表; */
    private List<MediaFilesListItemInfoData> list;
    /** 页数据大小; */
    private int pageSize;
    /** 下一页的起始索引, 如果是最后一页, 则返回-1(表示获取完毕); */
    private int nextIndex;
    /** 总记录数 */
    private int total;
    public GetMediaFilesListResults() {
    }

/**
     * 构造 GetMediaFilesListResults。
     *
     * @param list 媒体文件列表
     * @param pageSize 分页大小
     * @param nextIndex 下一页起始索引
     * @param total 总记录数
     */
    public GetMediaFilesListResults(List<MediaFilesListItemInfoData> list, int pageSize, int nextIndex, int total) {
        this.list = list != null ? list : Collections.emptyList();
        this.pageSize = pageSize;
        this.nextIndex = nextIndex;
        this.total = total;
    }

    /** 数据列表; */
    public List<MediaFilesListItemInfoData> getList() { return list; }
    /** 页数据大小; */
    public int getPageSize() { return pageSize; }
    /** 下一页的起始索引, 如果是最后一页, 则返回-1(表示获取完毕); */
    public int getNextIndex() { return nextIndex; }
    /** 总记录数 */
    public int getTotal() { return total; }

    /** @param list 数据列表; */
    public void setList(List<MediaFilesListItemInfoData> list) {
        this.list = list;
    }

    /** @param pageSize 页数据大小; */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** @param nextIndex 下一页的起始索引, 如果是最后一页, 则返回-1(表示获取完毕); */
    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    /** 总记录数 */
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
        GetMediaFilesListResults that = (GetMediaFilesListResults) o;
        return Objects.equals(list, that.list)
                && pageSize == that.pageSize
                && nextIndex == that.nextIndex
                && total == that.total;
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, pageSize, nextIndex, total);
    }

    @Override
    public String toString() {
        return "GetMediaFilesListResults{"
                + "list=" + list
                + ", pageSize=" + pageSize
                + ", nextIndex=" + nextIndex
                + ", total=" + total
                + '}';
    }
}
