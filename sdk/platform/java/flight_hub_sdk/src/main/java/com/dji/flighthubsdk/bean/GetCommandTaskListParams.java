// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 一键起飞任务列表查询参数 */
public class GetCommandTaskListParams {
    /** 页码，从 1 开始（必填） */
    private int page = 1;
    /** 每页数量，1~100（必填） */
    private int pageSize = 10;
    /** 设备 SN 过滤（网关 SN），与 drone_sn 至少传一个 */
    private String sn = "";
    /** 飞机 SN 过滤，与 sn 至少传一个 */
    private String droneSn = "";
    /** 开始时间（秒级时间戳），-1 表示不过滤，需与 end_at 配对 */
    private long beginAt = -1;
    /** 结束时间（秒级时间戳），-1 表示不过滤，需与 begin_at 配对 */
    private long endAt = -1;
    /** 任务状态过滤列表。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时 */
    private List<Integer> status = new ArrayList<>();

    public GetCommandTaskListParams() {
    }


    /** 页码，从 1 开始（必填） */
    public int getPage() { return page; }
    /** @param page 页码，从 1 开始（必填） */
    public void setPage(int page) { this.page = page; }
    /** 每页数量，1~100（必填） */
    public int getPageSize() { return pageSize; }
    /** @param pageSize 每页数量，1~100（必填） */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    /** 设备 SN 过滤（网关 SN），与 drone_sn 至少传一个 */
    public String getSn() { return sn; }
    /** @param sn 设备 SN 过滤（网关 SN），与 drone_sn 至少传一个 */
    public void setSn(String sn) { this.sn = sn != null ? sn : ""; }
    /** 飞机 SN 过滤，与 sn 至少传一个 */
    public String getDroneSn() { return droneSn; }
    /** @param droneSn 飞机 SN 过滤，与 sn 至少传一个 */
    public void setDroneSn(String droneSn) { this.droneSn = droneSn != null ? droneSn : ""; }
    /** 开始时间（秒级时间戳），-1 表示不过滤，需与 end_at 配对 */
    public long getBeginAt() { return beginAt; }
    /** @param beginAt 开始时间（秒级时间戳），-1 表示不过滤，需与 end_at 配对 */
    public void setBeginAt(long beginAt) { this.beginAt = beginAt; }
    /** 结束时间（秒级时间戳），-1 表示不过滤，需与 begin_at 配对 */
    public long getEndAt() { return endAt; }
    /** @param endAt 结束时间（秒级时间戳），-1 表示不过滤，需与 begin_at 配对 */
    public void setEndAt(long endAt) { this.endAt = endAt; }
    /** 任务状态过滤列表。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时 */
    public List<Integer> getStatus() { return status; }
    /** @param status 任务状态过滤列表。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时 */
    public void setStatus(List<Integer> status) { this.status = status != null ? status : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetCommandTaskListParams that = (GetCommandTaskListParams) o;
        return page == that.page
                && pageSize == that.pageSize
                && Objects.equals(sn, that.sn)
                && Objects.equals(droneSn, that.droneSn)
                && beginAt == that.beginAt
                && endAt == that.endAt
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                page,
                pageSize,
                sn,
                droneSn,
                beginAt,
                endAt,
                status);
    }

    @Override
    public String toString() {
        return "GetCommandTaskListParams{"
                + "page=" + page
                + ", pageSize=" + pageSize
                + ", sn='" + sn + '\''
                + ", droneSn='" + droneSn + '\''
                + ", beginAt=" + beginAt
                + ", endAt=" + endAt
                + ", status=" + status
                + '}';
    }
}
