// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 可操作设备数量统计结果 */
public class GetAIInsideModelDevicesSummaryResult {
    /** 该算法绑定的总设备数 */
    private int totalDevices;
    /** 满足当前操作类型条件的可操作设备数 */
    private int operableDevices;

    public GetAIInsideModelDevicesSummaryResult() {}

    public GetAIInsideModelDevicesSummaryResult(int totalDevices, int operableDevices) {
        this.totalDevices = totalDevices;
        this.operableDevices = operableDevices;
    }

    /** 该算法绑定的总设备数 */
    public int getTotalDevices() { return totalDevices; }

    /** @param v 该算法绑定的总设备数 */
    public void setTotalDevices(int v) { this.totalDevices = v; }

    /** 满足当前操作类型条件的可操作设备数 */
    public int getOperableDevices() { return operableDevices; }

    /** @param v 满足当前操作类型条件的可操作设备数 */
    public void setOperableDevices(int v) { this.operableDevices = v; }
}
