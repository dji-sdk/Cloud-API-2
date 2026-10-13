// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 批量下发算法操作指令结果 */
public class OperateAIInsideModelResult {
    /** 各设备操作结果列表 */
    private List<AIInsideOperateDeviceResult> results = new ArrayList<>();

    public OperateAIInsideModelResult() {}

    /** 各设备操作结果列表 */
    public List<AIInsideOperateDeviceResult> getResults() { return results; }

    /** @param v 各设备操作结果列表 */
    public void setResults(List<AIInsideOperateDeviceResult> v) { this.results = v != null ? v : new ArrayList<>(); }
}
