// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.model;

public class GimbalResetRequest {

    private String payloadIndex;
    /** 0 回中，1 向下，2 偏航回中，3 俯仰向下 */
    private int resetMode;

    public String getPayloadIndex() { return payloadIndex; }
    public void setPayloadIndex(String payloadIndex) { this.payloadIndex = payloadIndex; }
    public int getResetMode() { return resetMode; }
    public void setResetMode(int resetMode) { this.resetMode = resetMode; }
}
