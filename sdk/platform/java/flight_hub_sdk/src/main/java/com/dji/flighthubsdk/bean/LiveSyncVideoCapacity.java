// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 单路视频能力描述 */
public class LiveSyncVideoCapacity {
    /** 视频流类型（normal / wide 等） */
    private String videoType = "";
    /** 视频流索引 */
    private String videoIndex = "";

    public LiveSyncVideoCapacity() {}

    public String getVideoType() { return videoType; }
    public void setVideoType(String v) { this.videoType = v != null ? v : ""; }

    public String getVideoIndex() { return videoIndex; }
    public void setVideoIndex(String v) { this.videoIndex = v != null ? v : ""; }
}
