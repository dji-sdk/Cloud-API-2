// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 创建转码器结果 */
public class LiveSyncCreateResult {
    /** 创建成功的转码器记录 */
    private LiveSyncItem item = new LiveSyncItem();
    /** 成功时始终为 0；PaaS 超限时通过 FlightHubError.serverCode 判断（HTTP 400 + ConverterMaxNum） */
    private int limit = 0;
    /** 是否已超限 */
    private boolean hasLimit = false;

    public LiveSyncCreateResult() {}

    public LiveSyncItem getItem() { return item; }
    public void setItem(LiveSyncItem v) { this.item = v; }

    public int getLimit() { return limit; }
    public void setLimit(int v) { this.limit = v; }

    public boolean isHasLimit() { return hasLimit; }
    public void setHasLimit(boolean v) { this.hasLimit = v; }
}
