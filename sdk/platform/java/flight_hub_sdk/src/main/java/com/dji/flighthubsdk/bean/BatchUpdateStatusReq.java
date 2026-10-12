// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/** 批量更新任务状态请求 */
public class BatchUpdateStatusReq {
    /** 任务 UUID 列表，批量 1-50 个 */
    private List<String> taskUuids = new ArrayList<>();
    /** 目标状态。枚举：0=待开始, 6=已挂起（仅允许这两个值） */
    private int status;

    public BatchUpdateStatusReq() {
    }


    /** 任务 UUID 列表，批量 1-50 个 */
    public List<String> getTaskUuids() {
        return taskUuids;
    }

    /** @param taskUuids 任务 UUID 列表，批量 1-50 个 */
    public void setTaskUuids(List<String> taskUuids) {
        this.taskUuids = taskUuids != null ? taskUuids : new ArrayList<>();
    }

    /** 目标状态。枚举：0=待开始, 6=已挂起（仅允许这两个值） */
    public int getStatus() {
        return status;
    }

    /** @param status 目标状态。枚举：0=待开始, 6=已挂起（仅允许这两个值） */
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BatchUpdateStatusReq that = (BatchUpdateStatusReq) o;
        return Objects.equals(taskUuids, that.taskUuids)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskUuids, status);
    }

    @Override
    public String toString() {
        return "BatchUpdateStatusReq{taskUuids=" + taskUuids + ", status=" + status + '}';
    }
}
