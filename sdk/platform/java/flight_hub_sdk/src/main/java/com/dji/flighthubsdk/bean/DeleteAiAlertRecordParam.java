// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** DELETE /drc/api/v2/ai-alert-record 请求参数 */
public class DeleteAiAlertRecordParam {
    /** 告警记录唯一 ID；非空时精确删除该条记录（优先） */
    private String alertUuid = "";
    /** 架次 ID；alertUuid 为空时按架次批量删除该架次全部告警记录 */
    private String flightId = "";

    public DeleteAiAlertRecordParam() {}

    public String getAlertUuid() { return alertUuid; }
    public void setAlertUuid(String v) { this.alertUuid = v != null ? v : ""; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String v) { this.flightId = v != null ? v : ""; }
}
