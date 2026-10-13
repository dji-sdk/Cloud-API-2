// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 摄像头视频能力描述 */
public class LiveSyncCameraCapacity {
    /** 摄像头索引 */
    private String cameraIndex = "";
    /** 该摄像头支持的视频流列表 */
    private List<LiveSyncVideoCapacity> videosList = new ArrayList<>();

    public LiveSyncCameraCapacity() {}

    public String getCameraIndex() { return cameraIndex; }
    public void setCameraIndex(String v) { this.cameraIndex = v != null ? v : ""; }

    public List<LiveSyncVideoCapacity> getVideosList() { return videosList; }
    public void setVideosList(List<LiveSyncVideoCapacity> v) { this.videosList = v != null ? v : new ArrayList<>(); }
}
