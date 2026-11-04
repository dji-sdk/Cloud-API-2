// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 获取航线列表请求参数 */
public class GetWaylineListParam {
    /** 页码（必填） */
    private int page = 0;
    /** 每页数量（必填） */
    private int pageSize = 0;
    /** 开始时间（秒级时间戳）。需与 end_timestamp 同时传入 */
    private long startTimestamp = 0L;
    /** 结束时间（秒级时间戳）。需与 start_timestamp 同时传入 */
    private long endTimestamp = 0L;
    /** 飞行器类型过滤 */
    private List<String> droneTypes = new ArrayList<>();
    /** 航线 UUID 过滤 */
    private List<String> waylineUuids = new ArrayList<>();
    /** 载荷类型过滤 */
    private List<String> payloadTypes = new ArrayList<>();
    /** 航线类型过滤（历史字段，字符串；建议改用 waylineTypesInt） */
    private List<String> waylineTypes = new ArrayList<>();
    /** 航线类型过滤（int 数组，与后端一致） */
    private List<Integer> waylineTypesInt = new ArrayList<>();
    /** 关键字模糊搜索 */
    private String keyword = "";

    public GetWaylineListParam() {
    }


    /** 页码（必填） */
    public int getPage() { return page; }
    /** @param page 页码（必填） */
    public void setPage(int page) { this.page = page; }

    /** 每页数量（必填） */
    public int getPageSize() { return pageSize; }
    /** @param pageSize 每页数量（必填） */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    /** 开始时间（秒级时间戳）。需与 end_timestamp 同时传入 */
    public long getStartTimestamp() { return startTimestamp; }
    /** @param startTimestamp 开始时间（秒级时间戳）。需与 end_timestamp 同时传入 */
    public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }

    /** 结束时间（秒级时间戳）。需与 start_timestamp 同时传入 */
    public long getEndTimestamp() { return endTimestamp; }
    /** @param endTimestamp 结束时间（秒级时间戳）。需与 start_timestamp 同时传入 */
    public void setEndTimestamp(long endTimestamp) { this.endTimestamp = endTimestamp; }

    /** 飞行器类型过滤 */
    public List<String> getDroneTypes() { return droneTypes; }
    /** @param droneTypes 飞行器类型过滤 */
    public void setDroneTypes(List<String> droneTypes) { this.droneTypes = droneTypes != null ? droneTypes : new ArrayList<>(); }

    /** 航线 UUID 过滤 */
    public List<String> getWaylineUuids() { return waylineUuids; }
    /** @param waylineUuids 航线 UUID 过滤 */
    public void setWaylineUuids(List<String> waylineUuids) { this.waylineUuids = waylineUuids != null ? waylineUuids : new ArrayList<>(); }

    /** 载荷类型过滤 */
    public List<String> getPayloadTypes() { return payloadTypes; }
    /** @param payloadTypes 载荷类型过滤 */
    public void setPayloadTypes(List<String> payloadTypes) { this.payloadTypes = payloadTypes != null ? payloadTypes : new ArrayList<>(); }

    /** 航线类型过滤（历史字段，字符串；建议改用 waylineTypesInt） */
    public List<String> getWaylineTypes() { return waylineTypes; }
    /** @param waylineTypes 航线类型过滤（历史字段） */
    public void setWaylineTypes(List<String> waylineTypes) { this.waylineTypes = waylineTypes != null ? waylineTypes : new ArrayList<>(); }

    /** 航线类型过滤（int 数组） */
    public List<Integer> getWaylineTypesInt() { return waylineTypesInt; }
    /** @param waylineTypesInt 航线类型过滤（int 数组） */
    public void setWaylineTypesInt(List<Integer> waylineTypesInt) { this.waylineTypesInt = waylineTypesInt != null ? waylineTypesInt : new ArrayList<>(); }

    /** 关键字模糊搜索 */
    public String getKeyword() { return keyword; }
    /** @param keyword 关键字模糊搜索 */
    public void setKeyword(String keyword) { this.keyword = keyword != null ? keyword : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetWaylineListParam that = (GetWaylineListParam) o;
        return page == that.page
                && pageSize == that.pageSize
                && startTimestamp == that.startTimestamp
                && endTimestamp == that.endTimestamp
                && Objects.equals(droneTypes, that.droneTypes)
                && Objects.equals(waylineUuids, that.waylineUuids)
                && Objects.equals(payloadTypes, that.payloadTypes)
                && Objects.equals(waylineTypes, that.waylineTypes)
                && Objects.equals(waylineTypesInt, that.waylineTypesInt)
                && Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                page,
                pageSize,
                startTimestamp,
                endTimestamp,
                droneTypes,
                waylineUuids,
                payloadTypes,
                waylineTypes,
                waylineTypesInt,
                keyword);
    }

    @Override
    public String toString() {
        return "GetWaylineListParam{"
                + "page=" + page
                + ", pageSize=" + pageSize
                + ", startTimestamp=" + startTimestamp
                + ", endTimestamp=" + endTimestamp
                + ", droneTypes=" + droneTypes
                + ", waylineUuids=" + waylineUuids
                + ", payloadTypes=" + payloadTypes
                + ", waylineTypes=" + waylineTypes
                + ", waylineTypesInt=" + waylineTypesInt
                + ", keyword='" + keyword + '\''
                + '}';
    }
}
