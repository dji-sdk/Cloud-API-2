// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 告警推送 — 完整通知消息。
 *
 * <p>对应 WS topic {@code alert_records.alert_records_notify} 推送的完整消息体。
 */
public class AiAlertRecordsNotifyResult {

    /** 按架次分组的告警数据列表 */
    private List<AiAlertNotifyFlightData> data = new ArrayList<>();

    public AiAlertRecordsNotifyResult() {}

    /** @return 按架次分组的告警数据列表 */
    public List<AiAlertNotifyFlightData> getData() { return data; }
    /** @param v 按架次分组的告警数据列表 */
    public void setData(List<AiAlertNotifyFlightData> v) { this.data = v != null ? v : new ArrayList<>(); }
}
