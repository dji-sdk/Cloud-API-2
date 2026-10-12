// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 分页查询报修订单列表请求参数 */
public class GetMaintenanceOrdersListParams {
    /** 页码(默认 1); */
    private int page = 1;
    /** [1, 100]; */
    private int pageSize = 100;
    private String orderNumber = "";
    /** 订单状态：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消; */
    private String status = "";
    /** 来源类型：0=定损 2=日志反馈 3=直接报修; */
    private String initiatedFrom = "";
    /** 设备 SN（精确匹配); */
    private String deviceSn = "";
    private List<String> creatorIds = new ArrayList<>();
    /** 关键词搜索(匹配设备 SN 或组织内昵称); */
    private String keyWord = "";
    private String createdAtStart = "";
    private String createdAtEnd = "";
    /** 排序字段: ["order_number", "created_at", "submission_time", "completed_time"]; */
    private String sortColumn = "";
    /** 排序方向, ["asc", "desc"] 默认 "desc"; */
    private String sortType = "";

    public GetMaintenanceOrdersListParams() {
    }


    /** 页码(默认 1); */
    public int getPage() {
        return page;
    }

    /** @param page 页码(默认 1); */
    public void setPage(int page) {
        this.page = page;
    }

    /** [1, 100]; */
    public int getPageSize() {
        return pageSize;
    }

    /** @param pageSize [1, 100]; */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** 订单编号 */
    public String getOrderNumber() {
        return orderNumber;
    }

    /** @param orderNumber 订单编号 */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber != null ? orderNumber : "";
    }

    /** 订单状态：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消; */
    public String getStatus() {
        return status;
    }

    /** @param status 订单状态：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消; */
    public void setStatus(String status) {
        this.status = status != null ? status : "";
    }

    /** 来源类型：0=定损 2=日志反馈 3=直接报修; */
    public String getInitiatedFrom() {
        return initiatedFrom;
    }

    /** @param initiatedFrom 来源类型：0=定损 2=日志反馈 3=直接报修; */
    public void setInitiatedFrom(String initiatedFrom) {
        this.initiatedFrom = initiatedFrom != null ? initiatedFrom : "";
    }

    /** 设备 SN（精确匹配); */
    public String getDeviceSn() {
        return deviceSn;
    }

    /** @param deviceSn 设备 SN（精确匹配); */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn != null ? deviceSn : "";
    }

    /** 创建者 ID 列表 */
    public List<String> getCreatorIds() {
        return creatorIds;
    }

    /** @param creatorIds 创建者 ID 列表 */
    public void setCreatorIds(List<String> creatorIds) {
        this.creatorIds = creatorIds != null ? creatorIds : new ArrayList<>();
    }

    /** 关键词搜索(匹配设备 SN 或组织内昵称); */
    public String getKeyWord() {
        return keyWord;
    }

    /** @param keyWord 关键词搜索(匹配设备 SN 或组织内昵称); */
    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord != null ? keyWord : "";
    }

    /** 创建时间起始 */
    public String getCreatedAtStart() {
        return createdAtStart;
    }

    /** @param createdAtStart 创建时间起始 */
    public void setCreatedAtStart(String createdAtStart) {
        this.createdAtStart = createdAtStart != null ? createdAtStart : "";
    }

    /** 创建时间结束 */
    public String getCreatedAtEnd() {
        return createdAtEnd;
    }

    /** @param createdAtEnd 创建时间结束 */
    public void setCreatedAtEnd(String createdAtEnd) {
        this.createdAtEnd = createdAtEnd != null ? createdAtEnd : "";
    }

    /** 排序字段: ["order_number", "created_at", "submission_time", "completed_time"]; */
    public String getSortColumn() {
        return sortColumn;
    }

    /** @param sortColumn 排序字段: ["order_number", "created_at", "submission_time", "completed_time"]; */
    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn != null ? sortColumn : "";
    }

    /** 排序方向, ["asc", "desc"] 默认 "desc"; */
    public String getSortType() {
        return sortType;
    }

    /** @param sortType 排序方向, ["asc", "desc"] 默认 "desc"; */
    public void setSortType(String sortType) {
        this.sortType = sortType != null ? sortType : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetMaintenanceOrdersListParams that = (GetMaintenanceOrdersListParams) o;
        return page == that.page
                && pageSize == that.pageSize
                && Objects.equals(orderNumber, that.orderNumber)
                && Objects.equals(status, that.status)
                && Objects.equals(initiatedFrom, that.initiatedFrom)
                && Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(creatorIds, that.creatorIds)
                && Objects.equals(keyWord, that.keyWord)
                && Objects.equals(createdAtStart, that.createdAtStart)
                && Objects.equals(createdAtEnd, that.createdAtEnd)
                && Objects.equals(sortColumn, that.sortColumn)
                && Objects.equals(sortType, that.sortType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                page,
                pageSize,
                orderNumber,
                status,
                initiatedFrom,
                deviceSn,
                creatorIds,
                keyWord,
                createdAtStart,
                createdAtEnd,
                sortColumn,
                sortType);
    }

    @Override
    public String toString() {
        return "GetMaintenanceOrdersListParams{"
                + "page=" + page
                + ", pageSize=" + pageSize
                + ", orderNumber='" + orderNumber + '\''
                + ", status='" + status + '\''
                + ", initiatedFrom='" + initiatedFrom + '\''
                + ", deviceSn='" + deviceSn + '\''
                + ", creatorIds=" + creatorIds
                + ", keyWord='" + keyWord + '\''
                + ", createdAtStart='" + createdAtStart + '\''
                + ", createdAtEnd='" + createdAtEnd + '\''
                + ", sortColumn='" + sortColumn + '\''
                + ", sortType='" + sortType + '\''
                + '}';
    }
}
