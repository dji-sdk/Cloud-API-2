// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 可用设备 V2 条目（单台设备信息） */
public class LiveSyncDeviceV2 {
    /** 设备序列号 */
    private String sn = "";
    /** 设备名称 */
    private String name = "";
    /** 设备型号 key */
    private String deviceModelKey = "";
    /** 设备类型："airport" / "drone" / "dock" / "base_station" / "relay" / "rc" / "payload" / "dy" / ""（未知） */
    private String deviceType = "";
    /** 摄像头能力列表 */
    private List<LiveSyncCameraCapacity> cameraCapacity = new ArrayList<>();

    public LiveSyncDeviceV2() {}

    public String getSn() { return sn; }
    public void setSn(String v) { this.sn = v != null ? v : ""; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v != null ? v : ""; }

    public String getDeviceModelKey() { return deviceModelKey; }
    public void setDeviceModelKey(String v) { this.deviceModelKey = v != null ? v : ""; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String v) { this.deviceType = v != null ? v : ""; }

    public List<LiveSyncCameraCapacity> getCameraCapacity() { return cameraCapacity; }
    public void setCameraCapacity(List<LiveSyncCameraCapacity> v) { this.cameraCapacity = v != null ? v : new ArrayList<>(); }
}
