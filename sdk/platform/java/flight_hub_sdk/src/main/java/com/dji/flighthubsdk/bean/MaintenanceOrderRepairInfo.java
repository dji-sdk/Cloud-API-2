// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 报修订单 - 维修信息 */
public final class MaintenanceOrderRepairInfo {
    private String assignee;
    private String assigneePhone;
    private String repairStartTime;
    public MaintenanceOrderRepairInfo() {
    }

/**
     * 构造 MaintenanceOrderRepairInfo。
     *
     * @param assignee 维修负责人
     * @param assigneePhone 维修负责人电话
     * @param repairStartTime 维修开始时间
     */
    public MaintenanceOrderRepairInfo(String assignee, String assigneePhone, String repairStartTime) {
        this.assignee = assignee;
        this.assigneePhone = assigneePhone;
        this.repairStartTime = repairStartTime;
    }

    /** 维修负责人 */
    public String getAssignee() { return assignee; }
    /** 维修负责人电话 */
    public String getAssigneePhone() { return assigneePhone; }
    /** 维修开始时间 */
    public String getRepairStartTime() { return repairStartTime; }

    /** 维修负责人 */
    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /** 维修负责人电话 */
    public void setAssigneePhone(String assigneePhone) {
        this.assigneePhone = assigneePhone;
    }

    /** 维修开始时间 */
    public void setRepairStartTime(String repairStartTime) {
        this.repairStartTime = repairStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderRepairInfo that = (MaintenanceOrderRepairInfo) o;
        return Objects.equals(assignee, that.assignee)
                && Objects.equals(assigneePhone, that.assigneePhone)
                && Objects.equals(repairStartTime, that.repairStartTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignee, assigneePhone, repairStartTime);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderRepairInfo{assignee='" + assignee + '\'' + ", assigneePhone='" + assigneePhone + '\'' + ", repairStartTime='" + repairStartTime + '\'' + '}';
    }
}
