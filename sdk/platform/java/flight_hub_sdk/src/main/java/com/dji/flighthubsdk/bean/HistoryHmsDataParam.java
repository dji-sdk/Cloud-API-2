// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 历史 HMS 告警查询参数 */
public final class HistoryHmsDataParam {
    /** 设备 SN 列表 */
    private List<String> snList = new ArrayList<>();
    /** 查询起始时间（毫秒级时间戳） */
    private long createTimeBegin = 0;
    /** 查询结束时间（毫秒级时间戳） */
    private long createTimeEnd = 0;
    /** 每页数量 */
    private int pageSize = 0;
    /** 页码 */
    private int pageNum = 0;

    public HistoryHmsDataParam() {
    }

    /**
     * 构造 HistoryHmsDataParam。
     *
     * @param snList 设备 SN 列表
     * @param createTimeBegin 查询起始时间（毫秒级时间戳）
     * @param createTimeEnd 查询结束时间（毫秒级时间戳）
     * @param pageSize 每页数量
     * @param pageNum 页码
     */
    public HistoryHmsDataParam(List<String> snList, long createTimeBegin, long createTimeEnd, int pageSize, int pageNum) {
        this.snList = snList != null ? snList : new ArrayList<>();
        this.createTimeBegin = createTimeBegin;
        this.createTimeEnd = createTimeEnd;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }

    /** 设备 SN 列表 */
    public List<String> getSnList() {
        return snList;
    }

    /** @param snList 设备 SN 列表 */
    public void setSnList(List<String> snList) {
        this.snList = snList != null ? snList : new ArrayList<>();
    }

    /** 查询起始时间（毫秒级时间戳） */
    public long getCreateTimeBegin() {
        return createTimeBegin;
    }

    /** @param createTimeBegin 查询起始时间（毫秒级时间戳） */
    public void setCreateTimeBegin(long createTimeBegin) {
        this.createTimeBegin = createTimeBegin;
    }

    /** 查询结束时间（毫秒级时间戳） */
    public long getCreateTimeEnd() {
        return createTimeEnd;
    }

    /** @param createTimeEnd 查询结束时间（毫秒级时间戳） */
    public void setCreateTimeEnd(long createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    /** 每页数量 */
    public int getPageSize() {
        return pageSize;
    }

    /** @param pageSize 每页数量 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** 页码 */
    public int getPageNum() {
        return pageNum;
    }

    /** @param pageNum 页码 */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HistoryHmsDataParam that = (HistoryHmsDataParam) o;
        return createTimeBegin == that.createTimeBegin
                && createTimeEnd == that.createTimeEnd
                && pageSize == that.pageSize
                && pageNum == that.pageNum
                && Objects.equals(snList, that.snList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snList, createTimeBegin, createTimeEnd, pageSize, pageNum);
    }

    @Override
    public String toString() {
        return "HistoryHmsDataParam{snList=" + snList
                + ", createTimeBegin=" + createTimeBegin
                + ", createTimeEnd=" + createTimeEnd
                + ", pageSize=" + pageSize
                + ", pageNum=" + pageNum
                + '}';
    }
}
