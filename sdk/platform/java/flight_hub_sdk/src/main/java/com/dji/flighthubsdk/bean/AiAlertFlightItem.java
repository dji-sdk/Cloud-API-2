// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI 告警架次统计条目 */
public class AiAlertFlightItem {
    /** 架次 ID */
    private String flightId = "";
    /** 该架次告警数量 */
    private int count;
    /** 飞行任务名称 */
    private String flightTaskName = "";
    /** 架次状态 */
    private int status;
    /** 架次开始时间（毫秒时间戳） */
    private long startTime;

    public AiAlertFlightItem() {}

    /** 架次 ID */
    public String getFlightId() { return flightId; }
    /** @param flightId 架次 ID */
    public void setFlightId(String v) { this.flightId = v != null ? v : ""; }

    /** 该架次告警数量 */
    public int getCount() { return count; }
    /** @param count 该架次告警数量 */
    public void setCount(int v) { this.count = v; }

    /** 飞行任务名称 */
    public String getFlightTaskName() { return flightTaskName; }
    /** @param flightTaskName 飞行任务名称 */
    public void setFlightTaskName(String v) { this.flightTaskName = v != null ? v : ""; }

    /** 架次状态 */
    public int getStatus() { return status; }
    /** @param status 架次状态 */
    public void setStatus(int v) { this.status = v; }

    /** 架次开始时间（毫秒时间戳） */
    public long getStartTime() { return startTime; }
    /** @param startTime 架次开始时间（毫秒时间戳） */
    public void setStartTime(long v) { this.startTime = v; }
}
