// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 定损报告 - 查询定损报告列表返回结果 */
public final class GetSelfDiagnosisReportListResults {
    private CommonPaginationData pagination;
    private List<GetSelfDiagnosisReportResults> list;
    public GetSelfDiagnosisReportListResults() {
    }

/**
     * 构造 GetSelfDiagnosisReportListResults。
     *
     * @param pagination 分页信息
     * @param list 定损报告列表
     */
    public GetSelfDiagnosisReportListResults(CommonPaginationData pagination, List<GetSelfDiagnosisReportResults> list) {
        this.pagination = pagination;
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 分页信息 */
    public CommonPaginationData getPagination() { return pagination; }
    /** 定损报告列表 */
    public List<GetSelfDiagnosisReportResults> getList() { return list; }

    /** 分页信息 */
    public void setPagination(CommonPaginationData pagination) {
        this.pagination = pagination;
    }

    /** 定损报告列表 */
    public void setList(List<GetSelfDiagnosisReportResults> list) {
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
        GetSelfDiagnosisReportListResults that = (GetSelfDiagnosisReportListResults) o;
        return Objects.equals(pagination, that.pagination)
                && Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagination, list);
    }

    @Override
    public String toString() {
        return "GetSelfDiagnosisReportListResults{pagination=" + pagination + ", list=" + list + '}';
    }
}
