// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** POI任务事件。 */
public final class PoiTaskEvent {
    /** 事件唯一标识 (UUID) */
    private String eventId;
    /** 操作类型，对应 Routing Key */
    private String action;
    /** 任务 UUID */
    private String uuid;
    /** 时间戳（毫秒） */
    private long timestamp;
    /** 任务详情数据 */
    private PoiTaskData taskData;
    public PoiTaskEvent() {
    }

/**
     * 构造 PoiTaskEvent。
     *
     * @param eventId 事件ID
     * @param action 操作动作。枚举："uninstall"=卸载, "start"=启动, "stop"=停止, "clear"=清除数据
     * @param uuid 任务 UUID
     * @param timestamp 时间戳，单位：毫秒
     * @param taskData 任务数据
     */
    public PoiTaskEvent(String eventId, String action, String uuid, long timestamp,
            PoiTaskData taskData) {
        this.eventId = eventId != null ? eventId : "";
        this.action = action != null ? action : "";
        this.uuid = uuid != null ? uuid : "";
        this.timestamp = timestamp;
        this.taskData = taskData;
    }

    /** 事件唯一标识 (UUID) */
    public String getEventId() { return eventId; }
    /** 操作类型，对应 Routing Key */
    public String getAction() { return action; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 时间戳（毫秒） */
    public long getTimestamp() { return timestamp; }
    /** 任务详情数据 */
    public PoiTaskData getTaskData() { return taskData; }

    /** @param eventId 事件唯一标识 (UUID) */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** @param action 操作类型，对应 Routing Key */
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
    public void setTaskData(PoiTaskData taskData) {
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
        PoiTaskEvent that = (PoiTaskEvent) o;
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
        return "PoiTaskEvent{"
                + "eventId='" + eventId + '\''
                + ", action='" + action + '\''
                + ", uuid='" + uuid + '\''
                + ", timestamp=" + timestamp
                + ", taskData=" + taskData
                + '}';
    }
}
