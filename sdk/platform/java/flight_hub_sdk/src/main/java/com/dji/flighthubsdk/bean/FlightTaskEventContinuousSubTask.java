// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞行任务连续子任务事件。 */
public final class FlightTaskEventContinuousSubTask {

    private String taskUuid;
    private String parentUuid;
    private String groupUuid;
    private String sn;
    private String landingDockSn;
    public FlightTaskEventContinuousSubTask() {
    }

/**
     * 构造 FlightTaskEventContinuousSubTask。
     *
     * @param taskUuid 任务 UUID
     * @param parentUuid 父任务 UUID
     * @param groupUuid 任务组 UUID
     * @param sn 设备序列号
     * @param landingDockSn 降落机场序列号
     */
    public FlightTaskEventContinuousSubTask(
            String taskUuid, String parentUuid, String groupUuid, String sn, String landingDockSn) {
        this.taskUuid = taskUuid != null ? taskUuid : "";
        this.parentUuid = parentUuid != null ? parentUuid : "";
        this.groupUuid = groupUuid != null ? groupUuid : "";
        this.sn = sn != null ? sn : "";
        this.landingDockSn = landingDockSn != null ? landingDockSn : "";
    }

    /** 任务 UUID */
    public String getTaskUuid() { return taskUuid; }
    /** 父任务 UUID */
    public String getParentUuid() { return parentUuid; }
    /** 任务组 UUID */
    public String getGroupUuid() { return groupUuid; }
    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 降落机场序列号 */
    public String getLandingDockSn() { return landingDockSn; }

    /** 任务 UUID */
    public void setTaskUuid(String taskUuid) {
        this.taskUuid = taskUuid;
    }

    /** 父任务 UUID */
    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    /** 任务组 UUID */
    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 降落机场序列号 */
    public void setLandingDockSn(String landingDockSn) {
        this.landingDockSn = landingDockSn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightTaskEventContinuousSubTask that = (FlightTaskEventContinuousSubTask) o;
        return Objects.equals(taskUuid, that.taskUuid)
                && Objects.equals(parentUuid, that.parentUuid)
                && Objects.equals(groupUuid, that.groupUuid)
                && Objects.equals(sn, that.sn)
                && Objects.equals(landingDockSn, that.landingDockSn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                taskUuid,
                parentUuid,
                groupUuid,
                sn,
                landingDockSn);
    }

    @Override
    public String toString() {
        return "FlightTaskEventContinuousSubTask{"
                + "taskUuid='" + taskUuid + '\''
                + ", parentUuid='" + parentUuid + '\''
                + ", groupUuid='" + groupUuid + '\''
                + ", sn='" + sn + '\''
                + ", landingDockSn='" + landingDockSn + '\''
                + '}';
    }
}
