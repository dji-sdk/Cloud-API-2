// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 设备定损 - 分页查询定损报告列表请求参数 */
public class GetSelfDiagnosisReportListParams {
    /** 页码(默认 1); */
    private int page = 1;
    /** 默认 20; */
    private int pageSize = 20;
    private List<String> status = new ArrayList<>();
    /** 设备 SN（精确匹配); */
    private String deviceSn = "";
    /** 上报时间范围起始(Unix 秒，闭区间); */
    private String reportTimeBegin = "";
    /** 上报时间范围结束(Unix 秒，闭区间); */
    private String reportTimeEnd = "";
    /** 部件类型过滤（0=整机，10=单机业务定损，其他值由设备协议定义）; */
    private int part;
    /** 定损来源过滤（0=FH2，1=NeptuneApp）; */
    private int origin;
    private String query = "";
    private List<String> reportUser = new ArrayList<>();
    private String sortBy = "";
    /** 排序方向：asc=升序，desc=降序，默认 desc; */
    private String sortType = "";

    public GetSelfDiagnosisReportListParams() {
    }


    /** 页码(默认 1); */
    public int getPage() { return page; }
    /** @param page 页码(默认 1); */
    public void setPage(int page) { this.page = page; }
    /** 默认 20; */
    public int getPageSize() { return pageSize; }
    /** @param pageSize 默认 20; */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    /** 状态列表 */
    public List<String> getStatus() { return status; }
    /** @param status 状态列表 */
    public void setStatus(List<String> status) { this.status = status != null ? status : new ArrayList<>(); }
    /** 设备 SN（精确匹配); */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备 SN（精确匹配); */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }
    /** 上报时间范围起始(Unix 秒，闭区间); */
    public String getReportTimeBegin() { return reportTimeBegin; }
    /** @param reportTimeBegin 上报时间范围起始(Unix 秒，闭区间); */
    public void setReportTimeBegin(String reportTimeBegin) { this.reportTimeBegin = reportTimeBegin != null ? reportTimeBegin : ""; }
    /** 上报时间范围结束(Unix 秒，闭区间); */
    public String getReportTimeEnd() { return reportTimeEnd; }
    /** @param reportTimeEnd 上报时间范围结束(Unix 秒，闭区间); */
    public void setReportTimeEnd(String reportTimeEnd) { this.reportTimeEnd = reportTimeEnd != null ? reportTimeEnd : ""; }
    /** 部件类型过滤（0=整机，10=单机业务定损，其他值由设备协议定义）; */
    public int getPart() { return part; }
    /** @param part 部件类型过滤（0=整机，10=单机业务定损，其他值由设备协议定义）; */
    public void setPart(int part) { this.part = part; }
    /** 定损来源过滤（0=FH2，1=NeptuneApp）; */
    public int getOrigin() { return origin; }
    /** @param origin 定损来源过滤（0=FH2，1=NeptuneApp）; */
    public void setOrigin(int origin) { this.origin = origin; }
    /** 查询关键词 */
    public String getQuery() { return query; }
    /** @param query 查询关键词 */
    public void setQuery(String query) { this.query = query != null ? query : ""; }
    /** 报告用户列表 */
    public List<String> getReportUser() { return reportUser; }
    /** @param reportUser 报告用户列表 */
    public void setReportUser(List<String> reportUser) { this.reportUser = reportUser != null ? reportUser : new ArrayList<>(); }
    /** 排序字段 */
    public String getSortBy() { return sortBy; }
    /** @param sortBy 排序字段 */
    public void setSortBy(String sortBy) { this.sortBy = sortBy != null ? sortBy : ""; }
    /** 排序方向：asc=升序，desc=降序，默认 desc; */
    public String getSortType() { return sortType; }
    /** @param sortType 排序方向：asc=升序，desc=降序，默认 desc; */
    public void setSortType(String sortType) { this.sortType = sortType != null ? sortType : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetSelfDiagnosisReportListParams that = (GetSelfDiagnosisReportListParams) o;
        return page == that.page
                && pageSize == that.pageSize
                && Objects.equals(status, that.status)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(reportTimeBegin, that.reportTimeBegin)
                && Objects.equals(reportTimeEnd, that.reportTimeEnd)
                && part == that.part
                && origin == that.origin
                && Objects.equals(query, that.query)
                && Objects.equals(reportUser, that.reportUser)
                && Objects.equals(sortBy, that.sortBy)
                && Objects.equals(sortType, that.sortType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                page,
                pageSize,
                status,
                deviceSn,
                reportTimeBegin,
                reportTimeEnd,
                part,
                origin,
                query,
                reportUser,
                sortBy,
                sortType);
    }

    @Override
    public String toString() {
        return "GetSelfDiagnosisReportListParams{"
                + "page=" + page
                + ", pageSize=" + pageSize
                + ", status=" + status
                + ", deviceSn='" + deviceSn + '\''
                + ", reportTimeBegin='" + reportTimeBegin + '\''
                + ", reportTimeEnd='" + reportTimeEnd + '\''
                + ", part=" + part
                + ", origin=" + origin
                + ", query='" + query + '\''
                + ", reportUser=" + reportUser
                + ", sortBy='" + sortBy + '\''
                + ", sortType='" + sortType + '\''
                + '}';
    }
}
