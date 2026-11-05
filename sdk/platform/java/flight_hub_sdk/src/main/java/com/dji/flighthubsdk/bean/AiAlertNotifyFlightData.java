// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 告警推送 — 按架次分组的告警数据。
 *
 * <p>对应 WS topic {@code alert_records.alert_records_notify} 推送中
 * {@code data} 数组元素。
 */
public class AiAlertNotifyFlightData {

    /** 架次 ID */
    private String flightId = "";
    /** 飞行任务名称 */
    private String flightTaskName = "";
    /** 该架次下的告警记录列表 */
    private List<AiAlertNotifyRecordItem> records = new ArrayList<>();

    public AiAlertNotifyFlightData() {}

    /** @return 架次 ID */
    public String getFlightId() { return flightId; }
    /** @param v 架次 ID */
    public void setFlightId(String v) { this.flightId = v != null ? v : ""; }

    /** @return 飞行任务名称 */
    public String getFlightTaskName() { return flightTaskName; }
    /** @param v 飞行任务名称 */
    public void setFlightTaskName(String v) { this.flightTaskName = v != null ? v : ""; }

    /** @return 该架次下的告警记录列表 */
    public List<AiAlertNotifyRecordItem> getRecords() { return records; }
    /** @param v 该架次下的告警记录列表 */
    public void setRecords(List<AiAlertNotifyRecordItem> v) { this.records = v != null ? v : new ArrayList<>(); }
}
