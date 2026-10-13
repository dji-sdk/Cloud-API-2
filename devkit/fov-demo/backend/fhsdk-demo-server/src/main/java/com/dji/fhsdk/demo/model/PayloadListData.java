// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

import java.util.ArrayList;
import java.util.List;

public class PayloadListData {

    private List<PayloadVo> payloads = new ArrayList<>();
    private String selectedPayloadIndex;
    private boolean configured;

    public List<PayloadVo> getPayloads() { return payloads; }
    public void setPayloads(List<PayloadVo> payloads) { this.payloads = payloads; }
    public String getSelectedPayloadIndex() { return selectedPayloadIndex; }
    public void setSelectedPayloadIndex(String selectedPayloadIndex) { this.selectedPayloadIndex = selectedPayloadIndex; }
    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean configured) { this.configured = configured; }
}
