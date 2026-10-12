// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 日志报告列表结果。 */
public final class LogReportListResult {
    /** 分页信息 */
    private LogReportListResultPagination pagination;
    /** 日志报告列表 */
    private List<LogReportListResultItem> list;
    public LogReportListResult() {
    }

/**
     * 构造 LogReportListResult。
     *
     * @param pagination 分页信息
     * @param list 日志报告列表
     */
    public LogReportListResult(LogReportListResultPagination pagination, List<LogReportListResultItem> list) {
        this.pagination = pagination;
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 分页信息 */
    public LogReportListResultPagination getPagination() { return pagination; }
    /** 日志报告列表 */
    public List<LogReportListResultItem> getList() { return list; }

    /** 分页信息 */
    public void setPagination(LogReportListResultPagination pagination) {
        this.pagination = pagination;
    }

    /** 日志报告列表 */
    public void setList(List<LogReportListResultItem> list) {
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
        LogReportListResult that = (LogReportListResult) o;
        return Objects.equals(pagination, that.pagination)
                && Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, list);
    }

    @Override
    public String toString() {
        return "LogReportListResult{pagination=" + pagination + ", list=" + list + '}';
    }
}
