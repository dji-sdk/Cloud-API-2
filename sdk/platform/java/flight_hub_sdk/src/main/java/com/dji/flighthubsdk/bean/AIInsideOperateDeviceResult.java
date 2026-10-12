// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 单台设备操作结果 */
public class AIInsideOperateDeviceResult {
    /** 飞机 SN */
    private String droneSn = "";
    /** 操作结果码（0=成功，非 0 见错误码表，如 238002=机场状态不满足） */
    private int code;

    public AIInsideOperateDeviceResult() {}

    /** 飞机 SN */
    public String getDroneSn() { return droneSn; }

    /** @param v 飞机 SN */
    public void setDroneSn(String v) { this.droneSn = v != null ? v : ""; }

    /** 操作结果码（0=成功，非 0 见错误码表，如 238002=机场状态不满足） */
    public int getCode() { return code; }

    /** @param v 操作结果码（0=成功，非 0 见错误码表，如 238002=机场状态不满足） */
    public void setCode(int v) { this.code = v; }
}
