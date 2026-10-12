// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 可用设备树（V2）查询结果 */
public class LiveSyncDevicesV2Result {
    /** 设备总数 */
    private long total = 0;
    /** 设备树列表（网关 + 挂载） */
    private List<LiveSyncDeviceItemV2> list = new ArrayList<>();

    public LiveSyncDevicesV2Result() {}

    public long getTotal() { return total; }
    public void setTotal(long v) { this.total = v; }

    public List<LiveSyncDeviceItemV2> getList() { return list; }
    public void setList(List<LiveSyncDeviceItemV2> v) { this.list = v != null ? v : new ArrayList<>(); }
}
