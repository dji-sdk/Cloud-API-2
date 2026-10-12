// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

import java.util.Map;

public class TelemetryMessage {

    private String uuid;
    private String type;
    private long ts;
    private String gatewaySn;
    private String field;
    private Map<String, Object> data;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTs() { return ts; }
    public void setTs(long ts) { this.ts = ts; }
    public String getGatewaySn() { return gatewaySn; }
    public void setGatewaySn(String gatewaySn) { this.gatewaySn = gatewaySn; }
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
