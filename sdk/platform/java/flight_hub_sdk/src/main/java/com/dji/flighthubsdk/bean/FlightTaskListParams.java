// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 飞行任务列表查询参数 */
public class FlightTaskListParams {
    /** 页码，从 1 开始 */
    private int page = 1;
    /** 每页数量 */
    private int pageSize = 10;
    /** 设备 SN 过滤列表 */
    private List<String> sn = new ArrayList<>();
    /** 起始时间过滤（秒级时间戳），需与 end_at 配合使用 */
    private Long beginAt;
    /** 结束时间过滤（秒级时间戳） */
    private Long endAt;
    /** 任务名称过滤（模糊搜索） */
    private String name = "";
    /** 任务状态过滤列表。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中 */
    private List<Integer> status = new ArrayList<>();
    /** 任务类型过滤：0=不筛选 1=立即任务 2=条件任务 */
    private int taskType;
    /** 媒体上传类型过滤。枚举：0=全部, 1=上传至云端, 2=直传云云对接 */
    private int mediaUploadType;

    public FlightTaskListParams() {
    }


    /** 页码，从 1 开始 */
    public int getPage() { return page; }
    /** @param page 页码，从 1 开始 */
    public void setPage(int page) { this.page = page; }
    /** 每页数量 */
    public int getPageSize() { return pageSize; }
    /** @param pageSize 每页数量 */
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    /** 设备 SN 过滤列表 */
    public List<String> getSn() { return sn; }
    /** @param sn 设备 SN 过滤列表 */
    public void setSn(List<String> sn) { this.sn = sn != null ? sn : new ArrayList<>(); }
    /** 起始时间过滤（秒级时间戳），需与 end_at 配合使用 */
    public Long getBeginAt() { return beginAt; }
    /** @param beginAt 起始时间过滤（秒级时间戳），需与 end_at 配合使用 */
    public void setBeginAt(Long beginAt) { this.beginAt = beginAt; }
    /** 结束时间过滤（秒级时间戳） */
    public Long getEndAt() { return endAt; }
    /** @param endAt 结束时间过滤（秒级时间戳） */
    public void setEndAt(Long endAt) { this.endAt = endAt; }
    /** 任务名称过滤（模糊搜索） */
    public String getName() { return name; }
    /** @param name 任务名称过滤（模糊搜索） */
    public void setName(String name) { this.name = name != null ? name : ""; }
    /** 任务状态过滤列表。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中 */
    public List<Integer> getStatus() { return status; }
    /** @param status 任务状态过滤列表。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中 */
    public void setStatus(List<Integer> status) { this.status = status != null ? status : new ArrayList<>(); }
    /** 任务类型过滤：0=不筛选 1=立即任务 2=条件任务 */
    public int getTaskType() { return taskType; }
    /** @param taskType 任务类型过滤：0=不筛选 1=立即任务 2=条件任务 */
    public void setTaskType(int taskType) { this.taskType = taskType; }
    /** 媒体上传类型过滤。枚举：0=全部, 1=上传至云端, 2=直传云云对接 */
    public int getMediaUploadType() { return mediaUploadType; }
    /** @param mediaUploadType 媒体上传类型过滤。枚举：0=全部, 1=上传至云端, 2=直传云云对接 */
    public void setMediaUploadType(int mediaUploadType) { this.mediaUploadType = mediaUploadType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTaskListParams that = (FlightTaskListParams) o;
        return page == that.page
                && pageSize == that.pageSize
                && Objects.equals(sn, that.sn)
                && Objects.equals(beginAt, that.beginAt)
                && Objects.equals(endAt, that.endAt)
                && Objects.equals(name, that.name)
                && Objects.equals(status, that.status)
                && taskType == that.taskType
                && mediaUploadType == that.mediaUploadType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                page,
                pageSize,
                sn,
                beginAt,
                endAt,
                name,
                status,
                taskType,
                mediaUploadType);
    }

    @Override
    public String toString() {
        return "FlightTaskListParams{"
                + "page=" + page
                + ", pageSize=" + pageSize
                + ", sn=" + sn
                + ", beginAt=" + beginAt
                + ", endAt=" + endAt
                + ", name='" + name + '\''
                + ", status=" + status
                + ", taskType=" + taskType
                + ", mediaUploadType=" + mediaUploadType
                + '}';
    }
}
