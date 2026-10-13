// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 航线任务事件。 */
public final class WaylineTaskEvent {
    /** 事件唯一标识 (UUID) */
    private String eventId;
    private String action;
    /** 任务 UUID */
    private String uuid;
    /** 时间戳（毫秒） */
    private long timestamp;
    /** 任务详情数据 */
    private WaylineTaskData taskData;
    public WaylineTaskEvent() {
    }

/**
     * 构造 WaylineTaskEvent。
     *
     * @param eventId 事件ID
     * @param action 操作动作。枚举："wayline_task_created"=任务创建, "wayline_task_attr_updated"=任务属性更新, "wayline_task_deleted"=任务删除, "wayline_task_obstracle_changed"=障碍物变更, "wayline_task_execute_output"=任务执行输出
     * @param uuid 任务 UUID
     * @param timestamp 时间戳，单位：毫秒
     * @param taskData 任务数据
     */
    public WaylineTaskEvent(String eventId, String action, String uuid, long timestamp,
            WaylineTaskData taskData) {
        this.eventId = eventId != null ? eventId : "";
        this.action = action != null ? action : "";
        this.uuid = uuid != null ? uuid : "";
        this.timestamp = timestamp;
        this.taskData = taskData;
    }

    /** 事件唯一标识 (UUID) */
    public String getEventId() { return eventId; }
    /**
     * 操作动作枚举：
     * <ul>
     *   <li>"wayline_task_created" - 任务创建</li>
     *   <li>"wayline_task_attr_updated" - 任务属性更新</li>
     *   <li>"wayline_task_deleted" - 任务删除</li>
     *   <li>"wayline_task_obstracle_changed" - 障碍物变更</li>
     *   <li>"wayline_task_execute_output" - 任务执行输出</li>
     * </ul>
     */
    public String getAction() { return action; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 时间戳（毫秒） */
    public long getTimestamp() { return timestamp; }
    /** 任务详情数据 */
    public WaylineTaskData getTaskData() { return taskData; }

    /** @param eventId 事件唯一标识 (UUID) */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 操作动作枚举：
     * <ul>
     *   <li>"wayline_task_created" - 任务创建</li>
     *   <li>"wayline_task_attr_updated" - 任务属性更新</li>
     *   <li>"wayline_task_deleted" - 任务删除</li>
     *   <li>"wayline_task_obstracle_changed" - 障碍物变更</li>
     *   <li>"wayline_task_execute_output" - 任务执行输出</li>
     * </ul>
     */
    public void setAction(String action) {
        this.action = action;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** @param timestamp 时间戳（毫秒） */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** @param taskData 任务详情数据 */
    public void setTaskData(WaylineTaskData taskData) {
        this.taskData = taskData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineTaskEvent that = (WaylineTaskEvent) o;
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(action, that.action)
                && Objects.equals(uuid, that.uuid)
                && timestamp == that.timestamp
                && Objects.equals(taskData, that.taskData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                eventId,
                action,
                uuid,
                timestamp,
                taskData);
    }

    @Override
    public String toString() {
        return "WaylineTaskEvent{"
                + "eventId='" + eventId + '\''
                + ", action='" + action + '\''
                + ", uuid='" + uuid + '\''
                + ", timestamp=" + timestamp
                + ", taskData=" + taskData
                + '}';
    }
}
