// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 转码器汇总（总数与启用数） */
public class LiveSyncSummaryResult {
    /** 转码器总数 */
    private long total = 0;
    /** 已启用的转码器数量 */
    private long enableCnt = 0;

    public LiveSyncSummaryResult() {}

    public long getTotal() { return total; }
    public void setTotal(long v) { this.total = v; }

    public long getEnableCnt() { return enableCnt; }
    public void setEnableCnt(long v) { this.enableCnt = v; }
}
