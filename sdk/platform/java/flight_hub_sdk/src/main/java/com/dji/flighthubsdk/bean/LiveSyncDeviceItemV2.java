// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 设备树节点（网关 + 挂载无人机） */
public class LiveSyncDeviceItemV2 {
    /** 网关设备（机场/遥控器） */
    private LiveSyncDeviceV2 gateway = new LiveSyncDeviceV2();
    /** 挂载设备列表（V2 含无人机和 dock，V1 仅无人机） */
    private List<LiveSyncDeviceV2> hosts = new ArrayList<>();

    public LiveSyncDeviceItemV2() {}

    public LiveSyncDeviceV2 getGateway() { return gateway; }
    public void setGateway(LiveSyncDeviceV2 v) { this.gateway = v; }

    public List<LiveSyncDeviceV2> getHosts() { return hosts; }
    public void setHosts(List<LiveSyncDeviceV2> v) { this.hosts = v != null ? v : new ArrayList<>(); }
}
